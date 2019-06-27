package com.mycompany.mapper.dto;

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
public class PersonDTOSecondNested {

    public String hello;
    public String yes;
    public String goodString;
}
