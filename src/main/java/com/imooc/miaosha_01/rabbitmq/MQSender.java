package com.imooc.miaosha_01.rabbitmq;

import com.imooc.miaosha_01.service.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MQSender {
	private static Logger logger=LoggerFactory.getLogger(MQSender.class);
	@Autowired
	AmqpTemplate amqpTemplate;
		public void sendMiaoshaMessage(MiaoshaMessage message) {
			String msg = RedisService.beanToString(message);
			logger.info("send meg"+ msg);
			amqpTemplate.convertAndSend(MQConfig.MIAOSHA_QUEUE,msg);
	}

/*	public void send(Object message){
		String msg = RedisService.beanToString(message);
		logger.info("send meg"+ msg);
		amqpTemplate.convertAndSend(MQConfig.QUEUE,msg);
	}

	public void sendtopic(Object message){
		String msg = RedisService.beanToString(message);
		logger.info("send topic msg"+ msg);
		amqpTemplate.convertAndSend(MQConfig.TOPIC_EXCHANGE,MQConfig.ROUTION_KEY1,msg+"1");
		amqpTemplate.convertAndSend(MQConfig.TOPIC_EXCHANGE,MQConfig.ROUTION_KEY2,msg+"2");
	}

	public void sendfanout(Object message){
		String msg = RedisService.beanToString(message);
		logger.info("send fanout msg"+ msg);
		amqpTemplate.convertAndSend(MQConfig.FANOUT_EXCHANGE,"",msg+"1");
	}

	public void sendheaders(Object message){
		String msg = RedisService.beanToString(message);
		logger.info("send fanout msg"+ msg);
		MessageProperties properties=new MessageProperties();
		properties.setHeader("header1","value1");
		properties.setHeader("header2","value2");
		Message obj=new Message(msg.getBytes(),properties);
		amqpTemplate.convertAndSend(MQConfig.HEADERS_EXCHANGE,"",obj);
	}*/


}
