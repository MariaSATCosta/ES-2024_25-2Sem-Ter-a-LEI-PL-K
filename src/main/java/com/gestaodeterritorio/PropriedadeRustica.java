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

    public String getObjectId() {
        return objectId;
    }

    public String getParId() {
        return parId;
    }

    public String getParNum(){
        return parNum;
    }

    public String getShapeLength() {
        return shapeLength;
    }

    public String getShapeArea(){
        return shapeArea;
    }

    public String getGeometry() {
        return geometry;
    }

    public String getOwner() {
        return owner;
    }

    public String getFreguesia() {
        return freguesia;
    }

    public String getMunicipio() {
        return municipio;
    }

    public String getIlha() {
        return ilha;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s - %s - %s (Área: %s) [%s]",
                objectId, municipio, freguesia, parNum, shapeArea, ilha);
    }
}
