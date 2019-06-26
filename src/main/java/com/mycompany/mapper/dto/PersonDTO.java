package com.mycompany.mapper.dto;

import com.mycompany.mapper.entity.PersonNested;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author ajosse
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PersonDTO {

    private int age;
    private String name;
    private PersonDTONested dto;

}
