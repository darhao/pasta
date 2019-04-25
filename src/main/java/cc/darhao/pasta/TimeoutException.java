package cc.darhao.pasta;

/**
 * 请求超时异常类
 * <br>
 * <b>2019年4月25日</b>
 * @author <a href="https://github.com/darhao">鲁智深</a>
 */
public class TimeoutException extends RuntimeException {
	
	public TimeoutException(String message) {
		super(message);
	}
	
}
