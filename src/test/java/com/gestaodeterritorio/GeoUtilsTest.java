package com.gestaodeterritorio;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.locationtech.jts.geom.Geometry;

public class GeoUtilsTest {

    // ------------------ Constructor Test (CC = 1) ------------------
    @Test
    public void constructor() {
        // Even though GeoUtils contains only static methods,
        // instantiating it should not produce a null object.
        GeoUtils instance = new GeoUtils();
        assertNotNull(instance, "Error: GeoUtils instance should not be null after construction"); // Error if instance is null.
    }

    // ------------------ parseGeometry Tests (CC = 2) ------------------

    // Test 1: Valid WKT should be parsed to a non-null Geometry.
    @Test
    public void parseGeometry1() {
        String validWKT = "POINT (0 0)";
        Geometry geom = GeoUtils.parseGeometry(validWKT);
        assertNotNull(geom, "Error: Expected a valid Geometry object for valid WKT input"); // Error if geom is null.
    }

    // Test 2: Invalid WKT should result in a null Geometry.
    @Test
    public void parseGeometry2() {
        String invalidWKT = "INVALID_WKT";
        Geometry geom = GeoUtils.parseGeometry(invalidWKT);
        assertNull(geom, "Error: Expected null when parsing invalid WKT"); // Error if geom is not null.
    }

    // ------------------ saoAdjacentes Tests (CC = 3) ------------------

    // Test 1: Geometries that touch (share a boundary) should return true.
    @Test
    public void saoAdjacentes1() {
        String polygon1 = "POLYGON((0 0, 2 0, 2 2, 0 2, 0 0))";
        String polygon2 = "POLYGON((2 0, 4 0, 4 2, 2 2, 2 0))";
        Geometry g1 = GeoUtils.parseGeometry(polygon1);
        Geometry g2 = GeoUtils.parseGeometry(polygon2);
        boolean result = GeoUtils.saoAdjacentes(g1, g2);
        assertTrue(result, "Error: Expected true when geometries touch each other"); // Error if result is false.
    }

    // Test 2: Geometries that intersect (one completely inside the other) should return true.
    @Test
    public void saoAdjacentes2() {
        String outerPolygon = "POLYGON((0 0, 4 0, 4 4, 0 4, 0 0))";
        String innerPolygon = "POLYGON((1 1, 3 1, 3 3, 1 3, 1 1))";
        Geometry g1 = GeoUtils.parseGeometry(outerPolygon);
        Geometry g2 = GeoUtils.parseGeometry(innerPolygon);
        boolean result = GeoUtils.saoAdjacentes(g1, g2);
        assertTrue(result, "Error: Expected true when geometries intersect (one inside the other)"); // Error if result is false.
    }

    // Test 3: Geometries that neither touch nor intersect should return false.
    @Test
    public void saoAdjacentes3() {
        String polygon1 = "POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))";
        String polygon2 = "POLYGON((2 2, 3 2, 3 3, 2 3, 2 2))";
        Geometry g1 = GeoUtils.parseGeometry(polygon1);
        Geometry g2 = GeoUtils.parseGeometry(polygon2);
        boolean result = GeoUtils.saoAdjacentes(g1, g2);
        assertFalse(result, "Error: Expected false when geometries neither touch nor intersect"); // Error if result is true.
    }
}
