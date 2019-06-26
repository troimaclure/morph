package com.mycompany.mapper.entity;

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
public class PersonNested {

    public int score;
}
