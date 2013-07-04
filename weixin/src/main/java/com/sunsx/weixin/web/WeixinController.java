package com.sunsx.weixin.web;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Date;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.sunsx.weixin.utils.YunShiUtils;
/**
 * weixinapi
 * @author sunsx
 */
@Controller
public class WeixinController {

	private static final Logger logger = LoggerFactory.getLogger(WeixinController.class);
	private static final String TOKEN = "weixintoken";

	@RequestMapping(value = "/weixinapi/", method = RequestMethod.GET)
	public void weixinGet(HttpServletRequest request,HttpServletResponse response) throws IOException{
		PrintWriter out = response.getWriter();
		String signature = request.getParameter("signature");
		String timestamp = request.getParameter("timestamp");
		String nonce = request.getParameter("nonce");
		String echostr = request.getParameter("echostr");

		if(echostr==null||"".equals(echostr)){
			out.write("error");
			return;
		}
		logger.info(signature + " : " + timestamp + " : " + nonce + " : " + echostr);
		String[] tmpArr={TOKEN,timestamp,nonce};

		Arrays.sort(tmpArr);

		String tmpStr=arrayToString(tmpArr);

		tmpStr= SHA1Encode(tmpStr);

		if(tmpStr.equalsIgnoreCase(signature)){
			logger.info("sign success");
			out.write(echostr);
		}else{
			logger.info("sign error");
			out.write("error");
		}
	}



	public String arrayToString(String [] arr){  
		StringBuffer bf = new StringBuffer();  
		for(int i = 0; i < arr.length; i++){  
			bf.append(arr[i]);  
		}  
		return bf.toString();  
	}

	public String SHA1Encode(String sourceString) {  
		String resultString = null;  
		try {  
			resultString = new String(sourceString);  
			MessageDigest md = MessageDigest.getInstance("SHA-1");  
			resultString = byte2hexString(md.digest(resultString.getBytes()));  
		} catch (Exception ex) {  
		}  
		return resultString;  
	}

	public final String byte2hexString(byte[] bytes) {  
		StringBuffer buf = new StringBuffer(bytes.length * 2);  
		for (int i = 0; i < bytes.length; i++) {  
			if (((int) bytes[i] & 0xff) < 0x10) {  
				buf.append("0");  
			}  
			buf.append(Long.toString((int) bytes[i] & 0xff, 16));  
		}  
		return buf.toString().toUpperCase();  
	}

	public String readStreamParameter(ServletInputStream in){  
		StringBuilder buffer = new StringBuilder();  
		BufferedReader reader=null;  
		try{  
			reader = new BufferedReader(new InputStreamReader(in));  
			String line=null;  
			while((line = reader.readLine())!=null){  
				buffer.append(line);  
			}  
		}catch(Exception e){  
			e.printStackTrace();  
		}finally{  
			if(null!=reader){  
				try {  
					reader.close();  
				} catch (IOException e) {  
					e.printStackTrace();  
				}  
			}  
		}  
		return buffer.toString();  
	}

	@RequestMapping(value = "/weixinapi/", method = RequestMethod.POST)
	public void weixinPost(HttpServletRequest request,HttpServletResponse response) throws IOException{
		response.setContentType("application/xml");
		response.setCharacterEncoding("UTF-8");
		String postStr=null; 
		try{  
			postStr=this.readStreamParameter(request.getInputStream()); 
		}catch(Exception e){  
			e.printStackTrace();  
		}
		if (null!=postStr&&!postStr.isEmpty()){  
			Document document=null;  
			try{  
				document = DocumentHelper.parseText(postStr);  
			}catch(Exception e){  
				e.printStackTrace();  
			}  
			if(null==document){  
				response.getWriter().print("");  
				response.getWriter().flush();  
				response.getWriter().close();  
				return;  
			}  
			Element root=document.getRootElement();  
			String fromUsername = root.elementText("FromUserName");  
			String toUsername = root.elementText("ToUserName");  
			String keyword = root.elementTextTrim("Content");
			String msgType = root.elementTextTrim("MsgType");  

			String defaultTpl = "<xml>"+  
					"<ToUserName><![CDATA[%1$s]]></ToUserName>"+  
					"<FromUserName><![CDATA[%2$s]]></FromUserName>"+  
					"<CreateTime>%3$s</CreateTime>"+  
					"<MsgType><![CDATA[%4$s]]></MsgType>"+  
					"<Content><![CDATA[%5$s]]></Content>"+  
					"<FuncFlag>0</FuncFlag>"+  
					"</xml>";
			String defalutStr = "您好，请回复“星座名”如“处女座”可查询今日星座运势";
			String resultStr = "";
			if(msgType.equals("text")){  
				if(YunShiUtils.isStar(keyword)){
					String yunShiRet = YunShiUtils.getStarYunShi(keyword);
					if(yunShiRet==null||yunShiRet.trim().length()==0){
						String contentStr = "对不起，没有查到"+keyword+"今日运势";
						resultStr = String.format(defaultTpl, fromUsername, toUsername, new Date().getTime()+"","text", contentStr);
					}else {
						String yunShiTpl =  "<xml>"+  
								"<ToUserName><![CDATA[%1$s]]></ToUserName>"+  
								"<FromUserName><![CDATA[%2$s]]></FromUserName>"+  
								"<CreateTime>%3$s</CreateTime>"+  
								"<MsgType><![CDATA[news]]></MsgType>"+
								"<ArticleCount>1</ArticleCount>"+
								"<Articles>"+
								"<item>"+
								"<Title><![CDATA[%4$s]]></Title>"+
								"<Description><![CDATA[%5$s]]></Description>"+
								"<PicUrl><![CDATA[]]></PicUrl>"+  
								"<Url><![CDATA[%6$s]]></Url>"+
								"</item>"+
								"</Articles>"+
								"<FuncFlag>1</FuncFlag>"+
								"</xml>";
						String time = new Date().getTime()+"";
						resultStr = String.format(yunShiTpl, fromUsername, toUsername, time, keyword+"今日运势", yunShiRet,YunShiUtils.getYunShiWapUrl(keyword));
					}
				}
			}
			if(resultStr==null||resultStr.trim().length()==0){
				resultStr = String.format(defaultTpl, fromUsername, toUsername, new Date().getTime()+"","text", defalutStr);
			}
			response.getWriter().print(resultStr);  
			response.getWriter().flush();  
			response.getWriter().close();   
		} 
	}

}
