package cc.darhao.pasta;

import javax.websocket.Session;

/**
 * 空拦截器，位于Pasta拦截器列表的首位
 * <br>
 * <b>2019年4月23日</b>
 * @author <a href="https://github.com/darhao">鲁智深</a>
 */
class EmptyPastaFilter extends PastaFilter{
	
	@Override
	public Object filter(Session session, PastaPackage pastaPackage) {
		return doNext(session, pastaPackage);
	}
	
}
