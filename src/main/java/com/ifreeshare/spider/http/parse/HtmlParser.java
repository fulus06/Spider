package com.ifreeshare.spider.http.parse;

import io.vertx.core.json.JsonObject;

import java.util.Set;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public interface HtmlParser {
	
	public String getTitle(Document doc);
	
	public String getKeywords(Document doc);
	
	public String getDescription(Document doc);
	
	public Elements getLinks(Document doc);
	
	public Elements getImages(Document doc);
	
	public Set<JsonObject> getLinkValue(Document doc);

}
