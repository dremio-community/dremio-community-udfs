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
package org.sheinbergon.dremio.udf.gis;

import com.dremio.exec.expr.SimpleFunction;
import com.dremio.exec.expr.annotations.FunctionTemplate;
import com.dremio.exec.expr.annotations.Output;
import com.dremio.exec.expr.annotations.Param;

import javax.inject.Inject;

/**
 * Returns the geodesic area of a polygon on the WGS84 ellipsoid in square meters.
 * Uses the spherical excess formula (Girard's theorem) with Earth radius ~6,371,008.8 m.
 */
@FunctionTemplate(
    name = "ST_GeodesicAreaWGS84",
    scope = FunctionTemplate.FunctionScope.SIMPLE,
    nulls = FunctionTemplate.NullHandling.INTERNAL)
public class STGeodesicAreaWGS84 implements SimpleFunction {

  @Param
  org.apache.arrow.vector.holders.NullableVarBinaryHolder binaryInput;

  @Output
  org.apache.arrow.vector.holders.NullableFloat8Holder output;

  @Inject
  org.apache.arrow.memory.ArrowBuf buffer;

  public void setup() {
  }

  public void eval() {
    if (org.sheinbergon.dremio.udf.gis.util.GeometryHelpers.isHolderSet(binaryInput)) {
      org.locationtech.jts.geom.Geometry geom =
          org.sheinbergon.dremio.udf.gis.util.GeometryHelpers.toGeometry(binaryInput);
      double totalArea = 0.0;
      if (geom instanceof org.locationtech.jts.geom.Polygon) {
        totalArea = org.sheinbergon.dremio.udf.gis.util.GeodesicUtils.sphericalPolygonArea(
            (org.locationtech.jts.geom.Polygon) geom);
      } else if (geom instanceof org.locationtech.jts.geom.MultiPolygon) {
        org.locationtech.jts.geom.MultiPolygon mp = (org.locationtech.jts.geom.MultiPolygon) geom;
        for (int i = 0; i < mp.getNumGeometries(); i++) {
          totalArea += org.sheinbergon.dremio.udf.gis.util.GeodesicUtils.sphericalPolygonArea(
              (org.locationtech.jts.geom.Polygon) mp.getGeometryN(i));
        }
      }
      org.sheinbergon.dremio.udf.gis.util.GeometryHelpers.setDoubleValue(output, totalArea);
    } else {
      org.sheinbergon.dremio.udf.gis.util.GeometryHelpers.markHolderNotSet(output);
    }
  }
}
