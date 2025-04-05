package com.gestaodeterritorio;

import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.*;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Author: ${user.name}
 * Date: 2025-04-05T12:00:00  // (Replace with the current date/time as needed)
 *
 * Cyclomatic Complexity per method:
 *   - Constructor: 1
 *   - close: 1
 *   - criarPropriedadesGrafo: 3
 *   - obterPropriedadesExistentes: 2
 *   - inserirPropriedades: 1
 *   - criarRelacoesAdjacenciaGrafo: 7
 *   - obterRelacoesExistentes: 2
 *   - inserirRelacoes: 1
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class Neo4jConnectorTest {

    private Neo4j embeddedDatabase;
    private Driver testDriver;
    private Neo4jConnector connector;

    // Dummy PropriedadeRustica class for testing purposes.
    public static class PropriedadeRustica {
        private final String objectId;
        private final String parId;
        private final String parNum;
        private final String municipio;
        private final String freguesia;
        private final double shapeArea;
        private final String ilha;
        private final String geometry;

        public PropriedadeRustica(String objectId, String parId, String parNum,
                                  String municipio, String freguesia,
                                  double shapeArea, String ilha, String geometry) {
            this.objectId = objectId;
            this.parId = parId;
            this.parNum = parNum;
            this.municipio = municipio;
            this.freguesia = freguesia;
            this.shapeArea = shapeArea;
            this.ilha = ilha;
            this.geometry = geometry;
        }

        public String getObjectId() { return objectId; }
        public String getParId() { return parId; }
        public String getParNum() { return parNum; }
        public String getMunicipio() { return municipio; }
        public String getFreguesia() { return freguesia; }
        public double getShapeArea() { return shapeArea; }
        public String getIlha() { return ilha; }
        public String getGeometry() { return geometry; }
    }

    // A simple dummy GeoUtils for testing.
    public static class GeoUtils {
        // Returns a dummy Geometry object if the string equals "valid", otherwise null.
        public static Object parseGeometry(String geomStr) {
            return "valid".equals(geomStr) ? new Object() : null;
        }

        // For testing, returns true if both geometries are non-null and a provided flag is "adjacent".
        public static boolean saoAdjacentes(Object g1, Object g2) {
            // For our dummy, we assume they are adjacent if both are non-null.
            return (g1 != null && g2 != null);
        }
    }

    @BeforeAll
    public void setUpAll() {
        embeddedDatabase = Neo4jBuilders.newInProcessBuilder().withDisabledServer().build();
        testDriver = GraphDatabase.driver(embeddedDatabase.boltURI(), Config.builder().withoutEncryption().build());
    }

    @AfterAll
    public void tearDownAll() {
        testDriver.close();
        embeddedDatabase.close();
    }

    @BeforeEach
    public void setUp() throws Exception {
        connector = new Neo4jConnector();
        // Override the connector's driver field with our test driver using reflection
        Field driverField = Neo4jConnector.class.getDeclaredField("driver");
        driverField.setAccessible(true);
        driverField.set(connector, testDriver);

        // Clean up the database before each test
        try (Session session = testDriver.session()) {
            session.writeTransaction(tx -> {
                tx.run("MATCH (n) DETACH DELETE n");
                return null;
            });
        }
    }

    // ------------------ Constructor ------------------
    @Test
    public void constructor() {
        // Test that the constructor initializes a non-null driver.
        assertNotNull(connector, "Connector instance should not be null after construction"); // Error: Connector is null.
    }

    // ------------------ close ------------------
    @Test
    public void close() {
        connector.close();
        // After closing, trying to open a session should fail.
        Exception exception = assertThrows(Exception.class, () -> {
            try (Session session = testDriver.session()) {
                session.run("RETURN 1");
            }
        }, "Expected an exception when using a closed driver"); // Error: Driver did not throw exception on closed session.
    }

    // ------------------ criarPropriedadesGrafo (Cyclomatic Complexity = 3) ------------------
    @Test
    public void criarPropriedadesGrafo1() {
        // Path 1: Empty list -> should return immediately without insertion.
        List<PropriedadeRustica> emptyList = new ArrayList<>();
        connector.criarPropriedadesGrafo(emptyList);
        // Assert no nodes were created.
        try (Session session = testDriver.session()) {
            long count = session.readTransaction(tx -> tx.run("MATCH (n:Propriedade) RETURN count(n) AS count")
                    .single().get("count").asLong());
            assertEquals(0, count, "No properties should be inserted when given an empty list"); // Error: Unexpected node count.
        }
    }

    @Test
    public void criarPropriedadesGrafo2() {
        // Path 2: List with properties that already exist in the database.
        // First, insert a property manually.
        PropriedadeRustica prop = new PropriedadeRustica("1", "p1", "num1", "mun", "freg", 100.0, "ilha", "valid");
        // Insert manually using a direct Cypher call.
        try (Session session = testDriver.session()) {
            session.writeTransaction(tx -> {
                tx.run("CREATE (:Propriedade {objectId:'1', parId:'p1', parNum:'num1', municipio:'mun', freguesia:'freg', shapeArea:100.0, ilha:'ilha', geometry:'valid'})");
                return null;
            });
        }
        List<PropriedadeRustica> list = new ArrayList<>();
        list.add(prop);
        connector.criarPropriedadesGrafo(list);
        // Assert that no additional property was inserted.
        try (Session session = testDriver.session()) {
            long count = session.readTransaction(tx -> tx.run("MATCH (n:Propriedade) RETURN count(n) AS count")
                    .single().get("count").asLong());
            assertEquals(1, count, "No new property should be inserted if it already exists"); // Error: Duplicate property inserted.
        }
    }

    @Test
    public void criarPropriedadesGrafo3() {
        // Path 3: List with new properties not in the database.
        PropriedadeRustica prop1 = new PropriedadeRustica("1", "p1", "num1", "mun", "freg", 100.0, "ilha", "valid");
        PropriedadeRustica prop2 = new PropriedadeRustica("2", "p2", "num2", "mun", "freg", 150.0, "ilha", "valid");
        List<PropriedadeRustica> list = new ArrayList<>();
        list.add(prop1);
        list.add(prop2);
        connector.criarPropriedadesGrafo(list);
        // Assert that two properties were inserted.
        try (Session session = testDriver.session()) {
            long count = session.readTransaction(tx -> tx.run("MATCH (n:Propriedade) RETURN count(n) AS count")
                    .single().get("count").asLong());
            assertEquals(2, count, "Two new properties should be inserted"); // Error: Incorrect number of properties inserted.
        }
    }

    // ------------------ obterPropriedadesExistentes (Cyclomatic Complexity = 2) ------------------
    @Test
    public void obterPropriedadesExistentes1() throws Exception {
        // Path 1: When no properties exist.
        // Invoke private method via reflection.
        java.lang.reflect.Method method = Neo4jConnector.class.getDeclaredMethod("obterPropriedadesExistentes");
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        Set<String> result = (Set<String>) method.invoke(connector);
        assertTrue(result.isEmpty(), "Expected empty set when no properties exist"); // Error: Set is not empty.
    }

    @Test
    public void obterPropriedadesExistentes2() throws Exception {
        // Path 2: When properties exist.
        // Insert a property manually.
        try (Session session = testDriver.session()) {
            session.writeTransaction(tx -> {
                tx.run("CREATE (:Propriedade {objectId:'1'})");
                return null;
            });
        }
        java.lang.reflect.Method method = Neo4jConnector.class.getDeclaredMethod("obterPropriedadesExistentes");
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        Set<String> result = (Set<String>) method.invoke(connector);
        assertTrue(result.contains("1"), "Expected the set to contain the inserted property objectId '1'"); // Error: Inserted property not found.
    }

    // ------------------ inserirPropriedades (Cyclomatic Complexity = 1) ------------------
    @Test
    public void inserirPropriedades() throws Exception {
        // Prepare a list with one property.
        PropriedadeRustica prop = new PropriedadeRustica("10", "p10", "num10", "mun", "freg", 200.0, "ilha", "valid");
        List<PropriedadeRustica> list = new ArrayList<>();
        list.add(prop);
        // Invoke private method inserirPropriedades via reflection.
        java.lang.reflect.Method method = Neo4jConnector.class.getDeclaredMethod("inserirPropriedades", List.class);
        method.setAccessible(true);
        method.invoke(connector, list);
        // Verify that the property is in the database.
        try (Session session = testDriver.session()) {
            long count = session.readTransaction(tx -> tx.run("MATCH (n:Propriedade {objectId:'10'}) RETURN count(n) AS count")
                    .single().get("count").asLong());
            assertEquals(1, count, "Expected one property to be inserted via inserirPropriedades"); // Error: Property insertion failed.
        }
    }

    // ------------------ criarRelacoesAdjacenciaGrafo (Cyclomatic Complexity = 7) ------------------
    // For these tests, we simulate different paths by manipulating the geometry strings and objectId values.
    // Note: In these tests, we assume the dummy GeoUtils above is used.
    @Test
    public void criarRelacoesAdjacenciaGrafo1() {
        // Path 1: In index creation, a property returns null geometry.
        PropriedadeRustica prop = new PropriedadeRustica("A", "pA", "numA", "mun", "freg", 100.0, "ilha", "invalid");
        List<PropriedadeRustica> list = new ArrayList<>();
        list.add(prop);
        connector.criarRelacoesAdjacenciaGrafo(list);
        // Expect no relationships inserted.
        try (Session session = testDriver.session()) {
            long count = session.readTransaction(tx -> tx.run("MATCH ()-[:ADJACENTE_A]->() RETURN count(*) AS count")
                    .single().get("count").asLong());
            assertEquals(0, count, "No relationships should be inserted when geometry is invalid (null) in index creation"); // Error: Relationship inserted unexpectedly.
        }
    }

    @Test
    public void criarRelacoesAdjacenciaGrafo2() {
        // Path 2: In outer loop, p1 returns null geometry so is skipped.
        PropriedadeRustica prop1 = new PropriedadeRustica("A", "pA", "numA", "mun", "freg", 100.0, "ilha", "invalid");
        PropriedadeRustica prop2 = new PropriedadeRustica("B", "pB", "numB", "mun", "freg", 120.0, "ilha", "valid");
        List<PropriedadeRustica> list = new ArrayList<>();
        list.add(prop1);
        list.add(prop2);
        connector.criarRelacoesAdjacenciaGrafo(list);
        // Expect no relationships because p1 is skipped.
        try (Session session = testDriver.session()) {
            long count = session.readTransaction(tx -> tx.run("MATCH ()-[:ADJACENTE_A]->() RETURN count(*) AS count")
                    .single().get("count").asLong());
            assertEquals(0, count, "No relationships should be created when p1 has invalid geometry"); // Error: Unexpected relationship creation.
        }
    }

    @Test
    public void criarRelacoesAdjacenciaGrafo3() {
        // Path 3: When comparing a property with itself (objectId equal), relationship should not be created.
        PropriedadeRustica prop = new PropriedadeRustica("A", "pA", "numA", "mun", "freg", 100.0, "ilha", "valid");
        List<PropriedadeRustica> list = new ArrayList<>();
        // Add the same property twice.
        list.add(prop);
        list.add(prop);
        connector.criarRelacoesAdjacenciaGrafo(list);
        try (Session session = testDriver.session()) {
            long count = session.readTransaction(tx -> tx.run("MATCH ()-[:ADJACENTE_A]->() RETURN count(*) AS count")
                    .single().get("count").asLong());
            assertEquals(0, count, "No self-adjacency relationship should be created when comparing the same property"); // Error: Self-relationship created.
        }
    }

    @Test
    public void criarRelacoesAdjacenciaGrafo4() {
        // Path 4: Candidate property returns null geometry (simulate by using "invalid" for second property).
        PropriedadeRustica prop1 = new PropriedadeRustica("A", "pA", "numA", "mun", "freg", 100.0, "ilha", "valid");
        PropriedadeRustica prop2 = new PropriedadeRustica("B", "pB", "numB", "mun", "freg", 120.0, "ilha", "invalid");
        List<PropriedadeRustica> list = new ArrayList<>();
        list.add(prop1);
        list.add(prop2);
        connector.criarRelacoesAdjacenciaGrafo(list);
        try (Session session = testDriver.session()) {
            long count = session.readTransaction(tx -> tx.run("MATCH ()-[:ADJACENTE_A]->() RETURN count(*) AS count")
                    .single().get("count").asLong());
            assertEquals(0, count, "No relationship should be created when candidate property has invalid geometry"); // Error: Relationship created unexpectedly.
        }
    }

    @Test
    public void criarRelacoesAdjacenciaGrafo5() {
        // Path 5: GeoUtils.saoAdjacentes returns false (simulate by forcing non-adjacency with two valid but non-adjacent properties).
        // For our dummy GeoUtils, if both geometries are valid, saoAdjacentes returns true.
        // So to simulate false, we provide only one property.
        PropriedadeRustica prop1 = new PropriedadeRustica("A", "pA", "numA", "mun", "freg", 100.0, "ilha", "valid");
        List<PropriedadeRustica> list = new ArrayList<>();
        list.add(prop1);
        connector.criarRelacoesAdjacenciaGrafo(list);
        try (Session session = testDriver.session()) {
            long count = session.readTransaction(tx -> tx.run("MATCH ()-[:ADJACENTE_A]->() RETURN count(*) AS count")
                    .single().get("count").asLong());
            assertEquals(0, count, "No relationship should be created when there is only one property (no candidate to compare)"); // Error: Relationship created unexpectedly.
        }
    }

    @Test
    public void criarRelacoesAdjacenciaGrafo6() {
        // Path 6: p1.getObjectId().compareTo(p2.getObjectId()) is not < 0, so relationship should not be inserted.
        // Create two properties with objectIds in descending order.
        PropriedadeRustica prop1 = new PropriedadeRustica("B", "pB", "numB", "mun", "freg", 100.0, "ilha", "valid");
        PropriedadeRustica prop2 = new PropriedadeRustica("A", "pA", "numA", "mun", "freg", 100.0, "ilha", "valid");
        List<PropriedadeRustica> list = new ArrayList<>();
        list.add(prop1);
        list.add(prop2);
        connector.criarRelacoesAdjacenciaGrafo(list);
        try (Session session = testDriver.session()) {
            long count = session.readTransaction(tx -> tx.run("MATCH ()-[:ADJACENTE_A]->() RETURN count(*) AS count")
                    .single().get("count").asLong());
            assertEquals(0, count, "Relationship should not be created when objectId order condition is not met"); // Error: Incorrect relationship created.
        }
    }

    @Test
    public void criarRelacoesAdjacenciaGrafo7() {
        // Path 7: Valid adjacent relation inserted.
        // Create two properties with valid geometries and correct ordering.
        PropriedadeRustica prop1 = new PropriedadeRustica("A", "pA", "numA", "mun", "freg", 100.0, "ilha", "valid");
        PropriedadeRustica prop2 = new PropriedadeRustica("B", "pB", "numB", "mun", "freg", 120.0, "ilha", "valid");
        List<PropriedadeRustica> list = new ArrayList<>();
        list.add(prop1);
        list.add(prop2);
        connector.criarRelacoesAdjacenciaGrafo(list);
        try (Session session = testDriver.session()) {
            long count = session.readTransaction(tx -> tx.run("MATCH (:Propriedade {objectId:'A'})-[:ADJACENTE_A]->(:Propriedade {objectId:'B'}) RETURN count(*) AS count")
                    .single().get("count").asLong());
            assertEquals(1, count, "Expected one adjacent relationship to be created between valid properties"); // Error: Relationship not created as expected.
        }
    }

    // ------------------ obterRelacoesExistentes (Cyclomatic Complexity = 2) ------------------
    @Test
    public void obterRelacoesExistentes1() throws Exception {
        // Path 1: When no relationships exist.
        java.lang.reflect.Method method = Neo4jConnector.class.getDeclaredMethod("obterRelacoesExistentes");
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        Set<String> result = (Set<String>) method.invoke(connector);
        assertTrue(result.isEmpty(), "Expected empty set when no relationships exist in the graph"); // Error: Relationship set not empty.
    }

    @Test
    public void obterRelacoesExistentes2() throws Exception {
        // Path 2: When a relationship exists.
        // First, insert two properties and a relationship manually.
        try (Session session = testDriver.session()) {
            session.writeTransaction(tx -> {
                tx.run("CREATE (a:Propriedade {objectId:'A'}), (b:Propriedade {objectId:'B'})");
                tx.run("MATCH (a:Propriedade {objectId:'A'}), (b:Propriedade {objectId:'B'}) CREATE (a)-[:ADJACENTE_A]->(b)");
                return null;
            });
        }
        java.lang.reflect.Method method = Neo4jConnector.class.getDeclaredMethod("obterRelacoesExistentes");
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        Set<String> result = (Set<String>) method.invoke(connector);
        assertTrue(result.contains("A-B"), "Expected the relationship 'A-B' to exist in the set"); // Error: Relationship not found.
    }

    // ------------------ inserirRelacoes (Cyclomatic Complexity = 1) ------------------
    @Test
    public void inserirRelacoes() throws Exception {
        // Prepare: insert two properties.
        try (Session session = testDriver.session()) {
            session.writeTransaction(tx -> {
                tx.run("CREATE (:Propriedade {objectId:'X'})");
                tx.run("CREATE (:Propriedade {objectId:'Y'})");
                return null;
            });
        }
        // Prepare a list with one relation.
        List<String[]> relacoes = new ArrayList<>();
        relacoes.add(new String[]{"X", "Y"});
        java.lang.reflect.Method method = Neo4jConnector.class.getDeclaredMethod("inserirRelacoes", List.class);
        method.setAccessible(true);
        method.invoke(connector, relacoes);
        // Verify that the relationship exists.
        try (Session session = testDriver.session()) {
            long count = session.readTransaction(tx -> tx.run("MATCH (:Propriedade {objectId:'X'})-[:ADJACENTE_A]->(:Propriedade {objectId:'Y'}) RETURN count(*) AS count")
                    .single().get("count").asLong());
            assertEquals(1, count, "Expected one relationship to be created between properties X and Y"); // Error: Relationship insertion failed.
        }
    }
}
