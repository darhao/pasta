package cc.darhao.pasta;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import javax.websocket.Session;

import com.alibaba.fastjson.JSONObject;

/**
 * 报文拦截器抽象类<br>
 * 继承该类并覆盖filter方法以实现请求拦截功能<br>
 * 线程安全级别：不安全
 * <br>
 * <b>2019年4月23日</b>
 * @author <a href="https://github.com/darhao">鲁智深</a>
 */
public abstract class PastaFilter {
	
	/**
	 * 覆盖该方法以实现拦截请求的功能，你可以在该方法中随意读写参数值、随意读写结果<br>
	 * 只有当你调用父类的doNext方法后，才会执行下一个拦截器（如果下一个拦截器不存在，将执行处理器）<br>
	 * @param session 该次请求的会话实体
	 * @param pastaPackage 该次请求的报文
	 * @return Object 本拦截器返回的结果
	 */
	public abstract Object filter(Session session, PastaPackage pastaPackage);
	
	
	/**
	 * 调用该方法后，会执行下一个拦截器（如果下一个拦截器不存在，将执行处理器）<br>
	 * 本方法参数不能为空，否则会抛出{@link NullPointeException}
	 * @param session 该次请求的会话实体
	 * @param pastaPackage 该次请求的报文
	 * @return Object 执行下一个拦截器或处理器的结果
	 */
	protected final Object doNext(Session session, PastaPackage pastaPackage) {
		if(session == null || pastaPackage == null) {
			throw new NullPointerException("参数不能为空");
		}
		int myIndex = Pasta.filters.indexOf(this);
		if(myIndex == Pasta.filters.size() - 1) { //如果是最后一个拦截器，则执行处理器
			return callHandler(session, pastaPackage);
		}else { //否则执行下一个过滤器
			return Pasta.filters.get(myIndex + 1).filter(session, pastaPackage);
		}
	}
	

	private Object callHandler(Session session, PastaPackage pastaPackage) {
		String type = pastaPackage.getType();
		String[] ss = type.split("/");
		String requestMethodName = ss[ss.length - 1];
		try {
			Class handlerClazz = Pasta.routerMap.get(type);
			Method[] methods = handlerClazz.getMethods();
			//匹配方法名
			for (Method method : methods) {
				if(method.getName().equals(requestMethodName)) {
					//获取方法参数列表
					Parameter[] parameters = method.getParameters();
					//获取报文参数列表
					JSONObject body = (JSONObject) pastaPackage.getBody();
					//新建参数值列表
					Object[] values = new Object[parameters.length];
					for (int i = 0; i < parameters.length; i++) {
						Parameter parameter = parameters[i];
						String paraName = parameter.getName();
						//判断是session还是其他参数
						if(paraName.equals("session")) {
							values[i] = session;
						}else {
							Object value = body.get(paraName);
							if (value == null) {
								throw new NullPointerException(handlerClazz.getName()+"."+method.getName()+"()方法中定义的参数："+paraName+" 在报文参数列表中无法找到对应字段");
							}
							values[i] = value;
						}
					}
					Object handler = handlerClazz.newInstance();
					return method.invoke(handler, values);
				}
				throw new NullPointerException("没有在"+handlerClazz.getName()+"中找到方法名为"+requestMethodName+"的方法");
			}
		} catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
			if(e instanceof InvocationTargetException) {
				throw (RuntimeException) ((InvocationTargetException) e).getTargetException();
			}
			throw new RuntimeException(e.getMessage());
		}
		return null;
	}
	
}
