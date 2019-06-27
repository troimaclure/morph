package com.mycompany.mapper.entity;

import java.util.Date;
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
public class PersonSecondNested {

    public String hello;
    public String yes;
    public Date good;
}
