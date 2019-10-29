package cc.darhao.pasta;

import java.io.IOException;
import java.util.Date;

import javax.websocket.Session;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * 报文发送者
 * <br>
 * <b>2019年4月9日</b>
 * @author <a href="https://github.com/darhao">鲁智深</a>
 */
class PastaSender {
	
	static JSONObject sendRequest(Session session, PastaPackage requestPackage) throws IOException, TimeoutException {
		//构建请求响应对
		RequestResponsePair pair = new RequestResponsePair(new Date(), requestPackage, null, session, true);
		//把请求加入到请求堆中
		synchronized (Pasta.requestResponsePairSet) {
			Pasta.requestResponsePairSet.add(pair);
		}
		//发送请求报文数据
		session.getBasicRemote().sendText(JSON.toJSONString(requestPackage));
		//等待响应通知或超时通知
		synchronized (pair) {
			try {
				if(pair.getResponsePackage() == null) {
					pair.wait();
				}
			} catch (InterruptedException ignore) {
			}
		}
		//获取结果报文
		PastaPackage responsePackage = pair.getResponsePackage();
		//判断报文是否超时
		Object value = responsePackage.getBody();
		if(value != null) {
			if(value.equals("TIMEOUT")) {// 超时则抛出异常
				throw new TimeoutException("请求超时");
			}else { // 否则返回正常结果
				return (JSONObject)responsePackage.getBody();
			}
		}
		return null;
	}

	
	static void sendResponse(Session session, PastaPackage responsePackage) throws IOException {
		//发送响应报文数据
		String message = JSON.toJSONString(responsePackage);
		session.getBasicRemote().sendText(message);
	}
	
}
