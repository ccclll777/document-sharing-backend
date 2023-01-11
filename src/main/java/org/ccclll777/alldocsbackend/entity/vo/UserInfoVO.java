package org.ccclll777.alldocsbackend.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ccclll777.alldocsbackend.entity.User;

import javax.validation.constraints.NotBlank;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserInfoVO  {
    private Integer id;
    private String userName;
    private String nickName;
    private String phone;
    private String email;
    private Integer role;
    private Integer status;
    private Date createTime;
    private Date updateTime;
}
