package com.gestaodeterritorio;

import java.util.List;

public class LogisticaAreas {

    private final Neo4jConnector connector;

    public LogisticaAreas(Neo4jConnector connector) {
        this.connector = connector;
    }

    public double mediaPorFreguesia(String nomeFreguesia) {
        return calculaMedia("freguesia", nomeFreguesia);
    }

    public double mediaPorMunicipio(String nomeMunicipio) {
        return calculaMedia("municipio", nomeMunicipio);
    }

    public double mediaPorDistrito(String nomeDistrito) {
        return calculaMedia("ilha", nomeDistrito);
    }

    // método genérico reutilizável
    private double calculaMedia(String campoRegiao, String valorRegiao) {
        List<Double> areas = connector.devolverAreasPorRegiao(campoRegiao, valorRegiao);
        if (areas.isEmpty()) {
            return 0.0;
        }
        double soma = areas.stream().mapToDouble(Double::doubleValue).sum();
        return soma / areas.size();
    }

    /**
     * Calcula a média das áreas agrupadas por adjacência e proprietário numa freguesia.
     *
     * @param nomeFreguesia nome da freguesia a filtrar.
     * @return média das áreas agrupadas, ou 0.0 se nenhuma for encontrada.
     */
    public double mediaAgrupadaPorFreguesia(String nomeFreguesia) {
        return calculaMediaAgrupada("freguesia", nomeFreguesia);
    }

    /**
     * Calcula a média das áreas agrupadas por adjacência e proprietário num município.
     *
     * @param nomeMunicipio nome do município a filtrar.
     * @return média das áreas agrupadas, ou 0.0 se nenhuma for encontrada.
     */
    public double mediaAgrupadaPorMunicipio(String nomeMunicipio) {
        return calculaMediaAgrupada("municipio", nomeMunicipio);
    }

    /**
     * Calcula a média das áreas agrupadas por adjacência e proprietário numa ilha.
     *
     * @param nomeIlha nome da ilha a filtrar.
     * @return média das áreas agrupadas, ou 0.0 se nenhuma for encontrada.
     */
    public double mediaAgrupadaPorIlha(String nomeIlha) {
        return calculaMediaAgrupada("ilha", nomeIlha);
    }

    /**
     * Método auxiliar que calcula a média de áreas agrupadas por adjacência e proprietário,
     * para um determinado campo e valor de região.
     *
     * @param campoRegiao nome do campo geográfico (ex: freguesia, município, ilha).
     * @param valorRegiao valor específico a filtrar.
     * @return média das áreas agrupadas, ou 0.0 se nenhuma for encontrada.
     */
    private double calculaMediaAgrupada(String campoRegiao, String valorRegiao) {
        List<Double> areas = connector.devolverAreasAgrupadasPorRegiao(campoRegiao, valorRegiao);
        if (areas.isEmpty()) {
            return 0.0;
        }
        double soma = areas.stream().mapToDouble(Double::doubleValue).sum();
        return soma / areas.size();
    }
}
