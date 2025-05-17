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
        List<Double> areas = connector.fetchAreasByRegion(campoRegiao, valorRegiao);
        if (areas.isEmpty()) {
            return 0.0;
        }
        double soma = areas.stream().mapToDouble(Double::doubleValue).sum();
        return soma / areas.size();
    }

}
