package com.imooc.miaosha_01.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Service
public class JedisPoolFactory {
	@Autowired
	private RedisConfig redisConfig;
	@Bean
	public JedisPool JedisFactory(){
		//配置jedisPool
		JedisPoolConfig jedisPoolConfig=new JedisPoolConfig();
		jedisPoolConfig.setMaxIdle(redisConfig.getPoolMaxIdle());
		jedisPoolConfig.setMaxTotal(redisConfig.getPoolMaxTotal());
		jedisPoolConfig.setMaxWaitMillis(redisConfig.getPoolMaxWait()*1000);//我们配置的秒，它是毫秒
		//配置jedispool中的参数返回我们所设置的jedis
		JedisPool jedisPool=new JedisPool(jedisPoolConfig,redisConfig.getHost(),redisConfig.getPort(),
				redisConfig.getTimeout()*1000,redisConfig.getPassword(),0);
		return jedisPool;
	}
}
