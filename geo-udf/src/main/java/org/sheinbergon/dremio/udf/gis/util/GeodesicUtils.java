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
package org.sheinbergon.dremio.udf.gis.util;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;

/**
 * Geodesic math utilities for WGS84 spherical approximations.
 */
public final class GeodesicUtils {

  /** Mean Earth radius in meters (WGS84 authalic sphere) */
  private static final double EARTH_RADIUS_METERS = 6_371_008.8;

  private GeodesicUtils() {}

  /**
   * Haversine distance between two lon/lat points (degrees), result in meters.
   */
  public static double haversineDistance(double lon1, double lat1, double lon2, double lat2) {
    double dLat = Math.toRadians(lat2 - lat1);
    double dLon = Math.toRadians(lon2 - lon1);
    double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
        + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
        * Math.sin(dLon / 2) * Math.sin(dLon / 2);
    return EARTH_RADIUS_METERS * 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));
  }

  /**
   * Geodesic length of a LineString (coordinates in lon/lat degrees), result in meters.
   */
  public static double haversineLength(LineString line) {
    Coordinate[] coords = line.getCoordinates();
    double total = 0.0;
    for (int i = 1; i < coords.length; i++) {
      total += haversineDistance(coords[i - 1].x, coords[i - 1].y, coords[i].x, coords[i].y);
    }
    return total;
  }

  /**
   * Spherical excess area of a polygon in square meters.
   * Uses the spherical polygon area formula (Girard's theorem via the
   * Gauss–Bonnet approach on the unit sphere).
   * Exterior ring area minus interior ring areas (holes).
   */
  public static double sphericalPolygonArea(Polygon poly) {
    double area = Math.abs(ringSphericalArea(poly.getExteriorRing().getCoordinates()));
    for (int i = 0; i < poly.getNumInteriorRing(); i++) {
      area -= Math.abs(ringSphericalArea(poly.getInteriorRingN(i).getCoordinates()));
    }
    return area;
  }

  /**
   * Compute the signed spherical area for a ring of lon/lat coordinates (degrees).
   * Uses the spherical excess formula.
   */
  private static double ringSphericalArea(Coordinate[] coords) {
    if (coords.length < 4) {
      return 0.0;
    }
    int n = coords.length - 1; // last coord == first coord for closed ring
    double sum = 0.0;
    for (int i = 0; i < n; i++) {
      double lon1 = Math.toRadians(coords[i].x);
      double lat1 = Math.toRadians(coords[i].y);
      double lon2 = Math.toRadians(coords[(i + 1) % n].x);
      double lat2 = Math.toRadians(coords[(i + 1) % n].y);
      sum += (lon2 - lon1) * (2.0 + Math.sin(lat1) + Math.sin(lat2));
    }
    return Math.abs(sum * EARTH_RADIUS_METERS * EARTH_RADIUS_METERS / 2.0);
  }
}
