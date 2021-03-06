package com.ifreeshare.spider.http.server.route.image;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import com.ifreeshare.persistence.IDataSearch;
import com.ifreeshare.spider.core.CoreBase;
import com.ifreeshare.spider.core.ErrorBase;
import com.ifreeshare.spider.http.parse.BaseParser;
import com.ifreeshare.spider.http.server.page.PageDocument;
import com.ifreeshare.spider.http.server.route.BaseRoute;
import com.ifreeshare.spider.log.Log;
import com.ifreeshare.spider.log.Loggable.Level;
import com.ifreeshare.spider.redis.RedisPool;

public class ShowImageRouter extends BaseRoute {
	
	IDataSearch<JsonObject> search = IDataSearch.instance();
	public ShowImageRouter() {
		super("/public/show/image/:itype/:otype/", BaseRoute.GET, "templates/images/show.ftl");
	}


	@Override
	public void process(RoutingContext context) {
		HttpServerRequest request =  context.request();
		HttpServerResponse response = context.response();
		String id = request.getParam("id");
		String iType = request.getParam(CoreBase.DATA_I_TYPE);
		String oType = request.getParam(CoreBase.DATA_O_TYPE);
		
		if(!CoreBase.DATA_TYPE_GET.equals(iType)){
			faultRequest(response, ErrorBase.DATA_I_TYPE_ERROR);
			return;
		}
		
		JsonObject docJson = search.getValueById(CoreBase.INDEX_HTML, CoreBase.TYPE_IMAGE, id);
		Log.log(logger, Level.DEBUG, "router[%s],id[%s], image info[%s]", this.getUrl(), id , docJson);
		if(CoreBase.DATA_TYPE_JSON.equals(oType)){
			response.end(docJson.toString());
			return;
		}else if(CoreBase.DATA_TYPE_XML.equals(oType)){
			response.end("Temporarily not available ");
			return;
		}
		
		
		String keywords = docJson.getString(CoreBase.HTML_KEYWORDS);
		
		PageDocument doc = new PageDocument();
		doc.setUuid(docJson.getString(CoreBase.UUID));
		doc.setKeywords(keywords);
		doc.setDescription(docJson.getString(CoreBase.HTML_DESCRIPTION));
		doc.setName(docJson.getString(CoreBase.FILE_NAME));
		doc.setTitle(docJson.getString(CoreBase.HTML_TITLE));
		doc.setOrigin(docJson.getString(CoreBase.URL));
		doc.setSrc(docJson.getString(CoreBase.FILE_URL_PATH));
		doc.setResolution(docJson.getString(CoreBase.RESOLUTION));
	 	String[] keys = keywords.split(BaseParser.KEYWORD_SEPARATOR);
	 	
	 	for (int i = 0; i < keys.length; i++) {
			String string = keys[i];
			doc.getTags().add(string);
		}
		context.put("doc", doc);
		render(context);
	}
	
	
	
	
	
}
