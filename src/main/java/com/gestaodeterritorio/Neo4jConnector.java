package com.gestaodeterritorio;

import io.github.cdimascio.dotenv.Dotenv;
import org.neo4j.driver.*;

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
            String query = "MATCH (p:Propriedade {objectId: $objectId}) RETURN COUNT(p) AS count";
            return session.readTransaction(tx -> {
                Result result = tx.run(query, Values.parameters("objectId", objectId));
                return result.single().get("count").asInt() > 0;
            });
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






}
