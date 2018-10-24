package com.imooc.miaosha_01.dao;

import com.imooc.miaosha_01.domain.Goods;
import com.imooc.miaosha_01.vo.GoodsVo;
import com.imooc.miaosha_01.vo.MiaoshaGoods;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface GoodsDao {
	//查询商品表和秒杀商品表的信息 两个表的信息合在GoodsVo里
	@Select("select g.*,mg.stock_count,mg.start_date,mg.end_date,mg.miaosha_price from miaosha_goods as mg left join goods as g on mg.goods_id=g.id")
	public List<GoodsVo>listGoodsVo();
	@Select("select g.*,mg.stock_count,mg.start_date,mg.end_date,mg.miaosha_price from miaosha_goods as mg left join goods as g on mg.goods_id=g.id where g.id=#{goodsId}")
	public GoodsVo goodsVo(@Param("goodsId") long goodsId);
	@Update("update miaosha_goods set stock_count=stock_count-1 where goods_id=#{goodsId} and stock_count>0")
	public int reduceStock(MiaoshaGoods g);

	@Update("update miaosha_goods set stock_count = #{stockCount} where goods_id = #{goodsId}")
	public int resetStock(MiaoshaGoods g);

}
