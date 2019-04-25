# pasta 快速开始
  
## 协议介绍
### 协议实现
>底层实现: WebSocket  

>数据交换格式: JSON  

### 报文结构
>报文ID（id）: 类型为整数，唯一标识这个报文，在一对请求和响应报文对中，两者的id必须一致  

>报文方向（direction），表示这个报文是请求或是响应，值必须为"request"或"response"，该值与报文类型共同决定了报文主体的结构  

>报文类型（type），该值与报文方向共同决定了报文主体的结构。报文类型的值是在制定本协议时确定的  

>报文主体（body），为空的话该字段可以省略  

### 协议标准
>每一个请求都必须有且仅有一个对应的响应  

>A向B发送请求，B应在请求完成后发送响应，而不是在接到请求时发送响应  

>如果A在T秒后没有收到B的响应的话，后续即使接收到响应，也不做处理  
>>T的值取决于请求报文的类型  

>所有符合协议的请求都必须回复，不符合协议的请求不强制要求回复  
>>符合协议的定义  
>>报文ID符合规定  
>>报文方向符合规定  
>>报文类型符合双方协商后的规定  

## API介绍
### Maven依赖
```
<!-- https://mvnrepository.com/artifact/com.alibaba/fastjson -->
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>fastjson</artifactId>
    <version>1.2.47</version>
</dependency>
```
### 简单使用 —— 作为响应方
1. 在WebSocket实例中的onMessage方法里调用Pasta.receiverMessage()方法，如下：  
```
@OnMessage
public void onMessage(Session session, String message) {
  try {
    Pasta.receiveMessage(session, message);
  } catch (IOException e) {
    logger.error("...");
  } catch (JSONException e) {
    logger.warn("...");
  }
}
```

2. 在服务器的```启动方法```里配置Pasta，你至少需要配置Router用来处理接收到请求报文，如下：  
```
Pasta.bindRoute("login", MyHandler.class); 
```
第一个参数是请求报文的类型，第二个参数是对应的处理器类，以上代码表示在MyHandler类的login方法处理login类型的报文，还有一种表示法：
```
Pasta.bindRoute("main/login", MyHandler.class); 
```
这种表示法也表示上述同样的意思，只不过更加好看而已  

3. 编写处理报文的类 —— MyHandler，假设login请求报文具有两个参数```（name,password）```，则可以这样写：  
```
public class MyHandler {
  public Object login(String name, String password) {
    //TODO:...
  }
}
```
Pasta会为你的方法参数自动赋值，该方法的返回值可以为可被fastjson序列化的任意类型  

4. 以上就是基本的用法，更多用法（如配置请求超时检查器、配置请求拦截器、日志捕获器等）请参考文末的链接内附件的API文档  

### 简单实用 —— 作为请求方  
1. 在任何地方，调用Pasta.sendRequest()方法即可（该方法是阻塞的），如下：  
```
JSONObject paras = new JSONObject();
paras.put("id",1);
JSONObject result = Pasta.sendRequest(session, "getUser", paras); //获取ID为1的用户信息
```
2. 如果你开启了请求超时检查器，并且上述请求超时时，会抛出TimeoutException，这是一个未受检异常，当然你也可以捕获它  

## 获取Pasta  
>[点击获取](https://github.com/darhao/pasta/releases/tag/1.0.0 "Pasta")
