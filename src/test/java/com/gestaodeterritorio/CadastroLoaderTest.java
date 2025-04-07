package com.gestaodeterritorio;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CadastroLoaderTest {

    // ------------------ Constructor Test (CC = 1) ------------------
    @Test
    public void constructor() {
        CadastroLoader loader = new CadastroLoader();
        assertNotNull(loader, "Error: Constructor should create a non-null instance of CadastroLoader"); // Error if loader is null.
    }

    // ------------------ carregar Tests (Cyclomatic Complexity = 1) ------------------

    /**
     * carregar - Error path:
     * Assumes that the file "nonexistent.csv" does not exist in the classpath.
     * Verifies that the method throws an IllegalArgumentException with an expected error message.
     */
    @Test
    public void carregar() {
        CadastroLoader loader = new CadastroLoader();
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            loader.carregar("nonexistent.csv");
        }, "Error: Expected IllegalArgumentException when CSV file is missing"); // Error if no exception is thrown.
        String expectedMessage = "Ficheiro não encontrado:";
        assertTrue(exception.getMessage().contains(expectedMessage), "Error: Exception message should contain '" + expectedMessage + "'"); // Error if message does not contain expected text.
    }
}
