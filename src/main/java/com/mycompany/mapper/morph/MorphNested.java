package com.mycompany.mapper.morph;

/**
 *
 * @author ajosse
 */
public @interface MorphNested {

    String source();

    Class sourceType();

    String target();

    Class targetType();

    MorphField[] fields();
}
