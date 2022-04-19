package org.eclipse.leshan.mtserver.demo.model;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

public class WriteRequestAttributes {

    private static final String FORMAT_PARAM = "format";
    private static final String TIMEOUT_PARAM = "timeout";
    private static final String REPLACE_PARAM = "replace";
    private static final long DEFAULT_TIMEOUT = 5000; // ms

    private String pathInfo;
    private String queryString;
    private String contentFormatParam;
    private String replaceParam;
    private String contentType;
    private String content;
    private long timeout;



    public WriteRequestAttributes() {
    }

    public WriteRequestAttributes(HttpServletRequest req) throws IOException {
        this.pathInfo = "/" + req.getPathInfo().split(" - ")[1];
        this.queryString = req.getQueryString();
        this.contentFormatParam = req.getParameter(FORMAT_PARAM);
        this.replaceParam = req.getParameter(REPLACE_PARAM);
        this.contentType = req.getContentType();
        this.content = IOUtils.toString(req.getInputStream(), req.getCharacterEncoding());
        this.timeout = extractTimeout(req);
    }

    public WriteRequestAttributes(String pathInfo, String queryString, String contentFormatParam, String replaceParam, String contentType, String content) {
        this.pathInfo = pathInfo;
        this.queryString = queryString;
        this.contentFormatParam = contentFormatParam;
        this.replaceParam = replaceParam;
        this.contentType = contentType;
        this.content = content;
    }

    public String getPathInfo() {
        return this.pathInfo;
    }

    public void setPathInfo(String pathInfo) {
        this.pathInfo = pathInfo;
    }

    public String getQueryString() {
        return this.queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public String getContentFormatParam() {
        return this.contentFormatParam;
    }

    public void setContentFormatParam(String contentFormatParam) {
        this.contentFormatParam = contentFormatParam;
    }

    public String getReplaceParam() {
        return this.replaceParam;
    }

    public void setReplaceParam(String replaceParam) {
        this.replaceParam = replaceParam;
    }

    public String getContentType() {
        return this.contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTimeout() {
        return this.timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public WriteRequestAttributes path(String pathInfo) {
        setPathInfo(pathInfo);
        return this;
    }

    public WriteRequestAttributes queryString(String queryString) {
        setQueryString(queryString);
        return this;
    }

    public WriteRequestAttributes contentFormatParam(String contentFormatParam) {
        setContentFormatParam(contentFormatParam);
        return this;
    }

    public WriteRequestAttributes replaceParam(String replaceParam) {
        setReplaceParam(replaceParam);
        return this;
    }

    public WriteRequestAttributes contentType(String contentType) {
        setContentType(contentType);
        return this;
    }

    public WriteRequestAttributes content(String content) {
        setContent(content);
        return this;
    }

    public WriteRequestAttributes timeout(long timeout) {
        setTimeout(timeout);
        return this;
    }

    private long extractTimeout(HttpServletRequest req) {
        // get content format
        String timeoutParam = req.getParameter(TIMEOUT_PARAM);
        long timeout;
        if (timeoutParam != null) {
            try {
                timeout = Long.parseLong(timeoutParam) * 1000;
            } catch (NumberFormatException e) {
                timeout = DEFAULT_TIMEOUT;
            }
        } else {
            timeout = DEFAULT_TIMEOUT;
        }
        return timeout;
    }
}
