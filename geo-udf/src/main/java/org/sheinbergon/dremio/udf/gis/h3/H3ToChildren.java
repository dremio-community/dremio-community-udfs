/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sheinbergon.dremio.udf.gis.h3;

import javax.inject.Inject;

import com.dremio.exec.expr.SimpleFunction;
import com.dremio.exec.expr.annotations.FunctionTemplate;
import com.dremio.exec.expr.annotations.Output;
import com.dremio.exec.expr.annotations.Param;

/**
 *
 *  @name			H3_ToChildren
 *  @args			([bigint] {h3Value}, [number] {childResolution})
 *  @returnType		bigint[]
 *  @description	Returns an array with the indexes of the children/descendents of the given hexagon at the given resolution.
 *  @example		WITH H3Children AS (SELECT FLATTEN(H3_ToChildren(H3_FromText('837b59fffffffff'), 4)) AS H3Values) SELECT H3_AsText(H3Values) FROM H3Children -> '847b591ffffffff' '847b593ffffffff' '847b595ffffffff' '847b597ffffffff' '847b599ffffffff' '847b59bffffffff' '847b59dffffffff'
 *
 *  @author			Brian Holman <bholman@dezota.com>
 *
 */

@FunctionTemplate(name = "h3_tochildren", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL, derivation = org.sheinbergon.dremio.udf.gis.h3.H3ListOutputDerivation.class)
public class H3ToChildren implements SimpleFunction {
    @Param
    org.apache.arrow.vector.holders.BigIntHolder h3Value;

    @Param
    org.apache.arrow.vector.holders.IntHolder childResolution;

    @Output
    org.apache.arrow.vector.complex.writer.BaseWriter.ComplexWriter out;

    @Inject
    org.apache.arrow.memory.ArrowBuf buffer;

    public void setup() {
    }

    public void eval() {
        com.uber.h3core.H3Core h3;
        try {
            h3 = com.uber.h3core.H3Core.newInstance();

            /* Perform H3 To Children action */
            java.util.List<Long> childrenValues = h3.h3ToChildren(h3Value.value, childResolution.value);
            java.util.Iterator<Long> childrenIterator = childrenValues.iterator();

            org.apache.arrow.vector.complex.writer.BaseWriter.ListWriter listWriter = out.rootAsList();
            listWriter.startList();
            while(childrenIterator.hasNext()) {
                Long Lv = (java.lang.Long)(childrenIterator.next());
                listWriter.bigInt().writeBigInt(Lv.longValue());
            }
            listWriter.endList();

        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to initialize H3 library or bad H3 list.");
        }
    }
}
