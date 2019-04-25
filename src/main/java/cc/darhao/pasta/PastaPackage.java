package cc.darhao.pasta;

/**
 * Pasta协议报文
 * <br>
 * <b>2019年4月9日</b>
 * @author <a href="https://github.com/darhao">鲁智深</a>
 */
public class PastaPackage {
	
	private Integer id;
	
	private PastaDirection direction;
	
	private String type;
	
	private Object body;

	
	public PastaPackage() {}
	
	
	/**
	 * 构建请求报文
	 * @param type
	 * @param body
	 */
	PastaPackage(String requestType, Object requestBody) {
		this.id = IdCounter.inc();
		this.direction = PastaDirection.Request;
		this.type = requestType;
		this.body = requestBody;
	}
	
	
	/**
	 * 根据请求报文和响应体生成响应报文
	 * @param requestPackage
	 * @param responseBody
	 */
	PastaPackage(PastaPackage requestPackage, Object responseBody) {
		this.id = requestPackage.getId();
		this.direction = PastaDirection.Response;
		this.type = requestPackage.getType();
		this.body = responseBody;
	}


	public Integer getId() {
		return id;
	}


	public PastaDirection getDirection() {
		return direction;
	}


	public String getType() {
		return type;
	}


	public void setId(Integer id) {
		this.id = id;
	}


	public void setDirection(PastaDirection direction) {
		this.direction = direction;
	}


	public void setType(String type) {
		this.type = type;
	}


	public Object getBody() {
		return body;
	}


	public void setBody(Object body) {
		this.body = body;
	}

	
}
