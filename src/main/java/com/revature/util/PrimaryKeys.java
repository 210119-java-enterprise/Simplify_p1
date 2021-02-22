package com.revature.util;

import com.revature.annotations.Column;
import com.revature.annotations.PrimaryKey;

import java.lang.reflect.Field;

public class PrimaryKeys {

    private Field field;

    public PrimaryKeys(Field field) {
        if (field.getAnnotation(PrimaryKey.class) == null) {
            throw new IllegalStateException("Cannot create IdField object! Provided field, " + getName() + "is not annotated with @Id");
        }
        this.field = field;
    }

    public String getName() {
        return field.getName();
    }

    public Class<?> getType() {
        return field.getType();
    }

    public String getColumnName() {
        return field.getAnnotation(PrimaryKey.class).primaryKey();
    }

}