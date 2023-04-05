package bulkwriter;

import config.TestConfig;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.niogatori.mongohelper.MongoDBBulkWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.niogatori.mongohelper.LogicalOperator.AND;
import static org.niogatori.mongohelper.LogicalOperator.OR;
import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;

@Import(TestConfig.class)
@DataMongoTest(includeFilters = @ComponentScan.Filter(type = ASSIGNABLE_TYPE, classes = MongoDBBulkWriter.class))
class MongoDBBulkWriterTest {

    @Autowired
    private ReactiveMongoTemplate template;

    private MongoDBBulkWriter<MyObject> bulkWriter;

    @BeforeEach
    void setup() {
        bulkWriter = new MongoDBBulkWriter<MyObject>(template);
        template.dropCollection(MyObject.class).block();
    }

    @Getter
    @AllArgsConstructor
    @EqualsAndHashCode
    @ToString
    static class MyId {
        private String reference;
        private int bu;

        Document mapToBSON() {
            Document documentId = new Document("reference", this.reference).append("bu", this.bu);
            return new Document("_id", documentId);
        }
    }

    @Getter
    @AllArgsConstructor
    @EqualsAndHashCode
    @ToString
    @org.springframework.data.mongodb.core.mapping.Document("MyObject")
    static class MyObject {
        @Id
        private MyId id;
        private String name;
        private String desc;
        private Integer nbItems;
    }

    @Getter
    @AllArgsConstructor
    @EqualsAndHashCode
    @ToString
    static class MyPartialObject {
        private String name;
        private Integer nbItems;

        static Document mapMyPartialObject(MyPartialObject myPartialObject) {
            return new Document("name", myPartialObject.getName()).append("nbItems", myPartialObject.getNbItems());
        }
    }

    @Getter
    @AllArgsConstructor
    @EqualsAndHashCode
    static class MyEmbeddingObject {
        @Id
        private MyId id;
        private String name;
        private String desc;
        private Integer nbItems;
        private MyPartialObject partialObject;
    }

    @Nested
    class ToDocumentTest {
        @Test
        void shouldConvertObjectToBsonDocument() {
            Object object = new MyId("ref", 1);

            Document objectToSave = bulkWriter.toDocument(object);

            assertThat(objectToSave)
                    .containsEntry("reference", "ref")
                    .containsEntry("bu", 1)
                    .doesNotContainKey("_class");
        }

        @Test
        void shouldConvertEmbeddedObjectToBsonDocument() {
            MyId id = new MyId("ref", 1);
            MyEmbeddingObject myObject =
                    new MyEmbeddingObject(id, "Cadre Photo", "Cadre Photo 29x21cm", 1, new MyPartialObject("Cadre", 1));

            Document objectToSave = bulkWriter.toDocument(myObject);

            assertThat(objectToSave)
                    .containsEntry("partialObject", new Document("name", "Cadre").append("nbItems", 1))
                    .containsEntry("name", "Cadre Photo")
                    .containsEntry("desc", "Cadre Photo 29x21cm")
                    .containsEntry("nbItems", 1)
                    .doesNotContainKey("_class");

            assertThat(objectToSave.get("partialObject", Document.class))
                    .containsEntry("name", "Cadre")
                    .containsEntry("nbItems", 1)
                    .doesNotContainKey("_class");
        }

        @Test
        void shouldConvertObjectToBsonDocumentWithoutNullField() {
            Object object = new MyPartialObject(null, 2);

            Document objectToSave = bulkWriter.toDocument(object);

            assertThat(objectToSave)
                    .doesNotContainKey("name")
                    .containsEntry("nbItems", 2)
                    .doesNotContainKey("_class");
        }
    }

    @Nested
    class ToIDDocumentTest {
        @Test
        void shouldConvertObjectIDToBsonDocument() {
            MyId id = new MyId("ref", 1);
            Document myIdBson = id.mapToBSON();

            Document objectToSave = bulkWriter.toIdDocument(id);

            assertThat(objectToSave)
                    .containsEntry("_id", myIdBson.get("_id"))
                    .doesNotContainKey("_class");
        }
    }

    @Nested
    class ToCriteriaTest {
        @Test
        void shouldConcatCriteriasWithLogicalOperator_CriteriaSizeGt1() {
            List<Document> queries = List.of(new Document("name", "Fifi"), new Document("nbItems", 2));

            assertThat(bulkWriter.toCriteria(queries, OR).toJson().replaceAll(" ", ""))
                    .isEqualTo(new Document("$or", List.of(new Document("name", "Fifi"), new Document("nbItems", 2)))
                            .toJson().replaceAll(" ", ""));
        }

        @Test
        void shouldConcatCriteriasWithoutLogicalOperator_CriteriaSizeGt1_nullPointerException() {
            List<Document> queries = List.of(new Document("name", "Fifi"), new Document("nbItems", 2));

            assertThrows(NullPointerException.class, () -> bulkWriter.toCriteria(queries, null));
        }

        @Test
        void shouldConcatCriteriasWithLogicalOperator_CriteriaSize1() {
            List<Document> queries = List.of(new Document("name", "Fifi"));

            assertThat(bulkWriter.toCriteria(queries, OR).toJson().replaceAll(" ", ""))
                    .isEqualTo(new Document("name", "Fifi").toJson().replaceAll(" ", ""));
        }

        @Test
        void shouldConcatCriteriasWithoutLogicalOperator_CriteriaSize1() {
            List<Document> queries = List.of(new Document("name", "Fifi"));

            assertThat(bulkWriter.toCriteria(queries, null).toJson().replaceAll(" ", ""))
                    .isEqualTo(new Document("name", "Fifi").toJson().replaceAll(" ", ""));
        }

        @Test
        void shouldConcatNothingWithLogicalOperator_CriteriaEmpty() {
            List<Document> queries = List.of();

            assertThat(bulkWriter.toCriteria(queries, AND).toJson()).isEqualTo(new Document().toJson());
        }

        @Test
        void shouldConcatNothingWithoutLogicalOperator_CriteriaEmpty() {
            List<Document> queries = List.of();

            assertThat(bulkWriter.toCriteria(queries, null).toJson()).isEqualTo(new Document().toJson());
        }
    }

    @Nested
    class UpsertManySetTest {

        @Test
        void shouldUpsertManyAndReturnNumberOfUpdates() {
            MyId myId1 = new MyId("myObject1", 1);
            MyId myId2 = new MyId("myObject2", 1);
            MyId myId3 = new MyId("myObject3", 1);

            MyObject myObject1 = new MyObject(myId1, "Riri", null, 0);
            MyObject myObject2 = new MyObject(myId2, "Fifi", null, 1);
            MyObject myObject3 = new MyObject(myId3, "Loulou", null, 2);

            template.insertAll(List.of(myObject1, myObject2, myObject3)).blockLast();
            Document setDesc = new Document("desc", "filled");
            StepVerifier
                    .create(bulkWriter.upsertMany(MyObject.class, new Document(), setDesc))
                    .expectNext(3)
                    .verifyComplete();

            StepVerifier.create(template.findAll(MyObject.class))
                    .expectNextMatches(myObject -> Objects.equals(myObject.getDesc(), "filled"))
                    .expectNextMatches(myObject -> Objects.equals(myObject.getDesc(), "filled"))
                    .expectNextMatches(myObject -> Objects.equals(myObject.getDesc(), "filled"))
                    .verifyComplete();
        }

        @Test
        void shouldUpsertManyWithQueryAndReturnNumberOfUpdates() {
            MyId myId1 = new MyId("myObject1", 1);
            MyId myId2 = new MyId("myObject2", 1);
            MyId myId3 = new MyId("myObject3", 1);

            MyObject myObject1 = new MyObject(myId1, "Riri", null, 0);
            MyObject myObject2 = new MyObject(myId2, "Fifi", null, 1);
            MyObject myObject3 = new MyObject(myId3, "Loulou", null, 2);

            template.insertAll(List.of(myObject1, myObject2, myObject3)).blockLast();
            Document setDesc = new Document("desc", "filled");
            StepVerifier
                    .create(bulkWriter.upsertMany(MyObject.class,
                            new Document("$or", List.of(new Document("name", "Fifi"), new Document("nbItems", 2))),
                            setDesc))
                    .expectNext(2)
                    .verifyComplete();

            StepVerifier.create(template.findAll(MyObject.class))
                    .expectNextMatches(myObject -> (myObject.getId().getReference().equals("myObject1")
                            && Objects.equals(myObject.getDesc(), null)
                            || Objects.equals(myObject.getDesc(), "filled")))
                    .expectNextMatches(myObject -> (myObject.getId().getReference().equals("myObject1")
                            && Objects.equals(myObject.getDesc(), null)
                            || Objects.equals(myObject.getDesc(), "filled")))
                    .expectNextMatches(myObject -> (myObject.getId().getReference().equals("myObject1")
                            && Objects.equals(myObject.getDesc(), null)
                            || Objects.equals(myObject.getDesc(), "filled")))
                    .verifyComplete();
        }

    }

    @Nested
    class UpsertManyUnsetTest {

        @Test
        void shouldUpsertManyWithUnsetListOfFieldsAndReturnNumberOfUpdates() {
            MyId myId1 = new MyId("myObject1", 1);
            MyId myId2 = new MyId("myObject2", 1);
            MyId myId3 = new MyId("myObject3", 1);

            MyObject myObject1 = new MyObject(myId1, "Riri", null, 0);
            MyObject myObject2 = new MyObject(myId2, "Fifi", null, 1);
            MyObject myObject3 = new MyObject(myId3, "Loulou", null, 2);

            template.insertAll(List.of(myObject1, myObject2, myObject3)).blockLast();
            StepVerifier
                    .create(bulkWriter.upsertMany(MyObject.class, new Document(), List.of("name")))
                    .expectNext(3)
                    .verifyComplete();

            StepVerifier.create(template.findAll(MyObject.class))
                    .expectNextMatches(myObject -> Objects.equals(myObject.getName(), null))
                    .expectNextMatches(myObject -> Objects.equals(myObject.getName(), null))
                    .expectNextMatches(myObject -> Objects.equals(myObject.getName(), null))
                    .verifyComplete();
        }

        @Test
        void shouldUpsertManyWithQueryWithUnsetFieldsAndReturnNumberOfUpdates() {
            MyId myId1 = new MyId("myObject1", 1);
            MyId myId2 = new MyId("myObject2", 1);
            MyId myId3 = new MyId("myObject3", 1);

            MyObject myObject1 = new MyObject(myId1, "Riri", null, 0);
            MyObject myObject2 = new MyObject(myId2, "Fifi", null, 1);
            MyObject myObject3 = new MyObject(myId3, "Loulou", null, 2);

            template.insertAll(List.of(myObject1, myObject2, myObject3)).blockLast();
            StepVerifier
                    .create(bulkWriter.upsertMany(MyObject.class,
                            new Document("$or", List.of(new Document("name", "Fifi"), new Document("nbItems", 2))),
                            List.of("name")))
                    .expectNext(2)
                    .verifyComplete();

            StepVerifier.create(template.findAll(MyObject.class))
                    .expectNextMatches(myObject -> (myObject.getId().getReference().equals("myObject1") ^
                            Objects.equals(myObject.getName(), null)))
                    .expectNextMatches(myObject -> (myObject.getId().getReference().equals("myObject1") ^
                            Objects.equals(myObject.getName(), null)))
                    .expectNextMatches(myObject -> (myObject.getId().getReference().equals("myObject1") ^
                            Objects.equals(myObject.getName(), null)))
                    .verifyComplete();
        }

    }

    @Nested
    class PartialUpdateTest {

        @Test
        void shouldInsertAndReturnNumberOfUpdates() {
            MyId myId = new MyId("ref", 1);

            MyPartialObject myPartialObject = new MyPartialObject("tata", 3);
            MyObject myUpdatedObject = new MyObject(myId, "tata", null, 3);

            Document mappedMyId = myId.mapToBSON();
            Optional<Document> mappedMyPartialObject = Optional.of(MyPartialObject.mapMyPartialObject(myPartialObject));

            StepVerifier
                    .create(bulkWriter.upsert(MyObject.class, Map.of(mappedMyId, mappedMyPartialObject), null))
                    .expectNext(1)
                    .verifyComplete();

            StepVerifier.create(template.findById(myId, MyObject.class))
                    .expectNext(myUpdatedObject)
                    .verifyComplete();
        }

        @Test
        void shouldUpdateAndReturnNumberOfUpdates() {
            MyId myId = new MyId("ref", 1);
            MyObject myObject = new MyObject(myId, "toto", "un objet", 5);
            template.save(myObject, "MyObject").block();

            MyPartialObject myPartialObject = new MyPartialObject("tata", 4);
            MyObject myUpdatedObject = new MyObject(myId, "tata", "un objet", 4);

            Document mappedMyId = myId.mapToBSON();
            Optional<Document> mappedMyPartialObject = Optional.of(MyPartialObject.mapMyPartialObject(myPartialObject));

            StepVerifier
                    .create(bulkWriter.upsert(MyObject.class, Map.of(mappedMyId, mappedMyPartialObject), null))
                    .expectNext(1)
                    .verifyComplete();

            StepVerifier.create(template.findById(myId, MyObject.class))
                    .expectNext(myUpdatedObject)
                    .verifyComplete();
        }

        @Test
        void shouldUpdateWithNullNameAndReturnNumberOfUpdates() {
            MyId myId = new MyId("ref", 1);
            MyObject myObject = new MyObject(myId, "toto", "un objet", 7);
            template.save(myObject, "MyObject").block();

            MyPartialObject myPartialObject = new MyPartialObject(null, 6);
            MyObject myUpdatedObject = new MyObject(myId, null, "un objet", 6);

            Document mappedMyId = myId.mapToBSON();
            Optional<Document> mappedMyPartialObject = Optional.of(MyPartialObject.mapMyPartialObject(myPartialObject));

            StepVerifier
                    .create(bulkWriter.upsert(MyObject.class, Map.of(mappedMyId, mappedMyPartialObject), null))
                    .expectNext(1)
                    .verifyComplete();

            StepVerifier.create(template.findById(myId, MyObject.class))
                    .expectNext(myUpdatedObject)
                    .verifyComplete();
        }

    }

    @Nested
    class PatchAllTest {
        @Test
        void emptyMap_doNothing() {
            StepVerifier.create(bulkWriter.upsert(MyObject.class, Map.of(), null))
                    .expectNext(0)
                    .verifyComplete();
        }

        @Test
        void executeOperationOnMapToUpsert() {
            MyId myId = new MyId("ref", 1);

            MyPartialObject myPartialObject = new MyPartialObject("tata", 3);
            MyObject myUpdatedObject = new MyObject(myId, "tata", null, 3);

            Document mappedMyId = myId.mapToBSON();
            Document mappedMyPartialObject = MyPartialObject.mapMyPartialObject(myPartialObject);

            Map<Document, Optional<Document>> map = Map.of(mappedMyId, Optional.of(mappedMyPartialObject));

            StepVerifier
                    .create(bulkWriter.upsert(MyObject.class, map, null))
                    .expectNext(1)
                    .verifyComplete();

            StepVerifier.create(template.findById(myId, MyObject.class))
                    .expectNext(myUpdatedObject)
                    .verifyComplete();
        }

        @Test
        void shouldUpdateAndReturnNumberOfUpdates() {
            MyId myId = new MyId("ref", 1);
            MyObject myObject = new MyObject(myId, "toto", "un objet", 5);
            template.save(myObject, "MyObject").block();

            MyPartialObject myPartialObject = new MyPartialObject("tata", 4);
            MyObject myUpdatedObject = new MyObject(myId, "tata", "un objet", 4);

            Document mappedMyId = myId.mapToBSON();
            Document mappedMyPartialObject = MyPartialObject.mapMyPartialObject(myPartialObject);

            Map<Document, Optional<Document>> map = Map.of(mappedMyId, Optional.ofNullable(mappedMyPartialObject));

            StepVerifier
                    .create(bulkWriter.upsert(MyObject.class, map, null))
                    .expectNext(1)
                    .verifyComplete();

            StepVerifier.create(template.findById(myId, MyObject.class))
                    .expectNext(myUpdatedObject)
                    .verifyComplete();
        }

        @Test
        void shouldUpdateWithNullNameAndReturnNumberOfUpdates() {
            MyId myId = new MyId("ref", 1);
            MyObject myObject = new MyObject(myId, "toto", "un objet", 7);
            template.save(myObject, "MyObject").block();

            MyPartialObject myPartialObject = new MyPartialObject(null, 6);
            MyObject myUpdatedObject = new MyObject(myId, null, "un objet", 6);

            Document mappedMyId = myId.mapToBSON();
            Document mappedMyPartialObject = MyPartialObject.mapMyPartialObject(myPartialObject);

            Map<Document, Optional<Document>> map = Map.of(mappedMyId, Optional.ofNullable(mappedMyPartialObject));

            StepVerifier
                    .create(bulkWriter.upsert(MyObject.class, map, null))
                    .expectNext(1)
                    .verifyComplete();

            StepVerifier.create(template.findById(myId, MyObject.class))
                    .expectNext(myUpdatedObject)
                    .verifyComplete();
        }
    }

    @Nested
    class UnsetFieldsTest {
        @Test
        void shouldUnsetFields() {
            MyId myId = new MyId("ref", 1);
            MyObject myObject = new MyObject(myId, "toto", "un objet", 5);
            template.save(myObject, "MyObject").block();

            MyObject myUpdatedObject = new MyObject(myId, null, "un objet", 5);

            Document mappedMyId = myId.mapToBSON();

            Map<Document, Optional<Document>> map = Map.of(mappedMyId, Optional.empty());

            StepVerifier
                    .create(bulkWriter.upsert(MyObject.class, map, List.of("name")))
                    .expectNext(1)
                    .verifyComplete();

            StepVerifier.create(template.findById(myId, MyObject.class))
                    .expectNext(myUpdatedObject)
                    .verifyComplete();
        }

        @Test
        void shouldUnsetAllFields() {
            MyId myId = new MyId("ref", 1);
            MyObject myObject = new MyObject(myId, "toto", "un objet", 5);
            template.save(myObject, "MyObject").block();

            MyObject myUpdatedObject = new MyObject(myId, null, null, null);

            Document mappedMyId = myId.mapToBSON();

            Map<Document, Optional<Document>> map = Map.of(mappedMyId, Optional.empty());

            StepVerifier
                    .create(bulkWriter.upsert(MyObject.class, map, null))
                    .expectNext(1)
                    .verifyComplete();

            StepVerifier.create(template.findById(myId, MyObject.class))
                    .expectNext(myUpdatedObject)
                    .verifyComplete();
        }
    }

    @Nested
    class SetOnInsertFieldTest {
        @Test
        void shouldSetOnInsertDescriptionFields() {
            template.dropCollection("MyObject");
            MyId myId = new MyId("ref", 1);

            MyPartialObject myPartialObject = new MyPartialObject("tata", 3);
            MyObject myUpdatedObject = new MyObject(myId, "tata", "My description", 3);

            Document mappedMyId = myId.mapToBSON();
            Document mappedMyPartialObject = MyPartialObject.mapMyPartialObject(myPartialObject);

            Map<Document, Optional<Document>> map = Map.of(mappedMyId, Optional.of(mappedMyPartialObject));

            StepVerifier
                    .create(bulkWriter.upsert(MyObject.class, map, null, Map.of("desc", "My description")))
                    .expectNext(1)
                    .verifyComplete();

            StepVerifier.create(template.findById(myId, MyObject.class))
                    .expectNext(myUpdatedObject)
                    .verifyComplete();
        }

        @Test
        void shouldNotSetOnInsertDescriptionWhenUpdate() {
            template.dropCollection("MyObject");
            MyId myId = new MyId("ref", 1);

            template.save(new MyObject(myId, null, null, null), "MyObject").block();

            MyPartialObject myPartialObject = new MyPartialObject("tata", 3);
            MyObject myUpdatedObject = new MyObject(myId, "tata", null, 3);

            Document mappedMyId = myId.mapToBSON();
            Document mappedMyPartialObject = MyPartialObject.mapMyPartialObject(myPartialObject);

            Map<Document, Optional<Document>> map = Map.of(mappedMyId, Optional.of(mappedMyPartialObject));

            StepVerifier
                    .create(bulkWriter.upsert(MyObject.class, map, null, Map.of("desc", "My other description")))
                    .expectNext(1)
                    .verifyComplete();

            StepVerifier.create(template.findById(myId, MyObject.class))
                    .expectNext(myUpdatedObject)
                    .verifyComplete();
        }
    }
}
