package org.ccclll777.alldocsbackend.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Column;
import javax.validation.constraints.NotBlank;
import java.util.Date;

@Document
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class File {
    @Id
    private Integer id;
    //对应mongo中的gridfsId
    @NotBlank()
    private String gridfsId;
    private String name;
    private String contentType;
    private String suffix;
    private Date createTime = new Date();
    //是否审查，根据不同的权限确定
    //1为正在审查 0为审查完毕
    private Integer reviewing;
    //文档状态：（1）未索引 （2）已索引（3）已删除
    private Integer fileState;
    private Integer categoryId;
    private String errorMessage;
    private Integer userId;

    private Long size;
    /**
     * 预览图的GridFS的ID
     */
    private String thumbId;

    private String MongoFileId;

    private String md5;
}
