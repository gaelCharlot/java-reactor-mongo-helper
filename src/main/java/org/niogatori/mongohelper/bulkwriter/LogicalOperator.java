package org.niogatori.mongohelper.bulkwriter;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
enum LogicalOperator {
    AND("and"),
    OR("or");

    private final String operator;
}
