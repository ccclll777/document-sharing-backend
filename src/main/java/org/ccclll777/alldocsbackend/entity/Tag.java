package org.ccclll777.alldocsbackend.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import javax.validation.constraints.NotBlank;
import java.util.Date;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Tag extends AbstractAuditBase{

    @Id
    private Integer id;
    @NotBlank(message = "")
    private String name;
    private String description;
    private Integer userId;

}
