package com.sunsx.weixin.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
/**
 * 星运查询工具类
 * @author sunsx
 */
public class YunShiUtils {
	
	private static Map<String, String> starsMap = new HashMap<String, String>();
	private static Map<String, String> starsNumMap = new HashMap<String, String>();
	private static Map<String, String> yunshiMap = new HashMap<String, String>();
	static{
		
		starsMap.put("白羊座", "aries");
		starsMap.put("金牛座", "taurus");
		starsMap.put("双子座", "gemini");
		starsMap.put("巨蟹座", "cancer");
		starsMap.put("狮子座", "leo");
		starsMap.put("处女座", "virgo");
		starsMap.put("天秤座", "libra");
		starsMap.put("天蝎座", "scorpio");
		starsMap.put("射手座", "sagittarius");
		starsMap.put("摩羯座", "capricorn");
		starsMap.put("水瓶座", "aquarius");
		starsMap.put("双鱼座", "pisces");
		
		starsNumMap.put("白羊座", "1");
		starsNumMap.put("金牛座", "2");
		starsNumMap.put("双子座", "3");
		starsNumMap.put("巨蟹座", "4");
		starsNumMap.put("狮子座", "5");
		starsNumMap.put("处女座", "6");
		starsNumMap.put("天秤座", "7");
		starsNumMap.put("天蝎座", "8");
		starsNumMap.put("射手座", "9");
		starsNumMap.put("摩羯座", "10");
		starsNumMap.put("水瓶座", "11");
		starsNumMap.put("双鱼座", "12");
	}
	
	public void resetYunShi(){
		synchronized (yunshiMap) {
			yunshiMap.clear();
		}
	}
	
	public static boolean isStar(String starName){
		if(starsMap.containsKey(starName)){
			return true;
		}
		return false;
	}
	
	public static String getStarYunShi(String starKey){
		String result = yunshiMap.get(starKey);
		if(result==null||result.equals("")){
			result =catchYunShi(starKey);
			if(result.length()>0){
				synchronized (yunshiMap) {
					yunshiMap.put(starKey, result);
				}
			}
		}
		return result;
	}
	
	public static String getYunShiWapUrl(String starKey){
		String num = starsNumMap.get(starKey);
		return "http://dp.sina.cn/dpool/astro/starent/starent.php?type=day&ast="+num;
	}
	
	public static String catchYunShi(String starKey){
		String code = starsMap.get(starKey);
		String result = "";
		try {
			Document doc = Jsoup.connect("http://vip.astro.sina.com.cn/astro/view/"+code+"/day").get();
			Elements tabs = doc.select("div.lotstars > div.tab");
			for(Element tab : tabs){
				String title = tab.select("h4").text().replace(" ", "");
				if(title.trim().equals("")){
					continue;
				}
				Elements p = tab.select("p");
				String text = p.text();
				if(text.trim().equals("")){
					text = "";
					int starz = p.select("img").size();
					for(int i = 0;i<starz;i++){
						text=text+"★";
					}
				}
				if(text.trim().equals("")){
					continue;
				}
				result= result+(title+"："+text)+"\n";
			}
			Elements conts = doc.select("div.lotconts");
			result=result+"整体运势："+conts.text();
			return result;
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
	}
	
}
