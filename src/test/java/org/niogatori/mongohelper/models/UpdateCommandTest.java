package org.niogatori.mongohelper.models;

import org.bson.BsonString;
import org.bson.Document;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateCommandTest {

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
        void shouldAddUpdatesQuery() {
            Document updateQuery = new Document("q", new Document("_id", "myId"))
                    .append("u", new Document("$set", new Document("field", new BsonString("myNewValue"))))
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

            assertThat(updateCommand.withUpdates(new Document("_id", "myId"), new Document("field", "myNewValue"), null, null,true, false))
                    .isEqualTo(new UpdateCommand("MyCollection", List.of(updateQuery), true, null, false, null, null));

        }
    }

}
