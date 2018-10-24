package com.imooc.miaosha_01.service;

import com.alibaba.fastjson.JSON;
import com.imooc.miaosha_01.redis.KeyPrefix;
import com.imooc.miaosha_01.redis.RedisConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Service
public class RedisService {
	//通过jedisPool.getResource方法获取一个redis

	@Autowired
	private JedisPool jedisPool;

	/**
	 * 获得一个对象
	 * @param prefix
	 * @param key
	 * @param clazz
	 * @param <T>
	 * @return
	 */
	public <T> T get(KeyPrefix prefix, String key, Class<T> clazz){
		Jedis jedis=null;
		try {
			jedis=jedisPool.getResource();
			//通过jedis获得reids中的key，key取出来的值时一个bean，用方法进行转换
			String realKey=prefix.getPrefix()+key;
			String str = jedis.get(realKey);
			T t=stringsToBean(str,clazz);
			return t;
		}finally {
			returnToPool(jedis);
		}
	}

	/**
	 * 设置redis中的一个对象
	 * @param prefix
	 * @param key
	 * @param value
	 * @param <T>
	 * @return
	 */
	public <T> boolean set(KeyPrefix prefix,String key,T value){
		Jedis jedis=null;
		try {
			jedis=jedisPool.getResource();
			String str=beanToString(value);
			if (str==null||str.length()<=0){
				return false;
			}
			String realKey=prefix.getPrefix()+key;
			int seconed = prefix.expireSeconed();
			if (seconed<=0){
				jedis.set(realKey,str);
			}else {
				jedis.setex(realKey,seconed,str);
			}
			return true;
		}finally {
			returnToPool(jedis);
		}
	}

	public static  <T> String beanToString(T value) {
		if (value==null){
			return null;
		}else if (value==int.class || value==Integer.class){
			return ""+value;
		}else if (value==String.class){
			return (String)value;
		}else if (value==long.class || value==Long.class){
			return ""+value;
		}else {
			return JSON.toJSONString(value);
		}
	}
	@SuppressWarnings("unchecked")
	public static  <T> T stringsToBean(String str,Class<T> clazz) {
		if (str==null||str.length()<=0||clazz==null){
			return null;
		}else if (clazz==int.class || clazz==Integer.class){
			return (T)Integer.valueOf(str);
		}else if (clazz==String.class){
			return (T)str;
		}else if (clazz==long.class || clazz==Long.class){
			return (T)Long.valueOf(str);
		}else {
			return JSON.toJavaObject(JSON.parseObject(str),clazz);
		}
	}
	/**
	 * 删除
	 * */
	public boolean delete(KeyPrefix prefix, String key) {
		Jedis jedis = null;
		try {
			jedis =  jedisPool.getResource();
			//生成真正的key
			String realKey  = prefix.getPrefix() + key;
			long ret =  jedis.del(realKey);
			return ret > 0;
		}finally {
			returnToPool(jedis);
		}
	}
	public boolean delete(KeyPrefix prefix) {
		Jedis jedis = null;
		try {
			jedis =  jedisPool.getResource();
			//生成真正的key
			String realKey  = prefix.getPrefix();
			long ret =  jedis.del(realKey);
			return ret > 0;
		}finally {
			returnToPool(jedis);
		}
	}

	/**
	 * 是否存在key
	 * @param prefix
	 * @param key
	 * @param <T>
	 * @return
	 */
	public <T> Boolean exists(KeyPrefix prefix, String key){
		Jedis jedis=null;
		try {
			jedis=jedisPool.getResource();
			String realKey=prefix.getPrefix()+key;
			Boolean exists = jedis.exists(realKey);
			return exists;
		}finally {
			returnToPool(jedis);
		}
	}
//key自增
	public <T> Long incr(KeyPrefix prefix, String key){
		Jedis jedis=null;
		try {
			jedis=jedisPool.getResource();
			String realKey=prefix.getPrefix()+key;
			return jedis.incr(realKey);
		}finally {
			returnToPool(jedis);
		}
	}
//key自减
	public <T> Long decr(KeyPrefix prefix, String key){
		Jedis jedis=null;
		try {
			jedis=jedisPool.getResource();
			String realKey=prefix.getPrefix()+key;
			return jedis.decr(realKey);
		}finally {
			returnToPool(jedis);
		}
	}
	private void returnToPool(Jedis jedis) {
		if (jedisPool==null){
			jedis.close();//源码显示是返回掉，不是关掉
		}
	}
}
