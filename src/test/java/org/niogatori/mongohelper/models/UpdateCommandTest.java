package org.niogatori.mongohelper.models;

import org.bson.BsonDocument;
import org.bson.Document;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateCommandTest {

    private final static Clock CLOCK =
            Clock.fixed(LocalDateTime.of(2023, 4, 10, 12, 0, 0).toInstant(ZoneOffset.UTC), ZoneOffset.UTC);

    @Nested
    class BsonConvertorTest {
        @Test
        void shouldConvertUpdateCommandToBson() {
            Document update1 = new Document("q", new Document("_id", "myId"))
                    .append("u", new Document("field", "myNewValue"))
                    .append("upsert", true)
                    .append("multi", false);
            List<Document> updates = List.of(update1);
            UpdateCommand updateCommand = new UpdateCommand("MyCollection", updates, true, null, false, null, null);

            assertThat(updateCommand.toBson()).satisfies(command -> {
                assertThat(command)
                        .hasFieldOrPropertyWithValue("update", "MyCollection")
                        .hasFieldOrPropertyWithValue("updates", List.of(update1))
                        .hasFieldOrPropertyWithValue("ordered", true)
                        .hasFieldOrPropertyWithValue("bypassDocumentValidation", false)
                        .hasFieldOrPropertyWithValue("let", null)
                        .hasFieldOrPropertyWithValue("comment", null);
                assertThat(command)
                        .doesNotContainKey("writeConcern");
            });
        }
    }

    @Nested
    class WithUpdatesTest {
        @Test
        void shouldAddUpdatesQuery_WithOneQueryAndFieldsToSet() {
            Document updateQuery = new Document("q", new Document("_id", "myId"))
                    .append("u", new Document("$set", new Document("field", "myNewValue")).toBsonDocument())
                    .append("upsert", true)
                    .append("multi", false);

            UpdateCommand updateCommand = new UpdateCommand("MyCollection", null, true, null, false, null, null);

            assertThat(updateCommand)
                    .hasFieldOrPropertyWithValue("update", "MyCollection")
                    .hasFieldOrPropertyWithValue("updates", null)
                    .hasFieldOrPropertyWithValue("ordered", true)
                    .hasFieldOrPropertyWithValue("bypassDocumentValidation", false)
                    .hasFieldOrPropertyWithValue("comment", null)
                    .hasFieldOrPropertyWithValue("let", null);

            assertThat(updateCommand.withUpdates(new Document("_id", "myId"), new Document("field", "myNewValue"), null,
                    null, true, false))
                            .isEqualTo(new UpdateCommand("MyCollection", List.of(updateQuery), true, null, false, null,
                                    null));

        }

        @Test
        void shouldAddUpdatesQuery_WithManyQueriesAndFieldsToSet() {
            Document updateQuery = new Document("q", new Document("_id", "myId"))
                    .append("u", new Document("$set", new Document("field", "myNewValue")).toBsonDocument())
                    .append("upsert", true)
                    .append("multi", false);

            UpdateCommand updateCommand = new UpdateCommand("MyCollection", null, true, null, false, null, null);

            assertThat(updateCommand)
                    .hasFieldOrPropertyWithValue("update", "MyCollection")
                    .hasFieldOrPropertyWithValue("updates", null)
                    .hasFieldOrPropertyWithValue("ordered", true)
                    .hasFieldOrPropertyWithValue("bypassDocumentValidation", false)
                    .hasFieldOrPropertyWithValue("comment", null)
                    .hasFieldOrPropertyWithValue("let", null);

            assertThat(updateCommand.withUpdates(
                    Map.of(new Document("_id", "myId"), Optional.of(new Document("field", "myNewValue"))), null,
                    null, true, false))
                            .isEqualTo(new UpdateCommand("MyCollection", List.of(updateQuery), true, null, false, null,
                                    null));
        }

        @Test
        void shouldAddUpdatesQuery_WithFieldsToUnsetAndFieldsToSetOnInsert() {
            UpdateCommand updateCommand = new UpdateCommand("MyCollection", null, true, null, false, null, null);

            assertThat(updateCommand)
                    .hasFieldOrPropertyWithValue("update", "MyCollection")
                    .hasFieldOrPropertyWithValue("updates", null)
                    .hasFieldOrPropertyWithValue("ordered", true)
                    .hasFieldOrPropertyWithValue("bypassDocumentValidation", false)
                    .hasFieldOrPropertyWithValue("comment", null)
                    .hasFieldOrPropertyWithValue("let", null);

            assertThat(updateCommand.withUpdates(
                    Map.of(new Document("_id", "myId"), Optional.of(new Document("field", "myNewValue"))),
                    List.of("label", "metadata"),
                    new Document("creationDate", LocalDate.now(CLOCK)), true, false))
                            .hasFieldOrPropertyWithValue("update", "MyCollection")
                            .hasFieldOrPropertyWithValue("ordered", true)
                            .hasFieldOrPropertyWithValue("bypassDocumentValidation", false)
                            .hasFieldOrPropertyWithValue("comment", null)
                            .hasFieldOrPropertyWithValue("let", null)
                            .satisfies(cmd -> assertThat(cmd.getUpdates()).hasSize(1)
                                    .allMatch(document -> {
                                        BsonDocument u = document.get("u", BsonDocument.class);
                                        return u.containsKey("$set") && u.containsKey("$unset")
                                                && u.containsKey("$setOnInsert");
                                    }));
        }
    }

}
