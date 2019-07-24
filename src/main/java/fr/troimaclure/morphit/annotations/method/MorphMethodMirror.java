package fr.troimaclure.morphit.annotations.method;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicate to your MorphProcessor extension this method has to be processed
 *
 * @author ajosse
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MorphMethodMirror {

    String value() default "";

}
