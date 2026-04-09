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
 *  @name			H3_FromLongLat
 *  @args			([number] {lon}, [number] {lat}, [number] {resolution})
 *  @returnType		bigint
 *  @description	Returns the H3 cell index specified by {{lon}} and {{lat}} at the specified {{resolution}}.  It will throw an error for resolution outside the valid range [0,15].
 *  @example		H3_AsText(H3_FromLongLat(40.4168, -3.7038, 4)) -> '84390cbffffffff'
 *
 *  @author			Brian Holman <bholman@dezota.com>
 *
 */

@FunctionTemplate(name = "h3_fromlonglat", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
public class H3FromLongLat implements SimpleFunction {
    @Param
    org.apache.arrow.vector.holders.Float8Holder lonParam;

    @Param
    org.apache.arrow.vector.holders.Float8Holder latParam;

    @Param
    org.apache.arrow.vector.holders.NullableIntHolder resolution;

    @Output
    org.apache.arrow.vector.holders.NullableBigIntHolder out;

    @Inject
    org.apache.arrow.memory.ArrowBuf buffer;

    public void setup() {
    }

    public void eval() {
        com.uber.h3core.H3Core h3;
        if (!(resolution.value >= 0 && resolution.value <= 15))
            throw new IllegalArgumentException("H3 Resolution must be between 0 and 15.");
        try {
            h3 = com.uber.h3core.H3Core.newInstance();
            out.value = h3.geoToH3(lonParam.value, latParam.value, resolution.value);
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to initialize H3 library.");
        }
    }
}
