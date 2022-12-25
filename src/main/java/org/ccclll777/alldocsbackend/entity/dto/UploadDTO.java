package org.ccclll777.alldocsbackend.entity.dto;

import io.swagger.models.auth.In;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UploadDTO {
    @NotBlank
    private MultipartFile file;

    private Integer categoryId;
    private String description;
}
