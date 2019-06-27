package com.mycompany.mapper;

import com.mycompany.mapper.dto.PersonDTO;
import com.mycompany.mapper.dto.PersonDTONested;
import com.mycompany.mapper.entity.Person;
import com.mycompany.mapper.entity.PersonNested;
import com.mycompany.mapper.morph.Morph;
import com.mycompany.mapper.morph.MorphField;
import com.mycompany.mapper.morph.MorphMethod;
import com.mycompany.mapper.morph.MorphNested;

/**
 *
 * @author ajosse
 */
@Morph
public interface Custom {

    @MorphMethod(
            fields = {
                @MorphField(source = "lastname", sourceType = String.class, target = "name", targetType = String.class)
            },
            nesteds = {
                @MorphNested(source = "nested",
                        target = "dto",
                        sourceType = PersonNested.class,
                        targetType = PersonDTONested.class,
                        fields = {
                            @MorphField(source = "score", target = "scoreDto", sourceType = Integer.class, targetType = Integer.class)
                        })
            }
    )
    PersonDTO morph(Person p);

    @MorphMethod(fields = {
        @MorphField(source = "name", sourceType = String.class, target = "lastname", targetType = String.class)
    },
            nesteds = {
                @MorphNested(source = "dto",
                        target = "nested",
                        sourceType = PersonDTONested.class,
                        targetType = PersonNested.class,
                        fields = {
                            @MorphField(source = "scoreDto", target = "score", sourceType = Integer.class, targetType = Integer.class)
                        })
            })
    Person morph(PersonDTO p);

}
