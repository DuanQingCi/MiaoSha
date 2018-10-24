package com.imooc.miaosha_01.controller;

import com.imooc.miaosha_01.domain.MiaoshaUser;
import com.imooc.miaosha_01.rabbitmq.MQSender;
import com.imooc.miaosha_01.rabbitmq.MiaoshaMessage;
import com.imooc.miaosha_01.redis.AccessKey;
import com.imooc.miaosha_01.redis.GoodsKey;
import com.imooc.miaosha_01.redis.MiaoshaKey;
import com.imooc.miaosha_01.redis.OrderKey;
import com.imooc.miaosha_01.result.CodeMsg;
import com.imooc.miaosha_01.result.Result;
import com.imooc.miaosha_01.service.*;
import com.imooc.miaosha_01.util.MD5Util;
import com.imooc.miaosha_01.util.UUIDUtil;
import com.imooc.miaosha_01.vo.GoodsVo;
import com.imooc.miaosha_01.vo.MiaoshaOrder;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;

@Controller
@RequestMapping("/miaosha")
public class MiaoShaController implements InitializingBean {

	@Autowired
	MiaoshaUserService userService;
	@Autowired
	RedisService redisService;
	@Autowired
	OrderService orderService;
	@Autowired
	private GoodsService goodsService;
	@Autowired
	MiaoshaService miaoshaService;
	@Autowired
	MQSender sender;
	//一个系统初始化方法
	private HashMap<Long, Boolean> localOverMap = new HashMap<Long, Boolean>();

	@Override
	public void afterPropertiesSet() throws Exception {
		List<GoodsVo> goodsVoList = goodsService.goodsVos();
		if (goodsVoList == null) {
			return;
		}
		for (GoodsVo goods : goodsVoList) {
			redisService.set(GoodsKey.getMiaoshaGoods, "" + goods.getId(), goods.getStockCount());
			localOverMap.put(goods.getId(), false);
		}
	}

	@RequestMapping(value = "/reset", method = RequestMethod.GET)
	@ResponseBody
	public Result<Boolean> reset(Model model) {
		List<GoodsVo> goodsList = goodsService.goodsVos();
		for (GoodsVo goods : goodsList) {
			goods.setStockCount(10);
			redisService.set(GoodsKey.getMiaoshaGoods, "" + goods.getId(), 10);
			localOverMap.put(goods.getId(), false);
		}
		redisService.delete(OrderKey.getMiaoshaOrderByUidGid);
		redisService.delete(MiaoshaKey.isGoodsOver);
		miaoshaService.reset(goodsList);
		return Result.success(true);
	}

	@RequestMapping("/{path}/do_miaosha")
	@ResponseBody
	public Result<Integer> list(Model model, MiaoshaUser user,
								@RequestParam("goodsId") long goodsId,
								@PathVariable("path") String path) {
		GoodsVo goods = goodsService.getGoodsVoById(goodsId);
		model.addAttribute("user", user);
		if (user == null) {
			return Result.error(CodeMsg.SESSION_ERROR);
		}
		//验证path
		boolean check = miaoshaService.checkPath(user, goodsId, path);
		if(!check){
			return Result.error(CodeMsg.SERVER_ERROR);
		}
		//减库存（redis中存的库存）
		Long decr = redisService.decr(GoodsKey.getMiaoshaGoods, "" + goods.getId());
		if (decr < 0) {
			return Result.error(CodeMsg.MIAO_SHA_OVER);
		}
		//判断是否秒杀到
		MiaoshaOrder miaoshaOrder = orderService.getMiaoShaByUserIdGoodsId(user.getId(), goodsId);
		if (miaoshaOrder != null) {
			model.addAttribute("errMsg", CodeMsg.REPEATE_MIAOSHA.getMsg());
			return Result.error(CodeMsg.REPEATE_MIAOSHA);
		}
		//入队，进行下订单的操作
		MiaoshaMessage miaoshaMessage = new MiaoshaMessage();
		miaoshaMessage.setUser(user);
		miaoshaMessage.setGoodsId(goodsId);
		sender.sendMiaoshaMessage(miaoshaMessage);
		return Result.success(0);
		//
		/*//检查库存，是否秒杀完成
		int stock=goods.getStockCount();
		if (stock<=0){
			model.addAttribute("errMsg",CodeMsg.MIAO_SHA_OVER.getMsg());
			return "miaosha_fail";
		}
		//判断这个用户是否已经秒杀到（防止秒杀两件）通过它的秒杀订单查看
		MiaoshaOrder miaoshaOrder=orderService.getMiaoShaByUserIdGoodsId(user.getId(),goodsId);
		if (miaoshaOrder!=null){
			model.addAttribute("errMsg",CodeMsg.REPEATE_MIAOSHA.getMsg());
			return "miaosha_fail";
		}
		//秒杀成功进行，减库存，下订单，写入秒杀订单（一个事务当中原子性）
		OrderInfo orderInfo=miaoshaService.miaosha(user,goods);
		model.addAttribute("orderInfo",orderInfo);
		model.addAttribute("goods",goods);
		return "order_detail";*/
	}

	@RequestMapping(value = "/result", method = RequestMethod.GET)
	@ResponseBody
	public Result<Long> miaoshaResult(Model model, MiaoshaUser user,
									  @RequestParam("goodsId") long goodsId) {
		model.addAttribute("user", user);
		if (user == null) {
			return Result.error(CodeMsg.SESSION_ERROR);
		}
		long result = miaoshaService.getMiaoshaResult(user.getId(), goodsId);
		return Result.success(result);
	}

	@RequestMapping(value = "/path", method = RequestMethod.GET)
	@ResponseBody
	public Result<String> getMiaoshaPath(HttpServletRequest request,Model model, MiaoshaUser user,
										 @RequestParam("goodsId") long goodsId,
										 @RequestParam(value = "verifyCode",defaultValue = "0")int verifyCode) {
		GoodsVo goods = goodsService.getGoodsVoById(goodsId);
		model.addAttribute("user", user);
		if (user == null) {
			return Result.error(CodeMsg.SESSION_ERROR);
		}
		//接口防刷限流
		StringBuffer url = request.getRequestURL();
		String key=url+"_"+user.getId();
		Integer integer = redisService.get(AccessKey.access, key, Integer.class);
		if (integer==null){
			redisService.set(AccessKey.access, key, 1);
		}else if (integer<5){
			redisService.incr(AccessKey.access, key);
		}else {
			return Result.error(CodeMsg.SERVER_ERROR);
		}

		boolean check = miaoshaService.checkVerifyCode(user, goodsId, verifyCode);
		if(!check) {
			return Result.error(CodeMsg.SERVER_ERROR);
		}
		String path=miaoshaService.createMiaoshaPath(user,goodsId);
		return Result.success(path);
	}

	@RequestMapping(value = "/verifyCode", method = RequestMethod.GET)
	@ResponseBody
	public Result<String> getMiaoshaVerifyCode(HttpServletResponse response,Model model, MiaoshaUser user,
											   @RequestParam("goodsId") long goodsId) {
		GoodsVo goods = goodsService.getGoodsVoById(goodsId);
		model.addAttribute("user", user);
		if (user == null) {
			return Result.error(CodeMsg.SESSION_ERROR);
		}
		try {
			BufferedImage image  = miaoshaService.createVerifyCode(user, goodsId);
			OutputStream out = response.getOutputStream();
			ImageIO.write(image, "JPEG", out);
			out.flush();
			out.close();
			return null;
		}catch(Exception e) {
			e.printStackTrace();
			return Result.error(CodeMsg.SERVER_ERROR);
		}
	}
}


