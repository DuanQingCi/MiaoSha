package com.imooc.miaosha_01.redis;

public  abstract class BasePrefix implements KeyPrefix{
	public int expireSeconed;
	public String prefix;

	public BasePrefix(int expireSeconed, String prefix) {
		this.expireSeconed = expireSeconed;
		this.prefix = prefix;
	}
	public BasePrefix(String Prefix){
		this(0,Prefix);
	}
	@Override
	public int expireSeconed() {
		//默认0永不过期
		return expireSeconed;
	}

	@Override
	public String getPrefix() {
		String className=getClass().getSimpleName();
		return className+":"+prefix;
	}
}
