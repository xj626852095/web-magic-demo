package com.kevin.webmagic.demo1;

import java.util.List;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.pipeline.JsonFilePipeline;
import us.codecraft.webmagic.processor.PageProcessor;

public class GithubRepoPageProcessor implements PageProcessor {

    private Site site = Site.me().setRetryTimes(3).setSleepTime(5000);

    
    public void process(Page page) {
    	List<String> urls =  page.getHtml().links().regex("(https://github\\.com/\\w+/\\w+)").all();
        page.addTargetRequests(urls);
        page.putField("urls", urls.toString());
        page.putField("author", page.getUrl().regex("https://github\\.com/(\\w+)/.*").toString());
        //page.putField("name", page.getHtml().xpath("//h1[@class='entry-title public']/strong/a/text()").toString());
        page.putField("name", page.getHtml().xpath("//div[@class='vcard-fullname']/text()").toString());
        if (page.getResultItems().get("name")==null){
            //skip this page
            page.setSkip(true);
        }
        page.putField("readme", page.getHtml().xpath("//div[@id='readme']/tidyText()"));        
    }

    
    public Site getSite() {
        return site;
    }

    public static void main(String[] args) {
        Spider.create(new GithubRepoPageProcessor())
        .addUrl("https://github.com/code4craft")
        .addPipeline(new JsonFilePipeline("F:/temp/web-magic"))
        .thread(5)
        .run();
    }

}
