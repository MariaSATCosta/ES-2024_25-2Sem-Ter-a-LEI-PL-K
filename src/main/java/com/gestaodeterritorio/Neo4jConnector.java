package com.gestaodeterritorio;

import io.github.cdimascio.dotenv.Dotenv;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.index.strtree.STRtree;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Classe responsável pela integração com a base de dados Neo4j.
 *
 * Permite inserir propriedades rústicas como nós do grafo e estabelecer relações de adjacência entre elas,
 * com base em operações espaciais (interseção ou contiguidade geométrica).
 */
public class Neo4jConnector implements AutoCloseable {
    private final Driver driver;

    /**
     * Construtor que estabelece ligação ao servidor Neo4j utilizando variáveis do ficheiro credentials.env.
     */
    public Neo4jConnector() {
        Dotenv dotenv = Dotenv.configure().filename("credentials.env").load();
        String uri = dotenv.get("NEO4J_URI");
        String user = dotenv.get("NEO4J_USER");
        String password = dotenv.get("NEO4J_PASSWORD");

        driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }

    /**
     * Fecha a ligação com a base de dados Neo4j.
     */
    @Override
    public void close() {
        driver.close();
    }

    /**
     * Insere propriedades no grafo se ainda não existirem na base de dados.
     *
     * @param propriedades lista de propriedades a inserir
     */
    public void criarPropriedadesGrafo(List<PropriedadeRustica> propriedades) {
        if (propriedades.isEmpty()) return;

        Set<String> existentes = obterPropriedadesExistentes();
        List<PropriedadeRustica> novasPropriedades = new ArrayList<>();

        for (PropriedadeRustica p : propriedades) {
            if (!existentes.contains(p.getObjectId())) {
                novasPropriedades.add(p);
            }
        }

        if (!novasPropriedades.isEmpty()) {
            inserirPropriedades(novasPropriedades);
            System.out.println("Inseridas " + novasPropriedades.size() + " novas propriedades");
        }
    }

    /**
     * Obtém o conjunto de objectIds das propriedades já existentes no grafo.
     *
     * @return conjunto de objectIds existentes
     */
    private Set<String> obterPropriedadesExistentes() {
        Set<String> propriedades = new HashSet<>();
        try (Session session = driver.session()) {
            session.readTransaction(tx -> {
                Result result = tx.run("MATCH (p:Propriedade) RETURN p.objectId AS id");
                while (result.hasNext()) {
                    propriedades.add(result.next().get("id").asString());
                }
                return null;
            });
        }
        return propriedades;
    }

    /**
     * Insere uma lista de propriedades como nós no grafo.
     *
     * @param propriedades lista de propriedades a inserir
     */
    private void inserirPropriedades(List<PropriedadeRustica> propriedades) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                String query = "UNWIND $propriedades AS prop " +
                        "CREATE (p:Propriedade {objectId: prop.objectId, parId: prop.parId, parNum: prop.parNum, " +
                        "municipio: prop.municipio, freguesia: prop.freguesia, shapeArea: prop.shapeArea, ilha: prop.ilha, " +
                        "geometry: prop.geometry})";
                List<Value> parametros = new ArrayList<>();
                for (PropriedadeRustica p : propriedades) {
                    parametros.add(Values.parameters(
                            "objectId", p.getObjectId(),
                            "parId", p.getParId(),
                            "parNum", p.getParNum(),
                            "municipio", p.getMunicipio(),
                            "freguesia", p.getFreguesia(),
                            "shapeArea", p.getShapeArea(),
                            "ilha", p.getIlha(),
                            "geometry", p.getGeometry()
                    ));
                }
                tx.run(query, Values.parameters("propriedades", parametros));
                return null;
            });
        }
    }

    /**
     * Cria relações de adjacência no grafo entre propriedades cuja geometria se intersecta ou toca.
     *
     * @param propriedades lista de propriedades com geometria
     */
    public void criarRelacoesAdjacenciaGrafo(List<PropriedadeRustica> propriedades) {
        STRtree index = new STRtree();
        for (PropriedadeRustica p : propriedades) {
            Geometry g = GeoUtils.parseGeometry(p.getGeometry());
            if (g != null) {
                index.insert(g.getEnvelopeInternal(), p);
            }
        }

        Set<String> relacoesExistentes = obterRelacoesExistentes();
        List<String[]> novasRelacoes = new ArrayList<>();

        for (PropriedadeRustica p1 : propriedades) {
            Geometry g1 = GeoUtils.parseGeometry(p1.getGeometry());
            if (g1 == null) continue;

            List<?> candidatos = index.query(g1.getEnvelopeInternal());

            for (Object obj : candidatos) {
                PropriedadeRustica p2 = (PropriedadeRustica) obj;

                if (!p1.getObjectId().equals(p2.getObjectId())) {
                    Geometry g2 = GeoUtils.parseGeometry(p2.getGeometry());
                    if (g2 == null) continue;

                    if (GeoUtils.saoAdjacentes(g1, g2)) {
                        String relacao = p1.getObjectId() + "-" + p2.getObjectId();
                        if (p1.getObjectId().compareTo(p2.getObjectId()) < 0 && !relacoesExistentes.contains(relacao)) {
                            novasRelacoes.add(new String[]{p1.getObjectId(), p2.getObjectId()});
                        }
                    }
                }
            }
        }
        if (!novasRelacoes.isEmpty()) {
            inserirRelacoes(novasRelacoes);
            System.out.println("Inseridas " + novasRelacoes.size() + " novas relações adjacentes");
        }
    }

    /**
     * Obtém as relações já existentes no grafo entre propriedades.
     *
     * @return conjunto de pares objectId representando relações já inseridas
     */
    private Set<String> obterRelacoesExistentes() {
        Set<String> relacoes = new HashSet<>();
        try (Session session = driver.session()) {
            session.readTransaction(tx -> {
                Result result = tx.run("MATCH (p1:Propriedade)-[:ADJACENTE_A]->(p2:Propriedade) RETURN p1.objectId, p2.objectId");
                while (result.hasNext()) {
                    Record record = result.next();
                    String relacao = record.get("p1.objectId").asString() + "-" + record.get("p2.objectId").asString();
                    relacoes.add(relacao);
                }
                return null;
            });
        }
        return relacoes;
    }

    /**
     * Insere novas relações de adjacência no grafo entre propriedades.
     *
     * @param novasRelacoes lista de pares [objectId1, objectId2] representando as relações a criar
     */
    private void inserirRelacoes(List<String[]> novasRelacoes) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                String query = "UNWIND $relacoes AS relacao " +
                        "MATCH (a:Propriedade {objectId: relacao[0]}), (b:Propriedade {objectId: relacao[1]}) " +
                        "MERGE (a)-[:ADJACENTE_A]->(b)";
                tx.run(query, Values.parameters("relacoes", novasRelacoes));
                return null;
            });
        }
    }
}
