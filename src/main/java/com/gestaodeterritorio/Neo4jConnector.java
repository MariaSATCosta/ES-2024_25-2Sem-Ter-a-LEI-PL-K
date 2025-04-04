package com.gestaodeterritorio;

import io.github.cdimascio.dotenv.Dotenv;
import org.neo4j.driver.*;

public class Neo4jConnector implements AutoCloseable {
    private final Driver driver;

    public Neo4jConnector() {
        Dotenv dotenv = Dotenv.load();
        String uri = dotenv.get("NEO4J_URI");
        String user = dotenv.get("NEO4J_USER");
        String password = dotenv.get("NEO4J_PASSWORD");

        driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }

    @Override
    public void close() {
        driver.close();
    }

    public void addPropriedade(PropriedadeRustica prop) {
        try (Session session = driver.session()) {
            String query = "CREATE (:Propriedade {objectId: $objectId, parId: $parId, parNum: $parNum, municipio: $municipio, " +
                    "freguesia: $freguesia, shapeArea: $shapeArea, ilha: $ilha, geometry: $geometry}) RETURN 1";

            session.writeTransaction(tx -> {
                Result result = tx.run(query, Values.parameters(
                        "objectId", prop.getObjectId(),
                        "parId", prop.getParId(),
                        "parNum", prop.getParNum(),
                        "municipio", prop.getMunicipio(),
                        "freguesia", prop.getFreguesia(),
                        "shapeArea", prop.getShapeArea(),
                        "ilha", prop.getIlha(),
                        "geometry", prop.getGeometry()));

                result.consume();
                return null;
            });
        }
    }
    


}
