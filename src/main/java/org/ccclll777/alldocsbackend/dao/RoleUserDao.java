package org.ccclll777.alldocsbackend.dao;

import org.apache.ibatis.annotations.Mapper;
import org.ccclll777.alldocsbackend.entity.Role;
import org.ccclll777.alldocsbackend.entity.RoleUser;

import java.util.List;

@Mapper
public interface RoleUserDao {

    List<RoleUser> selectRoleUserByUserId(Integer userId);
    int insertRoleUser(Integer userId, Integer roleId);
    int deleteRoleUser(Integer userId);
}
