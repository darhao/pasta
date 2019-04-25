package cc.darhao.pasta;

import java.util.Date;

import javax.websocket.Session;

/**
 * 请求-响应对
 * <br>
 * <b>2019年4月9日</b>
 * @author <a href="https://github.com/darhao">鲁智深</a>
 */
public class RequestResponsePair {

	private Date requestTime;
	
	private PastaPackage requestPackage;
	
	private PastaPackage responsePackage;
	
	private Session session;
	
	private boolean isOurRequest;

	
	public RequestResponsePair(Date requestTime, PastaPackage requestPackage, PastaPackage responsePackage, Session session, boolean isOurRequest) {
		this.requestTime = requestTime;
		this.requestPackage = requestPackage;
		this.responsePackage = responsePackage;
		this.isOurRequest = isOurRequest;
		this.session = session;
	}


	public Date getRequestTime() {
		return requestTime;
	}


	public PastaPackage getRequestPackage() {
		return requestPackage;
	}


	public PastaPackage getResponsePackage() {
		return responsePackage;
	}


	public Session getSession() {
		return session;
	}


	public boolean isOurRequest() {
		return isOurRequest;
	}


	public void setRequestTime(Date requestTime) {
		this.requestTime = requestTime;
	}


	public void setRequestPackage(PastaPackage requestPackage) {
		this.requestPackage = requestPackage;
	}


	public void setResponsePackage(PastaPackage responsePackage) {
		this.responsePackage = responsePackage;
	}


	public void setSession(Session session) {
		this.session = session;
	}


	public void setOurRequest(boolean isOurRequest) {
		this.isOurRequest = isOurRequest;
	}
	
}
