package org.ccclll777.alldocsbackend.dao;

import org.apache.ibatis.annotations.Mapper;
import org.ccclll777.alldocsbackend.entity.Role;
import org.ccclll777.alldocsbackend.entity.RoleUser;

import java.util.List;

@Mapper
public interface RoleUserDao {
    /**
     * 查询用户拥有的角色
     * @param userId
     * @return
     */
    List<RoleUser> selectRoleUserByUserId(Integer userId);

    /**
     * 赋予用户一个角色
     * @param userId
     * @param roleId
     * @return
     */
    int insertRoleUser(Integer userId, Integer roleId);

    /**
     * 删除用户的角色
     * @param userId
     * @return
     */
    int deleteRoleUser(Integer userId);
}
