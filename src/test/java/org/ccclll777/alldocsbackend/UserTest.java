package org.ccclll777.alldocsbackend;

import org.ccclll777.alldocsbackend.dao.UserDao;
import org.ccclll777.alldocsbackend.entity.User;
import org.ccclll777.alldocsbackend.security.dto.LoginRequest;
import org.ccclll777.alldocsbackend.security.service.AuthUserService;
import org.ccclll777.alldocsbackend.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class UserTest {
    @Resource
    private UserDao userDao;
    @Resource
    private AuthUserService  authUserService;
    @Test
    public void getUser(){
        List<User> users = userDao.selectUserList(10,0);
        System.out.println(users.get(1).getUserName());
        System.out.println(users.get(0).getUserName());
    }
    @Test
    public void insertUser(){
       User user = User.builder().nickName("123234")
               .userName("213233").password("132234").build();
       System.out.println(user.getCreateTime());
        System.out.println(user.getUpdateTime());
       userDao.insertUser(user);
    }


}
