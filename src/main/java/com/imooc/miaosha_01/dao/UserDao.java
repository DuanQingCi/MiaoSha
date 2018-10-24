package com.imooc.miaosha_01.dao;

import com.imooc.miaosha_01.domain.User;
import com.imooc.miaosha_01.result.Result;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

@Component
@Mapper
public interface UserDao {
	@Select("select * from user where id=#{id}")
	public User selectById(@Param("id") int id);

	@Insert("insert into user(id,name) values(#{id},#{name})")
	public int  insertinto(User user);
}
