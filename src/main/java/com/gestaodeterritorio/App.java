package com.gestaodeterritorio;

import java.util.List;

public class App {
    public static void main(String[] args) {
        try {
            Neo4jConnector connector = new Neo4jConnector();


            CadastroLoader loader = new CadastroLoader();
            List<PropriedadeRustica> propriedades = loader.carregar("Madeira-Moodle.csv");

            for (PropriedadeRustica p : propriedades) {
                System.out.println(p);
                connector.addPropriedade(p);
            }



            connector.close();
        } catch (Exception e) {
            System.err.println("Erro ao carregar o CSV: " + e.getMessage());
        }



    }
}
