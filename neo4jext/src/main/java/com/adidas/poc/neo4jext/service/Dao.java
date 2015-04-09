package com.adidas.poc.neo4jext.service;

import java.util.Collection;

/**
 * Created by Oleh_Golovanov on 4/9/2015 for ADI-COM-trunk
 */
public interface Dao<I, T> {
    T create(T input);
    T update(T input);
    T delete(I id);
    T find(I id);
    Collection<T> findAll();
}
