package com.gestaodeterritorio;
// GestaodoTerritorio-1.0
import javax.swing.*;
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
 * - Criar interface
 */
public class App {

    /**
     * Método principal que inicia a execução da aplicação.
     *
     * @param args argumentos da linha de comandos (não utilizados)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Neo4jConnector connector = new Neo4jConnector();

            CadastroLoader loader = new CadastroLoader();
            List<PropriedadeRustica> propriedades = null;
            try {
                propriedades = loader.carregar("Madeira-Moodle.csv");
            } catch (Exception e) {
                System.err.println("Erro ao carregar o CSV: " + e.getMessage());
            }

            assert propriedades != null;
            connector.criarPropriedadesGrafo(propriedades);
            connector.criarRelacoesAdjacenciaGrafo(propriedades);

            LogisticaAreas logistica = new LogisticaAreas(connector);
            System.out.println(logistica.mediaAgrupadaPorFreguesia("Arco da Calheta"));
            AppUI app = new AppUI(logistica);
            app.setVisible(true);
        });
    }
}
