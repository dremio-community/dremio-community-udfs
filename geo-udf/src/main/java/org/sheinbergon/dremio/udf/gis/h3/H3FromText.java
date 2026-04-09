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
 *  @name			H3_FromText
 *  @args			([string] {h3Text})
 *  @returnType		bigint
 *  @description	Converts from String representation of H3 cell value to the bigint representation. It will throw an error if the hex representation is not valid as an H3 Value.
 *  @example		ST_AsText(H3_Center(H3_FromText('847b59dffffffff'))) -> 'POINT (40.305476423174326 -3.743203325561687)'
 *
 *  @author			Brian Holman <bholman@dezota.com>
 *
 */

@FunctionTemplate(name = "h3_fromtext", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
public class H3FromText implements SimpleFunction {
    @Param
    org.apache.arrow.vector.holders.VarCharHolder input;

    @Output
    org.apache.arrow.vector.holders.BigIntHolder out;

    @Inject
    org.apache.arrow.memory.ArrowBuf buffer;

    public void setup() {
    }

    public void eval() {
        byte[] h3Bytes = new byte[input.end - input.start];
        input.buffer.getBytes(input.start, h3Bytes);
        String h3Text = new String(h3Bytes, java.nio.charset.StandardCharsets.UTF_8);
        com.uber.h3core.H3Core h3;
        try {
            h3 = com.uber.h3core.H3Core.newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to initialize H3 library.");
        }
        try {
            out.value = h3.stringToH3(h3Text);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("The H3 cell text value is not a valid bigint: "+h3Text);
        }
        if (!(h3.h3IsValid(out.value)))
            throw new IllegalArgumentException("The H3 cell text value is not a valid H3 cell: "+h3Text);
    }
}
