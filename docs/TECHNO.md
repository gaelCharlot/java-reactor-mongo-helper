# Focus on Technologies Used in Our Projects

## MongoBulkWriter - Utility Class in Java Reactive App

### Introduction
We provide utility classes in our applications to enable modification of one or more data present in the database without overwriting other attributes, commonly known as "patching" data. The MongoBulkWriter class has the following features:

- Transforms a Java object into `org.bson.Document` that can be used to patch or insert data into a MongoDB database.
- Transforms a Java object (String, Integer, Complex Object) into `org.bson.Document` in MongoDB's `{ _id: yourObject }` format, allowing querying of the correct object to patch.
- Upserts a list of pre-transformed `org.bson.Document` from objects containing new fields with their new values.
- Deletes fields from documents present in the database.
- Provides a DEBUG level log of each `_id` of the upserted elements.
- Provides an ERROR level log of each error returned from a bulk write.
- Allows composing of custom modification queries by adding steps in the "update" pipeline according to your needs.

### Usage
To update fields of a data, you need to transform the fields into a `_id` `BsonDocument` and the entire fields into a `BsonDocument` as follows:
Example: You want to update a Product with this Java object: `{ "id": { "field1": "A", "field2": 1 }, "label": "My New Label" }`.

1. Transform the `_id` into a `Bson` document: `{ "_id": { "field1": "A", "field2": 1 } }` to query in MongoDB.
2. Transform the fields into a `Bson` document: `{ "_id": { "field1": "A", "field2": 1 }, "label": "My New Label" }` to update in MongoDB.
3. Call `upsert(MyPojoDocument.class, Map.of(bsonId, Optional.of(bsonDocument)), null)`.
4. Your data will change from `{ "_id": { "field1": "A", "field2": 1 }, "price": 0.88, "label": "My Old Label" }` to `{ "_id": { "field1": "A", "field2": 1 }, "price": 0.88, "label": "My New Label" }`.
5. Other fields than "label" will not change or vanish.

To delete fields from a data, you need to transform the `_id` into a `Bson` document only and specify the fields to be deleted as follows:
Example: You want to delete the "label" field of a Product with this Java object: `{ "id": { "field1": "A", "field2": 1 }, "price": 0.88, "label": "My Label" }`.

1. Transform the `_id` into a `Bson` document: `{ "_id": { "field1": "A", "field2": 1 } }` to query in MongoDB.
2. Call `upsert(MyPojoDocument.class, Map.of(bsonId, Optional.empty()), List.of("label"))`.
3. Your data will change from `{ "_id": { "field1": "A", "field2": 1 }, "price": 0.88, "label": "My Label" }` to `{ "_id": { "field1": "A", "field2": 1 }, "price": 0.88 }`.
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
