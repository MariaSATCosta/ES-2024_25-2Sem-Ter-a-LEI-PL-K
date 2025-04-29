package com.gestaodeterritorio;

import java.util.List;

/**
 * Classe responsável pelo cálculo de médias das áreas de propriedades
 * com base em diferentes divisões geográficas (freguesia, município ou ilha).
 */
public class LogisticaAreas {

    private final Neo4jConnector connector;

    /**
     * Construtor da classe LogisticaAreas.
     *
     * @param connector instância do conector Neo4j para realizar as consultas.
     */
    public LogisticaAreas(Neo4jConnector connector) {
        this.connector = connector;
    }

    /**
     * Calcula a média das áreas das propriedades numa determinada freguesia.
     *
     * @param nomeFreguesia nome da freguesia a filtrar.
     * @return média das áreas das propriedades, ou 0.0 se nenhuma for encontrada.
     */
    public double mediaPorFreguesia(String nomeFreguesia) {
        return calculaMedia("freguesia", nomeFreguesia);
    }

    /**
     * Calcula a média das áreas das propriedades num determinado município.
     *
     * @param nomeMunicipio nome do município a filtrar.
     * @return média das áreas das propriedades, ou 0.0 se nenhuma for encontrada.
     */
    public double mediaPorMunicipio(String nomeMunicipio) {
        return calculaMedia("municipio", nomeMunicipio);
    }

    /**
     * Calcula a média das áreas das propriedades numa determinada ilha.
     *
     * @param nomeIlha nome da ilha a filtrar.
     * @return média das áreas das propriedades, ou 0.0 se nenhuma for encontrada.
     */
    public double mediaPorIlha(String nomeIlha) {
        return calculaMedia("ilha", nomeIlha);
    }

    /**
     * Método auxiliar que calcula a média de áreas para um determinado campo e valor de região.
     *
     * @param campoRegiao nome do campo geográfico a filtrar (ex: freguesia, município, ilha).
     * @param valorRegiao valor a filtrar no campo indicado.
     * @return média das áreas das propriedades, ou 0.0 se nenhuma for encontrada.
     */
    private double calculaMedia(String campoRegiao, String valorRegiao) {
        List<Double> areas = connector.fetchAreasByRegion(campoRegiao, valorRegiao);
        if (areas.isEmpty()) {
            return 0.0;
        }
        double soma = areas.stream().mapToDouble(Double::doubleValue).sum();
        return soma / areas.size();
    }

}
