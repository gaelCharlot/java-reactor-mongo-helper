package org.niogatori.mongohelper.models;

import com.mongodb.WriteConcern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.Document;
import org.niogatori.mongohelper.bulkwriter.UpdateQueryBuilder;
import reactor.util.annotation.NonNull;
import reactor.util.annotation.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@EqualsAndHashCode
@Builder
@AllArgsConstructor
@Data
public class UpdateCommand {
    private String update;
    private List<Document> updates;
    private boolean ordered;
    private WriteConcern writeConcern;
    private boolean bypassDocumentValidation;
    private String comment;
    private Document let;

    public Document toBson() {
        Document updateCommand = new Document("update", update)
                .append("updates", updates)
                .append("ordered", ordered)
                .append("bypassDocumentValidation", bypassDocumentValidation)
                .append("let", let)
                .append("comment", comment);
        if (Objects.nonNull(writeConcern)) {
            updateCommand.append("writeConcern", writeConcern.asDocument());
        }
        return updateCommand;
    }

    public UpdateCommand withUpdates(@NonNull Map<Document, Optional<Document>> objectsToSaveById,
            @Nullable List<String> fieldsToUnset, Document fieldsToSetOnInsert, boolean upsertEnabled,
            boolean multiEnabled) {
        this.updates = objectsToSaveById.entrySet().stream()
                .map(entry -> new UpdateQueryBuilder()
                        .query(entry.getKey())
                        .addSetStage(entry.getValue().orElse(null))
                        .addUnsetStage(fieldsToUnset)
                        .addSetOnInsertStage(fieldsToSetOnInsert)
                        .isUpsertEnabled(upsertEnabled)
                        .isMultiEnabled(multiEnabled)
                        .build())
                .collect(Collectors.toList());
        return this;
    }

    public UpdateCommand withUpdates(Document fieldsToQuery, @Nullable Document fieldsToSet,
            @Nullable List<String> fieldsToUnset, Document fieldsToSetOnInsert, boolean upsertEnabled,
            boolean multiEnabled) {
        return this.withUpdates(Map.of(fieldsToQuery, Optional.ofNullable(fieldsToSet)), fieldsToUnset,
                fieldsToSetOnInsert, upsertEnabled, multiEnabled);
    }
}
