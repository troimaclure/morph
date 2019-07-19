package fr.milleis.morphit.annotations.converter;

import fr.milleis.morphit.utils.JavaLang;

/**
 *
 * @author ajosse
 */
public @interface Converter {

    ConverterMethods methods() default @ConverterMethods("");

    Class type() default JavaLang.class;

    boolean morph() default false;
}
