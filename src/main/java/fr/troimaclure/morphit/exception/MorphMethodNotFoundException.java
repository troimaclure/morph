package fr.troimaclure.morphit.exception;

/**
 *
 * @author ajosse
 */
public class MorphMethodNotFoundException extends Exception {

    public MorphMethodNotFoundException() {
        super("No MorphMethod root found, check your MorphOverride or MorphMirror");
    }

}
