package com.mycompany.mapper.dto;

import com.mycompany.mapper.entity.Person;

/**
 *
 * @author ajosse
 */
public class MapperDTO {

    public PersonDTO morph(Person p) {
        return PersonDTO.builder().age(p.getAge()).name(p.getName()).build();
//        Class c = Class.forName("zda"); 
//        Method method = c.getMethod(name, parameterTypes); 
//        method.i
    }
    
}
