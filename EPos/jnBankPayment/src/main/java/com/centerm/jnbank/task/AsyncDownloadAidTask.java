package com.centerm.jnbank.task;

import android.content.Context;
import android.text.TextUtils;

import com.centerm.cpay.midsdk.dev.DeviceFactory;
import com.centerm.cpay.midsdk.dev.define.IPbocService;
import com.centerm.cpay.midsdk.dev.define.pboc.EnumAidCapkOperation;
import com.centerm.jnbank.bean.iso.Iso62Aid;
import com.centerm.jnbank.common.ISORespCode;
import com.centerm.jnbank.common.TransCode;
import com.centerm.jnbank.db.CommonDao;
import com.centerm.jnbank.db.DbHelper;
import com.centerm.jnbank.net.SequenceHandler;
import com.centerm.jnbank.net.SocketClient;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.centerm.jnbank.common.TransCode.DOWNLOAD_AID;
import static com.centerm.jnbank.common.TransCode.DOWNLOAD_PARAMS_FINISHED;
import static com.centerm.jnbank.common.TransCode.POS_STATUS_UPLOAD;
import static com.centerm.jnbank.common.TransDataKey.KEY_PARAMS_COUNTS;
import static com.centerm.jnbank.common.TransDataKey.KEY_PARAMS_TYPE;
import static com.centerm.jnbank.common.TransDataKey.iso_f39;
import static com.centerm.jnbank.common.TransDataKey.iso_f62;

/**
 * author:wanliang527</br>
 * date:2016/11/22</br>
 */

public abstract class AsyncDownloadAidTask extends AsyncMultiRequestTask {

    private LinkedList<Iso62Aid> infoList;
    private CommonDao<Iso62Aid> dao;
    private int index;
    private int totalCounts;

    public AsyncDownloadAidTask(Context context, Map<String, String> dataMap) {
        super(context, dataMap);
        dao = new CommonDao<>(Iso62Aid.class, new DbHelper(context));
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        infoList = new LinkedList<>();
    }

    @Override
    protected String[] doInBackground(String... params) {
        dao.deleteByWhere("id is NOT NULL");
        final String[] result = new String[2];
        //先进行终端状态上送
        dataMap.put(KEY_PARAMS_TYPE, "2");//参数类型,
//        dataMap.put(KEY_PARAMS_COUNTS, "0");//已获取到的参数信息条数，用于继续发起请求
        Object msgPacket = factory.pack(TransCode.POS_STATUS_UPLOAD, dataMap);

        final SequenceHandler handler = new SequenceHandler() {

            private void startDownload() {
                sleep(SHORT_SLEEP);
                int counts = infoList.size();
                if (counts > 0) {
                    Iso62Aid aid = infoList.removeFirst();
                    dataMap.put(iso_f62, aid.getAid());
                    Object pkgMsg = factory.pack(DOWNLOAD_AID, dataMap);
                    logger.debug("总数==>" + totalCounts + "==>当前开始下载==>" + index + "==>" + dataMap.get(iso_f62));
                    publishProgress(totalCounts, ++index);//进度通知
                    sendNext(DOWNLOAD_AID, (byte[]) pkgMsg);
                } else {
                    Object pkgMsg = factory.pack(DOWNLOAD_PARAMS_FINISHED, dataMap);
                    logger.debug("AID下载结束，准备发送下载结束报文");
                    sendNext(DOWNLOAD_PARAMS_FINISHED, (byte[]) pkgMsg);
                    sleep(MEDIUM_SLEEP);
                    publishProgress(totalCounts, -1);//下载结束
                    sleep(MEDIUM_SLEEP);
                    publishProgress(totalCounts, -2);//开始导入
                    onImportOneTime();
                }
            }

            @Override
            protected void onReturn(String reqTag, byte[] respData, String code, String msg) {
                if (respData != null) {
                    Map<String, String> resp = factory.unpack(reqTag, respData);
                    String respCode = resp.get(iso_f39);
                    ISORespCode isoCode = ISORespCode.codeMap(respCode);
                    result[0] = isoCode.getCode();
                    result[1] = context.getString(isoCode.getResId());
                    String iso62 = resp.get(iso_f62);
                    switch (reqTag) {
                        case POS_STATUS_UPLOAD:
                            if ("00".equals(respCode)) {
                                String result = iso62.substring(0, 2);
                                String values = null;
                                if (iso62.length() > 2) {
                                    values = iso62.substring(2, iso62.length());
                                }
                                if ("31".equals(result) || "33".equals(result)) {//有参数，且一个报文就能存下
                                    logger.info("AID信息获取成功==>无更多AID信息==>准备下载");
                                    String[] arr = values == null ? new String[]{} : values.split("9F06");
                                    for (int i = 0; i < arr.length; i++) {
                                        if (TextUtils.isEmpty(arr[i]) || arr[i].length() < 2) {
                                            logger.warn("AID==>" + arr[i] + "==>非法");
                                            continue;
                                        }
                                        Iso62Aid aid = new Iso62Aid("9F06" + arr[i]);
                                        infoList.add(aid);
                                    }
                                    totalCounts = infoList.size();
                                    startDownload();//开始下载具体AID的参数
                                } else if ("32".equals(result)) {//AID参数，一个报文存不下，需要再次发送请求
                                    logger.info("AID信息获取成功==>继续获取更多");
                                    String[] arr = values == null ? new String[]{} : values.split("9F06");
                                    for (int i = 0; i < arr.length; i++) {
                                        if (TextUtils.isEmpty(arr[i]) || arr[i].length() < 2) {
                                            logger.warn("AID==>" + arr[i] + "==>非法");
                                            continue;
                                        }
                                        Iso62Aid aid = new Iso62Aid("9F06" + arr[i]);
                                        infoList.add(aid);
                                    }
                                    dataMap.put(KEY_PARAMS_COUNTS, "" + infoList.size());
                                    Object pkgMsg = factory.pack(POS_STATUS_UPLOAD, dataMap);
                                    sendNext(POS_STATUS_UPLOAD, (byte[]) pkgMsg);//继续获取AID参数信息
                                } else {//无AID参数
                                    logger.warn("无AID信息");
                                }
                            }
                            break;
                        case DOWNLOAD_AID:
                            if ("00".equals(respCode)) {
                                String result = iso62.substring(0, 2);
                                String values = null;
                                if (iso62.length() > 2) {
                                    values = iso62.substring(2, iso62.length());
                                }
                                if ("31".equals(result)) {
                                    onSave(values);
                                }
                                //继续下一条的下载
                                startDownload();
                            }
                            break;
                        case DOWNLOAD_PARAMS_FINISHED:
                            //下载结束报文结果，不关心
                            break;
                    }
                } else {
                    result[0] = code;
                    result[1] = msg;
                }
            }
        };
        SocketClient client = SocketClient.getInstance(context);
        client.syncSendSequenceData(TransCode.POS_STATUS_UPLOAD, (byte[]) msgPacket, handler);
        return result;
    }

    /**
     * 保存公钥到数据库，用于后续一次性导入到终端
     *
     * @param aid aid
     * @return 保存成功返回true，失败返回false
     */
    private boolean onSave(String aid) {
        Iso62Aid bean = new Iso62Aid(aid);
        boolean r = dao.save(bean);
        return r;
    }

    /**
     * 一次性导入已下载好的公钥参数
     *
     * @return
     */
    private boolean onImportOneTime() {
        List<Iso62Aid> list = dao.query();
        logger.info("正在导入公钥参数==>待导入的条数==>" + (list == null ? 0 : list.size()));
        if (list != null && list.size() > 0) {
         /*   try {
                IPbocService pbocService = DeviceFactory.getInstance().getPbocService();
                pbocService.updateAID(EnumAidCapkOperation.CLEAR, null);//清空所有的公钥参数
            } catch (Exception e) {
                e.printStackTrace();
            }*/
            for (int i = 0; i < list.size(); i++) {
                Iso62Aid aidBean = list.get(i);
                String aid = aidBean.getAid();
                boolean r = false;
                try {
                    IPbocService pbocService = DeviceFactory.getInstance().getPbocService();
                    r = pbocService.updateAID(EnumAidCapkOperation.UPDATE, aid);//更新终端的公钥参数
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (r) {
                    logger.info("导入AID参数成功==>删除数据库记录");
                    dao.delete(aidBean);
                } else {
                    aidBean.setImportTimes(aidBean.getImportTimes() + 1);
                    dao.update(aidBean);
                    logger.warn("导入AID参数失败==>更改数据库状态");
                }
            }
        }
        sleep(LONG_SLEEP);
        publishProgress(totalCounts, -3);//导入完成
        sleep(1000);
        return false;
    }

}
