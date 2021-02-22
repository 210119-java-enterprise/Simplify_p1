package com.revature.repositories;

import java.util.List;

public interface Crud<T> {
    void save(T newObj);
    List<T> findAll();
    T findById(int id);
}
