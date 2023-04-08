# Focus on technologies used in our projects

## MongoBulkWriter - utility class in Java Reactive App

### Introduction
We provide our applications with utility classes in order to be able to modify one (or more) data present in the database without overwriting its other attributes, in other words "patch" a data.
The MongoBulkWriter class has the following features:

- transform a Java object into org.bson.Document that we want to patch or insert into a database;
- transform a Java object (String, Integer, Complex Object) into org.bson.Document in MongoDB's "{ _id: yourObject }" format which will allow to query the correct object to patch;
- upserts a list of pre-transformed org.bson.Document from objects containing the new fields with their new values.
- delete fields from documents present in the database.
- a DEBUG level log of each _id of the upserted elements.
- a log at the ERROR level of each error returned from a bulk write.
- compose your own modification query by adding, according to your needs, steps in the "update" pipeline.

### Usage
To save some fields updating of a data, you have to transform in "_id" BsonDocument and your entire fields in BsonDocument.
Example : you wanna update a Product with this java object --> `{ "id": { "productRef": "A", "buIdentifier": 1 }, "label": "My New Label" }`.
1. Transform in id Bson like `{ "_id": { "productRef": "A", "buIdentifier": 1 } }` for mongo id to query.
2. Transform in Bson document like `{ "_id": { "productRef": "A", "buIdentifier": 1 }, "label": "My New Label" }` for mongo document to update.
3. Call `upsert(MyPojoDocument.class, Map.of(bsonId, Optional.of(bsonDocument)), null)`
4. Your data will change from `{ "_id": { "productRef": "A", "buIdentifier": 1 }, "matchingGlobalScore": 0.88, "label": "My Old Label" }` into `{ "_id": { "productRef": "A", "buIdentifier": 1 }, "matchingGlobalScore": 0.88, "label": "My New Label" }`.
5. Other fields than "label" will not change or vanish.

To delete some fields of a data, you have to transform in "_id" BsonDocument only and fill which fields you wanna delete.
Example : you wanna delete label field of a Product of this java object --> `{ "id": { "productRef": "A", "buIdentifier": 1 }, "matchingGlobalScore": 0.88, "label": "My Label" }`.
1. Transform in id Bson like `{ "_id": { "productRef": "A", "buIdentifier": 1 } }` for mongo id to query.
2. Call `upsert(MyPojoDocument.class, Map.of(bsonId, Optional.empty()), List.of("label"))`
3. Your data will change from `{ "_id": { "productRef": "A", "buIdentifier": 1 }, "matchingGlobalScore": 0.88, "label": "My Label" }` into `{ "_id": { "productRef": "A", "buIdentifier": 1 }, "matchingGlobalScore": 0.88 }`.
4. Other fields than "label" will not change or vanish.

Class UpdateQueryBuilder is here to simplify your update query creation which look like in the end of `{ q: { <query> }, u: [ <pipeline> ], upsert: <boolean> }`
It's a builder class that you use like :
```
new UpdateQueryBuilder()
.query(_idBsonDoc)
.setStage(optionalBsonDoc.getValue().orElse(null))
.unsetStage(fieldsToUnset)
.isUpsertEnable(trueOrFalse)
.build()
```

In the further, some upgrade could allow this builder to put other stages than `$set` and `$unset`.