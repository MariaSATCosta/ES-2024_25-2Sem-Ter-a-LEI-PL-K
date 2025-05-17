package com.gestaodeterritorio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SugestaoTrocaTest {

    private PropriedadeRustica p1;
    private PropriedadeRustica p2;

    @BeforeEach
    public void setUp() {
        p1 = new PropriedadeRustica();
        p1.setObjectId("P1");
        p1.setShapeArea("100.0");

        p2 = new PropriedadeRustica();
        p2.setObjectId("P2");
        p2.setShapeArea("150.0");
    }

    // Constructor test (CC = 1)
    @Test
    public void constructor() {
        SugestaoTroca st = new SugestaoTroca(p1, p2, 50.0);

        // Verifica se propriedade1 foi atribuída corretamente
        assertEquals(p1, st.getPropriedade1(), "Constructor should correctly assign propriedade1");

        // Verifica se propriedade2 foi atribuída corretamente
        assertEquals(p2, st.getPropriedade2(), "Constructor should correctly assign propriedade2");

        // Verifica se diferencaAreas foi atribuída corretamente
        assertEquals(50.0, st.getDiferencaAreas(), "Constructor should correctly assign diferencaAreas");
    }

    // getPropriedade1 test (CC = 1)
    @Test
    public void getPropriedade1() {
        SugestaoTroca st = new SugestaoTroca(p1, p2, 50.0);
        assertEquals(p1, st.getPropriedade1(), "getPropriedade1 should return the correct property");
    }

    // getPropriedade2 test (CC = 1)
    @Test
    public void getPropriedade2() {
        SugestaoTroca st = new SugestaoTroca(p1, p2, 50.0);
        assertEquals(p2, st.getPropriedade2(), "getPropriedade2 should return the correct property");
    }

    // getDiferencaAreas test (CC = 1)
    @Test
    public void getDiferencaAreas() {
        SugestaoTroca st = new SugestaoTroca(p1, p2, 50.0);
        assertEquals(50.0, st.getDiferencaAreas(), "getDiferencaAreas should return the correct value");
    }

    // setPropriedade1 test (CC = 1)
    @Test
    public void setPropriedade1() {
        SugestaoTroca st = new SugestaoTroca(p1, p2, 50.0);
        PropriedadeRustica novaP1 = new PropriedadeRustica();
        novaP1.setObjectId("P3");
        novaP1.setShapeArea("200.0");
        st.setPropriedade1(novaP1);
        assertEquals(novaP1, st.getPropriedade1(), "setPropriedade1 should update the property correctly");
    }

    // setPropriedade2 test (CC = 1)
    @Test
    public void setPropriedade2() {
        SugestaoTroca st = new SugestaoTroca(p1, p2, 50.0);
        PropriedadeRustica novaP2 = new PropriedadeRustica();
        novaP2.setObjectId("P4");
        novaP2.setShapeArea("250.0");
        st.setPropriedade2(novaP2);
        assertEquals(novaP2, st.getPropriedade2(), "setPropriedade2 should update the property correctly");
    }

    // setDiferencaAreas test (CC = 1)
    @Test
    public void setDiferencaAreas() {
        SugestaoTroca st = new SugestaoTroca(p1, p2, 50.0);
        st.setDiferencaAreas(75.0);
        assertEquals(75.0, st.getDiferencaAreas(), "setDiferencaAreas should update the value correctly");
    }

    // toString test (CC = 1)
    @Test
    public void toStringTest() {
        SugestaoTroca st = new SugestaoTroca(p1, p2, 50.0);
        String result = st.toString();

        // Verifica se a string contém as propriedades e a diferença de áreas
        assertTrue(result.contains("propriedade1=" + p1), "toString should include propriedade1");
        assertTrue(result.contains("propriedade2=" + p2), "toString should include propriedade2");
        assertTrue(result.contains("diferencaAreas=50.0"), "toString should include diferencaAreas");
    }
}
