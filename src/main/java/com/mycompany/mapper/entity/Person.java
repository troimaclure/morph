package com.mycompany.mapper.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author ajosse
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Person {

    private int age;
    private String lastname;
    private PersonNested nested;
    private PersonSecondNested nested2;

}
