package cc.darhao.pasta;

/**
 * 报文ID计数器
 * <br>
 * <b>2019年4月23日</b>
 * @author <a href="https://github.com/darhao">鲁智深</a>
 */
class IdCounter {

	private static int id = 0;
	
	static final synchronized int inc() {
		return id++;
	}
	
}
