package com.imooc.miaosha_01.controller;

import com.imooc.miaosha_01.redis.GoodsKey;
import com.imooc.miaosha_01.result.Result;
import com.imooc.miaosha_01.service.GoodsService;
import com.imooc.miaosha_01.vo.GoodsDetailVo;
import com.imooc.miaosha_01.vo.GoodsVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.imooc.miaosha_01.domain.MiaoshaUser;
import com.imooc.miaosha_01.service.RedisService;
import com.imooc.miaosha_01.service.MiaoshaUserService;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.spring4.context.SpringWebContext;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/goods")
public class GoodsController {
	@Autowired
	MiaoshaUserService userService;
	@Autowired
	RedisService redisService;
	@Autowired
	private GoodsService goodsService;
	@Autowired
	ThymeleafViewResolver thymeleafViewResolver;
	@Autowired
	ApplicationContext applicationContext;

    @RequestMapping(value="/to_list",produces="text/html")
	@ResponseBody
    public String list(HttpServletRequest request, HttpServletResponse response,Model model, MiaoshaUser user) {
		model.addAttribute("user", user);
    	//如果页面有redis缓存，直接取
		String html = redisService.get(GoodsKey.getGoodsList, "", String.class);
		if (!StringUtils.isEmpty(html)){
			return html;
		}
		List<GoodsVo> goodsList = goodsService.goodsVos();
		model.addAttribute("goodsList", goodsList);

		//如果redis为空，就手动渲染通过thymeleaf模板
		SpringWebContext ctx=new SpringWebContext(request,response,request.getServletContext(),
				request.getLocale(),model.asMap(),applicationContext);
		html= thymeleafViewResolver.getTemplateEngine().process("goods_list", ctx);
		if (!StringUtils.isEmpty(html)){
			redisService.set(GoodsKey.getGoodsList, "", html);
		}
		return html;
    }

	@RequestMapping(value = "/to_detail/{goodsId}" ,produces="text/html")
	@ResponseBody
	public String goodsDetail(HttpServletRequest request, HttpServletResponse response,Model model, MiaoshaUser user,
							  @PathVariable("goodsId") long goodsId) {
		GoodsVo goods = goodsService.getGoodsVoById(goodsId);
		model.addAttribute("goods", goods);
		model.addAttribute("user", user);
		//如果页面有redis缓存，直接取
		String html = redisService.get(GoodsKey.getGoodsDetail, ""+goodsId, String.class);
		if (!StringUtils.isEmpty(html)){
			return html;
		}


		long startAt=goods.getStartDate().getTime();
		long endAt=goods.getEndDate().getTime();
		long now=System.currentTimeMillis();
		//记录商品状态
		int miaoshaStatus;
		//记录倒计时时时间
		int remainSeconds;
		if(now < startAt ) {//秒杀还没开始，倒计时
			miaoshaStatus = 0;
			remainSeconds = (int)((startAt - now )/1000);
		}else  if(now > endAt){//秒杀已经结束
			miaoshaStatus = 2;
			remainSeconds = -1;
		}else {//秒杀进行中
			miaoshaStatus = 1;
			remainSeconds = 0;
		}
		model.addAttribute("remainSeconds", remainSeconds);
		model.addAttribute("miaoshaStatus", miaoshaStatus);
		//如果redis为空，就手动渲染通过thymeleaf模板
		SpringWebContext ctx=new SpringWebContext(request,response,request.getServletContext(),
				request.getLocale(),model.asMap(),applicationContext);
		html= thymeleafViewResolver.getTemplateEngine().process("goods_detail", ctx);
		if (!StringUtils.isEmpty(html)){
			redisService.set(GoodsKey.getGoodsDetail, ""+goodsId,html);
		}
		return html;
		//return "goods_detail";
	}
}
