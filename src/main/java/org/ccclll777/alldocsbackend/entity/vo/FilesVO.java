package org.ccclll777.alldocsbackend.entity.vo;

import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;
@Data
@Builder
public class FilesVO {
    private  Integer id;
    private  String name;
    private String suffix;
    private String categoryName;
    private List<String> tagNames;
    private String userName;
    private String fileState;
    private String errorMessage;
    private String reviewState;
    private String size;
    private String thumbId;
    private Date createTime;

}
