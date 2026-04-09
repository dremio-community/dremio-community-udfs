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
 *  @name			H3_KRing
 *  @args			([bigint] h3Origin, [int] ringSize)
 *  @returnType		bigint[]
 *  @description	Returns all cell indexes in a filled hexagonal k-ring centered at the origin in no particular order.
 *  @example		H3_KRing(H3_FromText('837b59fffffffff'), 1) -> [592141574821904400,592141506102427600,592141712260857900,592124875989057500,592124738550104000,592140544029753300,592141849699811300]
 *
 *  @author			Brian Holman <bholman@dezota.com>
 *
 */

@FunctionTemplate(name = "h3_kring", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL, derivation = org.sheinbergon.dremio.udf.gis.h3.H3ListOutputDerivation.class)
public class H3KRing implements SimpleFunction {
    @Param
    org.apache.arrow.vector.holders.BigIntHolder h3Origin;

    @Param
    org.apache.arrow.vector.holders.IntHolder ringSize;

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

            /* Perform H3 kRing action */
            java.util.List<Long> kringValues = h3.kRing(h3Origin.value, ringSize.value);
            java.util.Iterator<Long> kringIterator = kringValues.iterator();

            org.apache.arrow.vector.complex.writer.BaseWriter.ListWriter listWriter = out.rootAsList();
            listWriter.startList();
            while(kringIterator.hasNext()) {
                Long Lv = (java.lang.Long)(kringIterator.next());
                listWriter.bigInt().writeBigInt(Lv.longValue());
            }
            listWriter.endList();

        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to initialize H3 library or bad H3 list.");
        }
    }
}
