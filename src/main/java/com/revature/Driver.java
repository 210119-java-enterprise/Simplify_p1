package com.revature;

import com.revature.annotations.Entity;
import com.revature.annotations.Table;
import com.revature.model.Test;
import com.revature.model.User;
import com.revature.util.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;


public class Driver {

    public static void main(String[] args) {

        Configuration cfg = new Configuration();
        cfg.addAnnotatedClass(User.class)
                .addAnnotatedClass(Test.class);



        try (Connection conn = ConnectionFactory.getInstance().getConnection()) {
            CrudModel<User> mm2 = new CrudModel<>(User.class);
            mm2.grab("first_name")
                    .runGrab();
        } catch (SQLException e) {
            e.printStackTrace();
        }


        for (Metamodel<?> metamodel : cfg.getMetamodels()) {


            System.out.printf("Printing metamodel for class: %s\n", metamodel.getClass());
            IdField idField = metamodel.getPrimaryKey();
            List<ColumnField> columnFields = metamodel.getColumns();
            List<ForeignKeyField> foreignKeyFields = metamodel.getForeignKeys();

            System.out.printf("\tFound a primary key field named %s of type %s, which maps to the column with the name: %s\n", idField.getName(), idField.getType(), idField.getColumnName());

            for (ColumnField columnField : columnFields) {
                System.out.printf("\tFound a column field named: %s of type %s, which maps to the column with the name: %s\n", columnField.getName(), columnField.getType(), columnField.getColumnName());
            }

            for (ForeignKeyField foreignKeyField : foreignKeyFields) {
                System.out.printf("\tFound a foreign key field named %s of type %s, which maps to the column with the name: %s\n", foreignKeyField.getName(), foreignKeyField.getType(), foreignKeyField.getColumnName());
            }

            System.out.println();
        }
    }

}