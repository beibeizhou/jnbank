package com.centerm.jnbank.channels.helper;

import android.content.Context;
import android.text.TextUtils;

import com.centerm.cpay.midsdk.dev.common.utils.HexUtils;
import com.centerm.jnbank.activity.TradingActivity;
import com.centerm.jnbank.bean.TradeInfo;
import com.centerm.jnbank.bean.iso.Iso54Balance;
import com.centerm.jnbank.common.ISORespCode;
import com.centerm.jnbank.common.Settings;
import com.centerm.jnbank.common.StatusCode;
import com.centerm.jnbank.common.TransCode;
import com.centerm.jnbank.db.CommonDao;
import com.centerm.jnbank.db.DbHelper;
import com.centerm.jnbank.utils.ViewUtils;

import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import config.BusinessConfig;

import static com.centerm.jnbank.common.TransCode.BALANCE;
import static com.centerm.jnbank.common.TransCode.DISCOUNT_INTERGRAL;
import static com.centerm.jnbank.common.TransCode.INIT_TERMINAL;
import static com.centerm.jnbank.common.TransCode.OBTAIN_TMK;
import static com.centerm.jnbank.common.TransCode.SALE;
import static com.centerm.jnbank.common.TransCode.SCAN_PAY_ALI;
import static com.centerm.jnbank.common.TransCode.SCAN_PAY_SFT;
import static com.centerm.jnbank.common.TransCode.SCAN_PAY_WEI;
import static com.centerm.jnbank.common.TransCode.SIGN_IN;
import static com.centerm.jnbank.common.TransDataKey.iso_f11;
import static com.centerm.jnbank.common.TransDataKey.iso_f39;
import static com.centerm.jnbank.common.TransDataKey.iso_f41;
import static com.centerm.jnbank.common.TransDataKey.iso_f42;
import static com.centerm.jnbank.common.TransDataKey.iso_f47;
import static com.centerm.jnbank.common.TransDataKey.iso_f48;
import static com.centerm.jnbank.common.TransDataKey.iso_f54;
import static com.centerm.jnbank.common.TransDataKey.iso_f60;
import static com.centerm.jnbank.common.TransDataKey.iso_f62;
import static com.centerm.jnbank.common.TransDataKey.iso_f64;
import static com.centerm.jnbank.common.TransDataKey.keyBalanceAmt;
import static com.centerm.jnbank.common.TransDataKey.keyLocalMac;

/**
 * 报文解析帮助类，此类只针对不同的渠道、不同的返回数据做解析，不做UI层处理和业务处理
 * UI层和业务层处理直接透传到上层{@link TradingActivity}进行。
 * author:wanliang527</br>
 * date:2016/10/29</br>
 */

public class ShengPayRespHelper extends BaseRespHelper {
    private Logger logger = Logger.getLogger(ShengPayRespHelper.class);
    private Context context;
    private Map<String, String> stringMap = new HashMap<>();
    private CommonDao<TradeInfo> tradeDao;
    public ShengPayRespHelper(Context context, String transCode) {
        super(transCode);
        this.context = context;
        tradeDao = new CommonDao<>(TradeInfo.class, new DbHelper(context));
    }

    public void setTransCode(String transCode) {
        super.setTransCode(transCode);
    }

    @Override
    public void onRespSuccess(TradingActivity activity, Map<String, String> data) {
        logger.debug("进入到onRespSuccess方法");
        String respCode = data.get(iso_f39);
        String localMac = data.get(keyLocalMac);
        String iso64 = data.get(iso_f64);
        String iso11 = data.get(iso_f11);
        String iso39 = data.get(iso_f39);
        if (!TransCode.NO_MAC_SETS.contains(getTransCode())) {
            if (!localMac.equals(iso64) && (!"A0".equals(iso39))) {
                logger.warn("MAC校验失败，ISO64==>" + iso64 + "  LocalMac==>" + localMac);
                activity.onTradeFailed(iso11, StatusCode.MAC_INVALID);
                return;
            }
        }
        if ("00".equals(respCode)||"10".equals(respCode)||"11".equals(respCode)||"A2".equals(respCode)||"A4".equals(respCode)||"A5".equals(respCode)||"A6".equals(respCode)||"58".equals(respCode)) {
            //请求成功后，保留结算商户名称，结算商户号，结算终端号
            switch (getTransCode()) {
                case INIT_TERMINAL:
                    logger.info("完成机具初始化，保存终端信息");
                    BusinessConfig.getInstance().setValue(context, BusinessConfig.Key.PRESET_MERCHANT_CD, data.get(iso_f42));
                    BusinessConfig.getInstance().setValue(context, BusinessConfig.Key.PRESET_TERMINAL_CD, data.get(iso_f41));
                    String tempString = data.get(iso_f47);
                    if(!TextUtils.isEmpty(tempString)){
                        //可能有商户名称
                        String[] arrString = tempString.split("\\|");
                        for (String tempStr : arrString){
                            if("TT".equals(tempStr.split("=")[0])){
                                String merName = tempStr.split("=")[1];
                                try {
                                    merName = new String(HexUtils.hexStringToByte(merName),"gbk");
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }
                                BusinessConfig.getInstance().setValue(context, BusinessConfig.Key.PRESET_MERCHANT_NAME, merName);
                            }
                        }
                    }
                    Settings.setInit(context);
                    //初始化成功后进行参数下载和主密钥下载
                    activity.onTradeSuccess(data);
                    break;
                case OBTAIN_TMK:
                    //用于发起交易的商户号、终端号、商户名
                    String tmk = data.get(iso_f62);
                    String value = tmk.substring(0, 32);
                    String checkValue = tmk.substring(32, 40);
                    activity.loadTMK(value, checkValue);
                    break;
                case SIGN_IN:
                   //更新批次号
                    String iso60 = data.get(iso_f60);
                    if (iso60.length() > 8) {
                        String batch = iso60.substring(2, 8);
                        logger.info("更新批次号==>本地批次号：" + BusinessConfig.getInstance().getBatchNo(context) + "==>平台批次号：" + batch);
                        BusinessConfig.getInstance().setBatchNo(activity, batch);
                        String updateFlag = iso60.substring(8, iso60.length());
                        if ("990".equals(updateFlag)) {
                            BusinessConfig.getInstance().setValue(context, BusinessConfig.Key.FLAG_NEED_UPDATE_PARAM, "1");
                        }
                    }
                    String workKey = data.get(iso_f62);
                    String pik = workKey.substring(0, 40);
                    String mak = workKey.substring(40, 80);
                    String tdk = null;
                    if (BusinessConfig.FLAG_ENCRYPT_TRACK_DATA) {
                        tdk = workKey.substring(80, 120);
                    }
                    String pikValue = pik.substring(2, 32);
                    String pikCheckValue = pik.substring(32, 40);
                    String makValue = mak.substring(0, 32);
//                    if (makValue.endsWith("0000000000000000")) {
                        //XXX使用的单倍长mak密钥。。。。
                        logger.info("MAK为单倍长");
                        String left8Bytes = mak.substring(0, 16);
                        makValue = left8Bytes + left8Bytes;
//                    }
                    String makCheckValue = mak.substring(32, 40);
                    String tdkValue = tdk == null ? null : tdk.substring(0, 32);
                    String tdkCheckValue = tdk == null ? null : tdk.substring(32, 40);
                    logger.debug("pik == " + pikValue + "   " + pikCheckValue);
                    logger.debug("mak == " + makValue + "   " + makCheckValue);
                    logger.debug("tdk == " + tdkValue + "   " + tdkCheckValue);
                    activity.loadWorkKey(pikValue, pikCheckValue, makValue, makCheckValue, tdkValue, tdkCheckValue);
                    break;
                case BALANCE:
                    String balance = data.get(iso_f54);
                    Iso54Balance iso54Bean = new Iso54Balance(balance);
                    if("C".equals(iso54Bean.getAmtSign()))
                        data.put(keyBalanceAmt, iso54Bean.getAmount());
                    else
                        data.put(keyBalanceAmt, "-" + iso54Bean.getAmount());
                    activity.onTradeSuccess(data);
                    break;
                case DISCOUNT_INTERGRAL:
                    activity.onTradeSuccess(data);
                    break;
                case SALE:
                    activity.onTradeSuccess(data);
                    break;
                default:
                    activity.onTradeSuccess(data);
                    break;
            }
        } else if (null == respCode) {
            logger.debug("结算接收到返回》》》》》");
            String amountResp = data.get(iso_f48);
            if (null != amountResp && !"".equals(amountResp)) {
                //批结算请求返回成功时，要设置参数状态
                logger.debug("结算111111111111111");
                Settings.setValue(context, Settings.KEY.BATCH_SEND_RETURN_DATA, amountResp);
                Settings.setValue(context, Settings.KEY.BATCH_SEND_STATUS, "1");
                activity.onAccountCheckSuccess(amountResp);
            } else {
                logger.error("批结算返回的48域对账情况为空！");
                activityStack.pop();
                ViewUtils.showToast(context, "批结算请求失败，请重试！");
            }
        } else if ((getTransCode().equals(SCAN_PAY_WEI) || getTransCode().equals(SCAN_PAY_ALI) || getTransCode().equals(SCAN_PAY_SFT)) && "A7".equals(respCode)) {
            TradeInfo curTradeInfo = tradeDao.queryForId(data.get(iso_f11));
            //********************更新本地流水表***********************//
            if (curTradeInfo != null) {
                //保存返回数据
                curTradeInfo.update(data);
            } else {
                logger.warn(data.get(iso_f11) + "==>交易类型==>扫码支付==>无法在数据库中查询到该数据模型==>新建模型");
                curTradeInfo = new TradeInfo(getTransCode(), data);
            }
            tradeDao.update(curTradeInfo);
            logger.warn("交易失败，后台返回码：" + respCode);
            ISORespCode code = ISORespCode.codeMap(respCode);
            activity.onTradeFailed(iso11, code);
        } else {
            logger.warn("交易失败，后台返回码：" + respCode);
            ISORespCode code = ISORespCode.codeMap(respCode);
            activity.onTradeFailed(iso11, code);
        }
    }


    @Override
    public void onRespFailed(TradingActivity activity, String statusCode, String msg) {
        activity.onTradeFailed(null, statusCode, msg);
    }
}
