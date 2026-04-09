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
 * H3_FromGeomPoly — returns the smallest H3 cell that fully encloses the polygon.
 * Uses JTS WKB parsing (compatible with EWKB written by sheinbergon ST_ functions).
 */
@FunctionTemplate(name = "h3_fromgeompoly", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.INTERNAL)
public class H3FromGeomPoly implements SimpleFunction {
    @Param
    org.apache.arrow.vector.holders.NullableVarBinaryHolder polyGeom;

    @Output
    org.apache.arrow.vector.holders.NullableBigIntHolder out;

    @Inject
    org.apache.arrow.memory.ArrowBuf buffer;

    public void setup() {
    }

    public void eval() {
        if (polyGeom.isSet == 0) {
            out.value = 0L;
            out.isSet = 0;
        } else {
            try {
                org.locationtech.jts.geom.Geometry inputGeom =
                    org.sheinbergon.dremio.udf.gis.util.GeometryHelpers.toGeometry(polyGeom);

                if (!(inputGeom instanceof org.locationtech.jts.geom.Polygon)
                        && !(inputGeom instanceof org.locationtech.jts.geom.MultiPolygon)) {
                    throw new IllegalArgumentException("Must be a Polygon or MultiPolygon.");
                }

                // Get centroid to anchor H3 search
                org.locationtech.jts.geom.Point centroid = inputGeom.getCentroid();
                com.uber.h3core.H3Core h3 = com.uber.h3core.H3Core.newInstance();

                long finalH3Value = 0L;
                int finalResolution = -1;

                for (int resolution = 15; resolution > 0; resolution--) {
                    long h3Value = h3.geoToH3(centroid.getY(), centroid.getX(), resolution);

                    // Build the H3 boundary as a JTS polygon for containment check
                    java.util.List<com.uber.h3core.util.GeoCoord> boundary = h3.h3ToGeoBoundary(h3Value);
                    org.locationtech.jts.geom.Coordinate[] coords =
                        new org.locationtech.jts.geom.Coordinate[boundary.size() + 1];
                    for (int i = 0; i < boundary.size(); i++) {
                        coords[i] = new org.locationtech.jts.geom.Coordinate(
                            boundary.get(i).lng, boundary.get(i).lat);
                    }
                    coords[boundary.size()] = coords[0]; // close ring

                    org.locationtech.jts.geom.GeometryFactory gf =
                        new org.locationtech.jts.geom.GeometryFactory();
                    org.locationtech.jts.geom.Polygon h3Poly = gf.createPolygon(coords);

                    if (inputGeom.within(h3Poly)) {
                        finalH3Value = h3Value;
                        finalResolution = resolution;
                        break;
                    }
                }

                if (finalResolution == -1) {
                    out.value = 0L;
                    out.isSet = 0;
                } else {
                    out.value = finalH3Value;
                    out.isSet = 1;
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("Bad geometry or unable to initialize H3 library: " + e.getMessage());
            }
        }
    }
}
