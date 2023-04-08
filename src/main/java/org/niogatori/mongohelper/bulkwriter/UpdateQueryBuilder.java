package org.niogatori.mongohelper.bulkwriter;

import com.mongodb.client.model.Updates;
import org.bson.BsonDocument;
import org.bson.Document;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class UpdateQueryBuilder {

    private Document query;
    private Document setOperation;
    private Document unsetOperation;
    private Document setOnInsertOperation;
    private boolean upsertEnabled = false;
    private boolean multiEnabled = false;

    /**
     * { q: <query>, u: <document or pipeline>, upsert: <boolean> }
     */
    public Document build() {
        BsonDocument updates =
                Updates.combine(accumulateStages(setOperation, setOnInsertOperation, unsetOperation)).toBsonDocument();
        Document result = new Document("q", query)
                .append("u", updates)
                .append("upsert", upsertEnabled)
                .append("multi", multiEnabled);
        this.reset();
        return result;
    }

    private void reset() {
        this.upsertEnabled = false;
        this.setOperation = null;
        this.unsetOperation = null;
        this.query = null;
        this.setOnInsertOperation = null;
    }

    public UpdateQueryBuilder isUpsertEnabled(boolean enable) {
        this.upsertEnabled = enable;
        return this;
    }

    public UpdateQueryBuilder isMultiEnabled(boolean enable) {
        this.multiEnabled = enable;
        return this;
    }

    public UpdateQueryBuilder query(Document bsonId) {
        this.query = bsonId;
        return this;
    }

    public UpdateQueryBuilder addSetStage(Document documentToSet) {
        if (documentToSet != null && !documentToSet.isEmpty()) {
            if (this.setOperation == null || !this.setOperation.containsKey("$set")) {
                this.setOperation = new Document("$set", documentToSet);
            } else {
                Document setStage = this.setOperation.get("$set", Document.class);
                documentToSet.forEach(setStage::append);
                this.setOperation = new Document("$set", setStage);
            }
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    public UpdateQueryBuilder addUnsetStage(List<String> fields) {
        if (fields != null && !fields.isEmpty()) {
            Map<String, String> unsetFieldsMap = new HashMap<>();
            fields.forEach(fieldName -> unsetFieldsMap.put(fieldName, ""));
            if (this.unsetOperation != null && this.unsetOperation.containsKey("$unset")) {
                unsetFieldsMap.putAll(this.unsetOperation.get("$unset", HashMap.class));
            }
            this.unsetOperation = new Document("$unset", unsetFieldsMap);
        }
        return this;
    }

    public UpdateQueryBuilder addSetOnInsertStage(Document fields) {
        if (fields != null && !fields.isEmpty()) {
            if (this.setOnInsertOperation == null || !this.setOnInsertOperation.containsKey("$setOnInsert")) {
                this.setOnInsertOperation = new Document("$setOnInsert", fields);
            } else {
                Document existing = (Document) setOnInsertOperation.get("$setOnInsert");
                fields.forEach(existing::append);
                this.setOnInsertOperation = new Document("$setOnInsert", existing);
            }
        }
        return this;
    }

    private List<Document> accumulateStages(Document... stages) {
        return Arrays.stream(stages)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

}
