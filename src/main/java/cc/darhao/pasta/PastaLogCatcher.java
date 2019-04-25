package cc.darhao.pasta;

/**
 * 报文日志捕获器抽象类<br>
 * 线程安全级别：不可变
 * <br>
 * <b>2019年4月23日</b>
 * @author <a href="https://github.com/darhao">鲁智深</a>
 */
public abstract class PastaLogCatcher {

	/**
	 * 该方法会在我方请求被对方成功响应时调用
	 * @param info 请求-响应对信息
	 */
	public abstract void onRequestFinish(RequestResponsePair info);
	
	
	/**
	 * 该方法会在我方请求超时时调用
	 * @param info 请求-响应对信息
	 */
	public abstract void onRequestTimeout(RequestResponsePair info);
	
	
	/**
	 * 该方法会在对方请求被我方成功响应时调用
	 * @param info 请求-响应对信息
	 */
	public abstract void onResponseFinish(RequestResponsePair info);
	
}
