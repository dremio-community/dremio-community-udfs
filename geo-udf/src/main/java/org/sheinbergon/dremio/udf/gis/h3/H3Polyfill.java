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
 * H3_Polyfill — returns all H3 cell indexes inside the given polygon at the given resolution.
 * Uses JTS WKB parsing (compatible with EWKB written by sheinbergon ST_ functions).
 */
@FunctionTemplate(name = "h3_polyfill", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL,
        derivation = org.sheinbergon.dremio.udf.gis.h3.H3ListOutputDerivation.class)
public class H3Polyfill implements SimpleFunction {
    @Param
    org.apache.arrow.vector.holders.VarBinaryHolder inputGeomParam;

    @Param
    org.apache.arrow.vector.holders.IntHolder resolution;

    @Output
    org.apache.arrow.vector.complex.writer.BaseWriter.ComplexWriter out;

    @Inject
    org.apache.arrow.memory.ArrowBuf buffer;

    public void setup() {
    }

    public void eval() {
        if (!(resolution.value >= 0 && resolution.value <= 15))
            throw new IllegalArgumentException("H3 Resolution must be between 0 and 15.");
        try {
            com.uber.h3core.H3Core h3 = com.uber.h3core.H3Core.newInstance();

            // Read geometry using JTS WKBReader (handles EWKB from sheinbergon)
            byte[] geomBytes = new byte[inputGeomParam.end - inputGeomParam.start];
            inputGeomParam.buffer.getBytes(inputGeomParam.start, geomBytes);
            org.locationtech.jts.io.WKBReader reader = new org.locationtech.jts.io.WKBReader();
            org.locationtech.jts.geom.Geometry inputGeom = reader.read(geomBytes);

            org.locationtech.jts.geom.Polygon poly = null;
            if (inputGeom instanceof org.locationtech.jts.geom.Polygon) {
                poly = (org.locationtech.jts.geom.Polygon) inputGeom;
            } else if (inputGeom instanceof org.locationtech.jts.geom.MultiPolygon) {
                org.locationtech.jts.geom.Geometry hull = inputGeom.convexHull();
                poly = (org.locationtech.jts.geom.Polygon) hull;
            } else {
                throw new IllegalArgumentException("Must be a Polygon or MultiPolygon.");
            }

            // Build exterior ring GeoCoords (JTS: x=lon, y=lat; H3 GeoCoord: lat, lng)
            org.locationtech.jts.geom.Coordinate[] extCoords = poly.getExteriorRing().getCoordinates();
            java.util.List<com.uber.h3core.util.GeoCoord> polyfillPoints = new java.util.ArrayList<>();
            for (org.locationtech.jts.geom.Coordinate c : extCoords) {
                polyfillPoints.add(new com.uber.h3core.util.GeoCoord(c.y, c.x));
            }

            // Build interior ring GeoCoords (holes)
            java.util.List<java.util.List<com.uber.h3core.util.GeoCoord>> holes =
                new java.util.ArrayList<>();
            for (int i = 0; i < poly.getNumInteriorRing(); i++) {
                org.locationtech.jts.geom.Coordinate[] holeCoords =
                    poly.getInteriorRingN(i).getCoordinates();
                java.util.List<com.uber.h3core.util.GeoCoord> holePoints = new java.util.ArrayList<>();
                for (org.locationtech.jts.geom.Coordinate c : holeCoords) {
                    holePoints.add(new com.uber.h3core.util.GeoCoord(c.y, c.x));
                }
                holes.add(holePoints);
            }

            java.util.List<Long> polyfillValues = h3.polyfill(polyfillPoints, holes, resolution.value);
            org.apache.arrow.vector.complex.writer.BaseWriter.ListWriter listWriter = out.rootAsList();
            listWriter.startList();
            for (Long lv : polyfillValues) {
                listWriter.bigInt().writeBigInt(lv.longValue());
            }
            listWriter.endList();

        } catch (Exception e) {
            throw new IllegalArgumentException("Bad geometry or unable to initialize H3 library: " + e.getMessage());
        }
    }
}
