package com.centerm.jnbank.net.htttp.request;


import com.loopj.android.http.RequestParams;

import org.apache.log4j.Logger;

/**
 * Created by linwanliang on 2016/3/2.
 * 请求对象的基类，对通用报文头进行封装
 */
public class BaseRequest {
    private Logger logger = Logger.getLogger(getClass());
    private RequestParams  params;
    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public RequestParams getParams() {
        return params;
    }

    public void setParams(RequestParams params) {
        this.params = params;
    }

    @Override
    public String toString() {
        return "BaseRequest{" +
                "params=" + params +
                ", url='" + url + '\'' +
                '}';
    }
}
