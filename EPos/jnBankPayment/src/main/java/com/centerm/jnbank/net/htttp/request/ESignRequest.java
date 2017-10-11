package com.centerm.jnbank.net.htttp.request;

import com.loopj.android.http.RequestParams;

/**
 * Created by ysd on 2016/11/26.
 */

public class ESignRequest extends BaseRequest {
    public ESignRequest(String merchantId, String termId, String transData, String transTime, String transNum, String pic) {
//        setUrl(Config.ESIGN_UPLOAD_ADDRESS);
        RequestParams params = new RequestParams();
        params.put("mchtId", merchantId);
        params.put("termId", termId);
        params.put("transDate", transData);
        params.put("transTime", transTime);
        params.put("transNum", transNum);
        params.put("pic", pic);
        setParams(params);
    }
}
