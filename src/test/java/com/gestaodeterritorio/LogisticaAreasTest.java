package com.gestaodeterritorio;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LogisticaAreasTest {

    private static Neo4jConnector connector;
    private static LogisticaAreas logistica;

    @BeforeAll
    static void setup() {
        connector = new Neo4jConnector();
        logistica = new LogisticaAreas(connector);
    }

    @AfterAll
    static void teardown() {
        connector.close();
    }

    @Test
    void testMediaAgrupadaPorFreguesiaExistente() {
        double media = logistica.mediaAgrupadaPorFreguesia("Arco da Calheta");
        assertTrue(media > 0, "A média agrupada deve ser maior que zero para freguesias com dados válidos.");
    }

    @Test
    void testMediaAgrupadaPorFreguesiaInexistente() {
        double media = logistica.mediaAgrupadaPorFreguesia("Não Existe");
        assertEquals(0.0, media, "A média deve ser 0.0 para freguesias inexistentes.");
    }

    @Test
    void testMediaAgrupadaSemGruposAdjacentes() {
        // Supondo que há uma freguesia com dados mas sem propriedades adjacentes entre si:
        double media = logistica.mediaAgrupadaPorFreguesia("Freguesia Isolada");
        assertEquals(0.0, media, "A média deve ser 0.0 se não existirem grupos adjacentes válidos.");
    }
}
