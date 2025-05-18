package com.gestaodeterritorio;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AppUITest {

    private static final String AUTHOR = System.getProperty("user.name");
    private static final String DATE_TIME = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

    private LogisticaAreas mockLogisticaAreas;

    @BeforeEach
    void setup() {
        mockLogisticaAreas = Mockito.mock(LogisticaAreas.class);
    }

    // --- AppUI constructor CC=4 ---
    @Test
    void AppUI1() {
        // Test basic construction and presence of components without triggering any action
        AppUI app = new AppUI(mockLogisticaAreas);
        // Check JFrame properties
        assertEquals("Logística de Áreas", app.getTitle(), "Window title should be set correctly");
        assertEquals(700, app.getWidth(), "Window width should be 700");
        assertEquals(300, app.getHeight(), "Window height should be 300");
        // Check layout is GridLayout
        assertTrue(app.getContentPane().getLayout() instanceof java.awt.GridLayout,
                "Layout should be GridLayout");
        app.dispose();
    }

    @Test
    void AppUI2() {
        // Test adding panels - check if left and right panels are added (count of components)
        AppUI app = new AppUI(mockLogisticaAreas);
        int count = app.getContentPane().getComponentCount();
        assertEquals(2, count, "Two main panels (left and right) should be added");
        app.dispose();
    }

    @Test
    void AppUI3() {
        // Test that the combo box has expected items (Ilha, Município, Freguesia)
        AppUI app = new AppUI(mockLogisticaAreas);
        JPanel leftPanel = (JPanel) app.getContentPane().getComponent(0);
        JComboBox<?> combo = null;
        for (var comp : leftPanel.getComponents()) {
            if (comp instanceof JComboBox) {
                combo = (JComboBox<?>) comp;
                break;
            }
        }
        assertNotNull(combo, "Tipo de Região JComboBox should be present");
        assertEquals(3, combo.getItemCount(), "Tipo de Região combo should have 3 items");
        assertEquals("Ilha", combo.getItemAt(0), "First combo item should be Ilha");
        app.dispose();
    }

    @Test
    void AppUI4() {
        // Test that the right panel has the radio buttons correctly added
        AppUI app = new AppUI(mockLogisticaAreas);
        JPanel rightPanel = (JPanel) app.getContentPane().getComponent(1);
        boolean simplesFound = false;
        boolean complexaFound = false;
        for (var comp : rightPanel.getComponents()) {
            if (comp instanceof JRadioButton) {
                JRadioButton rb = (JRadioButton) comp;
                if ("Por Área Média".equals(rb.getText())) simplesFound = true;
                if ("Por Área Média/ Freguesia/ Quantidade de vizinhos".equals(rb.getText())) complexaFound = true;
            }
        }
        assertTrue(simplesFound, "Simple radio button should be present");
        assertTrue(complexaFound, "Complex radio button should be present");
        app.dispose();
    }

    // --- calcularMediaBtn actionPerformed CC=4 ---
    @Test
    void calcularMediaBtn1() {
        // Path: tipo = "ilha"
        AppUI app = new AppUI(mockLogisticaAreas);
        JComboBox<String> combo = findComboBox(app);
        JTextField nomeField = findNomeField(app);
        assert combo != null;
        combo.setSelectedItem("Ilha");
        assert nomeField != null;
        nomeField.setText("TestIlha");
        Mockito.when(mockLogisticaAreas.mediaPorIlha("TestIlha")).thenReturn(10.5);

        JButton btn = findButton(app);
        // Fire action
        assert btn != null;
        btn.doClick();

        Mockito.verify(mockLogisticaAreas).mediaPorIlha("TestIlha");
        app.dispose();
    }

    @Test
    void calcularMediaBtn2() {
        // Path: tipo = "município"
        AppUI app = new AppUI(mockLogisticaAreas);
        JComboBox<String> combo = findComboBox(app);
        JTextField nomeField = findNomeField(app);
        assert combo != null;
        combo.setSelectedItem("Município");
        assert nomeField != null;
        nomeField.setText("TestMun");

        Mockito.when(mockLogisticaAreas.mediaPorMunicipio("TestMun")).thenReturn(20.5);

        JButton btn = findButton(app);
        assert btn != null;
        btn.doClick();

        Mockito.verify(mockLogisticaAreas).mediaPorMunicipio("TestMun");
        app.dispose();
    }

    @Test
    void calcularMediaBtn3() {
        // Path: tipo = "freguesia"
        AppUI app = new AppUI(mockLogisticaAreas);
        JComboBox<String> combo = findComboBox(app);
        JTextField nomeField = findNomeField(app);
        assert combo != null;
        combo.setSelectedItem("Freguesia");
        assert nomeField != null;
        nomeField.setText("TestFreg");

        Mockito.when(mockLogisticaAreas.mediaPorFreguesia("TestFreg")).thenReturn(30.5);

        JButton btn = findButton(app);
        assert btn != null;
        btn.doClick();

        Mockito.verify(mockLogisticaAreas).mediaPorFreguesia("TestFreg");
        app.dispose();
    }
    @Test
    void calcularMediaBtn4_defaultCase() {
        AppUI app = new AppUI(mockLogisticaAreas);
        JComboBox<String> combo = findComboBox(app);
        JTextField nomeField = findNomeField(app);
        assert combo != null;
        combo.addItem("Outro"); // adicionar valor inesperado
        combo.setSelectedItem("Outro");
        assert nomeField != null;
        nomeField.setText("Invalido");

        JButton btn = findButton(app);
        assert btn != null;
        btn.doClick();

        // Nenhum método deve ser chamado
        Mockito.verifyNoInteractions(mockLogisticaAreas);
        app.dispose();
    }
    @Test
    void calcularAgrupadaBtn1() {
        AppUI app = new AppUI(mockLogisticaAreas);
        JComboBox<String> combo = findComboBox(app);
        JTextField nomeField = findNomeField(app);
        assert combo != null;
        combo.setSelectedItem("Ilha");
        assert nomeField != null;
        nomeField.setText("IlhaTeste");

        Mockito.when(mockLogisticaAreas.mediaAgrupadaPorIlha("IlhaTeste")).thenReturn(42.0);

        JButton btn = findButton(app, "Calcular Média Agrupada");
        assert btn != null;
        btn.doClick();

        Mockito.verify(mockLogisticaAreas).mediaAgrupadaPorIlha("IlhaTeste");
        app.dispose();
    }
    @Test
    void gerarBtn1_simplesComResultados() {
        AppUI app = new AppUI(mockLogisticaAreas);
        JSpinner spinner = findSpinner(app);
        assert spinner != null;
        spinner.setValue(2);

        var mockSugestoes = java.util.List.of(new SugestaoTroca(new PropriedadeRustica(), new PropriedadeRustica(), 8.0), new SugestaoTroca(new PropriedadeRustica(), new PropriedadeRustica(), 10.0));
        Mockito.when(mockLogisticaAreas.sugerirTrocas(2, false)).thenReturn(mockSugestoes);

        JButton gerarBtn = findButton(app, "Gerar Sugestões");
        assert gerarBtn != null;
        gerarBtn.doClick();

        Mockito.verify(mockLogisticaAreas).sugerirTrocas(2, false);
        app.dispose();
    }
    @Test
    void gerarBtn2_complexaSemResultados() {
        AppUI app = new AppUI(mockLogisticaAreas);
        JRadioButton complexaRadio = findRadioButton(app, "Por Área Média/ Freguesia/ Quantidade de vizinhos");
        assert complexaRadio != null;
        complexaRadio.setSelected(true);

        JSpinner spinner = findSpinner(app);
        assert spinner != null;
        spinner.setValue(3);

        Mockito.when(mockLogisticaAreas.sugerirTrocas(3, true)).thenReturn(java.util.Collections.emptyList());

        JButton gerarBtn = findButton(app, "Gerar Sugestões");
        assert gerarBtn != null;
        gerarBtn.doClick();

        Mockito.verify(mockLogisticaAreas).sugerirTrocas(3, true);
        app.dispose();
    }


    // Helper methods to find components inside the JFrame

    private JComboBox<String> findComboBox(AppUI app) {
        JPanel leftPanel = (JPanel) app.getContentPane().getComponent(0);
        for (var comp : leftPanel.getComponents()) {
            if (comp instanceof JComboBox) return (JComboBox<String>) comp;
        }
        return null;
    }

    private JTextField findNomeField(AppUI app) {
        JPanel leftPanel = (JPanel) app.getContentPane().getComponent(0);
        for (var comp : leftPanel.getComponents()) {
            if (comp instanceof JTextField) return (JTextField) comp;
        }
        return null;
    }

    private JButton findButton(AppUI app) {
        return findButton(app, "Calcular Média Simples");
    }

    private JButton findButton(AppUI app, String text) {
        for (Component comp : app.getContentPane().getComponents()) {
            if (comp instanceof JPanel panel) {
                for (Component c : panel.getComponents()) {
                    if (c instanceof JButton btn && btn.getText().equals(text)) {
                        return btn;
                    }
                }
            }
        }
        return null;
    }

    private JSpinner findSpinner(AppUI app) {
        JPanel rightPanel = (JPanel) app.getContentPane().getComponent(1);
        for (var comp : rightPanel.getComponents()) {
            if (comp instanceof JSpinner) return (JSpinner) comp;
        }
        return null;
    }

    private JRadioButton findRadioButton(AppUI app, String text) {
        JPanel rightPanel = (JPanel) app.getContentPane().getComponent(1);
        for (var comp : rightPanel.getComponents()) {
            if (comp instanceof JRadioButton rb && rb.getText().equals(text)) return rb;
        }
        return null;
    }

}
