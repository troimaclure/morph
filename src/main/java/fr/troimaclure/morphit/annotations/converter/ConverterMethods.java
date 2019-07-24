package fr.troimaclure.morphit.annotations.converter;

/**
 *
 * @author ajosse
 */
public @interface ConverterMethods {

    String value();

    String mirror() default "";
}
