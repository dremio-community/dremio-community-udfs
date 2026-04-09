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
 * H3_FromGeomPoint — returns the H3 cell containing the point at the given resolution.
 * Uses JTS WKB parsing (compatible with EWKB written by sheinbergon ST_ functions).
 */
@FunctionTemplate(name = "h3_fromgeompoint", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.INTERNAL)
public class H3FromGeomPoint implements SimpleFunction {
    @Param
    org.apache.arrow.vector.holders.NullableVarBinaryHolder pointGeom;

    @Param
    org.apache.arrow.vector.holders.NullableIntHolder resolution;

    @Output
    org.apache.arrow.vector.holders.NullableBigIntHolder out;

    @Inject
    org.apache.arrow.memory.ArrowBuf buffer;

    public void setup() {
    }

    public void eval() {
        if (pointGeom.isSet == 0 || resolution.isSet == 0) {
            out.value = 0L;
            out.isSet = 0;
        } else {
            if (!(resolution.value >= 0 && resolution.value <= 15))
                throw new IllegalArgumentException("H3 Resolution must be between 0 and 15.");
            try {
                // Use JTS to parse (handles both WKB and EWKB from sheinbergon)
                org.locationtech.jts.geom.Geometry geom =
                    org.sheinbergon.dremio.udf.gis.util.GeometryHelpers.toGeometry(pointGeom);
                if (geom instanceof org.locationtech.jts.geom.Point) {
                    org.locationtech.jts.geom.Point pt = (org.locationtech.jts.geom.Point) geom;
                    // JTS stores x=lon, y=lat; H3 geoToH3 expects (lat, lon)
                    com.uber.h3core.H3Core h3 = com.uber.h3core.H3Core.newInstance();
                    out.isSet = 1;
                    out.value = h3.geoToH3(pt.getY(), pt.getX(), resolution.value);
                } else {
                    out.value = 0L;
                    out.isSet = 0;
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("Bad geometry or unable to initialize H3 library: " + e.getMessage());
            }
        }
    }
}
