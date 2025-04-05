package com.gestaodeterritorio;

import java.util.List;

/**
 * Classe principal da aplicação de Gestão do Território.
 *
 * Esta classe inicializa a aplicação, carregando os dados de propriedades rústicas
 * a partir de um ficheiro CSV e construindo um grafo em Neo4j com as propriedades
 * e as suas relações de adjacência.
 *
 * Responsabilidades:
 * - Criar conexão com a base de dados Neo4j
 * - Carregar dados de um ficheiro CSV
 * - Criar os nós e relações no grafo
 */
public class App {

    /**
     * Método principal que inicia a execução da aplicação.
     *
     * @param args argumentos da linha de comandos (não utilizados)
     */
    public static void main(String[] args) {
        try {
            Neo4jConnector connector = new Neo4jConnector();

            CadastroLoader loader = new CadastroLoader();
            List<PropriedadeRustica> propriedades = loader.carregar("Madeira-Moodle.csv");

            connector.criarPropriedadesGrafo(propriedades);
            connector.criarRelacoesAdjacenciaGrafo(propriedades);

            connector.close();
        } catch (Exception e) {
            System.err.println("Erro ao carregar o CSV: " + e.getMessage());
        }
    }
}
