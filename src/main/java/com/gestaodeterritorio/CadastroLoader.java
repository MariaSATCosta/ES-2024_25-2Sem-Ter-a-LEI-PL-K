package com.gestaodeterritorio;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.util.Objects;
import java.io.FileReader;
import java.io.Reader;
import java.util.List;

public class CadastroLoader {
    public List<PropriedadeRustica> carregar(String nomeFicheiro) throws Exception {
        InputStream input = getClass().getClassLoader().getResourceAsStream(nomeFicheiro);

        if (input == null) {
            throw new IllegalArgumentException("Ficheiro não encontrado: " + nomeFicheiro);
        }

        InputStreamReader reader = new InputStreamReader(input);

        return new CsvToBeanBuilder<PropriedadeRustica>(reader)
                .withType(PropriedadeRustica.class)
                .withSeparator(';') // <- usa ponto e vírgula corretamente
                .withIgnoreLeadingWhiteSpace(true)
                .withIgnoreEmptyLine(true)
                .build()
                .parse();
    }
}

