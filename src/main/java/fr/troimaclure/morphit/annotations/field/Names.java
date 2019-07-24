package fr.troimaclure.morphit.annotations.field;

/**
 *
 * @author ajosse
 */
/**
 *
 * If target equals value , do not fill target
 */
public @interface Names {

    public String value();

    /**
     * Optional if equals value
     *
     * @return
     */
    public String target() default "";
}
