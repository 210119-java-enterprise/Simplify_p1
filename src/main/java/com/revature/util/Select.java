package com.revature.util;
import com.revature.annotations.Table;

public class Select {
    private String select="";

    public Select(Metamodel<?> model) {
        selectAll(model);
    }
    private void selectAll(Metamodel<?> model){
        String tableName = model.getModel().getAnnotation(Table.class).tableName();

        select = "SELECT * FROM " + tableName;
    }
    public String getSelect() {
        return select;
    }
}
