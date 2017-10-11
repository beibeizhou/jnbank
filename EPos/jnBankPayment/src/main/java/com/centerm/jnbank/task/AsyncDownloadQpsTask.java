package com.centerm.jnbank.task;

import android.content.Context;

import com.centerm.jnbank.base.BaseTradeActivity;
import com.centerm.jnbank.bean.iso.Iso62Qps;
import com.centerm.jnbank.common.ISORespCode;
import com.centerm.jnbank.common.TransCode;
import com.centerm.jnbank.common.TransDataKey;
import com.centerm.jnbank.db.CommonDao;
import com.centerm.jnbank.db.DbHelper;
import com.centerm.jnbank.net.SequenceHandler;
import com.centerm.jnbank.net.SocketClient;

import java.util.Map;

import static com.centerm.jnbank.common.TransCode.DOWNLOAD_PARAMS_FINISHED;
import static com.centerm.jnbank.common.TransDataKey.iso_f39;
import static com.centerm.jnbank.common.TransDataKey.iso_f62;

/**
 * 异步下载非接参数任务
 * author:wanliang527</br>
 * date:2016/11/23</br>
 */

public abstract class AsyncDownloadQpsTask extends AsyncMultiRequestTask {

    private CommonDao<Iso62Qps> dao;

    public AsyncDownloadQpsTask(Context context, Map<String, String> dataMap) {
        super(context, dataMap);
        dao = new CommonDao<>(Iso62Qps.class, new DbHelper(context));
    }

    @Override
    protected String[] doInBackground(String... params) {
        final String[] result = new String[2];
        dataMap.put(TransDataKey.KEY_PARAMS_TYPE, "4");
        Object msgPacket = factory.pack(TransCode.DOWNLOAD_QPS_PARAMS, dataMap);
        final SequenceHandler handler = new SequenceHandler() {
            @Override
            protected void onReturn(String reqTag, byte[] respData, String code, String msg) {
                if (respData != null) {
                    Map<String, String> resp = factory.unpack(reqTag, respData);
                    String respCode = resp.get(iso_f39);
                    ISORespCode isoCode = ISORespCode.codeMap(respCode);
                    result[0] = isoCode.getCode();
                    result[1] = context.getString(isoCode.getResId());
                    String iso62 = resp.get(iso_f62);
                    boolean dbResult;
                    switch (reqTag) {
                        case TransCode.DOWNLOAD_QPS_PARAMS:
                            if ("00".equals(respCode)) {
                                //请求成功
                                publishProgress(0, -1);
                                Iso62Qps qps = new Iso62Qps(iso62);
                                dbResult = dao.deleteByWhere("id IS NOT NULL");
                                dbResult = dbResult && dao.save(qps);
                                if (dbResult) {
                                    logger.info("更新小额免密免签参数成功==>" + qps.toString());
                                    BaseTradeActivity.nullQpsParams();
                                } else {
                                    logger.warn("更新小额免密免签参数失败");
                                }
                                sleep(LONG_SLEEP);
                                Object pkgMsg = factory.pack(DOWNLOAD_PARAMS_FINISHED, dataMap);
                                sendNext(TransCode.DOWNLOAD_PARAMS_FINISHED, (byte[]) pkgMsg);
                            }
                            break;
                        case TransCode.DOWNLOAD_PARAMS_FINISHED:
                            //下载结束报文结果，不关心
                            break;
                    }
                } else {
                    result[0] = code;
                    result[1] = msg;
                }
            }
        };
        sleep(LONG_SLEEP);
        SocketClient client = SocketClient.getInstance(context);
        client.syncSendSequenceData(TransCode.DOWNLOAD_QPS_PARAMS, (byte[]) msgPacket, handler);
        return result;
    }


}
