package com.gestaodeterritorio;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.util.Objects;
import java.io.FileReader;
import java.io.Reader;
import java.util.List;

/**
 * Classe responsável por carregar dados de propriedades rústicas a partir de um ficheiro CSV.
 *
 * Utiliza a biblioteca OpenCSV para mapear as linhas do CSV para objetos do tipo {@code PropriedadeRustica}.
 */
public class CadastroLoader {

    /**
     * Carrega os dados do ficheiro CSV fornecido e transforma-os numa lista de objetos {@code PropriedadeRustica}.
     *
     * @param nomeFicheiro o nome do ficheiro CSV localizado em {@code src/main/resources}
     * @return uma lista de objetos {@code PropriedadeRustica} com base nos dados do ficheiro
     * @throws Exception se o ficheiro não for encontrado ou se ocorrer algum erro durante a leitura
     */
    public List<PropriedadeRustica> carregar(String nomeFicheiro) throws Exception {
        InputStream input = getClass().getClassLoader().getResourceAsStream(nomeFicheiro);

        if (input == null) {
            throw new IllegalArgumentException("Ficheiro não encontrado: " + nomeFicheiro);
        }

        InputStreamReader reader = new InputStreamReader(input);

        return new CsvToBeanBuilder<PropriedadeRustica>(reader)
                .withType(PropriedadeRustica.class)
                .withSeparator(';') // usa ponto e vírgula como delimitador
                .withIgnoreLeadingWhiteSpace(true)
                .withIgnoreEmptyLine(true)
                .build()
                .parse();
    }
}
