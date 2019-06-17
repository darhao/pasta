package cc.darhao.pasta;

public class EmptyPastaLogCatcher extends PastaLogCatcher {

	@Override
	public void onRequestFinish(RequestResponsePair info) {
	}

	@Override
	public void onRequestTimeout(RequestResponsePair info) {
	}

	@Override
	public void onResponseFinish(RequestResponsePair info) {
	}

}
