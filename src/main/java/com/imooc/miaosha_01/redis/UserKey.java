package com.imooc.miaosha_01.redis;

public class UserKey extends BasePrefix {
	private UserKey(String Prefix) {
		super(Prefix);
	}
	public static UserKey getById=new UserKey("id");
	public static UserKey getByName=new UserKey("name");
}
