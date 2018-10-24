package com.imooc.miaosha_01.rabbitmq;


import com.imooc.miaosha_01.domain.MiaoshaUser;
import com.imooc.miaosha_01.service.GoodsService;
import com.imooc.miaosha_01.service.MiaoshaService;
import com.imooc.miaosha_01.service.OrderService;
import com.imooc.miaosha_01.service.RedisService;
import com.imooc.miaosha_01.vo.GoodsVo;
import com.imooc.miaosha_01.vo.MiaoshaOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MQReciver {
	@Autowired
	RedisService redisService;

	@Autowired
	GoodsService goodsService;

	@Autowired
	OrderService orderService;

	@Autowired
	MiaoshaService miaoshaService;
	private static Logger logger=LoggerFactory.getLogger(MQReciver.class);
	@RabbitListener(queues = MQConfig.MIAOSHA_QUEUE)
	public void reciver(String message){
		logger.info("recive meg"+ message);
		MiaoshaMessage message1 = RedisService.stringsToBean(message, MiaoshaMessage.class);
		MiaoshaUser user = message1.getUser();
		long goodsId = message1.getGoodsId();

		GoodsVo goods = goodsService.getGoodsVoById(goodsId);
		int stock = goods.getStockCount();
			if(stock <= 0) {
		return;
		}
		//判断是否已经秒杀到了
		MiaoshaOrder order = orderService.getMiaoShaByUserIdGoodsId(user.getId(), goodsId);
			if(order != null) {
		return;
		}
	//减库存 下订单 写入秒杀订单
	    	miaoshaService.miaosha(user, goods);
}

/*	@RabbitListener(queues = MQConfig.QUEUE)
	public void reciver(String message){
		logger.info("recive meg"+ message);
	}

	@RabbitListener(queues = MQConfig.TOPIC_QUEUE1)
	public void recivertopic1(String message){
		logger.info("recive topic1meg"+ message);
	}

	@RabbitListener(queues = MQConfig.TOPIC_QUEUE2)
	public void recivertopic2(String message){
		logger.info("recive topic2meg"+ message);
	}

	@RabbitListener(queues = MQConfig.FANOUT_EXCHANGE)
	public void reciverfanout(String message){
		logger.info("recive fan meg"+ message);
	}
	@RabbitListener(queues = MQConfig.HEADER_QUEUE)
	public void reciverheader(byte[] message){
		logger.info("recive header queue msg"+ new String(message));
	}*/
}
