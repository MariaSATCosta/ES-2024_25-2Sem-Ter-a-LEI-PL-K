package com.gestaodeterritorio;

import com.opencsv.bean.CsvBindByName;

public class PropriedadeRustica {

    @CsvBindByName(column = "OBJECTID")
    private String objectId;

    @CsvBindByName(column = "PAR_ID")
    private String parId;

    @CsvBindByName(column = "PAR_NUM")
    private String parNum;

    @CsvBindByName(column = "Shape_Length")
    private String shapeLength;

    @CsvBindByName(column = "Shape_Area")
    private String shapeArea;

    @CsvBindByName(column = "geometry")
    private String geometry;

    @CsvBindByName(column = "OWNER")
    private String owner;

    @CsvBindByName(column = "Freguesia")
    private String freguesia;

    @CsvBindByName(column = "Municipio")
    private String municipio;

    @CsvBindByName(column = "Ilha")
    private String ilha;

    @Override
    public String toString() {
        return String.format("[%s] %s - %s - %s (Área: %s) [%s]",
                objectId, municipio, freguesia, parNum, shapeArea, ilha);
    }
}
