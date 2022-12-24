package org.ccclll777.alldocsbackend.entity.dto;

import io.swagger.models.auth.In;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDTO {

    private Integer id;
    @NotBlank(message = "")
    private String name;
    private String description;
    private Integer userId;

}
