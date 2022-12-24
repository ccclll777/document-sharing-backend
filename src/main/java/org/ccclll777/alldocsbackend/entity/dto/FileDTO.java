package org.ccclll777.alldocsbackend.entity.dto;


import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;


@Data
public class FileDTO {
    @NotNull()
    private String filterWord;

    @NotNull()
    @Min(value = 1)
    private Integer page;

    @NotNull()
    @Min(value = 1)
    private Integer rows;

    @NotNull()
    private String categoryId;

    private String tagId;

}
