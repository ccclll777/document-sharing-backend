package org.ccclll777.alldocsbackend.entity.dto;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TagDTO {

    private Integer id;
    @NotBlank(message = "")
    private String name;
    private String description;
    private Integer userId;
    @Override
    public String toString () {
        return JSON.toJSONString(this);
    }

}
