package com.mycompany.mapper;

import com.mycompany.mapper.dto.PersonDTO;
import com.mycompany.mapper.entity.Person;
import com.mycompany.mapper.morph.Morph;
import com.mycompany.mapper.morph.MorphField;
import com.mycompany.mapper.morph.MorphMethod;

/**
 *
 * @author ajosse
 */
@Morph
public interface Custom  {

    @MorphMethod(fields = {
        @MorphField(source = "age", sourceType = Integer.class, target = "age", targetType = Integer.class)
        ,
        @MorphField(source = "name", sourceType = String.class, target = "name", targetType = String.class)
    })
    PersonDTO morph(Person p);

}
