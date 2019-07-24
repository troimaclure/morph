package fr.troimaclure.morphit.annotations.field;

import fr.troimaclure.morphit.utils.JavaLang;

/**
 *
 * @author ajosse
 */
public @interface Types {

    Class value();

    Class target() default JavaLang.class;
}
