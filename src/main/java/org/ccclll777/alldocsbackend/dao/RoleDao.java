package org.ccclll777.alldocsbackend.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.ccclll777.alldocsbackend.entity.Role;
import org.ccclll777.alldocsbackend.entity.User;

import java.util.List;
import java.util.Optional;

@Mapper
public interface RoleDao {
    /**
     * 查询角色信息
     * @param roleId
     * @return
     */
    Role selectRoleById(Integer roleId);

    /**
     * 根据角色名称查询角色信息
     * @param roleName
     * @return
     */
    Role selectRoleByName(String roleName);

    /**
     * 查询用户拥有的角色信息
     * @param userId
     * @return
     */
    List<Role> selectRoleByUserId(Integer userId);
}
