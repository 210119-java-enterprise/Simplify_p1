package com.revature.util;
import com.revature.annotations.Column;
import com.revature.annotations.ForeignKey;
import com.revature.annotations.PrimaryKey;
import com.revature.annotations.Table;
import java.lang.reflect.Field;
import java.util.ArrayList;

public class Insert {
    private String insert="";

    public Insert(Metamodel<?> model, Object o) {
        parseModel(model, o);
    }

    private void parseModel(Metamodel<?> model, Object o) {
        String table = model.getModel().getAnnotation(Table.class).tableName();
        ArrayList<String> columns = new ArrayList<>();

        for(Field f: o.getClass().getDeclaredFields()){
            Column column = f.getAnnotation(Column.class);
            PrimaryKey pk = f.getAnnotation(PrimaryKey.class);
            ForeignKey fk = f.getAnnotation(ForeignKey.class);

            if(column != null) {
                columns.add(column.columnName());
            }else if(pk != null) {
                columns.add(column.columnName());
            }else if(fk != null) {
                columns.add(column.columnName());
            }
        }

        int numberOfColumns = columns.size();
        StringBuilder tableInsert = new StringBuilder("INSERT INTO " + table + " (");
        StringBuilder values = new StringBuilder("VALUES (");

        for (int i = 0; i < numberOfColumns; i++) {
            if(i == (numberOfColumns -1))
            {
                tableInsert.append(columns.get(i)).append(") ");
                values.append(" ? ").append(") ");
            }else {
                tableInsert.append(columns.get(i)).append(", ");
                values.append(" ? ").append(", ");
            }
        }
        insert = tableInsert.toString() + values.toString();
    }
    public String getInsert() {
        return insert;
    }

}