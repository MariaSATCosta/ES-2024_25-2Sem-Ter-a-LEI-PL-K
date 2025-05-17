package com.gestaodeterritorio;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class AppUI extends JFrame {

    private LogisticaAreas logisticaAreas;

    public AppUI(LogisticaAreas logisticaAreas) {
        this.logisticaAreas = logisticaAreas;

        setTitle("Logística de Áreas");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 300);
        setLayout(new GridLayout(1, 2, 20, 0));

        // === PAINEL ESQUERDO: Cálculo de Áreas ===
        JPanel areaPanel = new JPanel();
        areaPanel.setBorder(BorderFactory.createTitledBorder("Cálculo de Áreas"));
        areaPanel.setLayout(new GridLayout(6, 1, 10, 10));

        JComboBox<String> tipoCombo = new JComboBox<>(new String[]{"Ilha", "Município", "Freguesia"});
        JTextField nomeField = new JTextField();

        JButton calcularMediaBtn = new JButton("Calcular Média Simples");
        JButton calcularAgrupadaBtn = new JButton("Calcular Média Agrupada");

        areaPanel.add(new JLabel("Tipo de Região:"));
        areaPanel.add(tipoCombo);
        areaPanel.add(new JLabel("Nome da Região:"));
        areaPanel.add(nomeField);
        areaPanel.add(calcularMediaBtn);
        areaPanel.add(calcularAgrupadaBtn);

        // === PAINEL DIREITO: Sugestões de Trocas ===
        JPanel sugestaoPanel = new JPanel();
        sugestaoPanel.setBorder(BorderFactory.createTitledBorder("Gerar Sugestões de Troca"));
        sugestaoPanel.setLayout(new GridLayout(6, 1, 10, 10));

        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(3, 1, 20, 1);
        JSpinner numSugestoes = new JSpinner(spinnerModel);

        JRadioButton simplesRadio = new JRadioButton("Por Área Média", true);
        JRadioButton complexaRadio = new JRadioButton("Por Área Média/ Freguesia/ Quantidade de vizinhos");

        ButtonGroup group = new ButtonGroup();
        group.add(simplesRadio);
        group.add(complexaRadio);

        JButton gerarBtn = new JButton("Gerar Sugestões");

        sugestaoPanel.add(new JLabel("Número de sugestões:"));
        sugestaoPanel.add(numSugestoes);
        sugestaoPanel.add(simplesRadio);
        sugestaoPanel.add(complexaRadio);
        sugestaoPanel.add(new JLabel());
        sugestaoPanel.add(gerarBtn);

        // === ADICIONAR PAINÉIS ===
        add(areaPanel);
        add(sugestaoPanel);

        // === AÇÕES ===
        calcularMediaBtn.addActionListener((ActionEvent e) -> {
            String tipo = ((String) tipoCombo.getSelectedItem()).toLowerCase();
            String nome = nomeField.getText().trim();

            double resultado = switch (tipo) {
                case "ilha" -> logisticaAreas.mediaPorIlha(nome);
                case "município" -> logisticaAreas.mediaPorMunicipio(nome);
                case "freguesia" -> logisticaAreas.mediaPorFreguesia(nome);
                default -> 0.0;
            };

            JOptionPane.showMessageDialog(this,
                    String.format("Média das áreas em %s '%s': %.2f", tipo, nome, resultado));
        });

        calcularAgrupadaBtn.addActionListener((ActionEvent e) -> {
            String tipo = ((String) tipoCombo.getSelectedItem()).toLowerCase();
            String nome = nomeField.getText().trim();

            double resultado = switch (tipo) {
                case "ilha" -> logisticaAreas.mediaAgrupadaPorIlha(nome);
                case "município" -> logisticaAreas.mediaAgrupadaPorMunicipio(nome);
                case "freguesia" -> logisticaAreas.mediaAgrupadaPorFreguesia(nome);
                default -> 0.0;
            };

            JOptionPane.showMessageDialog(this,
                    String.format("Média das áreas agrupadas por proprietário em %s '%s': %.2f", tipo, nome, resultado));
        });

        gerarBtn.addActionListener((ActionEvent e) -> {
            int n = (Integer) numSugestoes.getValue();
            boolean complexa = complexaRadio.isSelected();

            var sugestoes = logisticaAreas.sugerirTrocas(n, complexa);

            StringBuilder sb = new StringBuilder();
            if (sugestoes.isEmpty()) {
                sb.append("Nenhuma sugestão encontrada.");
            } else {
                for (int i = 0; i < sugestoes.size(); i++) {
                    sb.append("Sugestão ").append(i + 1).append(": ")
                            .append(sugestoes.get(i).toString()).append("\n");
                }
            }

            JTextArea outputArea = new JTextArea(sb.toString(), 10, 40);
            outputArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(outputArea);
            JOptionPane.showMessageDialog(this, scrollPane, "Sugestões de Troca", JOptionPane.INFORMATION_MESSAGE);
        });

        setVisible(true);
    }
}
