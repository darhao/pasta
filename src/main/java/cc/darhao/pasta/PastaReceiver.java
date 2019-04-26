package cc.darhao.pasta;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;

import javax.websocket.Session;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

/**
 * 报文接受者
 * <br>
 * <b>2019年4月9日</b>
 * @author <a href="https://github.com/darhao">鲁智深</a>
 */
class PastaReceiver {

	static void receiveMessage(Session session, String message) throws IOException {
		//解析报文数据
		PastaPackage pastaPackage = parseToPackage(message);
		//判断是否存在空字段
		checkNullField(pastaPackage);
		//方向判断
		PastaDirection direction = pastaPackage.getDirection();
		if(direction.equals(PastaDirection.Request)) {
			handleRequest(session, pastaPackage);
		}else{
			handleResponse(session, pastaPackage);
		}
	}


	private static void handleRequest(Session session, PastaPackage requestPackage) throws IOException{
		//记录下开始处理请求的时间
		Date requestTime = new Date();
		//分发请求，下达业务控制层进行实际处理，返回响应报文
		Object body = Pasta.filters.get(0).filter(session, requestPackage);
		PastaPackage responsePackage = new PastaPackage(requestPackage, body);
		//发送响应报文
		PastaSender.sendResponse(session, responsePackage);
		//生成请求响应对
		RequestResponsePair pair = new RequestResponsePair(requestTime, requestPackage, responsePackage, session, false);
		//触发日志捕获器
		Pasta.catcher.onResponseFinish(pair);
	}


	private static void handleResponse(Session session, PastaPackage responsePackage) {
		synchronized (Pasta.requestResponsePairSet) {
			//遍历RequestResponsePairSet中是否存在满足ID为id并且session为ws的元素？
			Iterator<RequestResponsePair> it = Pasta.requestResponsePairSet.iterator();
			while (it.hasNext()) {
				RequestResponsePair pair = it.next();
				int idInSet = pair.getRequestPackage().getId();
				int responseId = responsePackage.getId();
				Session sessionInSet = pair.getSession();
				if (idInSet == responseId && sessionInSet == session) {
					//触发日志捕获器
					Pasta.catcher.onRequestFinish(pair);
					//移除元素
					it.remove();
					//设置响应
					pair.setResponsePackage(responsePackage);
					synchronized (pair) {
						//唤醒线程
						pair.notify();
					}
					return;
				}
			}
		}
	}


	private static void checkNullField(PastaPackage pastaPackage) {
		if(pastaPackage.getId() == null) {
			throw new JSONException("报文ID不能为空");
		}else if(pastaPackage.getDirection() == null) {
			throw new JSONException("报文方向必须为\"Request\"或\"Response\"字符串");
		}else if(pastaPackage.getType() == null) {
			throw new JSONException("报文类型不能为空");
		}
	}


	private static PastaPackage parseToPackage(String message) {
		try {
			PastaPackage pastaPackage = JSON.parseObject(message, PastaPackage.class);
			if(pastaPackage.getBody() != null && pastaPackage.getBody() instanceof JSONObject == false) {
				throw new JSONException("无法解析JSON：" + message);
			}
			return pastaPackage;
		} catch (JSONException ignore) {
			throw new JSONException("无法解析JSON：" + message);
		}
	}
	
}
