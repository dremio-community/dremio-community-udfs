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
 * Returns the geodesic length of a LineString or MultiLineString on the WGS84 ellipsoid
 * in meters. Uses the Haversine formula with Earth radius ~6,371,008.8 m.
 */
@FunctionTemplate(
    name = "ST_GeodesicLengthWGS84",
    scope = FunctionTemplate.FunctionScope.SIMPLE,
    nulls = FunctionTemplate.NullHandling.INTERNAL)
public class STGeodesicLengthWGS84 implements SimpleFunction {

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
      double totalLen = 0.0;
      if (geom instanceof org.locationtech.jts.geom.LineString) {
        totalLen = org.sheinbergon.dremio.udf.gis.util.GeodesicUtils.haversineLength(
            (org.locationtech.jts.geom.LineString) geom);
      } else if (geom instanceof org.locationtech.jts.geom.MultiLineString) {
        org.locationtech.jts.geom.MultiLineString mls = (org.locationtech.jts.geom.MultiLineString) geom;
        for (int i = 0; i < mls.getNumGeometries(); i++) {
          totalLen += org.sheinbergon.dremio.udf.gis.util.GeodesicUtils.haversineLength(
              (org.locationtech.jts.geom.LineString) mls.getGeometryN(i));
        }
      }
      org.sheinbergon.dremio.udf.gis.util.GeometryHelpers.setDoubleValue(output, totalLen);
    } else {
      org.sheinbergon.dremio.udf.gis.util.GeometryHelpers.markHolderNotSet(output);
    }
  }
}
