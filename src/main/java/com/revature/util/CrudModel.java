package com.revature.util;
import com.revature.annotations.*;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.ArrayList;

public class CrudModel<T> {
    private Class<T> clas;
    private ArrayList<ForeignKeyField> fkFields;
    private ArrayList<AttrField> attrFields;
    private ArrayList<AttrField> appliedAttrs; // contains the fields/attributes a user wants to select/insert/update/delete
    private Method[] methods;
    private PreparedStatement ps;
    private Connection conn;


    public CrudModel(Class<T> clas) {
        this.clas = clas;
        this.attrFields = new ArrayList<>();
        getColumns();
        this.appliedAttrs = new ArrayList<>();
        this.methods = clas.getMethods();
        this.fkFields = getForeignKeys();
        this.conn = ConnectionFactory.getInstance().getConnection();
    }

    public CrudModel<T> grab(String... attrs) {
        ps = null; // clear the prepared statement
        appliedAttrs.clear();

        try {
            Table table = clas.getAnnotation(Table.class);
            String tableName = table.tableName();

            if (attrs.length == 0) {
                ps = conn.prepareStatement("select * from " + tableName);
                attrFields.stream().forEach(attr -> appliedAttrs.add(attr));
                return this;
            }

            StringBuilder queryPlaceholders = new StringBuilder();
            String delimiter;

            for (int i=0; i<attrs.length; i++) {
                delimiter = (i < attrs.length - 1) ? ", " : "";

                AttrField currentAttr = getAttributeByColumnName(attrs[i]);

                if (currentAttr != null) {
                    queryPlaceholders.append(attrs[i] + delimiter);
                    appliedAttrs.add(currentAttr);
                }
            }

            ps = conn.prepareStatement("select " + queryPlaceholders.toString() + " from " + tableName);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return this;
    }

    public CrudModel<T> add(String... attrs) {
        ps = null;
        appliedAttrs.clear();

        try {
            Table table = clas.getAnnotation(Table.class);
            ArrayList<String> attrFilter = new ArrayList<>();
            StringBuilder queryPlaceholders = new StringBuilder();
            String tableName = table.tableName();
            String delimiter;

            for (String attrStr: attrs) {
                attrFields.stream()
                        .filter(attr -> attr.getColumnName().equals(attrStr))
                        .forEach(attr -> { attrFilter.add(attrStr); appliedAttrs.add(attr); });
            }

            for (int i=0; i<attrFilter.size(); i++) {
                delimiter = (i < attrFilter.size() - 1) ? ", " : "";
                queryPlaceholders.append(attrFilter.get(i) + delimiter);
            }

            ps = conn.prepareStatement("insert into " + tableName
                    + " (" + queryPlaceholders.toString() + ") values ");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return this;
    }


    public CrudModel<T> remove() throws SQLException {
        appliedAttrs.clear();
        ps = null;
        Table table = clas.getAnnotation(Table.class);
        String tableName = table.tableName();
        ps = conn.prepareStatement("delete from " + tableName);
        return this;
    }


    public ArrayList<ForeignKeyField> getForeignKeys() {

        ArrayList<ForeignKeyField> foreignKeyFields = new ArrayList<>();
        Field[] fields = clas.getDeclaredFields();
        for (Field field : fields) {
            ForeignKey attr = field.getAnnotation(ForeignKey.class);
            if (attr != null) {
                foreignKeyFields.add(new ForeignKeyField(field));
            }
        }

        return foreignKeyFields;
    }

    private AttrField getAttributeByColumnName(String name) {
        for (AttrField attr : attrFields) {
            if (attr.getColumnName().equals(name)) {
                return attr;
            }
        }

        return null;
    }


    public PrimaryKeys getPrimaryKey() {

        Field[] fields = clas.getDeclaredFields();
        for (Field field : fields) {
            PrimaryKey primaryKey = field.getAnnotation(PrimaryKey.class);
            if (primaryKey != null) {
                return new PrimaryKeys(field);
            }
        }
        throw new RuntimeException("Did not find a field annotated with @Id in: " + clas.getName());
    }

    public ArrayList<AttrField> getColumns() {

        Field[] fields = clas.getDeclaredFields();
        for (Field field : fields) {
            Attr column = field.getAnnotation(Attr.class);
            if (column != null) {
                attrFields.add(new AttrField(field));
            }
        }

        if (attrFields.isEmpty()) {
            throw new RuntimeException("No columns found in: " + clas.getName());
        }

        return attrFields;
    }


    public String getPreparedStatement() {
        return ps.toString();
    }

    public ArrayList<T> runGrab() {
        if (!ps.toString().startsWith("select")) {
            throw new BadMethodChainCallException("runGrab() can only be called when grab() is the head of the method chain.");
        }

        ArrayList<T> models = new ArrayList<>();
        try {
            ResultSet rs = ps.executeQuery();
            models = mapResultSet(rs);
        } catch (SQLException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            System.out.println(e.getMessage());
        }

        return models;
    }


    public int runAdd() throws Exception {
        if (!ps.toString().startsWith("insert")) {
            throw new BadMethodChainCallException("runAdd() can only be called from addValues()");
        }

        String psStr = ps.toString();
        System.out.println(psStr);
        ps = conn.prepareStatement(psStr.substring(0, psStr.length() - 2));

        return ps.executeUpdate();
    }


    public int runChange() throws Exception {
        if (!ps.toString().startsWith("update")) {
            throw new BadMethodChainCallException("runChange() can only be called from set()");
        }

        return ps.executeUpdate();
    }


    public int runRemove() throws Exception {
        if (!ps.toString().startsWith("delete")) {
            throw new BadMethodChainCallException("runRemove() can only be called when remove() is the head of the method chain.");
        }

        return ps.executeUpdate();
    }


    private Method getMethodByFieldName(String fieldName) {
        for (Method currentMethod : methods) {
            if (currentMethod.getName().equals(fieldName)) {
                return currentMethod;
            }
        }

        return null;
    }

    private ArrayList<T> mapResultSet(ResultSet rs) throws SQLException, IllegalAccessException,
            InstantiationException, InvocationTargetException {
        T model;
        PrimaryKeys pkField = getPrimaryKey();
        ArrayList<T> models = new ArrayList<>();

        while (rs.next()) {
            model = clas.newInstance();
            char[] pkNameArr = pkField.getName().toCharArray();
            pkNameArr[0] = Character.toUpperCase(pkNameArr[0]);
            String pkName = String.valueOf(pkNameArr);
            Method pkSetId = getMethodByFieldName("set" + pkName);

            try {
                int pkk = rs.getInt(pkField.getColumnName());
                pkSetId.invoke(model, pkk);
            } catch (SQLException | InvocationTargetException e) {/* do nothing */}

            for (ForeignKeyField fk: fkFields) {
                String FKName = fk.getName();
                char[] setterFKNameArr = FKName.toCharArray();

                setterFKNameArr[0] = Character.toUpperCase(setterFKNameArr[0]);
                FKName = String.valueOf(setterFKNameArr);
                Method fkSetId = getMethodByFieldName("set" + FKName);

                try {
                    fkSetId.invoke(model, rs.getInt(fk.getColumnName()));
                } catch (SQLException | InvocationTargetException e) {
                    // do nothing; added try-catch block since ResultSet throws an exception if the params aren't
                    // in the result set. Sometimes, we don't need to retrieve a FK
                }
            }

            for (AttrField selectedAttr: appliedAttrs) {
                Class<?> type = selectedAttr.getType();
                char[] getterAttrNameArr = selectedAttr.getName().toCharArray();
                getterAttrNameArr[0] = Character.toUpperCase(getterAttrNameArr[0]);
                String attrMethodName = String.valueOf(getterAttrNameArr);
                Method setAttr = getMethodByFieldName("set" + attrMethodName);

                if (type == String.class) {
                    setAttr.invoke(model, rs.getString(selectedAttr.getColumnName()));
                } else if (type == int.class) {
                    setAttr.invoke(model, rs.getInt(selectedAttr.getColumnName()));
                } else if (type == double.class) {
                    setAttr.invoke(model, rs.getDouble(selectedAttr.getColumnName()));
                }
            }
            models.add(model);
        }
        return models;
    }
}