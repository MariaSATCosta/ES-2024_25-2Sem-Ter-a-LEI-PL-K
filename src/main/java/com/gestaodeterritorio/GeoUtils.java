package com.gestaodeterritorio;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTReader;

/**
 * Classe utilitária para operações geográficas relacionadas com objetos espaciais.
 *
 * Esta classe é usada para interpretar geometrias em formato WKT (Well-Known Text)
 * e para avaliar relações espaciais, como adjacência e interseção entre geometrias.
 *
 * É particularmente útil no contexto da criação de grafos geográficos de propriedades rústicas.
 */
public class GeoUtils {

    /**
     * Converte uma string WKT (Well-Known Text) numa geometria do tipo {@link Geometry}.
     *
     * @param wkt a string em formato WKT que representa uma geometria
     * @return um objeto {@code Geometry} correspondente à string WKT, ou {@code null} se ocorrer erro
     */
    public static Geometry parseGeometry(String wkt) {
        try {
            WKTReader reader = new WKTReader();
            return reader.read(wkt);
        } catch (Exception e) {
            System.err.println("Erro ao converter WKT: " + e.getMessage());
            return null;
        }
    }

    /**
     * Verifica se duas geometrias são adjacentes, isto é, se se tocam ou se se intersectam.
     *
     * @param g1 a primeira geometria
     * @param g2 a segunda geometria
     * @return {@code true} se as geometrias forem adjacentes, {@code false} caso contrário
     */
    public static boolean saoAdjacentes(Geometry g1, Geometry g2) {
        return g1.touches(g2) || g1.intersects(g2);
    }
}
