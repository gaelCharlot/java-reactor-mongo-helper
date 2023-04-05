package bulkwriter;

import org.bson.Document;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.niogatori.mongohelper.bulkwriter.UpdateQueryBuilder;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateQueryBuilderTest {

    private final Document QUERY_ON_ID = new Document("_id", "myIdValue");

    @Nested
    class QueryToggleUpsertTest {

        @Test
        void shouldCreateQueryUpdateDocumentWithoutStages_defaultUpsertFalse() {
            Document query = new UpdateQueryBuilder()
                    .query(QUERY_ON_ID)
                    .build();
            assertThat(query.entrySet()).hasSize(4);
            assertThat(query.toJson().replaceAll(" ", ""))
                    .isEqualTo("{\"q\":{\"_id\":\"myIdValue\"},\"u\":{},\"upsert\":false,\"multi\":false}");
        }

        @Test
        void shouldCreateQueryUpdateDocumentWithoutStages_upsertTrue() {
            Document query = new UpdateQueryBuilder()
                    .query(QUERY_ON_ID)
                    .isUpsertEnabled(true)
                    .build();
            assertThat(query.entrySet()).hasSize(4);
            assertThat(query.toJson().replaceAll(" ", ""))
                    .isEqualTo("{\"q\":{\"_id\":\"myIdValue\"},\"u\":{},\"upsert\":true,\"multi\":false}");
        }

        @Test
        void shouldCreateQueryUpdateDocumentWithoutStages_setUpsertFalse() {
            Document query = new UpdateQueryBuilder()
                    .query(QUERY_ON_ID)
                    .isUpsertEnabled(false)
                    .build();
            assertThat(query.entrySet()).hasSize(4);
            assertThat(query.toJson().replaceAll(" ", ""))
                    .isEqualTo("{\"q\":{\"_id\":\"myIdValue\"},\"u\":{},\"upsert\":false,\"multi\":false}");
        }

    }

    @Nested
    class QueryToggleMultiTest {

        @Test
        void shouldCreateQueryUpdateDocumentWithoutStages_defaultMultiFalse() {
            Document query = new UpdateQueryBuilder()
                    .query(QUERY_ON_ID)
                    .build();
            assertThat(query.entrySet()).hasSize(4);
            assertThat(query.toJson().replaceAll(" ", ""))
                    .isEqualTo("{\"q\":{\"_id\":\"myIdValue\"},\"u\":{},\"upsert\":false,\"multi\":false}");
        }

        @Test
        void shouldCreateQueryUpdateDocumentWithoutStages_MultiTrue() {
            Document query = new UpdateQueryBuilder()
                    .query(QUERY_ON_ID)
                    .isMultiEnabled(true)
                    .build();
            assertThat(query.entrySet()).hasSize(4);
            assertThat(query.toJson().replaceAll(" ", ""))
                    .isEqualTo("{\"q\":{\"_id\":\"myIdValue\"},\"u\":{},\"upsert\":false,\"multi\":true}");
        }

        @Test
        void shouldCreateQueryUpdateDocumentWithoutStages_setMultiFalse() {
            Document query = new UpdateQueryBuilder()
                    .query(QUERY_ON_ID)
                    .isMultiEnabled(false)
                    .build();
            assertThat(query.entrySet()).hasSize(4);
            assertThat(query.toJson().replaceAll(" ", ""))
                    .isEqualTo("{\"q\":{\"_id\":\"myIdValue\"},\"u\":{},\"upsert\":false,\"multi\":false}");
        }

    }

    @Nested
    class AddSetStageTest {

        @Test
        void shouldNotUpdateSetStageWhenArgIsEmptyOrNull() {
            Document query1 = new UpdateQueryBuilder()
                    .query(QUERY_ON_ID)
                    .addSetStage(new Document()) // empty
                    .build();

            assertThat(query1.entrySet()).hasSize(4);
            assertThat(query1.toJson().replaceAll(" ", ""))
                    .isEqualTo("{\"q\":{\"_id\":\"myIdValue\"},\"u\":{},\"upsert\":false,\"multi\":false}");

            Document query2 = new UpdateQueryBuilder()
                    .query(QUERY_ON_ID)
                    .addSetStage(null) // null
                    .build();

            assertThat(query2.entrySet()).hasSize(4);
            assertThat(query2.toJson().replaceAll(" ", ""))
                    .isEqualTo("{\"q\":{\"_id\":\"myIdValue\"},\"u\":{},\"upsert\":false,\"multi\":false}");
        }
    }

    @Nested
    class AddUnsetStageTest {
        @Test
        void shouldNotUpdateUnsetStageWhenArgIsEmptyOrNull() {
            Document query1 = new UpdateQueryBuilder()
                    .query(QUERY_ON_ID)
                    .addUnsetStage(List.of()) // empty
                    .build();

            assertThat(query1.entrySet()).hasSize(4);
            assertThat(query1.toJson().replaceAll(" ", ""))
                    .isEqualTo("{\"q\":{\"_id\":\"myIdValue\"},\"u\":{},\"upsert\":false,\"multi\":false}");

            Document query2 = new UpdateQueryBuilder()
                    .query(QUERY_ON_ID)
                    .addUnsetStage(null) // null
                    .build();

            assertThat(query2.entrySet()).hasSize(4);
            assertThat(query2.toJson().replaceAll(" ", ""))
                    .isEqualTo("{\"q\":{\"_id\":\"myIdValue\"},\"u\":{},\"upsert\":false,\"multi\":false}");
        }

        @Test
        void shouldCreateQueryUpdateDocumentWithUnsetStage_unsetField1() {
            Document query = new UpdateQueryBuilder()
                    .query(QUERY_ON_ID)
                    .addUnsetStage(List.of("field1"))
                    .build();
            assertThat(query.entrySet()).hasSize(4);
            assertThat(query.toJson().replaceAll(" ", ""))
                    .isEqualTo(
                            "{\"q\":{\"_id\":\"myIdValue\"},\"u\":{\"$unset\":{\"field1\":\"\"}},\"upsert\":false,\"multi\":false}");
        }

        @Test
        void shouldCreateQueryUpdateDocumentWithSeveralUnsetStages_unsetField1AndField2() {
            Document query = new UpdateQueryBuilder()
                    .query(QUERY_ON_ID)
                    .addUnsetStage(List.of("field1"))
                    .addUnsetStage(List.of("field2"))
                    .build();
            assertThat(query.entrySet()).hasSize(4);
            assertThat(query.toJson().replaceAll(" ", ""))
                    .isEqualTo(
                            "{\"q\":{\"_id\":\"myIdValue\"},\"u\":{\"$unset\":{\"field1\":\"\",\"field2\":\"\"}},\"upsert\":false,\"multi\":false}");
        }
    }

    @Nested
    class AddSetOnInsertStageTest {

        @Test
        void shouldNotUpdateSetOnInsertStageWhenArgIsEmptyOrNull() {
            Document query1 = new UpdateQueryBuilder()
                    .query(QUERY_ON_ID)
                    .addSetOnInsertStage(new Document()) // empty
                    .build();

            assertThat(query1.entrySet()).hasSize(4);
            assertThat(query1.toJson().replaceAll(" ", ""))
                    .isEqualTo("{\"q\":{\"_id\":\"myIdValue\"},\"u\":{},\"upsert\":false,\"multi\":false}");

            Document query2 = new UpdateQueryBuilder()
                    .query(QUERY_ON_ID)
                    .addSetOnInsertStage(null) // null
                    .build();

            assertThat(query2.entrySet()).hasSize(4);
            assertThat(query2.toJson().replaceAll(" ", ""))
                    .isEqualTo("{\"q\":{\"_id\":\"myIdValue\"},\"u\":{},\"upsert\":false,\"multi\":false}");
        }

        @Test
        void shouldUpdateSetOnInsertStageWhenArgIsValidDocument() {
            UpdateQueryBuilder builder = new UpdateQueryBuilder();

            Document query1 = builder.query(QUERY_ON_ID)
                    .addSetOnInsertStage(new Document("field4", "value4").append("field1", "value1"))
                    .isUpsertEnabled(true)
                    .build();
            assertThat(query1.entrySet()).hasSize(4);
            assertThat(query1.toJson().replaceAll(" ", "")).isEqualTo(
                    "{\"q\":{\"_id\":\"myIdValue\"},\"u\":{\"$setOnInsert\":{\"field4\":\"value4\",\"field1\":\"value1\"}},\"upsert\":true,\"multi\":false}");
        }
    }

    @Nested
    class AddSeveralSameTypeOfStagesTest {

        private final Document PATCH_BSON = new Document(Map.of("field1", "newValueField1"));
        private final Document PATCH2_BSON = new Document(Map.of("field2", "newValueField2"));

        @Test
        void shouldCreateQueryUpdateDocumentWithSetAndUnsets() {
            Document query = new UpdateQueryBuilder()
                    .query(QUERY_ON_ID)
                    .addSetStage(PATCH_BSON)
                    .addSetStage(PATCH2_BSON)
                    .addUnsetStage(List.of("field3"))
                    .build();
            assertThat(query.entrySet()).hasSize(4);
            assertThat(query.toJson().replaceAll(" ", ""))
                    .isEqualTo(
                            "{\"q\":{\"_id\":\"myIdValue\"},\"u\":{\"$set\":{\"field1\":\"newValueField1\",\"field2\":\"newValueField2\"},\"$unset\":{\"field3\":\"\"}},\"upsert\":false,\"multi\":false}");
        }

        @Test
        void shouldCreateQueryUpdateDocumentWithSetOnInsertAndSetsAndUnsets() {
            Document query = new UpdateQueryBuilder()
                    .query(QUERY_ON_ID)
                    .addUnsetStage(List.of("field4"))
                    .addSetOnInsertStage(new Document("field1", "value1").append("field2", "value2"))
                    .addSetStage(new Document("field3", 4))
                    .build();
            assertThat(query.entrySet()).hasSize(4);
            assertThat(query.toJson().replaceAll(" ", ""))
                    .isEqualTo(
                            "{\"q\":{\"_id\":\"myIdValue\"},\"u\":{\"$set\":{\"field3\":4},\"$setOnInsert\":{\"field1\":\"value1\",\"field2\":\"value2\"},\"$unset\":{\"field4\":\"\"}},\"upsert\":false,\"multi\":false}");
        }
    }

    @Nested
    class ResetClassAttributesTest {

        private final Document ID_BSON = new Document("_id", "myIdValue");
        private final Document PATCH_BSON = new Document(Map.of("field1", "newValueField1"));

        @Test
        void shouldInitClassAttributesAfterCallBuild() {
            UpdateQueryBuilder builder = new UpdateQueryBuilder();

            Document query1 = builder.query(ID_BSON)
                    .addSetStage(PATCH_BSON)
                    .addUnsetStage(List.of("field2", "field3"))
                    .addSetOnInsertStage(new Document("field4", "value4"))
                    .isUpsertEnabled(true)
                    .build();
            assertThat(query1.entrySet()).hasSize(4);
            assertThat(query1.toJson().replaceAll(" ", "")).isEqualTo(
                    "{\"q\":{\"_id\":\"myIdValue\"},\"u\":{\"$set\":{\"field1\":\"newValueField1\"},\"$setOnInsert\":{\"field4\":\"value4\"},\"$unset\":{\"field3\":\"\",\"field2\":\"\"}},\"upsert\":true,\"multi\":false}");

            Document query2 = builder.build();
            assertThat(query2.entrySet()).hasSize(4);
            assertThat(query2.toJson().replaceAll(" ", ""))
                    .isEqualTo("{\"q\":null,\"u\":{},\"upsert\":false,\"multi\":false}");
        }
    }

}
