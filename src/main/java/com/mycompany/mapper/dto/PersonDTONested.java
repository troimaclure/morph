package com.mycompany.mapper.dto;

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
public class PersonDTONested {

    public int scoreDto;
    public int same;
    public String mirror;
}
