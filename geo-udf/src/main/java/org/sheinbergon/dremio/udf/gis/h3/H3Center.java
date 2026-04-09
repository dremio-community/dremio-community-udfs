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
 * H3_Center — returns the center of the H3 cell as a point geometry.
 * Uses JTS geometry output (compatible with sheinbergon ST_AsText and other ST_ functions).
 */
@FunctionTemplate(name = "h3_center", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
public class H3Center implements SimpleFunction {
    @Param
    org.apache.arrow.vector.holders.BigIntHolder h3Value;

    @Output
    org.apache.arrow.vector.holders.VarBinaryHolder out;

    @Inject
    org.apache.arrow.memory.ArrowBuf buffer;

    public void setup() {
    }

    public void eval() {
        try {
            com.uber.h3core.H3Core h3 = com.uber.h3core.H3Core.newInstance();
            if (!h3.h3IsValid(h3Value.value))
                throw new IllegalArgumentException("Not a valid H3 value.");

            // H3 returns GeoCoord(lat, lng); JTS Point is (x=lon, y=lat)
            com.uber.h3core.util.GeoCoord center = h3.h3ToGeo(h3Value.value);
            org.locationtech.jts.geom.GeometryFactory gf = new org.locationtech.jts.geom.GeometryFactory();
            org.locationtech.jts.geom.Point pt = gf.createPoint(
                new org.locationtech.jts.geom.Coordinate(center.lng, center.lat));

            byte[] bytes = org.sheinbergon.dremio.udf.gis.util.GeometryHelpers.toEWKB(pt);
            buffer = out.buffer = buffer.reallocIfNeeded(bytes.length);
            out.start = 0;
            out.end = bytes.length;
            buffer.setBytes(0, bytes);

        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to initialize H3 library: " + e.getMessage());
        }
    }
}
