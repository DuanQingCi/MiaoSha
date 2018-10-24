package com.imooc.miaosha_01.controller;

import com.imooc.miaosha_01.dao.UserDao;
import com.imooc.miaosha_01.domain.User;
import com.imooc.miaosha_01.rabbitmq.MQSender;
import com.imooc.miaosha_01.redis.UserKey;
import com.imooc.miaosha_01.result.Result;
import com.imooc.miaosha_01.service.RedisService;
import com.imooc.miaosha_01.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/demo")
public class DemoController {
	@Autowired
	private MQSender sender;
	@Autowired
	private UserService userService;
	@Autowired
	private RedisService redisService;
	@RequestMapping("/thymeleaf")
	public String thymeleaf(Model model){
		model.addAttribute("msg","hello");
		return "hello";
	}

	@RequestMapping("/select")
	@ResponseBody
	public Result<User> select(){
		User user=userService.selectByid(1);
		return Result.success(user);
	}
	@RequestMapping("/dbtx")
	@ResponseBody
	public Result<Boolean> tx(User user){
		boolean insertinto = userService.insertinto(user);
		return Result.success(insertinto);
	}

	@RequestMapping("/redisget")
	@ResponseBody
	public Result<User> redisget(){
		User user = redisService.get(UserKey.getById,""+1,User.class);
		return Result.success(user);
	}

	@RequestMapping("/redisset")
	@ResponseBody
	public Result<Boolean> redisset(){
		User user=new User();
		user.setId(1);
		user.setName("1111");
		boolean result = redisService.set(UserKey.getById,""+1,user);
		//String res = redisService.get("key10",String.class);
		return Result.success(result);
	}

		@RequestMapping("/mq")
		@ResponseBody
		public Result<String> mq() {
		sender.send("hello,imooc");
		return Result.success("Hello，world");
		}
		@RequestMapping("/mq/topic")
		@ResponseBody
		public Result<String> topicmq() {
		sender.sendtopic("hello,imooc");
		return Result.success("Hello，world");
		}

		@RequestMapping("/mq/fanout")
		@ResponseBody
		public Result<String> fanoutmq() {
		sender.sendfanout("hello,imooc");
		return Result.success("Hello，world");
		}

		@RequestMapping("/mq/headers")
		@ResponseBody
		public Result<String> headersmq() {
		sender.sendheaders("hello,imooc");
		return Result.success("Hello，world");
		}
}