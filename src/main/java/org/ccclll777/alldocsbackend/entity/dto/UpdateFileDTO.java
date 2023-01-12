package org.ccclll777.alldocsbackend.entity.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateFileDTO {
    @NotNull()
    private Integer categoryId;

    private Integer fileId;
    private String name;
    private String description;

}
