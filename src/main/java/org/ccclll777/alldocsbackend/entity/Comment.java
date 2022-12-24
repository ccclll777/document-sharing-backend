package org.ccclll777.alldocsbackend.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ccclll777.alldocsbackend.enums.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Id;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Comment extends AbstractAuditBase {

    @Id
    private Integer id;

    private Integer userId;
    @NotNull
    private String fileId;
    @NotBlank()
    @Size(min = 1, max = 140)
    private String content;

}
