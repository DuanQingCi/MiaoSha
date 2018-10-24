package com.imooc.miaosha_01.dao;

import com.imooc.miaosha_01.domain.MiaoshaUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Component;


@Mapper
@Component
public interface MiaoshaUserDao {
	
	@Select("select * from miaosha_user where id = #{id}")
	public MiaoshaUser getById(@Param("id") long id);
	@Update("update miaosha_user set password=#{password} where id=#{id}")
	int update(MiaoshaUser toBeUpdate);
}
