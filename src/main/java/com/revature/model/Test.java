package com.revature.model;

import com.revature.annotations.Column;
import com.revature.annotations.Entity;
import com.revature.annotations.Id;

@Entity(tableName = "test")
public class Test {

    @Id(columnName = "id")
    private int id;

    @Column(columnName = "test_field_1")
    private String testField1;

    @Column(columnName = "test_field_2")
    private String testField2;
}