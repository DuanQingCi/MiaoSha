package com.imooc.miaosha_01.controller;

import com.imooc.miaosha_01.domain.User;
import com.imooc.miaosha_01.result.CodeMsg;
import com.imooc.miaosha_01.result.Result;
import com.imooc.miaosha_01.service.MiaoshaUserService;
import com.imooc.miaosha_01.service.UserService;
import com.imooc.miaosha_01.vo.LoginVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Controller
@RequestMapping("/login")
public class LoginController {
	private static Logger logger=LoggerFactory.getLogger(LoginController.class);
	@Autowired
	MiaoshaUserService userService;
	@RequestMapping("/to_login")
	public String to_login(){
		return "login";
	}
	@RequestMapping("/do_login")
	@ResponseBody
	public Result<Boolean> do_login(HttpServletResponse response, @Valid LoginVo loginVo){
		logger.info(loginVo.toString());
		//登录
		userService.login(response, loginVo);
		return Result.success(true);
	}
}
