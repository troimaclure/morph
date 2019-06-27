package com.mycompany.mapper.morph;

import com.mycompany.mapper.utils.JavaLang;

/**
 *
 * @author ajosse
 */
public @interface MorphField {

    String source();

    Class sourceType() default JavaLang.class;

    String target();

    Class targetType() default JavaLang.class;

    Class converterType() default JavaLang.class;

    String converterMethod() default "";
}
