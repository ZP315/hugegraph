package com.baidu.hugegraph2.example;

import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.hugegraph2.HugeFactory;
import com.baidu.hugegraph2.HugeGraph;
import com.baidu.hugegraph2.backend.BackendException;
import com.baidu.hugegraph2.backend.tx.GraphTransaction;
import com.baidu.hugegraph2.schema.SchemaManager;
import com.baidu.hugegraph2.structure.GraphManager;

/**
 * Created by jishilei on 17/3/16.
 */
public class ExampleGraphFactory {

    private static final Logger logger = LoggerFactory.getLogger(ExampleGraphFactory.class);

    public static void main(String[] args) {

        logger.info("ExampleGraphFactory start!");

        String confFile = ExampleGraphFactory.class.getClassLoader().getResource("hugegraph.properties").getPath();
        HugeGraph graph = HugeFactory.open(confFile);

        ExampleGraphFactory.showFeatures(graph);
        ExampleGraphFactory.load(graph);
    }

    public static void showFeatures(final HugeGraph graph) {
        logger.info("supportsPersistence : " + graph.features().graph().supportsPersistence());
    }

    public static void load(final HugeGraph graph) {

        /************************* schemaManager operating *************************/
        SchemaManager schemaManager = graph.openSchemaManager();
        logger.info("===============  propertyKey  ================");
        schemaManager.propertyKey("name").asText().create();
        schemaManager.propertyKey("gender").asText().create();
        schemaManager.propertyKey("instructions").asText().create();
        schemaManager.propertyKey("category").asText().create();
        schemaManager.propertyKey("year").asInt().create();
        schemaManager.propertyKey("timestamp").asTimestamp().create();
        schemaManager.propertyKey("ISBN").asText().create();
        schemaManager.propertyKey("calories").asInt().create();
        schemaManager.propertyKey("amount").asText().create();
        schemaManager.propertyKey("stars").asInt().create();
        schemaManager.propertyKey("comment").asText().single().create();
        schemaManager.propertyKey("nickname").asText().multiple().create();
        schemaManager.propertyKey("lived").asText().create();
        schemaManager.propertyKey("country").asText().multiple().properties("livedIn").create();
        schemaManager.propertyKey("city_id").asInt().create();
        schemaManager.propertyKey("sensor_id").asUuid().create();

        logger.info("===============  vertexLabel  ================");

        schemaManager.vertexLabel("author").properties("name").create();
        schemaManager.vertexLabel("recipe").properties("name", "instructions").create();
        schemaManager.vertexLabel("ingredient").create();
        schemaManager.vertexLabel("book").properties("name").primaryKeys("name").create();
        schemaManager.vertexLabel("meal").create();
        schemaManager.vertexLabel("reviewer").create();
        // vertex label must have the properties that specified in primary key
        schemaManager.vertexLabel("FridgeSensor").properties("city_id").primaryKeys("city_id").create();

        logger.info("===============  vertexLabel & index  ================");
        // TODO: implement index feature.
        // schemaManager.vertexLabel("author").index("byName").secondary().by("name").add();
        // schemaManager.vertexLabel("recipe").index("byRecipe").materialized().by("name").add();
        // schemaManager.vertexLabel("meal").index("byMeal").materialized().by("name").add();
        // schemaManager.vertexLabel("ingredient").index("byIngredient").materialized().by("name").add();
        // schemaManager.vertexLabel("reviewer").index("byReviewer").materialized().by("name").add();

        logger.info("===============  edgeLabel  ================");

        schemaManager.edgeLabel("authored").linkOne2One().properties("contribution").sortKeys("contribution").create();
        schemaManager.edgeLabel("created").single().linkMany2Many().create();
        schemaManager.edgeLabel("includes").single().linkOne2Many().create();
        schemaManager.edgeLabel("includedIn").linkMany2One().create();
        schemaManager.edgeLabel("rated").multiple().linkMany2Many().link("reviewer", "recipe").create();


        logger.info("===============  schemaManager desc  ================");
        schemaManager.desc();

        /************************* data operating *************************/

        GraphManager graphManager = graph.openGraphManager();

        // Directly into the back-end
        graphManager.addVertex(T.label, "book", "name", "java-3");
        graphManager.addVertex(T.label, "person", "name", "zhangsan");

        // Must commit manually
        GraphTransaction tx = graph.openGraphTransaction();

        logger.info("===============  addVertex  ================");
        Vertex person = tx.addVertex(T.label, "person",
                "name", "James Gosling", "age", "60");

        Vertex book1 = tx.addVertex(T.label, "book", "name", "java-1");
        Vertex book2 = tx.addVertex(T.label, "book", "name", "java-2");

        person.addEdge("authored", book1, "contribution", "1990-1-1");
        person.addEdge("authored", book2, "contribution", "2017-4-28");

        // commit data changes
        try {
            tx.commit();
        } catch (BackendException e) {
            e.printStackTrace();
            try {
                tx.rollback();
            } catch (BackendException e2) {
                // TODO Auto-generated catch block
                e2.printStackTrace();
            }
        }

        // use the default Transaction to commit
        graph.addVertex(T.label, "book", "name", "java-3");
        graph.tx().commit();
    }
}