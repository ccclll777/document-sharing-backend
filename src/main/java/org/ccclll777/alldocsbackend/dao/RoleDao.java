package org.ccclll777.alldocsbackend.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.ccclll777.alldocsbackend.entity.Role;
import org.ccclll777.alldocsbackend.entity.User;

import java.util.List;
import java.util.Optional;

@Mapper
public interface RoleDao {

    Role selectRoleById(Integer roleId);
    Role selectRoleByName(String roleName);
    List<Role> selectRoleByUserId(Integer userId);
}
