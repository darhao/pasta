package cc.darhao.pasta;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;

/**
 * 超时请求检测定时器 <br>
 * <b>2019年4月9日</b>
 * @author <a href="https://github.com/darhao">鲁智深</a>
 */
class RequestTimeoutChecker extends Thread {

	private Map<String, Long> typeTimeoutMap;
	
	
	RequestTimeoutChecker(Map<String, Long> typeTimeoutMap) {
		this.typeTimeoutMap = typeTimeoutMap;
	}
	
	
	Map<String, Long> getTypeTimeoutMap() {
		return typeTimeoutMap;
	}
	
	
	@Override
	public void run() {
		try {
			while (true) {
				handleTimeout();
				sleep(1000);// 周期为1秒
			}
		} catch (InterruptedException ignore) {
		}
	}

	
	private void handleTimeout() {
		synchronized (Pasta.requestResponsePairSet) {
			// 遍历RequestResponsePairSet中是否存在超时的请求？（根据类型-超时表判断）
			Iterator<RequestResponsePair> it = Pasta.requestResponsePairSet.iterator();
			while (it.hasNext()) {
				RequestResponsePair pair = it.next();
				String typeName = pair.getRequestPackage().getType();
				long timeoutTime =  typeTimeoutMap.get(typeName);
				long requestTime = pair.getRequestTime().getTime();
				long nowTime = new Date().getTime();
				if (nowTime - requestTime > timeoutTime) {
					// 移除元素
					it.remove();
					// 设置超时特殊响应
					PastaPackage responsePackage = new PastaPackage(pair.getRequestPackage(), "TIMEOUT");
					pair.setResponsePackage(responsePackage);
					// 触发日志捕获器
					Pasta.catcher.onRequestTimeout(pair);
					synchronized (pair) {
						// 唤醒线程
						pair.notify();
					}
				}
			}
		}
	}

}
