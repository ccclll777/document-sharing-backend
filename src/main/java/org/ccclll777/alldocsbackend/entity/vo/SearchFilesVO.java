package org.ccclll777.alldocsbackend.entity.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SearchFilesVO {
    private String mongoFileId;
    private  String name;
    private String description;
}
