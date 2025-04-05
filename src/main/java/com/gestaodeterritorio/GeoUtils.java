package com.gestaodeterritorio;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTReader;

public class GeoUtils {
    public static Geometry parseGeometry(String wkt) {
        try {
            WKTReader reader = new WKTReader();
            return reader.read(wkt);
        } catch (Exception e) {
            System.err.println("Erro ao converter WKT: " + e.getMessage());
            return null;
        }
    }

    public static boolean saoAdjacentes(Geometry g1, Geometry g2) {
        return g1.touches(g2) || g1.intersects(g2);
    }
}
