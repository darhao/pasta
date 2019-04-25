package cc.darhao.pasta;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import javax.websocket.Session;

import com.alibaba.fastjson.JSONObject;

/**
 * 该类是一个公共API入口类，所有导出的方法该类的静态内部类中声明<br>
 * 该类所有方法的参数，没有特别说明的情况下，都不能为空，否则会抛出{@link NullPointeException}<br>
 * 线程安全级别：不安全
 * <br>
 * <b>2019年4月23日</b>
 * @author <a href="https://github.com/darhao">鲁智深</a>
 */
public class Pasta {
	
	static final List<PastaFilter> filters = new ArrayList<>();
	
	static final Map<String, Class> routerMap = new HashMap<>();
	
	static PastaLogCatcher catcher = null;
	
	static RequestTimeoutChecker checker = null;
	
	static Set<RequestResponsePair> requestResponsePairSet = new HashSet<>();
	
	
	static {
		//添加空拦截器
		filters.add(new EmptyPastaFilter());
	}
	
	
	/**
	 * 将指定的请求类型与指定的处理器类绑定，请求类型与处理器类方法的关系如下：<br>
	 * 1、type="bbb",clazz=cc.darhao.MyController -> 执行cc.darhao.MyController.bbb()方法 <br>
	 * 2、type="aaa/bbb",clazz=cc.darhao.MyController -> Pasta会直接忽略"/"前面的字符串 -> 执行cc.darhao.MyController.bbb()方法<br>
	 * 注意，以下情况未正确处理可能会抛出未受检异常：<br>
	 * 1、Pasta仅根据type参数的值来获取处理类的中对应的方法，请不要在处理类中使用重载！否则可能会获取到不是你想要的方法<br>
	 * 2、处理器类中，处理请求的方法里，定义的所有参数必须被包含在请求报文的body字段内，参数名必须与字段内的字段名一致<br>
	 * 3、另外，可以定义一个类型为javax.websocket.Session、名为session的参数，所以其他参数均不能使用session这个名<br>
	 * 4、方法必须是公有实例方法而不是静态方法，所以方法的线程安全级别是不可变<br>
	 * 5、处理器类必须保留公有无参构造器<br>
	 * 6、必须保证方法的返回值可以被fastjson成功序列化<br>
	 * <br>
	 * 可能会抛出的未受检异常包括但不限于以下几种：<br>
	 * 如果无法根据定位到处理方法，则会抛出{@link IllegalArgumentException}
	 * @param type 请求类型
	 * @param clazz 处理器类
	 */
	public static final void bindRoute(String type, Class clazz) {
		if(type == null || clazz == null) {
			throw new NullPointerException("参数不能为null");
		}
		routerMap.put(type, clazz);
	}
	
	
	/**
	 * 添加一个请求拦截器到Pasta中<br>
	 * 拦截器的执行顺序与调用该方法的顺序一致
	 * @param filter 拦截器
	 */
	public static final void addFilter(PastaFilter filter) {
		if(filter == null) {
			throw new NullPointerException("参数不能为null");
		}
		filters.add(filter);
	}
	
	
	/**
	 * 该方法必须且仅能在WebSocket的onMessage方法中调用<br>
	 * 如果不调用此方法，Pasta将无法处理任何请求<br>
	 * <br>
	 * 可能会抛出的未受检异常包括但不限于以下几种：<br>
	 * 如果接受的报文字符串出现解析json错误、重要字段缺失，则会抛出{@link JSONException}
	 * @param session 请求会话实体
	 * @param message 请求报文字符串
	 * @throws IOException 当回复响应时出现问题抛出
	 */
	public static final void receiveMessage(Session session, String message) throws IOException {
		if(session == null || message == null) {
			throw new NullPointerException("参数不能为null");
		}
		PastaReceiver.receiveMessage(session, message);
	}
	
	
	/**
	 * 向指定会话发送一个请求<br>
	 * 在接收到响应或者请求超时之前，调用该方法的线程会处于阻塞状态<br>
	 * <br>
	 * 可能会抛出的未受检异常包括但不限于以下几种：<br>
	 * 如果未开启请求超时检查器功能，并且发送了一个双方协议内未定义的一个type的话，则请求线程可能会陷入无限阻塞当中<br>
	 * 如果开启了请求超时检查器功能，并且发送了一个双方协议内未定义的一个type的话，则会抛出{@link IllegalArgumentException}<br>
	 * @param session 用于发送请求的会话实体
	 * @param type 请求的类型
	 * @param body 请求参数，如果没有参数，可以为null
	 * @return 响应体的body值
	 * @throws IOException 如果发送时出现问题抛出
	 * @throws TimeoutException 如果请求超时时抛出
	 */
	public static final JSONObject sendRequest(Session session, String type, JSONObject body) throws IOException, TimeoutException {
		if(session == null || type == null) {
			throw new NullPointerException("参数不能为null");
		}
		if(checker != null) {
			if(checker.getTypeTimeoutMap().get(type) == null) {
				throw new IllegalArgumentException("请求类型"+type+"未在请求超时检查器内定义");
			}
		}
		PastaPackage requestPackage = new PastaPackage(type, body);
		return PastaSender.sendRequest(session, requestPackage);
	}
	
	
	/**
	 * 开启请求超时检查器<br>
	 * 开启该功能后，Pasta会创建一个子线程专门用来检查我方发送的请求是否超时，超时时间与请求的类型有关<br>
	 * 如果发生超时，Pasta会触发PastaLogCatcher，并且解除发送请求线程的阻塞状态<br>
	 * 而迟来的响应Pasta不会进行任何处理<br>
	 * 可能会抛出的未受检异常包括但不限于以下几种：<br>
	 * 如果重复调用该方法，不会开启新的额外子线程
	 * @param typeTimeoutMap 请求类型与超时时间映射关系
	 */
	public static final void startRequestTimeoutChecker(Map<String, Long> typeTimeoutMap) {
		if(typeTimeoutMap == null) {
			throw new NullPointerException("参数不能为null");
		}
		if(checker == null) {
			checker = new RequestTimeoutChecker(typeTimeoutMap);
			checker.setName("request-timeout-check-timer");
			checker.start();
		}
	}
	
	
	/**
	 * 停止请求超时检查器，并结束其中的子线程，如果线程未运行，则不处理
	 */
	public static final void stopRequestTimeoutChecker() {
		 if(checker != null) {
			 checker.interrupt();
			 checker = null;
		 }
	}
	
	
	/**
	 * 设置Pasta日志捕获器，在请求完成、请求超时、响应完成时均会调用该捕获器中的相应方法
	 * @param catcher 日志捕获器实体
	 */
	public static final void setLogCatcher(PastaLogCatcher catcher) {
		if(catcher == null) {
			throw new NullPointerException("参数不能为null");
		}
		Pasta.catcher = catcher;
	}
	
}
