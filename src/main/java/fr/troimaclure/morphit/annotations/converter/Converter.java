package fr.troimaclure.morphit.annotations.converter;

import fr.troimaclure.morphit.utils.JavaLang;

/**
 *
 * @author ajosse
 */
public @interface Converter {

    ConverterMethods methods() default @ConverterMethods("");

    Class type() default JavaLang.class;

    boolean morph() default false;
}
