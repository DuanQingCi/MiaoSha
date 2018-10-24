package com.imooc.miaosha_01.service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.imooc.miaosha_01.dao.MiaoshaUserDao;
import com.imooc.miaosha_01.domain.MiaoshaUser;
import com.imooc.miaosha_01.exception.GlobalException;
import com.imooc.miaosha_01.redis.MiaoshaUserKey;
import com.imooc.miaosha_01.service.RedisService;
import com.imooc.miaosha_01.result.CodeMsg;
import com.imooc.miaosha_01.util.MD5Util;
import com.imooc.miaosha_01.util.UUIDUtil;
import com.imooc.miaosha_01.vo.LoginVo;

import java.util.UUID;

@Service
public class MiaoshaUserService {
	
	
	public static final String COOKI_NAME_TOKEN = "token";
	
	@Autowired
	MiaoshaUserDao miaoshaUserDao;
	
	@Autowired
	RedisService redisService;
	//改造此方法redis中存user对象
	public MiaoshaUser getById(long id) {
		//缓存取
		MiaoshaUser miaoshaUser = redisService.get(MiaoshaUserKey.getById, "" + id, MiaoshaUser.class);
		if (miaoshaUser!=null){
			return miaoshaUser;
		}
		//缓存没有，自取数据库取,然后加载到redis中
		miaoshaUser=miaoshaUserDao.getById(id);
		if (miaoshaUser!=null){
			redisService.set(MiaoshaUserKey.getById,""+id,miaoshaUser);
		}
		return miaoshaUser;
	}
	// http://blog.csdn.net/tTU1EvLDeLFq5btqiK/article/details/78693323
	public boolean updatePassword(String token, long id, String formPass) {
		//取user
		MiaoshaUser user = getById(id);
		if(user == null) {
			throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
		}
		//更新数据库
		MiaoshaUser toBeUpdate = new MiaoshaUser();
		toBeUpdate.setId(id);
		toBeUpdate.setPassword(MD5Util.formPassToDBPass(formPass, user.getSalt()));
		miaoshaUserDao.update(toBeUpdate);
		//处理缓存
		redisService.delete(MiaoshaUserKey.getById, ""+id);
		user.setPassword(toBeUpdate.getPassword());
		redisService.set(MiaoshaUserKey.token, token, user);
		return true;
	}

	public MiaoshaUser getByToken(HttpServletResponse response, String token) {
		if(StringUtils.isEmpty(token)) {
			return null;
		}
		MiaoshaUser user = redisService.get(MiaoshaUserKey.token, token, MiaoshaUser.class);
		//延长有效期(如果缓存中已经存在一个要保存30分钟的，那么再次访问时，再更新保存30分钟)
		if(user != null) {
			addCookie(response, token, user);
		}
		return user;
	}
	

	public boolean login(HttpServletResponse response, LoginVo loginVo) {
		if(loginVo == null) {
			throw new GlobalException(CodeMsg.SERVER_ERROR);
		}
		String mobile = loginVo.getMobile();
		String formPass = loginVo.getPassword();
		//判断手机号是否存在
		MiaoshaUser user = getById(Long.parseLong(mobile));
		if(user == null) {
			throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
		}
		//验证密码
		String dbPass = user.getPassword();
		String saltDB = user.getSalt();
		String calcPass = MD5Util.formPassToDBPass(formPass, saltDB);
		if(!calcPass.equals(dbPass)) {
			throw new GlobalException(CodeMsg.PASSWORD_ERROR);
		}
		//生成cookie
		String token=UUIDUtil.uuid();
		addCookie(response,token,user);
		return true;
	}
	
	private void addCookie(HttpServletResponse response, String token, MiaoshaUser user) {
		redisService.set(MiaoshaUserKey.token,token,user);
		Cookie cookie=new Cookie(COOKI_NAME_TOKEN,token);
		cookie.setMaxAge(MiaoshaUserKey.TOKEN_EXPIRE);
		cookie.setPath("/");
		response.addCookie(cookie);
	}

}
