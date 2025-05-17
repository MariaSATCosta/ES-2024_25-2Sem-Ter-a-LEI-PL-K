package com.gestaodeterritorio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class LogisticaAreasTest {

    private static Neo4jConnector mockConnector;
    private LogisticaAreas service;

    private static PropriedadeRustica p1, p2, p3;

    @BeforeEach
    void setUp() {
        mockConnector = mock(Neo4jConnector.class);
        service = Mockito.spy(new LogisticaAreas(mockConnector));


        p1 = new PropriedadeRustica();
        p1.setObjectId("1");
        p1.setOwner("Dono1");
        p1.setShapeArea("1000");
        p1.setFreguesia("F1");
        p1.setMunicipio("M1");

        p2 = new PropriedadeRustica();
        p2.setObjectId("2");
        p2.setOwner("Dono2");
        p2.setShapeArea("1005");
        p2.setFreguesia("F1");
        p2.setMunicipio("M1");

        p3 = new PropriedadeRustica();
        p3.setObjectId("3");
        p3.setOwner("Dono2");
        p3.setShapeArea("1003");
        p3.setFreguesia("F1");
        p3.setMunicipio("M1");

//        List<PropriedadeRustica> propriedades = new ArrayList<>();
//        propriedades.add(p1);
//        propriedades.add(p2);
//        propriedades.add(p3);
//        mockConnector.criarPropriedadesGrafo(propriedades);
//        mockConnector.criarRelacoesAdjacenciaGrafo(propriedades);
    }

    @AfterAll
    static void teardown() {
        mockConnector.close();
    }

    // ------------------ Constructor Tests (Cyclomatic Complexity = 1) ------------------
    @Test
    void constructor() throws Exception {
        // Verify that the connector passed in is stored correctly via reflection.
        Field field = LogisticaAreas.class.getDeclaredField("connector");
        field.setAccessible(true);
        Object stored = field.get(service);
        assertSame(mockConnector, stored,
                "Error: The constructor should store the provided Neo4jConnector instance"); // Error if connector not set.
    }

    @Test
    void testMediaAgrupadaPorFreguesiaExistente() {
        // Stub fetchAreasByRegion to return two values whose average is 15.0.
        when(mockConnector.devolverAreasAgrupadasPorRegiao("freguesia", "FregX"))
                .thenReturn(Arrays.asList(10.0, 20.0));
        double result = service.mediaAgrupadaPorFreguesia("FregX");
        assertEquals(15.0, result, 1e-6,
                "Error: mediaAgrupadaPorFreguesia should compute the correct average for non-empty list");
    }

    @Test
    void testMediaAgrupadaPorFreguesiaInexistente() {
        double media = service.mediaAgrupadaPorFreguesia("Não Existe");
        assertEquals(0.0, media, "A média deve ser 0.0 para freguesias inexistentes.");
    }

    @Test
    void testMediaAgrupadaSemGruposAdjacentes() {
        // Supondo que há uma freguesia com dados mas sem propriedades adjacentes entre si:
        double media = service.mediaAgrupadaPorFreguesia("Freguesia Isolada");
        assertEquals(0.0, media, "A média deve ser 0.0 se não existirem grupos adjacentes válidos.");
    }

    // ------------------ mediaPorFreguesia Tests (Cyclomatic Complexity = 1) ------------------
    @Test
    void mediaPorFreguesia() {
        // Stub fetchAreasByRegion to return two values whose average is 15.0.
        when(mockConnector.devolverAreasPorRegiao("freguesia", "FregX"))
                .thenReturn(Arrays.asList(10.0, 20.0));
        double result = service.mediaPorFreguesia("FregX");
        assertEquals(15.0, result, 1e-6,
                "Error: mediaPorFreguesia should compute the correct average for non-empty list"); // Error if average wrong.
    }

    // ------------------ mediaPorMunicipio Tests (Cyclomatic Complexity = 1) ------------------
    @Test
    void mediaPorMunicipio() {
        // Stub fetchAreasByRegion to return three values whose average is 20.0.
        when(mockConnector.devolverAreasPorRegiao("municipio", "MunY"))
                .thenReturn(Arrays.asList(10.0, 20.0, 30.0));
        double result = service.mediaPorMunicipio("MunY");
        assertEquals(20.0, result, 1e-6,
                "Error: mediaPorMunicipio should compute the correct average for non-empty list"); // Error if average wrong.
    }

    // ------------------ mediaPorDistrito Tests (Cyclomatic Complexity = 1) ------------------
    @Test
    void mediaPorDistrito() {
        // Stub fetchAreasByRegion to return four values whose average is 25.0.
        // Note: implementation uses campoRegiao="ilha" due to a typo, but we stub accordingly.
        when(mockConnector.devolverAreasPorRegiao("ilha", "DistZ"))
                .thenReturn(Arrays.asList(10.0, 20.0, 30.0, 40.0));
        double result = service.mediaPorIlha("DistZ");
        assertEquals(25.0, result, 1e-6,
                "Error: mediaPorDistrito should compute the correct average for non-empty list"); // Error if average wrong.
    }

    // ------------------ calculaMedia Tests (Cyclomatic Complexity = 2) ------------------
    @Test
    void calculaMedia1() throws Exception {
        // Path 1: empty list should yield 0.0
        when(mockConnector.devolverAreasPorRegiao("anyField", "anyValue"))
                .thenReturn(Collections.emptyList());
        Method m = LogisticaAreas.class.getDeclaredMethod("calculaMedia", String.class, String.class);
        m.setAccessible(true);
        double result = (double) m.invoke(service, "anyField", "anyValue");
        assertEquals(0.0, result, 1e-6,
                "Error: calculaMedia should return 0.0 when the list of areas is empty"); // Error if not zero.
    }

    @Test
    void calculaMedia2() throws Exception {
        // Path 2: non-empty list should yield sum/size
        when(mockConnector.devolverAreasPorRegiao("field", "value"))
                .thenReturn(Arrays.asList(5.0, 15.0, 20.0));
        Method m = LogisticaAreas.class.getDeclaredMethod("calculaMedia", String.class, String.class);
        m.setAccessible(true);
        double result = (double) m.invoke(service, "field", "value");
        double expected = (5.0 + 15.0 + 20.0) / 3.0;
        assertEquals(expected, result, 1e-6,
                "Error: calculaMedia should compute the correct average for a non-empty list"); // Error if average wrong.

    }
    @Test
    void sugerirTrocas_numTrocasZero() {
        List<SugestaoTroca> resultado = service.sugerirTrocas(0, false);
        assertTrue(resultado.isEmpty(), "A lista deve estar vazia quando numTrocas <= 0.");
    }

    @Test
    void sugerirTrocas1_propriedadesSemAdjacentes() {
        when(mockConnector.obterPropriedadesComAdjacentes()).thenReturn(Set.of(p1));
        when(mockConnector.obterPropriedadesAdjacentes("1")).thenReturn(List.of());

        List<SugestaoTroca> resultado = service.sugerirTrocas(1, false);

        assertTrue(resultado.isEmpty(), "Nenhuma sugestão deve ser feita se não houver adjacentes.");
    }

    @Test
    void sugerirTrocas2_mesmoDonoIgnorado() {
        p2.setOwner("Dono1"); // Mesmo dono que p1
        when(mockConnector.obterPropriedadesComAdjacentes()).thenReturn(Set.of(p1));
        when(mockConnector.obterPropriedadesAdjacentes("1")).thenReturn(List.of(p2));

        List<SugestaoTroca> resultado = service.sugerirTrocas(1, false);

        assertTrue(resultado.isEmpty(), "Trocas entre o mesmo proprietário devem ser ignoradas.");
    }

}