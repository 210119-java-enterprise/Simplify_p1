package com.revature.util;

import com.revature.annotations.Id;

import java.lang.reflect.Type;

public class Entity {
    private Type tableName;

    public Entity(Type tableName){
        if(tableName.getTypeName() == null){
            throw new IllegalStateException("Cannot create Entity object! Provided field, " + getName() + "is not annotated with @Id");
        }
        this.tableName = tableName;
    }

    public String getName(){
        return tableName.getTypeName();
    }
}