# Dremio Geo UDF Library

100 geospatial SQL functions for Dremio, combining OGC/PostGIS-compatible `ST_` functions with Uber H3 hexagonal grid indexing. Query, transform, and analyze spatial data directly in your lakehouse — no external GIS engine required.

> Geometry is stored and exchanged as **Extended WKB (EWKB)** binary — compatible with PostGIS, GeoParquet, and most GIS toolchains.

## Available Functions

### Geometry Constructors
| Function | Description |
|---|---|
| `ST_GeomFromText(wkt)` | Create geometry from WKT string |
| `ST_Point(lon, lat)` | Create a Point from longitude/latitude |
| `ST_MakeLine(geom1, geom2)` | Create LineString from two points |
| `ST_MakePolygon(shell)` | Create Polygon from a closed LineString |
| `ST_GeomFromWKB(wkb)` | Create geometry from WKB binary |
| `ST_GeomFromGeoJSON(json)` | Create geometry from GeoJSON string |

### Geometry Properties
| Function | Description | Returns |
|---|---|---|
| `ST_X(point)` | Longitude of a Point | DOUBLE |
| `ST_Y(point)` | Latitude of a Point | DOUBLE |
| `ST_GeometryType(geom)` | Type name e.g. `ST_POLYGON` | VARCHAR |
| `ST_NumPoints(geom)` | Number of vertices | INT |
| `ST_NPoints(geom)` | Alias for ST_NumPoints | INT |
| `ST_NumGeometries(geom)` | Count of sub-geometries in a collection | INT |
| `ST_SRID(geom)` | Spatial Reference ID | INT |
| `ST_IsEmpty(geom)` | True if geometry is empty | INT (1/0) |
| `ST_IsSimple(geom)` | True if geometry is topologically simple | INT (1/0) |
| `ST_IsValid(geom)` | True if geometry is valid OGC | INT (1/0) |
| `ST_IsClosed(geom)` | True if LineString is closed | INT (1/0) |
| `ST_IsRing(geom)` | True if LineString is a ring | INT (1/0) |
| `ST_StartPoint(geom)` | First point of a LineString | VARBINARY |
| `ST_EndPoint(geom)` | Last point of a LineString | VARBINARY |
| `ST_PointN(geom, n)` | Nth point of a LineString (1-based) | VARBINARY |
| `ST_ExteriorRing(geom)` | Exterior ring of a Polygon | VARBINARY |
| `ST_InteriorRingN(geom, n)` | Nth interior ring of a Polygon (1-based) | VARBINARY |

### Measurement
| Function | Description | Returns |
|---|---|---|
| `ST_Area(geom)` | Planar area (projected units) | DOUBLE |
| `ST_Length(geom)` | Planar length of LineString | DOUBLE |
| `ST_Perimeter(geom)` | Planar perimeter of Polygon | DOUBLE |
| `ST_Distance(geom1, geom2)` | Planar distance between geometries | DOUBLE |
| `ST_GeodesicAreaWGS84(geom)` | Geodesic (spherical) area in m² | DOUBLE |
| `ST_GeodesicLengthWGS84(geom)` | Geodesic length along WGS84 ellipsoid in m | DOUBLE |

### Spatial Relationships (Predicates)
All return `INT`: 1 = true, 0 = false.

| Function | Description |
|---|---|
| `ST_Contains(geom1, geom2)` | geom1 contains geom2 |
| `ST_Within(geom1, geom2)` | geom1 is within geom2 |
| `ST_Intersects(geom1, geom2)` | Geometries share any space |
| `ST_Crosses(geom1, geom2)` | Geometries cross each other |
| `ST_Touches(geom1, geom2)` | Geometries share a boundary point |
| `ST_Overlaps(geom1, geom2)` | Geometries partially overlap |
| `ST_Disjoint(geom1, geom2)` | Geometries share no space |
| `ST_Equals(geom1, geom2)` | Geometries are spatially equal |

### Geometry Operations
| Function | Description |
|---|---|
| `ST_Intersection(geom1, geom2)` | Shared area of two geometries |
| `ST_Union(geom1, geom2)` | Combined area of two geometries |
| `ST_Difference(geom1, geom2)` | Area in geom1 not in geom2 |
| `ST_SymDifference(geom1, geom2)` | Symmetric difference |
| `ST_Buffer(geom, distance)` | Expand geometry by distance |
| `ST_ConvexHull(geom)` | Convex hull |
| `ST_Centroid(geom)` | Centroid point |
| `ST_Envelope(geom)` | Minimum bounding rectangle |
| `ST_Boundary(geom)` | Topological boundary |
| `ST_Simplify(geom, tolerance)` | Douglas-Peucker simplification |
| `ST_SimplifyPreserveTopology(geom, tol)` | Topology-safe simplification |
| `ST_Reverse(geom)` | Reverse vertex order |
| `ST_ForceRHR(geom)` | Force right-hand rule winding |
| `ST_Normalize(geom)` | Normalize geometry form |

### Output / Serialization
| Function | Description | Returns |
|---|---|---|
| `ST_AsText(geom)` | WKT representation | VARCHAR |
| `ST_AsGeoJSON(geom)` | GeoJSON representation | VARCHAR |
| `ST_AsBinary(geom)` | WKB binary | VARBINARY |

### H3 Hexagonal Grid Functions
[Uber H3](https://h3geo.org/) divides the globe into hexagonal cells at 16 resolutions (0 = coarse, 15 = fine).

| Function | Description | Returns |
|---|---|---|
| `H3_FromLonLat(lon, lat, res)` | H3 index for a lon/lat coordinate | BIGINT |
| `H3_FromGeomPoint(geom, res)` | H3 index for a Point geometry | BIGINT |
| `H3_FromGeomPoly(geom, res)` | H3 index that best contains a Polygon | BIGINT |
| `H3_IsValid(h3)` | Is this a valid H3 index? | INT (1/0) |
| `H3_ToParent(h3, res)` | Parent cell at coarser resolution | BIGINT |
| `H3_Resolution(h3)` | Resolution level of an H3 index | INT |
| `H3_BaseCellNumber(h3)` | Base cell number (0–121) | INT |
| `H3_IsPentagon(h3)` | Is this cell a pentagon? | INT (1/0) |
| `H3_Boundary(h3)` | Cell boundary as a Polygon geometry | VARBINARY |
| `H3_Center(h3)` | Cell center as a Point geometry | VARBINARY |
| `H3_Polyfill(geom, res)` | All H3 cells that fill a Polygon | VARCHAR (JSON array) |
| `H3_Compact(h3s)` | Compact a set of H3 cells | VARCHAR (JSON array) |
| `H3_Uncompact(h3s, res)` | Uncompact H3 cells to a target resolution | VARCHAR (JSON array) |
| `H3_KRing(h3, k)` | All cells within k rings | VARCHAR (JSON array) |
| `H3_KRingDistances(h3, k)` | Cells grouped by distance | VARCHAR (JSON array) |
| `H3_HexRing(h3, k)` | Cells at exactly distance k | VARCHAR (JSON array) |
| `H3_Distance(h3a, h3b)` | Grid distance between two cells | INT |
| `H3_AreNeighbors(h3a, h3b)` | Are two cells adjacent? | INT (1/0) |
| `H3_ToChildren(h3, res)` | Child cells at finer resolution | VARCHAR (JSON array) |
| `H3_CenterChild(h3, res)` | Center child at finer resolution | BIGINT |
| `H3_FromText(str)` | Parse H3 index from hex string | BIGINT |
| `H3_ToString(h3)` | Format H3 index as hex string | VARCHAR |

---

## Quick Start

### Point-in-Polygon — Is a location inside a region?
```sql
SELECT
    store_id,
    ST_Contains(
        ST_GeomFromText('POLYGON((-74.1 40.6, -73.9 40.6, -73.9 40.8, -74.1 40.8, -74.1 40.6))'),
        ST_Point(longitude, latitude)
    ) AS in_nyc_bbox
FROM stores;
```

### Nearest Stores — Distance from a point
```sql
SELECT
    store_id,
    ST_GeodesicLengthWGS84(
        ST_MakeLine(ST_Point(longitude, latitude), ST_Point(-73.985, 40.748))
    ) AS dist_meters
FROM stores
ORDER BY dist_meters ASC
LIMIT 10;
```

### H3 Hexagonal Aggregation — Events per hex cell
```sql
-- Group events into resolution-7 hexagons (~5km²)
SELECT
    H3_FromLonLat(longitude, latitude, 7) AS hex_id,
    COUNT(*)                               AS event_count
FROM events
GROUP BY 1
ORDER BY event_count DESC
LIMIT 20;
```

### Geofencing — Flag deliveries outside the service area
```sql
SELECT
    delivery_id,
    ST_Within(
        ST_Point(delivery_lon, delivery_lat),
        ST_GeomFromText('POLYGON((...))')   -- service area boundary
    ) AS in_service_area
FROM deliveries
WHERE ST_Within(...) = 0;   -- only out-of-area deliveries
```

### Polyfill a City — Get all H3 cells inside a boundary
```sql
SELECT H3_Polyfill(city_boundary, 8) AS hex_cells
FROM city_boundaries
WHERE city_name = 'San Francisco';
-- Returns JSON array of H3 cell IDs at resolution 8 (~0.74km²)
```

### Geodesic Area of Polygons
```sql
SELECT
    region_name,
    ST_GeodesicAreaWGS84(boundary) / 1e6  AS area_km2
FROM regions
ORDER BY area_km2 DESC;
```

---

## Geometry Format

Geometries are stored as **Extended WKB (EWKB)** binary (`VARBINARY`):
- Created with `ST_GeomFromText(wkt)`, `ST_Point(lon, lat)`, `ST_GeomFromGeoJSON(json)`, etc.
- Exported with `ST_AsText(geom)`, `ST_AsGeoJSON(geom)`, `ST_AsBinary(geom)`
- Compatible with PostGIS, GeoParquet, GDAL/OGR, and most GIS toolchains

H3 indexes are stored as `BIGINT` (64-bit integer cell IDs).

---

## Installation

```bash
cp jars/dremio-geo-udf-1.0.0.jar /opt/dremio/jars/3rdparty/
# restart Dremio
```

**Docker:**
```bash
docker cp jars/dremio-geo-udf-1.0.0.jar try-dremio:/opt/dremio/jars/3rdparty/
docker restart try-dremio
```

---

## Building from Source

Requires Java 11+ and Maven (or the included `./mvnw` wrapper).

```bash
./mvnw clean package -Dmaven.javadoc.skip=true
# JAR will be in jars/dremio-geo-udf-1.0.0.jar
```

---

## Dependencies

All dependencies are shaded into the single JAR — nothing else to install:

| Library | Purpose |
|---|---|
| `org.locationtech.jts` 1.19 | JTS geometry engine (all ST_ functions) |
| `com.uber:h3` 3.7.2 | H3 hexagonal indexing |
| `org.osgeo:proj4j` 0.1.0 | Coordinate reference system support |
| `com.esri.geometry:esri-geometry-api` 2.2.4 | Geometry API (bundled, JTS-primary) |
