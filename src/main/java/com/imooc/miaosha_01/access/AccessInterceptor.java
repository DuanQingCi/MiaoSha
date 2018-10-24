package com.imooc.miaosha_01.access;

import com.alibaba.fastjson.JSON;
import com.imooc.miaosha_01.domain.MiaoshaUser;
import com.imooc.miaosha_01.redis.AccessKey;
import com.imooc.miaosha_01.result.CodeMsg;
import com.imooc.miaosha_01.result.Result;
import com.imooc.miaosha_01.service.MiaoshaUserService;
import com.imooc.miaosha_01.service.RedisService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.HandlerMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;

@Service
public class AccessInterceptor extends HandlerInterceptorAdapter {
	@Autowired
	MiaoshaUserService userService;

	@Autowired
	RedisService redisService;

	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		if (handler instanceof HandlerMethod) {
			MiaoshaUser user=getUser(request,response);
			UserContext.setUser(user);//保存到Threadlocal中，多线程下线程安全

			HandlerMethod hm = (HandlerMethod) handler;
			AccessLimit accessLimit = hm.getMethodAnnotation(AccessLimit.class);
			if (accessLimit == null) {
				return true;
			}
			int seconds = accessLimit.seconds();
			int maxCount = accessLimit.maxCount();
			boolean needLogin = accessLimit.needLogin();

		String key=request.getRequestURI();
		if(needLogin) {
			if(user == null) {
				render(response, CodeMsg.SESSION_ERROR);
				return false;
			}
			key += "_" + user.getId();
		}else {
			//do nothing
		}
		AccessKey ak = AccessKey.withExpire(seconds);
		Integer count = redisService.get(ak, key, Integer.class);
		if(count  == null) {
			redisService.set(ak, key, 1);
		}else if(count < maxCount) {
			redisService.incr(ak, key);
		}else {
			render(response, CodeMsg.SERVER_ERROR);
			return false;
		}
	}
		return true;
	}

	private void render(HttpServletResponse response, CodeMsg cm)throws Exception {
		response.setContentType("application/json;charset=UTF-8");
		OutputStream out = response.getOutputStream();
		String str  = JSON.toJSONString(Result.error(cm));
		out.write(str.getBytes("UTF-8"));
		out.flush();
		out.close();
	}

	private MiaoshaUser getUser(HttpServletRequest request, HttpServletResponse response) {
		String paramToken = request.getParameter(MiaoshaUserService.COOKI_NAME_TOKEN);
		String cookieToken = getCookieValue(request, MiaoshaUserService.COOKI_NAME_TOKEN);
		if(StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken)) {
			return null;
		}
		String token = StringUtils.isEmpty(paramToken)?cookieToken:paramToken;
		return userService.getByToken(response, token);
	}

	private String getCookieValue(HttpServletRequest request, String cookiName) {
		Cookie[]  cookies = request.getCookies();
		if(cookies == null || cookies.length <= 0){
			return null;
		}
		for(Cookie cookie : cookies) {
			if(cookie.getName().equals(cookiName)) {
				return cookie.getValue();
			}
		}
		return null;
	}

}
