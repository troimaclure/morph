package fr.milleis.morphit.annotations.field;

import fr.milleis.morphit.annotations.converter.Converter;
import fr.milleis.morphit.utils.JavaLang;

/**
 * Simple case :<br>
 * Builder().{name.target}({convert.type}.{convert.value}({name.source}))<br><br><hr>
 * if {name.target}.contains('.') :<br>
 * returnType.set{name.target}[0]({type.target}.builder().{name.target}[1](parent.get{name.source}()).build())<br><br><hr>
 * if {name.source}.contains('.') :<br>
 * Builder.{name.target}({convert.type}({name.source}[0].get{name.source}[N]));<br><br><hr>
 * if {convert.morph} = true
 * Builder.{name.target}(MorphBuilder.get({type.target}.class).{convert.value}({name.source}))<br><br><hr>
 * converter is null , don't forget to put {type.source} and {type.target} that
 * not equals to java.lang.types (string , integer , boolean... )
 * <br><br><hr>
 * IF primitive boolean field , do not forget to set isPrimitiveBoolean to true
 * <br><br><hr>
 * if converter is present , type is not needed<br><br><hr>
 * <b>IMPORTANT</b><br>
 * target <b>OR</b> source can be dotted ('.')<br>
 * target support one nested <b>only</b> 'target.target'<br>
 * source can support N nested BUT ensure nothing is null<br>
 * if target or source contains dot, dont forget to complete sourceType or
 * targetType by the nested class type
 * <hr>
 *
 * @author ajosse
 */
public @interface MorphField {

    /**
     * target only if not equas to value
     *
     * @return
     */
    Names names();

    /**
     * Optional if converter is present
     *
     * @return
     */
    Types types() default @Types(JavaLang.class);

    Converter converter() default @Converter();

    /**
     * for get/is getter
     *
     * @return
     */
    boolean isPrimitiveBoolean() default false;

    /**
     * if field is an List class
     *
     * @return
     */
    public boolean list() default false;

}
