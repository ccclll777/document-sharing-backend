package org.ccclll777.alldocsbackend.entity;

import io.swagger.models.auth.In;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileTag {
    private Integer id;

    private Integer fileId;

    private Integer tagId;
}
