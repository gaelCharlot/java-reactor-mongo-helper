package org.niogatori.mongohelper;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LogicalOperator {
    AND("and"),
    OR("or");

    private final String operator;
}
