package com.gestaodeterritorio;

import java.util.*;

/**
 * Classe responsável por operações das propriedades, como pelo cálculo de médias das áreas de propriedades
 * com base em diferentes divisões geográficas (freguesia, município ou ilha) e sugestões de troca.
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
    public double calculaMedia(String campoRegiao, String valorRegiao) {
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
    public double calculaMediaAgrupada(String campoRegiao, String valorRegiao) {
        List<Double> areas = connector.devolverAreasAgrupadasPorRegiao(campoRegiao, valorRegiao);
        if (areas.isEmpty()) {
            return 0.0;
        }
        double soma = areas.stream().mapToDouble(Double::doubleValue).sum();
        return soma / areas.size();
    }

    /**
     * Sugere um número limitado de trocas de propriedades entre proprietários diferentes,
     * simulando a troca e reversão de propriedades para verificar se a média das áreas agrupadas
     * (por adjacência e proprietário) no respetivo município de ambas as partes melhora após a troca.
     *
     * O critério base de troca é sempre a proximidade de área entre propriedades adjacentes
     * de donos distintos. Opcionalmente, podem ser usados critérios adicionais para ordenar
     * as propriedades candidatas:
     * <ul>
     *     <li><strong>Semelhança de área:</strong> diferença absoluta de áreas.</li>
     *     <li><strong>Mesma freguesia:</strong> prioriza propriedades da mesma freguesia.</li>
     *     <li><strong>Semelhança de adjacentes:</strong> prioriza número semelhante de propriedades adjacentes.</li>
     * </ul>
     *
     * @param numTrocas número máximo de sugestões de trocas a devolver.
     * @param usarCriteriosAvancados se verdadeiro, aplica critérios adicionais além da área.
     * @return lista de sugestões de trocas vantajosas para ambos os proprietários.
     */
    public List<SugestaoTroca> sugerirTrocas(int numTrocas, boolean usarCriteriosAvancados) {
        if (numTrocas <= 0) return new ArrayList<>();

        Set<PropriedadeRustica> propriedadesSet = connector.obterPropriedadesComAdjacentes();
        List<PropriedadeRustica> propriedades = new ArrayList<>(propriedadesSet);
        Collections.shuffle(propriedades);

        List<SugestaoTroca> sugestoes = new ArrayList<>();
        Set<String> trocasFeitas = new HashSet<>();

        for (PropriedadeRustica p1 : propriedades) {
            List<PropriedadeRustica> adjacentes = connector.obterPropriedadesAdjacentes(p1.getObjectId());

            for (PropriedadeRustica p2 : adjacentes) {
                if (p1.getOwner().equals(p2.getOwner())) continue;

                List<PropriedadeRustica> propriedadesDono2 = connector.obterPropriedadesPorOwner(p2.getOwner());

                Comparator<PropriedadeRustica> comparator = Comparator
                        .comparingDouble((PropriedadeRustica p3) -> {
                            try {
                                double area1 = Double.parseDouble(p1.getShapeArea());
                                double area3 = Double.parseDouble(p3.getShapeArea());
                                return Math.abs(area1 - area3);
                            } catch (NumberFormatException e) {
                                return Double.MAX_VALUE;
                            }
                        });

                if (usarCriteriosAvancados) {
                    comparator = comparator
                            .thenComparing(p3 -> !p1.getFreguesia().equals(p3.getFreguesia()))
                            .thenComparingInt(p3 -> {
                                int adj1 = adjacentes.size();
                                int adj3 = connector.obterPropriedadesAdjacentes(p3.getObjectId()).size();
                                return Math.abs(adj1 - adj3);
                            });
                }

                //propriedadesDono2.sort(comparator);
                List<PropriedadeRustica> listaMutavel = new ArrayList<>(propriedadesDono2);
                listaMutavel.sort(comparator);


                for (PropriedadeRustica p3 : listaMutavel) {
                    if (p3.getObjectId().equals(p2.getObjectId())) continue;

                    String key = gerarChaveTroca(p1.getObjectId(), p3.getObjectId());
                    if (trocasFeitas.contains(key)) continue;

                    try {
                        double area1 = Double.parseDouble(p1.getShapeArea());
                        double area3 = Double.parseDouble(p3.getShapeArea());
                        double areaDiff = Math.abs(area1 - area3);

                        double mediaAntesDono1 = calculaMediaAgrupada("municipio", p1.getMunicipio());
                        double mediaAntesDono2 = p1.getMunicipio().equals(p3.getMunicipio())
                                ? mediaAntesDono1
                                : calculaMediaAgrupada("municipio", p3.getMunicipio());

                        connector.trocarProprietarios(p1.getObjectId(), p3.getOwner(), p3.getObjectId(), p1.getOwner());

                        double mediaDepoisDono1 = calculaMediaAgrupada("municipio", p1.getMunicipio());
                        double mediaDepoisDono2 = p1.getMunicipio().equals(p3.getMunicipio())
                                ? mediaDepoisDono1
                                : calculaMediaAgrupada("municipio", p3.getMunicipio());

                        connector.reverterProprietarios(p1.getObjectId(), p1.getOwner(), p3.getObjectId(), p3.getOwner());

                        if (mediaDepoisDono1 > mediaAntesDono1 && mediaDepoisDono2 > mediaAntesDono2) {
                            sugestoes.add(new SugestaoTroca(p1, p3, areaDiff));
                            trocasFeitas.add(key);
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
     * Gera uma chave única para uma troca entre duas identificações.
     * A chave é composta pelos dois IDs concatenados em ordem lexicográfica crescente,
     * separados por um underscore ("_").
     *
     * Isso garante que a chave seja a mesma independentemente da ordem dos IDs fornecidos.
     *
     * @param id1 Primeiro identificador.
     * @param id2 Segundo identificador.
     * @return Uma string que representa a chave única da troca entre id1 e id2.
     */
    private String gerarChaveTroca(String id1, String id2) {
        return id1.compareTo(id2) < 0 ? id1 + "_" + id2 : id2 + "_" + id1;
    }

}
