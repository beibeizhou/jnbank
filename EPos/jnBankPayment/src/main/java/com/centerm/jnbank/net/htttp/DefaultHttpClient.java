package com.centerm.jnbank.net.htttp;

import android.content.Context;
import android.text.TextUtils;

import com.centerm.jnbank.net.ResponseHandler;
import com.centerm.jnbank.net.htttp.request.BaseRequest;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.MySSLSocketFactory;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.security.KeyStore;

import config.Config;

/**
 * Created by ysd on 2016/11/25.
 */

public class DefaultHttpClient {
    private Logger logger = Logger.getLogger(DefaultHttpClient.class);
    private final static String DEFAULT_CHARSET = "utf-8";
    private AsyncHttpClient innerClient;
    private static DefaultHttpClient instance;
    private String charset = DEFAULT_CHARSET;

    private DefaultHttpClient() {
        innerClient = new AsyncHttpClient();
        innerClient.setConnectTimeout(Config.HTTP_CONNECTION_TIMEOUT);
        innerClient.setResponseTimeout(Config.HTTP_RESPONSE_TIMEOUT);
    }

    public static DefaultHttpClient getInstance() {
        if (instance == null) {
            synchronized (DefaultHttpClient.class) {
                if (instance == null) {
                    instance = new DefaultHttpClient();
                }
            }
        }
        return instance;
    }

    /**
     * post一个HTTP请求
     */
    public void post(Context context, final BaseRequest request, final ResponseHandler handler) {
        final String url = request.getUrl();
        if (context == null || url == null) {
            logger.warn("[请求失败] - Context或者请求地址为空");
            return;
        }
        logger.info("[正在发送请求] - " + request.toString());
        final RequestParams params = request.getParams();
        if (isHttps(url)) {
            innerClient.setSSLSocketFactory(createSSLSocketFactory());
        }
        innerClient.post(context, url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] bytes) {
                logger.debug("[请求地址] - " + url);
                logger.debug("[请求参数] - " + params);
                String response = null;
                try {
                    response = new String(bytes, charset);
                    logger.debug("[返回报文] - " + new String(bytes, charset));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                handler.onSuccess(i + "", response, bytes);
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                logger.debug("[请求地址] - " + url);
                logger.debug("[请求参数] - " + params);
                logger.warn("[请求失败] - 响应码：" + i);
                if (throwable != null) {
                    throwable.printStackTrace();
                }
                if (null != handler) {
                    handler.onFailure(i + "", null, throwable);
                }
            }
        });
    }

    private SSLSocketFactory createSSLSocketFactory() {
        MySslSocketFactory sf = null;
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            sf = new MySslSocketFactory(trustStore);
            sf.setHostnameVerifier(MySSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sf;
    }

    public boolean isHttps(String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        return url.contains("https:");
    }


}
