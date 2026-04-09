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
 *  @name			H3_Resolution
 *  @args			([bigint] {h3Value})
 *  @returnType		integer
 *  @description	Returns the H3 cell resolution as an integer. It will throw an error if the {{h3Value}} is not valid as an H3 Value.
 *  @example		H3_Resolution(H3_FromText('847b59dffffffff')) -> 4
 *
 *  @author			Brian Holman <bholman@dezota.com>
 *
 */

@FunctionTemplate(name = "h3_resolution", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
public class H3Resolution implements SimpleFunction {
    @Param
    org.apache.arrow.vector.holders.BigIntHolder h3Value;

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

            if (h3.h3IsValid(h3Value.value)) {
                out.value = h3.h3GetResolution(h3Value.value);
            } else
                throw new IllegalArgumentException("Not a valid H3 value.");
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to initialize H3 library.");
        }
    }
}
