package com.gestaodeterritorio;

import java.util.*;

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

    /**
     * Sugere um número limitado de trocas de propriedades entre proprietarios diferentes,
     * priorizando trocas que aumentem a média das áreas agrupadas (por adjacência e proprietário)
     * no respetivo município de ambas as partes.
     *
     * As trocas são baseadas na proximidade de área entre propriedades adjacentes
     * e de proprietarios distintos. A operação é simulada (troca e reversão) para verificar o impacto
     * nas médias antes de ser sugerida.
     *
     * @param numTrocas número máximo de sugestões de trocas a devolver.
     * @return lista de sugestões de trocas vantajosas para ambos os proprietarios.
     */
    public List<SugestaoTroca> sugerirTrocasArea(int numTrocas) {
        Set<PropriedadeRustica> propriedadesSet = connector.obterPropriedadesComAdjacentes();
        List<PropriedadeRustica> propriedades = new ArrayList<>(propriedadesSet);
        Collections.shuffle(propriedades);

        List<SugestaoTroca> sugestoes = new ArrayList<>();

        for (PropriedadeRustica p1 : propriedades) {
            List<PropriedadeRustica> adjacentes = connector.obterPropriedadesAdjacentes(p1.getObjectId());

            for (PropriedadeRustica p2 : adjacentes) {
                if (p1.getOwner().equals(p2.getOwner())) continue; // Proprietarios diferentes

                List<PropriedadeRustica> propriedadesDono2 = connector.obterPropriedadesPorOwner(p2.getOwner());

                propriedadesDono2.sort(Comparator
                        .comparingDouble((PropriedadeRustica p3) -> {
                            try {
                                double area1 = Double.parseDouble(p1.getShapeArea());
                                double area3 = Double.parseDouble(p3.getShapeArea());
                                return Math.abs(area1 - area3);
                            } catch (NumberFormatException e) {
                                return Double.MAX_VALUE;
                            }
                        })
                );

                for (PropriedadeRustica p3 : propriedadesDono2) {
                    if (p3.getObjectId().equals(p2.getObjectId())) continue;

                    try {
                        double area1 = Double.parseDouble(p1.getShapeArea());
                        double area3 = Double.parseDouble(p3.getShapeArea());
                        double areaDiff = Math.abs(area1 - area3);

                        double areaDono1Antes = calculaMediaAgrupada("municipio", p1.getMunicipio());
                        double areaDono2Antes = p1.getMunicipio().equals(p3.getMunicipio())
                                ? areaDono1Antes
                                : calculaMediaAgrupada("municipio", p3.getMunicipio());

                        connector.trocarProprietarios(p1.getObjectId(), p3.getOwner(), p3.getObjectId(), p1.getOwner());

                        double areaDono1Depois = calculaMediaAgrupada("municipio", p1.getMunicipio());
                        double areaDono2Depois = p1.getMunicipio().equals(p3.getMunicipio())
                                ? areaDono1Depois
                                : calculaMediaAgrupada("municipio", p3.getMunicipio());

                        connector.reverterProprietarios(p1.getObjectId(), p1.getOwner(), p3.getObjectId(), p3.getOwner());

                        if (areaDono1Depois > areaDono1Antes && areaDono2Depois > areaDono2Antes) {
                            sugestoes.add(new SugestaoTroca(p1, p3, areaDiff));
                            if (sugestoes.size() == numTrocas) {
                                return sugestoes;
                            }
                        }

                    } catch (NumberFormatException e) {
                        System.err.println("Erro ao converter áreas: " + e.getMessage());
                    }
                }
            }
        }

        return sugestoes;
    }

    /**
     * Sugere um número limitado de trocas de propriedades entre proprietarios diferentes,
     * priorizando trocas que aumentem a média das áreas agrupadas no município de ambos,
     * e que apresentem maior compatibilidade com base nos seguintes critérios (em ordem):
     * <ul>
     *     <li>Semelhança de área</li>
     *     <li>Mesma freguesia</li>
     *     <li>Número semelhante de adjacentes</li>
     * </ul>
     *
     * As trocas são simuladas para avaliar se a média de áreas agrupadas aumenta
     * para ambos os lados, e só são sugeridas se isso ocorrer.
     *
     * @param numTrocas número máximo de sugestões de trocas a devolver.
     * @return lista de sugestões de trocas vantajosas para ambos os proprietarios com base em múltiplos critérios.
     */
    public List<SugestaoTroca> sugerirTrocas(int numTrocas) {
        Set<PropriedadeRustica> propriedadesSet = connector.obterPropriedadesComAdjacentes();
        List<PropriedadeRustica> propriedades = new ArrayList<>(propriedadesSet);
        Collections.shuffle(propriedades);

        List<SugestaoTroca> sugestoes = new ArrayList<>();

        for (PropriedadeRustica p1 : propriedades) {
            List<PropriedadeRustica> adjacentes = connector.obterPropriedadesAdjacentes(p1.getObjectId());

            for (PropriedadeRustica p2 : adjacentes) {
                if (p1.getOwner().equals(p2.getOwner())) continue; // Donos diferentes

                List<PropriedadeRustica> propriedadesDono2 = connector.obterPropriedadesPorOwner(p2.getOwner());

                propriedadesDono2.sort(Comparator
                        .comparingDouble((PropriedadeRustica p3) -> {
                            try {
                                double area1 = Double.parseDouble(p1.getShapeArea());
                                double area3 = Double.parseDouble(p3.getShapeArea());
                                return Math.abs(area1 - area3);
                            } catch (NumberFormatException e) {
                                return Double.MAX_VALUE;
                            }
                        })
                        .thenComparing((PropriedadeRustica p3) ->
                                !p1.getFreguesia().equals(p3.getFreguesia())
                        )
                        .thenComparingInt((PropriedadeRustica p3) -> {
                            int adj1 = connector.obterPropriedadesAdjacentes(p1.getObjectId()).size();
                            int adj3 = connector.obterPropriedadesAdjacentes(p3.getObjectId()).size();
                            return Math.abs(adj1 - adj3);
                        })
                );

                for (PropriedadeRustica p3 : propriedadesDono2) {
                    if (p3.getObjectId().equals(p2.getObjectId())) continue;

                    try {
                        double area1 = Double.parseDouble(p1.getShapeArea());
                        double area3 = Double.parseDouble(p3.getShapeArea());
                        double areaDiff = Math.abs(area1 - area3);

                        double areaDono1Antes = calculaMediaAgrupada("municipio", p1.getMunicipio());
                        double areaDono2Antes = p1.getMunicipio().equals(p3.getMunicipio())
                                ? areaDono1Antes
                                : calculaMediaAgrupada("municipio", p3.getMunicipio());

                        connector.trocarProprietarios(p1.getObjectId(), p3.getOwner(), p3.getObjectId(), p1.getOwner());

                        double areaDono1Depois = calculaMediaAgrupada("municipio", p1.getMunicipio());
                        double areaDono2Depois = p1.getMunicipio().equals(p3.getMunicipio())
                                ? areaDono1Depois
                                : calculaMediaAgrupada("municipio", p3.getMunicipio());

                        connector.reverterProprietarios(p1.getObjectId(), p1.getOwner(), p3.getObjectId(), p3.getOwner());

                        if (areaDono1Depois > areaDono1Antes && areaDono2Depois > areaDono2Antes) {
                            sugestoes.add(new SugestaoTroca(p1, p3, areaDiff));
                            if (sugestoes.size() == numTrocas) {
                                return sugestoes;
                            }
                        }

                    } catch (NumberFormatException e) {
                        System.err.println("Erro ao converter áreas: " + e.getMessage());
                    }
                }
            }
        }

        return sugestoes;
    }
}
