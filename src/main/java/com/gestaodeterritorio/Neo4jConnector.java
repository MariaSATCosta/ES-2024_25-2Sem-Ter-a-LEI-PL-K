package com.gestaodeterritorio;

import io.github.cdimascio.dotenv.Dotenv;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.index.strtree.STRtree;
import org.neo4j.driver.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Neo4jConnector implements AutoCloseable {
    private final Driver driver;

    public Neo4jConnector() {
        Dotenv dotenv = Dotenv.configure().filename("credentials.env").load();
        String uri = dotenv.get("NEO4J_URI");
        String user = dotenv.get("NEO4J_USER");
        String password = dotenv.get("NEO4J_PASSWORD");

        driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }

    @Override
    public void close() {
        driver.close();
    }

    public boolean propriedadeExiste(String objectId) {
        try (Session session = driver.session()) {
            String query = "MATCH (p:Propriedade {objectId: $objectId}) RETURN COUNT(p) > 0 AS existe";
            return session.readTransaction(tx -> tx.run(query, Values.parameters("objectId", objectId))
                    .single().get("existe").asBoolean());
        }
    }

    public void addPropriedade(PropriedadeRustica prop) {
        if (propriedadeExiste(prop.getObjectId())) {
            System.out.println("Propriedade já existe no grafo: " + prop.getObjectId());
            return;
        }

        try (Session session = driver.session()) {
            String query = "CREATE (p:Propriedade {objectId: $objectId, parId: $parId, parNum: $parNum, " +
                    "municipio: $municipio, freguesia: $freguesia, shapeArea: $shapeArea, ilha: $ilha, geometry: $geometry})";

            session.writeTransaction(tx -> {
                tx.run(query, Values.parameters(
                        "objectId", prop.getObjectId(),
                        "parId", prop.getParId(),
                        "parNum", prop.getParNum(),
                        "municipio", prop.getMunicipio(),
                        "freguesia", prop.getFreguesia(),
                        "shapeArea", prop.getShapeArea(),
                        "ilha", prop.getIlha(),
                        "geometry", prop.getGeometry()));
                return null;
            });

            System.out.println("Propriedade inserida no grafo: " + prop.getObjectId());
        }
    }

    public void criarRelacoesAdjacencia(List<PropriedadeRustica> propriedades) {
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

        inserirRelacoes(novasRelacoes);
    }

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

    private void inserirRelacoes(List<String[]> novasRelacoes) {
        if (novasRelacoes.isEmpty()) return;

        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                String query = "UNWIND $relacoes AS relacao " +
                        "MATCH (a:Propriedade {objectId: relacao[0]}), (b:Propriedade {objectId: relacao[1]}) " +
                        "MERGE (a)-[:ADJACENTE_A]->(b)";
                tx.run(query, Values.parameters("relacoes", novasRelacoes));
                return null;
            });
        }
        System.out.println("Criadas " + novasRelacoes.size() + " relações adjacentes.");
    }

}
