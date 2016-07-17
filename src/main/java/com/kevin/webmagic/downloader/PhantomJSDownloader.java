package com.kevin.webmagic.downloader;

import org.apache.http.annotation.ThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.downloader.AbstractDownloader;
import us.codecraft.webmagic.selector.PlainText;

import java.io.*;

/**
 * this downloader is used to download pages which need to render the javascript
 *
 * @author dolphineor@gmail.com
 * @version 0.5.3
 */
/**
 * @author kevin
 *
 */
@ThreadSafe
public class PhantomJSDownloader extends AbstractDownloader {

    private static Logger logger = LoggerFactory.getLogger(PhantomJSDownloader.class);
    private static String phantomJSCfgPath;    
    private static String phantomJSBinPath;

    private int retryNum;
    private int threadNum;

    
    /**
     *	需要将 phantomJS的可执行文件和配置文件crawl.js 放到类路径
     */
    public PhantomJSDownloader() {
    	String classPath = this.getClass().getResource("/").getPath();
    	if(System.getProperty("os.name").toLowerCase().startsWith("win"))
    		phantomJSBinPath = classPath + System.getProperty("file.separator") + "phantomjs.exe";
    	else
    		phantomJSBinPath = classPath + System.getProperty("file.separator") + "phantomjs";    	
        PhantomJSDownloader.phantomJSCfgPath = new File(classPath).getPath() + System.getProperty("file.separator") + "crawl.js ";
    }

    
    public Page download(Request request, Task task) {
        if (logger.isInfoEnabled()) {
            logger.info("downloading page: " + request.getUrl());
        }
        String content = getPage(request);
        if (content.contains("HTTP request failed")) {
            for (int i = 1; i <= getRetryNum(); i++) {
                content = getPage(request);
                if (!content.contains("HTTP request failed")) {
                    break;
                }
            }
            if (content.contains("HTTP request failed")) {
                //when failed
                Page page = new Page();
                page.setRequest(request);
                return page;
            }
        }

        Page page = new Page();
        page.setRawText(content);
        page.setUrl(new PlainText(request.getUrl()));
        page.setRequest(request);
        page.setStatusCode(200);
        return page;
    }

    
    public void setThread(int threadNum) {
        this.threadNum = threadNum;
    }

    protected String getPage(Request request) {
        try {
            String url = request.getUrl();
            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec(phantomJSBinPath + "  " + phantomJSCfgPath + url);
            InputStream is = process.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuffer stringBuffer = new StringBuffer();
            String line;
            while ((line = br.readLine()) != null) {
                stringBuffer.append(line).append("\n");
            }
            return stringBuffer.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public int getRetryNum() {
        return retryNum;
    }

    public PhantomJSDownloader setRetryNum(int retryNum) {
        this.retryNum = retryNum;
        return this;
    }
}
