package com.mycompany.mapper;

/**
 *
 * @author ajosse
 * @param <T>
 * @param <E>
 */
public class Couple<T, E> {

    T t;
    E e;

    public Couple(T t, E e) {
        this.t = t;
        this.e = e;
    }

}
