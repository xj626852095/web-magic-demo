package com.kevin.webmagic.demo1;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.pipeline.JsonFilePipeline;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.scheduler.RedisScheduler;

public class SinaBlogProcessor implements PageProcessor {
	
    public static final String URL_LIST = "http://blog\\.sina\\.com\\.cn/s/articlelist_1487828712_0_\\d+\\.html";
    public static final String URL_POST = "http://blog\\.sina\\.com\\.cn/s/blog_\\w+\\.html";
	
	private Site site = Site
        .me()
        .setDomain("blog.sina.com.cn")
        .setRetryTimes(3)
        .setSleepTime(3000)
        .setUserAgent(
        	"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_2) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31");

	public Site getSite() {
		return this.site;
	}

	public void process(Page page) {
		if(page.getUrl().regex(URL_LIST).match()){ //列表页, 把详情页和列表页都加到待下载URL中
			page.addTargetRequests( page.getHtml().links().regex(URL_LIST).all() );
			page.addTargetRequests( page.getHtml().xpath("//div[@class=\"articleList\"").links().regex(URL_POST).all() );			
		}else if(page.getUrl().regex(URL_POST).match()){ //详情页
			page.putField("url", page.getUrl().toString());
			page.putField("title", page.getHtml().xpath("//div[@class='articalTitle']/h2/text()").toString());
            //page.putField("content", page.getHtml().xpath("//div[@id='articlebody']//div[@class='articalContent']"));
            page.putField("date",
            		page.getHtml().xpath("//div[@id='articlebody']//span[@class='time SG_txtc']").regex("\\((.*)\\)").toString());            
		}
		if(page.getResultItems().get("title")==null)
        	page.setSkip(true);
	}
	
	public static void main(String[] args) {
		Spider.create(new SinaBlogProcessor())
		.addUrl("http://blog.sina.com.cn/s/articlelist_1487828712_0_1.html")
		.addPipeline(new JsonFilePipeline("F:/temp/web-magic"))
		.setScheduler( new RedisScheduler("127.0.0.1") )
		.run();

	}

}
