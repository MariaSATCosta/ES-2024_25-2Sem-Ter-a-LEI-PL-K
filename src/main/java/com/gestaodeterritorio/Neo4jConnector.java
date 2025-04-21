package com.gestaodeterritorio;

import io.github.cdimascio.dotenv.Dotenv;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.index.strtree.STRtree;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.summary.ResultSummary;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Classe responsável pela integração com a base de dados Neo4j.
 * <p>
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
     * Insere propriedades no grafo de propriedades se ainda não existirem na base de dados.
     * Insere proprietários no grafo de proprietários se ainda não existirem na base de dados
     *
     * @param propriedades lista de propriedades a inserir
     */
    public void criarPropriedadesGrafo(List<PropriedadeRustica> propriedades) {
        if (propriedades.isEmpty()) return;

        Set<String> propriedadesExistentes = obterPropriedadesExistentes();
        Set<String> proprietariosExistentes = obterProprietariosExistentes();
        List<PropriedadeRustica> novasPropriedades = new ArrayList<>();
        Set<String> novosProprietarios = new HashSet<>();

        for (PropriedadeRustica p : propriedades) {
            if (!propriedadesExistentes.contains(p.getObjectId())) {
                novasPropriedades.add(p);
                if (!proprietariosExistentes.contains(p.getOwner())) {
                    novosProprietarios.add(p.getOwner());
                }
            }
        }

        if (!novasPropriedades.isEmpty()) {
            inserirPropriedades(novasPropriedades);
            System.out.println("Inseridas " + novasPropriedades.size() + " novas propriedades");
            System.out.println("Inseridos " + novosProprietarios.size() + " novos proprietários");
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
     * Obtém o conjunto de identificadores dos proprietários já existentes no grafo de proprietários.
     *
     * @return conjunto de owners existentes
     */
    private Set<String> obterProprietariosExistentes() {
        Set<String> proprietarios = new HashSet<>();
        try (Session session = driver.session()) {
            session.readTransaction(tx -> {
                Result result = tx.run("MATCH (p:Proprietario) RETURN p.owner AS id");
                while (result.hasNext()) {
                    proprietarios.add(result.next().get("id").asString());
                }
                return null;
            });
        }
        return proprietarios;
    }

    /**
     * Insere uma lista de propriedades como nós no grafo de propriedades.
     * Insere os respetivos proprietários como nós no grafo de proprietários.
     *
     * @param propriedades lista de propriedades a inserir
     */
    private void inserirPropriedades(List<PropriedadeRustica> propriedades) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                String query = "UNWIND $propriedades AS prop " +
                        "CREATE (p:Propriedade {objectId: prop.objectId, parId: prop.parId, parNum: prop.parNum, " +
                        "municipio: prop.municipio, freguesia: prop.freguesia, shapeArea: prop.shapeArea, ilha: prop.ilha, " +
                        "geometry: prop.geometry, owner: prop.owner})" + "MERGE (owner:Proprietario {owner: prop.owner})";
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
                            "geometry", p.getGeometry(),
                            "owner", p.getOwner()
                    ));
                }
                tx.run(query, Values.parameters("propriedades", parametros));
                return null;
            });
        }
    }

    /**
     * Cria relações de adjacência no grafo entre propriedades cuja geometria se intersecta ou toca.
     * Cria relações de vizinhança no grafo entre proprietários.
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
        List<String[]> novasRelacoesProprietarios = new ArrayList<>();

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
                            novasRelacoesProprietarios.add(new String[]{p1.getOwner(), p2.getOwner()});
                        }
                    }
                }
            }
        }
        if (!novasRelacoes.isEmpty()) {
            inserirRelacoes(novasRelacoes);
            System.out.println("Inseridas " + novasRelacoes.size() * 2 + " novas relações adjacentes");
            int relacoesProprietariosCriadas = inserirRelacoesProprietarios(novasRelacoesProprietarios);
            System.out.println("Inseridas " + relacoesProprietariosCriadas + " novas relações de vizinhança de proprietários");
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
                        "MERGE (a)-[:ADJACENTE_A]->(b)" + "MERGE (b)-[:ADJACENTE_A]->(a)";
                tx.run(query, Values.parameters("relacoes", novasRelacoes));
                return null;
            });
        }
    }

    /**
     * Insere novas relações de vizinhança no grafo de proprietários.
     *
     * @param novasRelacoesProprietarios lista de pares [owner1, owner2] representando as relações a criar
     * @return número de relações inseridas no grafo
     */
    private int inserirRelacoesProprietarios(List<String[]> novasRelacoesProprietarios) {
        try (Session session = driver.session()) {
            int relacoesCriadas = session.writeTransaction(tx -> {
                String query = "UNWIND $relacoes AS relacao " +
                        "MATCH (a:Proprietario {owner: relacao[0]}), (b:Proprietario {owner: relacao[1]}) " +
                        "MERGE (a)-[:VIZINHO_DE]->(b)" + "MERGE (b)-[:VIZINHO_DE]->(a)";
                Result result = tx.run(query, Values.parameters("relacoes", novasRelacoesProprietarios));
                ResultSummary summary = result.consume();
                return summary.counters().relationshipsCreated();
            });
            return relacoesCriadas;
        }

    }
}
