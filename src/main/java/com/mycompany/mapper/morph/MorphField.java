package com.mycompany.mapper.morph;

/**
 *
 * @author ajosse
 */
public @interface MorphField {

    String source();

    Class sourceType();

    String target();

    Class targetType();
}
