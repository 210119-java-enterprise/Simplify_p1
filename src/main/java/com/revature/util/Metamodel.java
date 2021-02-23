package com.revature.util;

import com.revature.annotations.Column;
import com.revature.annotations.Entity;
import com.revature.annotations.Id;
import com.revature.annotations.JoinColumn;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Metamodel<T> {

    private Class<T> clazz;
    private IdField primaryKeyField;
    private List<ColumnField> columnFields;
    private List<ForeignKeyField> foreignKeyFields;

    public static <T> Metamodel<T> of(Class<T> clazz) {
        if (clazz.getAnnotation(Entity.class) == null) {
            throw new IllegalStateException("Cannot create Metamodel object! Provided class, " + clazz.getName() + "is not annotated with @Entity");
        }
        return new Metamodel<>(clazz);
    }

    public Metamodel(Class<T> clazz) {
        this.clazz = clazz;
        this.columnFields = new LinkedList<>();
        this.foreignKeyFields = new LinkedList<>();
    }

    public String getClassName() {
        return clazz.getName();
    }

    public Class<T> getModel() {
        return clazz;
    }
        public IdField getPrimaryKey() {
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            Id primaryKey = field.getAnnotation(Id.class);
            if (primaryKey != null) {
                return new IdField(field);
            }
        }
        throw new RuntimeException("Did not find a field annotated with @Id in: " + clazz.getName());
    }

    public List<ColumnField> getColumns() {

        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            Column column = field.getAnnotation(Column.class);
            if (column != null) {
                columnFields.add(new ColumnField(field));
            }
        }

        if (columnFields.isEmpty()) {
            throw new RuntimeException("No columns found in: " + clazz.getName());
        }

        return columnFields;
    }

    public List<ForeignKeyField> getForeignKeys() {

        List<ForeignKeyField> foreignKeyFields = new ArrayList<>();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            JoinColumn column = field.getAnnotation(JoinColumn.class);
            if (column != null) {
                foreignKeyFields.add(new ForeignKeyField(field));
            }
        }

        return foreignKeyFields;

    }
    public String findFieldNameOfColumn(String columnName){
        for(ColumnField c : this.getColumns()){
            if(c.getColumnName().equals(columnName)){
                return c.getName();
            }
        }
        if(getPrimaryKey().getColumnName().equals(columnName)){
            return getPrimaryKey().getName();
        }
        for(ForeignKeyField f : getForeignKeys()){
            if(f.getColumnName().equals(columnName)){
                return f.getName();
            }
        }
        return null;
    }

    /**
     * gets the class type of a given column
     * @param columnName the name of the column
     * @return the class type of column else returns null
     */
    public Class<?> findClassOfColumn(String columnName){
        for(ColumnField c : this.getColumns()){
            if(c.getColumnName().equals(columnName)){
                return c.getType();
            }
        }
        if(getPrimaryKey().getColumnName().equals(columnName)){
            return getPrimaryKey().getType();
        }
        for(ForeignKeyField f : getForeignKeys()){
            if(f.getColumnName().equals(columnName)){
                return f.getType();
            }
        }
        return null;
    }

}
