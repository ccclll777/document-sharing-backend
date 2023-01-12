package org.ccclll777.alldocsbackend.dao;

import io.swagger.models.auth.In;
import org.apache.ibatis.annotations.Mapper;
import org.ccclll777.alldocsbackend.entity.User;

import java.util.List;
import java.util.Optional;

@Mapper
public interface UserDao {

    /**
     * 根据Id选择用户
     * @param id
     * @return
     */
    User selectUserById(Integer id);

    /*
       通过UserName查找用户
     */
    User selectUserByUserName(String userName);

    /**
     * 新增用户
     * @param user
     * @return
     */
    int insertUser(User user);

    /**
     * 查询用户列表
     * @param limit
     * @param offset
     * @return
     */
    List<User> selectUserList(Integer limit, Integer offset);

    /**
     * 更新用户信息
     * @param user
     * @return
     */
    int updateUser(User user);

    /**
     * 删除用户
     * @param userId
     * @return
     */
    int deleteUser(Integer userId);

    /**
     * 用户数量
     * @return
     */
    int userCount();

}
