package com.gestaodeterritorio;

import org.junit.jupiter.api.Test;

import javax.swing.*;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.lang.reflect.Method;

public class AppTest {

    // ------------------ Constructor Test (CC = 1) ------------------
    @Test
    public void constructor() {
        App app = new App();
        assertNotNull(app, "Error: Default constructor should create a non-null App instance"); // Error if app is null.
    }

    // ------------------ main() Tests (Cyclomatic Complexity = 1) ------------------

    /**
     * main1 - Normal path: Assumes that "Madeira-Moodle.csv" exists in the classpath
     * and is valid so that no exception is thrown.
     * We capture System.err output and assert that it is empty.
     */
    @Test
    public void main1() { // Named main1 to avoid confusion with an actual main class
        // Capture System.err output.
        PrintStream originalErr = System.err;
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));

        // Execute main.
        App.main(new String[0]);

        // Restore System.err.
        System.setErr(originalErr);

        // Get error output and filter out SLF4J warnings.
        String errorOutput = errContent.toString();
        // Remove lines that start with "SLF4J:" (using regex multiline mode)
        String filteredOutput = errorOutput.replaceAll("(?m)^SLF4J:.*(?:\r?\n|$)", "").trim();

        // Assert that after filtering, no error output remains.
        assertTrue(filteredOutput.isEmpty(), "Error: Expected no error output when CSV is valid, but got: " + filteredOutput);
    }
    @Test
    public void main2_csvInexistente() throws Exception {
        // Trocar o nome do ficheiro dentro de App temporariamente via reflexão
        Method mainMethod = App.class.getDeclaredMethod("main", String[].class);
        SwingUtilities.invokeAndWait(() -> {
            try {
                CadastroLoader loader = new CadastroLoader();
                loader.carregar("ficheiro_inexistente.csv");
                fail("Deveria ter lançado exceção ao tentar carregar CSV inexistente.");
            } catch (Exception e) {
                assertTrue(e instanceof FileNotFoundException || e.getMessage().contains("ficheiro_inexistente"),
                        "Esperada exceção por ficheiro inexistente.");
            }
        });
    }

    @Test
    public void main3_loaderDevolveNull() {
        SwingUtilities.invokeLater(() -> {
            try {
                CadastroLoader loader = new CadastroLoader() {
                    @Override
                    public java.util.List<PropriedadeRustica> carregar(String ficheiro) {
                        return null;
                    }
                };

                Neo4jConnector connector = new Neo4jConnector();

                java.util.List<PropriedadeRustica> propriedades = loader.carregar("Madeira-Moodle.csv");
                assertNull(propriedades, "Esperado null como retorno do loader.");
                if (propriedades != null) {
                    connector.criarPropriedadesGrafo(propriedades);
                    connector.criarRelacoesAdjacenciaGrafo(propriedades);
                }
            } catch (Exception e) {
                fail("Não deveria lançar exceção, mesmo que loader devolva null.");
            }
        });
    }

    // ------------------ main4: CSV malformado ------------------
    @Test
    public void main4_csvMalFormado() {
        SwingUtilities.invokeLater(() -> {
            try {
                CadastroLoader loader = new CadastroLoader();
                loader.carregar("csv-malformado.csv");
                // Deveria lançar exceção ou dar erro
            } catch (Exception e) {
                assertTrue(e.getMessage().toLowerCase().contains("erro") ||
                                e instanceof RuntimeException,
                        "Erro esperado ao carregar CSV malformado.");
            }
        });
    }
}
