package com.imooc.miaosha_01.service;

import com.imooc.miaosha_01.dao.UserDao;
import com.imooc.miaosha_01.domain.User;
import com.imooc.miaosha_01.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
	@Autowired
	private UserDao userDao;
	public User selectByid(int id){
		User user = userDao.selectById(1);
		return user;
	}
	public boolean insertinto(User user){
		User u1=new User();
		u1.setId(2);
		u1.setName("lili");
		int insertinto = userDao.insertinto(u1);
		return true;
	}
}
