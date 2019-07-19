package fr.milleis.morphit.annotations.field;

import fr.milleis.morphit.utils.JavaLang;

/**
 *
 * @author ajosse
 */
public @interface Types {

    Class value();

    Class target() default JavaLang.class;
}
