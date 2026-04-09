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
 *  @name			H3_Distance
 *  @args			([bigint] {h3Value1}, [bigint] {h3Value2})
 *  @returnType		numeric
 *  @description	Returns the grid distance between two hexagon indexes. This function may fail to find the distance between two indexes if they are very far apart or on opposite sides of a pentagon.
 *  @example		H3_Distance(H3_FromText('847b591ffffffff'), H3_FromText('847b59bffffffff')) -> 1
 *
 *  @author			Brian Holman <bholman@dezota.com>
 *
 */

@FunctionTemplate(name = "h3_distance", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
public class H3Distance implements SimpleFunction {
    @Param
    org.apache.arrow.vector.holders.BigIntHolder h3Value1;

    @Param
    org.apache.arrow.vector.holders.BigIntHolder h3Value2;

    @Output
    org.apache.arrow.vector.holders.IntHolder out;

    @Inject
    org.apache.arrow.memory.ArrowBuf buffer;

    public void setup() {
    }

    public void eval() {
        com.uber.h3core.H3Core h3;
        try {
            h3 = com.uber.h3core.H3Core.newInstance();

            if (h3.h3IsValid(h3Value1.value) && h3.h3IsValid(h3Value2.value) ) {
                out.value = h3.h3Distance(h3Value1.value, h3Value2.value);
            } else
                throw new IllegalArgumentException("Not valid H3 values.");
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to initialize H3 library.");
        }
    }
}