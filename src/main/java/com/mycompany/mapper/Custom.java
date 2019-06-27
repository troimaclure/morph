package com.mycompany.mapper;

import com.mycompany.mapper.dto.PersonDTO;
import com.mycompany.mapper.dto.PersonDTONested;
import com.mycompany.mapper.dto.PersonDTOSecondNested;
import com.mycompany.mapper.entity.Person;
import com.mycompany.mapper.entity.PersonNested;
import com.mycompany.mapper.entity.PersonSecondNested;
import com.mycompany.mapper.morph.Morph;
import com.mycompany.mapper.morph.MorphField;
import com.mycompany.mapper.morph.MorphMethod;
import com.mycompany.mapper.morph.MorphNested;
import java.util.Date;

/**
 *
 * @author ajosse
 */
@Morph
public interface Custom {

    @MorphMethod(
            fields = {
                @MorphField(source = "lastname", target = "name")},
            nesteds = {
                @MorphNested(source = "nested",
                        target = "dto",
                        sourceType = PersonNested.class,
                        targetType = PersonDTONested.class,
                        fields = {
                            @MorphField(source = "score", target = "scoreDto")
                        })
                , 
                @MorphNested(source = "nested2", target = "dto2", sourceType = PersonSecondNested.class, targetType = PersonDTOSecondNested.class,
                        fields = {
                            @MorphField(source = "good", target = "goodString", converterType = DateUtil.class, converterMethod = "toStringDate")})
            }
    )
    PersonDTO morph(Person p);

    @MorphMethod(fields = {
        @MorphField(source = "name", target = "lastname")
    },
            nesteds = {
                @MorphNested(source = "dto",
                        target = "nested",
                        sourceType = PersonDTONested.class,
                        targetType = PersonNested.class,
                        fields = {
                            @MorphField(source = "scoreDto", target = "score")
                        })
            })
    Person morph(PersonDTO p);

}
