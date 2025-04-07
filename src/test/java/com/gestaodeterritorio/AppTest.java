package com.gestaodeterritorio;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

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
}
