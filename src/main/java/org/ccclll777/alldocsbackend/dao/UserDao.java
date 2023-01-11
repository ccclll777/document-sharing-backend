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

    int insertUser(User user);
    List<User> selectUserList(Integer limit, Integer offset);

    int updateUser(User user);

    int deleteUser(Integer userId);
    int userCount();

}
