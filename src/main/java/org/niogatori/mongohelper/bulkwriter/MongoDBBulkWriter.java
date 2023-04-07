package org.niogatori.mongohelper.bulkwriter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;
import reactor.util.annotation.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class MongoDBBulkWriter<T> {

    private static final String CLASS_KEY = "_class";
    private static final String MONGO_ID = "_id";

    private final ReactiveMongoTemplate template;

    public Document toDocument(Object objectToSave) {
        Document document = new Document();
        template.getConverter().write(objectToSave, document);
        document.remove(CLASS_KEY);
        document.remove(MONGO_ID);
        return document;
    }

    public Document toIdDocument(Object id) {
        Document idDocument = new Document();
        template.getConverter().write(id, idDocument);
        idDocument.remove(CLASS_KEY);
        return new Document(MONGO_ID, idDocument);
    }

    public Document toCriteria(List<Document> criteria, LogicalOperator logicalOperator) {
        List<Document> queriesUpdate = !CollectionUtils.isEmpty(criteria) ? criteria : List.of();
        Document queries = new Document();

        if (queriesUpdate.size() > 1) {
            queries = new Document(String.format("$%s", logicalOperator.getOperator()), criteria);
        } else if (queriesUpdate.size() == 1) {
            queries = new Document(criteria.get(0));
        }

        return queries;
    }

    public Mono<Integer> upsertMany(Class<T> clazz, Document queries, Document fieldsToSet) {
        return upsertMany(clazz, queries, fieldsToSet, null, null);
    }

    public Mono<Integer> upsertMany(Class<T> clazz, Document queries, List<String> fieldsToUnset) {
        return upsertMany(clazz, queries, null, fieldsToUnset, null);
    }

    public Mono<Integer> upsertMany(Class<T> clazz, Document queries, Document fieldsToSet,
            List<String> fieldsToUnset, Document fieldsToSetOnInsert) {
        return template.getMongoDatabase()
                .map(mongoDatabase -> mongoDatabase.runCommand(
                        getUpdateCommand(clazz, queries, fieldsToSet, fieldsToUnset, fieldsToSetOnInsert)))
                .flatMapMany(Mono::from)
                .doOnNext(MongoDBBulkWriter::logErrors)
                .map(MongoDBBulkWriter::getNbTotalChanges)
                .reduce(Integer::sum);
    }

    public Mono<Integer> upsert(Class<T> clazz, @NonNull Map<Document, Optional<Document>> objectsToSaveById) {
        return this.upsert(clazz, objectsToSaveById, null, null);
    }

    public Mono<Integer> upsert(Class<T> clazz, @NonNull Map<Document, Optional<Document>> objectsToSaveById,
            @Nullable List<String> fieldsToUnset) {
        return this.upsert(clazz, objectsToSaveById, fieldsToUnset, null);
    }

    public Mono<Integer> upsert(Class<T> clazz, @NonNull Map<Document, Optional<Document>> objectsToSaveById,
            @Nullable List<String> fieldsToUnset, Map<String, Object> fieldsToSetOnInsert) {
        if (objectsToSaveById.isEmpty()) {
            return Mono.just(0);
        }

        Document command = getUpdateCommand(clazz, objectsToSaveById, fieldsToUnset,
                toDocument(fieldsToSetOnInsert));

        return Flux.just(objectsToSaveById)
                .flatMapSequential(list -> template.getMongoDatabase()
                        .map(mongoDatabase -> mongoDatabase.runCommand(command))
                        .flatMapMany(Mono::from), 1)
                .doOnNext(MongoDBBulkWriter::logErrors)
                .map(MongoDBBulkWriter::getNbTotalChanges)
                .reduce(Integer::sum);
    }

    private static Integer getNbTotalChanges(@NonNull Document bulkWriteResult) {
        List<Document> upserted =
                bulkWriteResult.containsKey("upserted") ? bulkWriteResult.getList("upserted", Document.class)
                        : List.of();
        upserted.forEach(document -> log.debug("{} is successfully upserted.", document.toJson()));
        int nModified = bulkWriteResult.containsKey("nModified") ? bulkWriteResult.getInteger("nModified") : 0;
        return upserted.size() + nModified;
    }

    private static void logErrors(@NonNull Document bulkWriteResult) {
        if (bulkWriteResult.containsKey("writeErrors")) {
            List<Document> writeErrors = bulkWriteResult.getList("writeErrors", Document.class);
            writeErrors.forEach(err -> log.error(err.getString("errmsg")));
            log.warn("{} error(s) has been result post bulkWrite process", writeErrors.size());
        }
    }

    Document getUpdateCommand(Class<T> clazz, @NonNull Map<Document, Optional<Document>> objectsToSaveById,
            @Nullable List<String> fieldsToUnset, Document fieldsToSet) {
        List<Document> queriesUpdate = objectsToSaveById.entrySet().stream()
                .map(entry -> new UpdateQueryBuilder()
                        .query(entry.getKey())
                        .addSetStage(entry.getValue().orElse(null))
                        .addUnsetStage(fieldsToUnset)
                        .addSetOnInsertStage(fieldsToSet)
                        .isUpsertEnabled(true)
                        .build())
                .collect(Collectors.toList());
        return new Document("update", template.getCollectionName(clazz))
                .append("updates", queriesUpdate)
                .append("ordered", true);
    }

    Document getUpdateCommand(Class<T> clazz, Document fieldsToQuery, Document fieldsToSet,
            @Nullable List<String> fieldsToUnset, Document fieldsToSetOnInsert) {
        Document query = new UpdateQueryBuilder()
                .query(fieldsToQuery)
                .addSetStage(fieldsToSet)
                .addUnsetStage(fieldsToUnset)
                .addSetOnInsertStage(fieldsToSetOnInsert)
                .isUpsertEnabled(true)
                .isMultiEnabled(true)
                .build();

        return new Document("update", template.getCollectionName(clazz))
                .append("updates", List.of(query))
                .append("ordered", true);
    }
}
