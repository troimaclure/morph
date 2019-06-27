package com.mycompany.mapper;

import com.mycompany.mapper.dto.PersonDTO;
import com.mycompany.mapper.entity.Person;
import com.mycompany.mapper.entity.PersonNested;
import com.mycompany.mapper.entity.PersonSecondNested;
import com.mycompany.mapper.morph.MorphBuilder;
import java.util.Date;

/**
 *
 * @author ajosse
 */
public class NewMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Custom get = MorphBuilder.get(Custom.class);
        PersonDTO morph = get.morph(new Person(5, "coucou", new PersonNested(7, 5, "hehe"), new PersonSecondNested("hello", "yes", new Date())));
        System.out.println(morph.toString());
        Person morph1 = get.morph(morph);
        System.out.println(morph1);
    }
}
