package com.imooc.miaosha_01.rabbitmq;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;


@Configuration
public class MQConfig {
	public static final String MIAOSHA_QUEUE = "miaosha.queue";
	public static final String QUEUE = "queue";
	public static final String TOPIC_QUEUE1 = "topic.queue1";
	public static final String TOPIC_QUEUE2 = "topic.queue2";
	public static final String HEADER_QUEUE = "header.queue";
	public static final String TOPIC_EXCHANGE = "topicExchage";
	public static final String FANOUT_EXCHANGE = "fanoutxchage";
	public static final String HEADERS_EXCHANGE = "headersExchage";
	public static final String ROUTION_KEY1="topic.key1";
	public static final String ROUTION_KEY2="topic.#";
	@Bean
	public Queue queue() {
		//import org.springframework.amqp.core.Queue;
		return new Queue(QUEUE,true);
	}
	@Bean
	public Queue topicQueue1(){
		return new Queue(TOPIC_QUEUE1,true);
	}
	@Bean
	public Queue topicQueue2(){
		return new Queue(TOPIC_QUEUE2,true);
	}
	@Bean
	public TopicExchange topicExchange(){
		return new TopicExchange(TOPIC_EXCHANGE);
	}
	@Bean
	public Binding topicBinding1(){
		return BindingBuilder.bind(topicQueue1()).to(topicExchange()).with(ROUTION_KEY1);
	}
	@Bean
	public Binding topicBinding2(){
		return BindingBuilder.bind(topicQueue2()).to(topicExchange()).with(ROUTION_KEY2);
	}

	@Bean
	public FanoutExchange FanoutExchange(){
		return new FanoutExchange(FANOUT_EXCHANGE);
	}
	@Bean
	public Binding FanoutBinding1(){
		return BindingBuilder.bind(topicQueue1()).to(FanoutExchange());
	}
	@Bean
	public Binding FanoutBinding2(){
		return BindingBuilder.bind(topicQueue2()).to(FanoutExchange());
	}

	@Bean
	public HeadersExchange HeadersExchange(){
		return new HeadersExchange(HEADERS_EXCHANGE);
	}
	@Bean
	public Binding HeadersBinding(){
		Map<String,Object> map=new HashMap<String, Object>();
		map.put("head1","value1");
		map.put("head2","value2");
		return BindingBuilder.bind(topicQueue2()).to(HeadersExchange()).whereAll(map).match();
	}
}
