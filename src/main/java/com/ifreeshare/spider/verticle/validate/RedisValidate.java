package com.ifreeshare.spider.verticle.validate;

import java.net.MalformedURLException;

import com.ifreeshare.spider.http.HttpUtil;
import com.ifreeshare.spider.verticle.SpiderMainVerticle;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import io.vertx.core.json.JsonObject;

public class RedisValidate implements MainVerticleValidate {
	
	private static  JedisPool jedisPool = null;
	
	
	static{
		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxTotal(10);
		config.setMaxIdle(5);
		config.setMaxWaitMillis(1000l);
		config.setTestOnBorrow(false);
		jedisPool = new JedisPool(config, "127.0.0.1", 6379);
	}


	@Override
	public  boolean urlExist(String url) {
		
		boolean flag = false;
		Jedis jedis = null;
		try {
			String domain = HttpUtil.getDomain(url);
			if(SpiderMainVerticle.shieldBox.contains(domain)){
				return true;
			}
			jedis = jedisPool.getResource();
			flag = jedis.sismember(domain, url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}finally {
			if(jedis != null){
				jedis.close();
			}
		}
		return flag;
	}


	@Override
	public   boolean delUrl(String url) {
		boolean flag = false;
		Jedis jedis = null;
		try {
			String domain = HttpUtil.getDomain(url);
			jedis = jedisPool.getResource();
			jedis.srem(domain, url);
			flag = true;
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(jedis != null){
				jedis.close();
			}
		}
		return flag;
	}


	@Override
	public   boolean addOrUpdateUrl(String url, JsonObject info) {
		boolean flag = false;
		Jedis jedis = null;
		try {
			String domain = HttpUtil.getDomain(url);
			jedis = jedisPool.getResource();
			jedis.sadd(domain, url);
			flag = true;
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(jedis != null){
				jedis.close();
			}
		}
		return flag;
	}
	
	

}
