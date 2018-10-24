package com.imooc.miaosha_01.service;

import com.imooc.miaosha_01.dao.GoodsDao;
import com.imooc.miaosha_01.domain.Goods;
import com.imooc.miaosha_01.vo.GoodsVo;
import com.imooc.miaosha_01.vo.MiaoshaGoods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GoodsService {
	@Autowired
	private GoodsDao goodsDao;
	public List<GoodsVo> goodsVos(){
		return goodsDao.listGoodsVo();
	}

	public GoodsVo getGoodsVoById(long goodsId) {
		return  goodsDao.goodsVo(goodsId);
	}

	public boolean reduceStock(GoodsVo goods) {
		MiaoshaGoods g=new MiaoshaGoods();
		g.setGoodsId(goods.getId());
		int i = goodsDao.reduceStock(g);
		return i>0;
	}

	public void resetStock(List<GoodsVo> goodsList) {
		for(GoodsVo goods : goodsList ) {
			MiaoshaGoods g = new MiaoshaGoods();
			g.setGoodsId(goods.getId());
			g.setStockCount(goods.getStockCount());
			goodsDao.resetStock(g);
		}
	}

}
