package com.gestaodeterritorio;

import io.github.cdimascio.dotenv.Dotenv;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.index.strtree.STRtree;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.summary.ResultSummary;
import org.neo4j.driver.types.Node;

import java.util.*;

import static org.neo4j.driver.Values.parameters;

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
            return session.writeTransaction(tx -> {
                String query = "UNWIND $relacoes AS relacao " +
                        "MATCH (a:Proprietario {owner: relacao[0]}), (b:Proprietario {owner: relacao[1]}) " +
                        "MERGE (a)-[:VIZINHO_DE]->(b)" + "MERGE (b)-[:VIZINHO_DE]->(a)";
                Result result = tx.run(query, Values.parameters("relacoes", novasRelacoesProprietarios));
                ResultSummary summary = result.consume();
                return summary.counters().relationshipsCreated();
            });
        }

    }

    /**
     * Obtém a lista de áreas (shapeArea) de todas as propriedades que pertencem
     * a uma região geográfica/administrativa específica.
     *
     * @param regionField o nome do campo de localização administrativa a filtrar (ex: "freguesia", "municipio", "ilha").
     * @param regionValue o valor do campo da região que será usado como filtro.
     * @return uma lista de valores representando as áreas das propriedades que pertencem à região indicada.
     */
    public List<Double> devolverAreasPorRegiao(String regionField, String regionValue) {
        String cypher =
                "MATCH (p:Propriedade) " +
                        "WHERE p." + regionField + " = $valor " +
                        "RETURN toFloat(p.shapeArea) AS area";
        try (Session session = driver.session()) {
            return session.readTransaction(tx ->
                    tx.run(cypher, Collections.singletonMap("valor", regionValue))
                            .list(r -> r.get("area").asDouble())
            );
        }
    }


    /**
     * Calcula as áreas agrupadas de propriedades que pertencem a uma região específica,
     * considerando que propriedades adjacentes com o mesmo proprietário devem ser tratadas
     * como uma única propriedade.
     *
     * O agrupamento é feito com base em:
     * - Adjacência entre propriedades (`:ADJACENTE_A`)
     * - Mesmo valor no campo de localização (`freguesia`, `municipio` ou `ilha`)
     * - Mesmo proprietário (`owner`)
     *
     * Cada grupo de propriedades adjacentes do mesmo proprietário é tratado como uma única
     * propriedade fundida. Para cada grupo, soma-se a área (`shapeArea`) das propriedades que o compõem.
     *
     * @param campoRegiao o nome do campo geográfico a filtrar (ex: "freguesia", "municipio", "ilha")
     * @param valorRegiao o valor a filtrar nesse campo (ex: "Arco da Calheta")
     * @return uma lista de áreas agrupadas, onde cada valor representa a soma das áreas de um grupo
     */
    public List<Double> devolverAreasAgrupadasPorRegiao(String campoRegiao, String valorRegiao) {
        String cypher = String.format("""
    MATCH (p:Propriedade)
    WHERE p.%s = $valorRegiao
    CALL {
        WITH p
        MATCH grupo = (p)-[:ADJACENTE_A*]-(p2:Propriedade)
        WHERE p.owner = p2.owner AND p.%s = p2.%s
        RETURN collect(DISTINCT p2.objectId) AS grupoIds
    }
    WITH DISTINCT grupoIds
    UNWIND grupoIds AS id
    MATCH (prop:Propriedade {objectId: id})
    WITH grupoIds, sum(toFloat(prop.shapeArea)) AS areaGrupo
    RETURN areaGrupo
""", campoRegiao, campoRegiao, campoRegiao);




        try (Session session = driver.session()) {
            return session.readTransaction(tx ->
                    tx.run(cypher, Collections.singletonMap("valorRegiao", valorRegiao))
                            .list(r -> r.get("areaGrupo").asDouble())
            );
        }
    }

    /**
     * Obtém todas as propriedades rústicas associadas a um determinado proprietário.
     *
     * @param owner Nome do proprietário.
     * @return Lista de propriedades do proprietário especificado.
     */
    public List<PropriedadeRustica> obterPropriedadesPorOwner(String owner) {
        List<PropriedadeRustica> propriedades = new ArrayList<>();

        String query = "MATCH (p:Propriedade) WHERE p.owner = $owner RETURN p";

        try (Session session = driver.session()) {
            Result result = session.run(query, parameters("owner", owner));  // Passa o parâmetro 'owner'
            while (result.hasNext()) {
                Record record = result.next();
                Node node = record.get("p").asNode();

                // Ignora propriedades com valores "NA"
                if (node.get("objectId").isNull() || node.get("objectId").asString().equalsIgnoreCase("NA")) continue;
                if (node.get("shapeArea").isNull() || node.get("shapeArea").asString().equalsIgnoreCase("NA")) continue;
                if (node.get("owner").isNull() || node.get("owner").asString().equalsIgnoreCase("NA")) continue;

                PropriedadeRustica propriedade = new PropriedadeRustica(node);

                propriedades.add(propriedade);
            }
        } catch (Exception e) {
            System.err.println("Erro ao obter propriedades por owner: " + e.getMessage());
        }

        return propriedades;
    }

    /**
     * Obtém todas as propriedades que possuem pelo menos uma ligação de adjacência com outra propriedade.
     *
     * @return Conjunto de propriedades com adjacentes.
     */
    public Set<PropriedadeRustica> obterPropriedadesComAdjacentes() {
        String query = "MATCH (p:Propriedade)-[:ADJACENTE_A]-(:Propriedade) RETURN DISTINCT p";

        Set<PropriedadeRustica> propriedades = new HashSet<>();

        try (Session session = driver.session()) {
            Result result = session.run(query);
            while (result.hasNext()) {
                Record record = result.next();
                Node node = record.get("p").asNode();

                // Ignora se tiver valores "NA"
                if (node.get("freguesia").isNull() || node.get("freguesia").asString().equalsIgnoreCase("NA")) continue;
                if (node.get("municipio").isNull() || node.get("municipio").asString().equalsIgnoreCase("NA")) continue;
                if (node.get("ilha").isNull() || node.get("ilha").asString().equalsIgnoreCase("NA")) continue;

                PropriedadeRustica propriedade = new PropriedadeRustica(node);

                propriedades.add(propriedade);
            }
        } catch (Exception e) {
            System.err.println("Erro ao obter propriedades com adjacentes: " + e.getMessage());
        }

        return propriedades;
    }

    /**
     * Obtém a lista de propriedades adjacentes a uma propriedade específica, com base no seu objectId.
     *
     * @param idPropriedade Identificador da propriedade.
     * @return Lista de propriedades adjacentes.
     */
    public List<PropriedadeRustica> obterPropriedadesAdjacentes(String idPropriedade) {
        String query = "MATCH (p:Propriedade {objectId: $id})-[:ADJACENTE_A]-(adj:Propriedade) " +
                "RETURN adj";

        List<PropriedadeRustica> adjacentes = new ArrayList<>();

        try (Session session = driver.session()) {
            Result result = session.run(query, parameters("id", idPropriedade));
            while (result.hasNext()) {
                Record record = result.next();
                Node node = record.get("adj").asNode();

                // Ignorar nós com campos essenciais "NA"
                if (node.get("freguesia").isNull() || node.get("freguesia").asString().equalsIgnoreCase("NA")) continue;
                if (node.get("municipio").isNull() || node.get("municipio").asString().equalsIgnoreCase("NA")) continue;
                if (node.get("ilha").isNull() || node.get("ilha").asString().equalsIgnoreCase("NA")) continue;

                PropriedadeRustica propriedade = new PropriedadeRustica(node);

                adjacentes.add(propriedade);
            }
        } catch (Exception e) {
            System.err.println("Erro ao obter propriedades adjacentes: " + e.getMessage());
        }

        return adjacentes;
    }

    /**
     * Troca os proprietários entre duas propriedades.
     *
     * @param id1 Identificador da primeira propriedade.
     * @param novoDono1 Novo proprietario da primeira propriedade.
     * @param id2 Identificador da segunda propriedade.
     * @param novoDono2 Novo proprietario da segunda propriedade.
     */
    public void trocarProprietarios(String id1, String novoDono1, String id2, String novoDono2) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                tx.run("""
                MATCH (p1:Propriedade {objectId: $id1}), (p2:Propriedade {objectId: $id2})
                SET p1.owner = $novoDono1, p2.owner = $novoDono2
            """,
                        parameters("id1", id1, "novoDono1", novoDono1, "id2", id2, "novoDono2", novoDono2));
                return null;
            });
        }
    }

    /**
     * Reverte os proprietários das propriedades para os valores originais.
     *
     * @param id1 Identificador da primeira propriedade.
     * @param donoOriginal1 proprietario original da primeira propriedade.
     * @param id2 Identificador da segunda propriedade.
     * @param donoOriginal2 proprietario original da segunda propriedade.
     */
    public void reverterProprietarios(String id1, String donoOriginal1, String id2, String donoOriginal2) {
        trocarProprietarios(id1, donoOriginal1, id2, donoOriginal2);
    }
}

