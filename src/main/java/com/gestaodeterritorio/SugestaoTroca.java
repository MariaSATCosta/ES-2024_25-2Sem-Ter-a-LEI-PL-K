package com.gestaodeterritorio;

/**
 * Representa uma sugestão de troca entre duas propriedades rústicas.
 * A troca é sugerida com base na diferença de áreas entre as duas propriedades.
 */
public class SugestaoTroca {

    private PropriedadeRustica propriedade1;
    private PropriedadeRustica propriedade2;
    private double diferencaAreas;

    /**
     * Constrói uma nova sugestão de troca entre duas propriedades.
     *
     * @param p1              primeira propriedade envolvida na troca
     * @param p2              segunda propriedade envolvida na troca
     * @param diferencaAreas  diferença de áreas entre as duas propriedades
     */
    public SugestaoTroca(PropriedadeRustica p1, PropriedadeRustica p2, double diferencaAreas) {
        this.propriedade1 = p1;
        this.propriedade2 = p2;
        this.diferencaAreas = diferencaAreas;
    }

    /**
     * @return a primeira propriedade da sugestão de troca
     */
    public PropriedadeRustica getPropriedade1() {
        return propriedade1;
    }

    /**
     * @return a segunda propriedade da sugestão de troca
     */
    public PropriedadeRustica getPropriedade2() {
        return propriedade2;
    }

    /**
     * @return a diferença de áreas entre as duas propriedades
     */
    public double getDiferencaAreas() {
        return diferencaAreas;
    }

    /**
     * Define a primeira propriedade da sugestão de troca.
     *
     * @param propriedade1 primeira propriedade
     */
    public void setPropriedade1(PropriedadeRustica propriedade1) {
        this.propriedade1 = propriedade1;
    }

    /**
     * Define a segunda propriedade da sugestão de troca.
     *
     * @param propriedade2 segunda propriedade
     */
    public void setPropriedade2(PropriedadeRustica propriedade2) {
        this.propriedade2 = propriedade2;
    }

    /**
     * Define a diferença de áreas entre as propriedades.
     *
     * @param diferencaAreas valor da diferença de áreas
     */
    public void setDiferencaAreas(double diferencaAreas) {
        this.diferencaAreas = diferencaAreas;
    }

    /**
     * Representação textual da sugestão de troca.
     *
     * @return string com as propriedades e a diferença de áreas
     */
    @Override
    public String toString() {
        return "SugestaoTroca{" +
                "propriedade1=" + propriedade1 +
                ", propriedade2=" + propriedade2 +
                ", diferencaAreas=" + diferencaAreas +
                '}';
    }
}
