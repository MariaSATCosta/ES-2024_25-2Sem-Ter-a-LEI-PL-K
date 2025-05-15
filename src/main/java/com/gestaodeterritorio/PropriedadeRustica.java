package com.gestaodeterritorio;

import com.opencsv.bean.CsvBindByName;

/**
 * Representa uma propriedade rústica com atributos espaciais e administrativos.
 *
 * Esta classe é usada para mapear diretamente os dados provenientes de um ficheiro CSV,
 * utilizando anotações da biblioteca OpenCSV.
 */
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

    /**
     * @return o identificador único da propriedade
     */
    public String getObjectId() {
        return objectId;
    }

    /**
     * @return o identificador do parcela
     */
    public String getParId() {
        return parId;
    }

    /**
     * @return o número da parcela
     */
    public String getParNum() {
        return parNum;
    }

    /**
     * @return o perímetro da geometria da propriedade
     */
    public String getShapeLength() {
        return shapeLength;
    }

    /**
     * @return a área da propriedade
     */
    public String getShapeArea() {
        return shapeArea;
    }

    /**
     * @return a geometria da propriedade em formato WKT
     */
    public String getGeometry() {
        return geometry;
    }

    /**
     * @return o nome do proprietário
     */
    public String getOwner() {
        return owner;
    }

    /**
     * @return a freguesia onde a propriedade se encontra
     */
    public String getFreguesia() {
        return freguesia;
    }

    /**
     * @return o município da propriedade
     */
    public String getMunicipio() {
        return municipio;
    }

    /**
     * @return a ilha onde a propriedade se situa (ex: Madeira)
     */
    public String getIlha() {
        return ilha;
    }

    /**
     * Define o identificador único da propriedade.
     *
     * @param objectId identificador da propriedade
     */
    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    /**
     * Define o identificador da parcela.
     *
     * @param parId identificador da parcela
     */
    public void setParId(String parId) {
        this.parId = parId;
    }

    /**
     * Define o número da parcela.
     *
     * @param parNum número da parcela
     */
    public void setParNum(String parNum) {
        this.parNum = parNum;
    }

    /**
     * Define o comprimento da geometria da propriedade.
     *
     * @param shapeLength comprimento da geometria
     */
    public void setShapeLength(String shapeLength) {
        this.shapeLength = shapeLength;
    }

    /**
     * Define a área da propriedade.
     *
     * @param shapeArea área da propriedade
     */
    public void setShapeArea(String shapeArea) {
        this.shapeArea = shapeArea;
    }

    /**
     * Define a geometria da propriedade no formato WKT.
     *
     * @param geometry geometria da propriedade
     */
    public void setGeometry(String geometry) {
        this.geometry = geometry;
    }

    /**
     * Define o nome do proprietário da propriedade.
     *
     * @param owner nome do proprietário
     */
    public void setOwner(String owner) {
        this.owner = owner;
    }

    /**
     * Define a freguesia da propriedade.
     *
     * @param freguesia freguesia onde se localiza a propriedade
     */
    public void setFreguesia(String freguesia) {
        this.freguesia = freguesia;
    }

    /**
     * Define a ilha onde a propriedade se localiza.
     *
     * @param ilha nome da ilha
     */
    public void setIlha(String ilha) {
        this.ilha = ilha;
    }

    /**
     * Define o município da propriedade.
     *
     * @param municipio nome do município
     */
    public void setMunicipio(String municipio) {
        this.municipio = municipio;
    }
    
    /**
     * Representação textual formatada da propriedade rústica.
     *
     * @return string com os principais dados da propriedade
     */
    @Override
    public String toString() {
        return String.format("[%s] %s - %s - %s (Área: %s) [%s]",
                objectId, municipio, freguesia, parNum, shapeArea, ilha);
    }
}
