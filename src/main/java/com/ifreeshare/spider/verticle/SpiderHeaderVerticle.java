package com.ifreeshare.spider.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;



import java.io.ObjectOutputStream.PutField;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;



import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;



import org.apache.logging.log4j.Logger;



import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.FiberForkJoinScheduler;
import co.paralleluniverse.fibers.FiberScheduler;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.okhttp.FiberOkHttpClient;
import co.paralleluniverse.strands.channels.Channel;
import co.paralleluniverse.strands.channels.Channels;



import com.ifreeshare.spider.http.HttpUtil;
import com.ifreeshare.spider.log.Log;
import com.ifreeshare.spider.log.Loggable.Level;
import com.ifreeshare.spider.verticle.msg.MessageType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;



public class SpiderHeaderVerticle extends AbstractVerticle {
	private static  Logger logger  = Log.register(SpiderHeaderVerticle.class.getName());
	
	FiberScheduler fs = null;

	public static final String WORKER_ADDRESS = "com.ifreeshare.spider.verticle.SpiderHeaderVerticle";
	
	Channel<JsonObject> urlsChannel = Channels.newChannel(10000);
	
	
	Map<String, String> contentTypeMapVerticle = new HashMap<String, String>();
	
	private long loadValue;
	
	OkHttpClient   sClient;

	public SpiderHeaderVerticle(Vertx vertx , Context context) {
		this.vertx = vertx;
		this.context = context;
		fs =  new  FiberForkJoinScheduler(WORKER_ADDRESS, 10000);
		
		sClient  = new FiberOkHttpClient();
		
		
		sClient.setConnectTimeout(2, TimeUnit.MINUTES);
		sClient.setReadTimeout(2, TimeUnit.MINUTES);
		SSLContext sc;
		try {
			sc = SSLContext.getInstance("SSL");
			sc.init(null, new TrustManager[]{new X509TrustManager() {
			     @Override
			     public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

			     }

			     @Override
			     public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

			     }

			     @Override
			     public X509Certificate[] getAcceptedIssuers() {
			         return null;
			     }
			 }}, new SecureRandom());
			 sClient.setSslSocketFactory(sc.getSocketFactory());
			 sClient.setHostnameVerifier(new HostnameVerifier() {
			     @Override
			     public boolean verify(String hostname, SSLSession session) {
			         return true;
			     }
			 });
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		
		contentTypeMapVerticle.put(HttpUtil.TEXT_HTML, SpiderHtmlVerticle.WORKER_ADDRESS);
		
	}
	
	
	@Override
	public void start() throws Exception {
		
		vertx.eventBus().consumer(WORKER_ADDRESS, message -> {
			JsonObject mbody = (JsonObject) message.body();
			processor(mbody);
		});
		
		processUrl();
	}

	

	@Override
	public void init(Vertx vertx, Context context) {
		
		
		
	}


	private void  processor(JsonObject message) {
		int type  =  message.getInteger(MessageType.MESSAGE_TYPE);
		JsonObject body = message.getJsonObject(MessageType.MESSAGE_BODY);
		switch (type) {
		case MessageType.URL_DISTR:
			urlDistr(body);
			break;
		default:
			break;
		}

	}
	
	public void  urlDistr(JsonObject body){
		try {
			boolean succ = urlsChannel.send(body, 10000, TimeUnit.SECONDS);
			if(!succ){
				JsonObject newURl = new JsonObject();
				newURl.put(MessageType.MESSAGE_TYPE, MessageType.Fail_URL);
				newURl.put(MessageType.MESSAGE_BODY, body);
				vertx.eventBus().send(SpiderMainVerticle.MAIN_ADDRESS, newURl);
			}
			
		} catch (SuspendExecution e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	


	private void processUrl() {
 		Fiber fiber = new Fiber(() -> {
		
				JsonObject body = null;
				while ((body = urlsChannel.receive()) != null) {
					try {
						String url = body.getString("url"); 
						
						Request request = new Request.Builder().url(url).head().build();
						
						Response response = sClient.newCall(request).execute();
						
						String contentType = response.header("Content-Type");
						
						String conetentLength = response.header("Content-Length");
						
						
						String charset = body.getString(HttpUtil.CHARSET);

						if(contentType.startsWith("\"")){
							contentType = contentType.substring(1, contentType.length()-1);
						}
						String[] types =  contentType.split(";");
						
						
						if(types.length > 1){
							contentType = types[0];
							if(contentType.equals(HttpUtil.TEXT_HTML)){
								String[] charsets = types[1].split("=");
								if(charsets.length > 1) charset = charsets[1];
							}
						}
						
						String verticleAddress = contentTypeMapVerticle.get(contentType);
						
						JsonObject object = new JsonObject();
						object.put(MessageType.MESSAGE_TYPE, MessageType.URL_DISTR);
						object.put(MessageType.MESSAGE_BODY, body);
						
						body.put(HttpUtil.Content_Type, contentType);
						body.put(HttpUtil.CHARSET, charset);
						
						
						
						
						vertx.eventBus().send(verticleAddress, object);
						Log.log(logger, Level.INFO, "Send ----------------------------- Address:%s; URL:%s; Content-type:%s; Content-Length:%s. charset:%s",
								verticleAddress, url,contentType,conetentLength,charset);
					  	
					} catch (Exception e) {
						e.printStackTrace();
						Log.log(logger, Level.INFO, "e.printStackTrace ----------------------------- message:%s",body);
						
					}
					
					
				}
				
				
				
				
//			 	fileType =  HttpUtil.getFileType(contentType);
//			 	if(fileType == null || fileType.length() == 0){
//			 		String[] urlSp = url.split("\\.");
//			 		fileType = "."+urlSp[urlSp.length-1];
//			 	}
//				Elements links = doc.getElementsByTag(JsoupUtil.LINK_A);
//				Iterator<Element> eleIt = links.iterator();
//				while(eleIt.hasNext()){
//					Element a = eleIt.next();
//					String href = a.attr(JsoupUtil.LINK_A_HREF);
//					JsonObject newURl = new JsonObject();
//					newURl.put(MessageType.MESSAGE_TYPE, MessageType.NEW_URL);
//					newURl.put(MessageType.MESSAGE_BODY, href);
//					vertx.eventBus().send(SpiderMainVerticle.MAIN_ADDRESS, newURl);
//				}
//				
//				if(url.contains("item.taobao.com")){
//					System.out.println(url);
//					String title = doc.title();
//					System.out.println(title);
//					String[] urlSplit =  url.split("?");
//					if(urlSplit.length > 1){
//						String[] params = url.split("&");
//						String id = null;
//						for (int i = 0; i < params.length; i++) {
//							String param = params[i];
//							if(param.startsWith("id=")){
//								 id =  param.split("=")[1];
//								 break;
//							}
//							
//							if(id != null){
//								String iteminfourl = "https://detailskip.taobao.com/service/getData/1/p2/item/detail/sib.htm?itemId="+id
//										+ "&modules=qrcode,viewer,price,contract,duty,xmpPromotion,dynStock,delivery,upp,activity,fqg,zjys,coupon&callback=onSibRequestSuccess";
//								
//								Document priceDoc = Jsoup.connect(iteminfourl).header("Referer", "https://item.taobao.com/item.htm?id=534016703208").get();
//								System.out.println(priceDoc.html());
//							}
//						}
//						
//						
////					  	Elements metas = doc.getElementsByTag("meta");
////					  	Elements keywords = metas.attr("name","keywords"); 
////					  	Element keyword =  keywords.get(0);
////					  	System.out.println(keyword.html());
//					}
//				}
				
		});
 		
 		fiber.start();
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
//	Map<String, List<String>> headers = response.headers().toMultimap();
//  	Iterator<String> keyIt = headers.keySet().iterator();
//  	
//  	while (keyIt.hasNext()) {
//  		String key = keyIt.next();
//  		System.out.println("-----------------key:"+key);
//  		Iterator<String> valIt = headers.get(key).iterator();
//  		while (valIt.hasNext()) {
//		String value =  valIt
//				.next();
//		
//		System.out
//				.println("value:"+value);
//		
//	}
//}
  	
//  	String fileType = null;
	
	
	
	
	
	

}
