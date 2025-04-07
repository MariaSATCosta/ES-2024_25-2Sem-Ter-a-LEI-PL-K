package com.gestaodeterritorio;

import org.junit.jupiter.api.*;
import org.neo4j.driver.*;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;
import org.locationtech.jts.geom.Geometry;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class Neo4jConnectorTest {

    private Neo4j embeddedDatabase;
    private Driver testDriver;
    private Neo4jConnector connector;

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
        // Instantiate the connector and override its driver with our in-memory test driver.
        connector = new Neo4jConnector();
        Field driverField = Neo4jConnector.class.getDeclaredField("driver");
        driverField.setAccessible(true);
        driverField.set(connector, testDriver);

        // Clean the database before each test.
        try (Session session = testDriver.session()) {
            session.writeTransaction(tx -> {
                tx.run("MATCH (n) DETACH DELETE n");
                return null;
            });
        }
    }

    /**
     * Helper method to populate a PropriedadeRustica instance via reflection.
     * Uses the production PropriedadeRustica class.
     */
    private PropriedadeRustica createPropriedade(String objectId, String parId, String parNum,
                                                 String shapeLength, String shapeArea, String geometry,
                                                 String owner, String freguesia, String municipio, String ilha)
            throws Exception {
        PropriedadeRustica prop = new PropriedadeRustica();

        Field field = PropriedadeRustica.class.getDeclaredField("objectId");
        field.setAccessible(true);
        field.set(prop, objectId);

        field = PropriedadeRustica.class.getDeclaredField("parId");
        field.setAccessible(true);
        field.set(prop, parId);

        field = PropriedadeRustica.class.getDeclaredField("parNum");
        field.setAccessible(true);
        field.set(prop, parNum);

        field = PropriedadeRustica.class.getDeclaredField("shapeLength");
        field.setAccessible(true);
        field.set(prop, shapeLength);

        field = PropriedadeRustica.class.getDeclaredField("shapeArea");
        field.setAccessible(true);
        field.set(prop, shapeArea);

        field = PropriedadeRustica.class.getDeclaredField("geometry");
        field.setAccessible(true);
        field.set(prop, geometry);

        field = PropriedadeRustica.class.getDeclaredField("owner");
        field.setAccessible(true);
        field.set(prop, owner);

        field = PropriedadeRustica.class.getDeclaredField("freguesia");
        field.setAccessible(true);
        field.set(prop, freguesia);

        field = PropriedadeRustica.class.getDeclaredField("municipio");
        field.setAccessible(true);
        field.set(prop, municipio);

        field = PropriedadeRustica.class.getDeclaredField("ilha");
        field.setAccessible(true);
        field.set(prop, ilha);

        return prop;
    }

    // ------------------ Constructor Tests (Cyclomatic Complexity = 1) ------------------
    @Test
    public void constructor() {
        // Verify that the connector is instantiated.
        assertNotNull(connector, "Error: Connector instance should not be null after construction"); // Error if connector is null.
    }

    // ------------------ close() Tests (Cyclomatic Complexity = 1) ------------------
    @Test
    public void close() {
        connector.close();
        // After closing, attempting to open a session should throw an exception.
        Exception exception = assertThrows(Exception.class, () -> {
            try (Session session = testDriver.session()) {
                session.run("RETURN 1");
            }
        }, "Error: Expected exception when accessing session after driver is closed"); // Error if no exception is thrown.
        assertNotNull(exception, "Error: Exception must not be null after closing the connector");
    }

    // ------------------ criarPropriedadesGrafo Tests (Cyclomatic Complexity = 3) ------------------
    @Test
    public void criarPropriedadesGrafo1() {
        // Path 1: Empty list provided, no properties should be inserted.
        connector.criarPropriedadesGrafo(new ArrayList<>());
        try (Session session = testDriver.session()) {
            long count = session.readTransaction(tx -> tx.run("MATCH (n:Propriedade) RETURN count(n) AS count")
                    .single().get("count").asLong());
            assertEquals(0, count, "Error: No properties should be inserted when list is empty"); // Error if count != 0.
        }
    }

    @Test
    public void criarPropriedadesGrafo2() throws Exception {
        // Path 2: Property already exists should not be inserted again.
        // Manually insert one property.
        try (Session session = testDriver.session()) {
            session.writeTransaction(tx -> {
                tx.run("CREATE (:Propriedade {objectId:'1', parId:'p1', parNum:'num1', municipio:'mun', " +
                        "freguesia:'freg', shapeArea:'100', ilha:'ilha', geometry:'POINT(0 0)'})");
                return null;
            });
        }
        List<PropriedadeRustica> props = new ArrayList<>();
        props.add(createPropriedade("1", "p1", "num1", "10", "100", "POINT(0 0)", "owner", "freg", "mun", "ilha"));
        connector.criarPropriedadesGrafo(props);
        try (Session session = testDriver.session()) {
            long count = session.readTransaction(tx -> tx.run("MATCH (n:Propriedade) RETURN count(n) AS count")
                    .single().get("count").asLong());
            assertEquals(1, count, "Error: Duplicate property should not be inserted if it already exists"); // Error if count != 1.
        }
    }

    @Test
    public void criarPropriedadesGrafo3() throws Exception {
        // Path 3: Insert two new properties.
        List<PropriedadeRustica> props = new ArrayList<>();
        props.add(createPropriedade("1", "p1", "num1", "10", "100", "POINT(0 0)", "owner1", "freg1", "mun1", "ilha"));
        props.add(createPropriedade("2", "p2", "num2", "12", "150", "POINT(1 1)", "owner2", "freg2", "mun2", "ilha"));
        connector.criarPropriedadesGrafo(props);
        try (Session session = testDriver.session()) {
            long count = session.readTransaction(tx -> tx.run("MATCH (n:Propriedade) RETURN count(n) AS count")
                    .single().get("count").asLong());
            assertEquals(2, count, "Error: Two new properties should be inserted"); // Error if count != 2.
        }
    }

    // ------------------ criarRelacoesAdjacenciaGrafo Tests (Cyclomatic Complexity = 6) ------------------
    @Test
    public void criarRelacoesAdjacenciaGrafo1() throws Exception {
        // Path 1: A property with invalid geometry in index creation should yield no relationships.
        List<PropriedadeRustica> props = new ArrayList<>();
        props.add(createPropriedade("A", "pA", "numA", "10", "100", "INVALID_WKT", "ownerA", "fregA", "munA", "ilha"));
        connector.criarRelacoesAdjacenciaGrafo(props);
        try (Session session = testDriver.session()) {
            long count = session.readTransaction(tx ->
                    tx.run("MATCH ()-[:ADJACENTE_A]->() RETURN count(*) AS count")
                            .single().get("count").asLong()
            );
            assertEquals(0, count, "Error: No relationships should be created when geometry is invalid (index creation)"); // Error if count != 0.
        }
    }

    @Test
    public void criarRelacoesAdjacenciaGrafo2() throws Exception {
        // Path 2: First property's geometry is invalid so it is skipped.
        List<PropriedadeRustica> props = new ArrayList<>();
        props.add(createPropriedade("A", "pA", "numA", "10", "100", "INVALID_WKT", "ownerA", "fregA", "munA", "ilha"));
        props.add(createPropriedade("B", "pB", "numB", "12", "150",
                "POLYGON((2 2, 4 2, 4 4, 2 4, 2 2))", "ownerB", "fregB", "munB", "ilha"));
        connector.criarRelacoesAdjacenciaGrafo(props);
        try (Session session = testDriver.session()) {
            long count = session.readTransaction(tx ->
                    tx.run("MATCH ()-[:ADJACENTE_A]->() RETURN count(*) AS count")
                            .single().get("count").asLong()
            );
            assertEquals(0, count, "Error: No relationships should be created when first property has invalid geometry"); // Error if count != 0.
        }
    }

    @Test
    public void criarRelacoesAdjacenciaGrafo3() throws Exception {
        // Path 3: Self-comparison: property compared with itself should yield no relationship.
        List<PropriedadeRustica> props = new ArrayList<>();
        PropriedadeRustica prop = createPropriedade("A", "pA", "numA", "10", "100",
                "POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))", "ownerA", "fregA", "munA", "ilha");
        props.add(prop);
        props.add(prop); // same instance added twice.
        connector.criarRelacoesAdjacenciaGrafo(props);
        try (Session session = testDriver.session()) {
            long count = session.readTransaction(tx ->
                    tx.run("MATCH ()-[:ADJACENTE_A]->() RETURN count(*) AS count")
                            .single().get("count").asLong()
            );
            assertEquals(0, count, "Error: No self-adjacency relationship should be created when same property is compared"); // Error if count != 0.
        }
    }

    @Test
    public void criarRelacoesAdjacenciaGrafo4() throws Exception {
        // Path 4: Candidate property has invalid geometry.
        List<PropriedadeRustica> props = new ArrayList<>();
        props.add(createPropriedade("A", "pA", "numA", "10", "100",
                "POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))", "ownerA", "fregA", "munA", "ilha"));
        props.add(createPropriedade("B", "pB", "numB", "12", "150", "INVALID_WKT",
                "ownerB", "fregB", "munB", "ilha"));
        connector.criarRelacoesAdjacenciaGrafo(props);
        try (Session session = testDriver.session()) {
            long count = session.readTransaction(tx ->
                    tx.run("MATCH ()-[:ADJACENTE_A]->() RETURN count(*) AS count")
                            .single().get("count").asLong()
            );
            assertEquals(0, count, "Error: No relationship should be created when candidate property has invalid geometry"); // Error if count != 0.
        }
    }

    @Test
    public void criarRelacoesAdjacenciaGrafo5() throws Exception {
        // Path 5: Only one property exists, so no candidate exists to form a relationship.
        List<PropriedadeRustica> props = new ArrayList<>();
        props.add(createPropriedade("A", "pA", "numA", "10", "100",
                "POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))", "ownerA", "fregA", "munA", "ilha"));
        connector.criarRelacoesAdjacenciaGrafo(props);
        try (Session session = testDriver.session()) {
            long count = session.readTransaction(tx ->
                    tx.run("MATCH ()-[:ADJACENTE_A]->() RETURN count(*) AS count")
                            .single().get("count").asLong()
            );
            assertEquals(0, count, "Error: No relationship should be created when there is only one property"); // Error if count != 0.
        }
    }

    @Test
    public void criarRelacoesAdjacenciaGrafo6() throws Exception {
        // Path 6: ObjectId ordering condition: if p1.getObjectId() is not less than p2.getObjectId(), no relationship is formed.
        List<PropriedadeRustica> props = new ArrayList<>();
        props.add(createPropriedade("B", "pB", "numB", "10", "100",
                "POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))", "ownerB", "fregB", "munB", "ilha"));
        props.add(createPropriedade("A", "pA", "numA", "12", "150",
                "POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))", "ownerA", "fregA", "munA", "ilha"));
        connector.criarRelacoesAdjacenciaGrafo(props);
        try (Session session = testDriver.session()) {
            long count = session.readTransaction(tx ->
                    tx.run("MATCH ()-[:ADJACENTE_A]->() RETURN count(*) AS count")
                            .single().get("count").asLong()
            );
            assertEquals(0, count, "Error: Relationship should not be created when objectId order condition is not met"); // Error if count != 0.
        }
    }

    // ------------------ obterRelacoesExistentes Tests (Cyclomatic Complexity = 2) ------------------
    @Test
    public void obterRelacoesExistentes1() throws Exception {
        // Path 1: No relationships exist; expect an empty set.
        Method method = Neo4jConnector.class.getDeclaredMethod("obterRelacoesExistentes");
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        Set<String> result = (Set<String>) method.invoke(connector);
        assertTrue(result.isEmpty(), "Error: Expected empty set when no relationships exist"); // Error if set is not empty.
    }

    @Test
    public void obterRelacoesExistentes2() throws Exception {
        // Path 2: Insert a relationship manually and verify that it is returned.
        try (Session session = testDriver.session()) {
            session.writeTransaction(tx -> {
                tx.run("CREATE (a:Propriedade {objectId:'A'})");
                tx.run("CREATE (b:Propriedade {objectId:'B'})");
                tx.run("MATCH (a:Propriedade {objectId:'A'}), (b:Propriedade {objectId:'B'}) CREATE (a)-[:ADJACENTE_A]->(b)");
                return null;
            });
        }
        Method method = Neo4jConnector.class.getDeclaredMethod("obterRelacoesExistentes");
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        Set<String> result = (Set<String>) method.invoke(connector);
        assertTrue(result.contains("A-B"), "Error: Expected relationship 'A-B' to be present in the set"); // Error if not contained.
    }
}
