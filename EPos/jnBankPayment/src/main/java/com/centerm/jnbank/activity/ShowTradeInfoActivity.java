package com.centerm.jnbank.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.centerm.cloudsys.sdk.common.utils.StringUtils;
import com.centerm.cpay.midsdk.dev.common.utils.HexUtils;
import com.centerm.cpay.midsdk.dev.define.IPinPadDev;
import com.centerm.jnbank.R;
import com.centerm.jnbank.base.BaseTradeActivity;
import com.centerm.jnbank.bean.TradeInfo;
import com.centerm.jnbank.common.TransCode;
import com.centerm.jnbank.utils.CommonUtils;
import com.centerm.jnbank.utils.DataHelper;

import config.BusinessConfig;

import static com.centerm.jnbank.common.TransDataKey.iso_f22;
import static com.centerm.jnbank.common.TransDataKey.iso_f2_result;
import static com.centerm.jnbank.common.TransDataKey.iso_f36;
import static com.centerm.jnbank.common.TransDataKey.iso_f47;

/**
 * author:wanliang527</br>
 * date:2016/11/2</br>
 */

public class ShowTradeInfoActivity extends BaseTradeActivity {

    private LinearLayout itemContainer;
    private TradeInfo oriTradeInfo;


    @Override
    public void onInitLocalData(Bundle savedInstanceState) {
        super.onInitLocalData(savedInstanceState);
        oriTradeInfo = (TradeInfo) getIntent().getSerializableExtra(KEY_ORIGIN_INFO);
    }

    @Override
    public int onLayoutId() {
        return R.layout.activity_show_trade_info;
    }

    @Override
    public void onInitView() {
        setTitle(R.string.title_origin_trade_info);
        itemContainer = (LinearLayout) findViewById(R.id.info_block);
        if (TransCode.VOID.equals(transCode)||TransCode.COMPLETE_VOID.equals(transCode)||TransCode.SCAN_CANCEL.equals(transCode)) {
            addItemView(getString(R.string.label_org_trans_type2), context.getString(TransCode.codeMapName(oriTradeInfo.getTransCode())), true);
        }
        addItemView(getString(R.string.label_pos_serial), oriTradeInfo.getIso_f11(), true);
        addItemView(getString(R.string.label_trans_amt2), DataHelper.formatAmountForShow(oriTradeInfo.getIso_f4()), true);

        if(oriTradeInfo.getTransCode().equals(TransCode.SCAN_PAY_ALI)){
            dataMap.put(iso_f22,oriTradeInfo.getIso_f22());
            dataMap.put(iso_f47,"TXNWAY=" + "ZFB01");
        }else if(oriTradeInfo.getTransCode().equals(TransCode.SCAN_PAY_SFT)) {
            dataMap.put(iso_f22,oriTradeInfo.getIso_f22());
            dataMap.put(iso_f47,"TXNWAY=" + "SFT01");
        } else if (oriTradeInfo.getTransCode().equals(TransCode.SCAN_PAY_WEI)) {
            dataMap.put(iso_f22, oriTradeInfo.getIso_f22());
            dataMap.put(iso_f47, "TXNWAY=" + "TX01");
        } else {
//            addItemView(getString(R.string.label_card_no), DataHelper.shieldCardNo(oriTradeInfo.getIso_f2()), true);
            addItemView(getString(R.string.label_card_no), oriTradeInfo.getIso_f2(), true);
        }
        String iso12 = oriTradeInfo.getIso_f12();
        String iso13 = oriTradeInfo.getIso_f13();
        logger.warn("Iso12 == " + iso12 + "  Iso13" + iso13);
        if (TransCode.VOID.equals(transCode)||TransCode.COMPLETE_VOID.equals(transCode)||TransCode.SCAN_CANCEL.equals(transCode)) {
            addItemView(getString(R.string.label_sys_ref_no), oriTradeInfo.getIso_f37(), true);
        }
        addItemView(getString(R.string.label_trans_time), DataHelper.formatIsoF12F13(oriTradeInfo.getIso_f12(), oriTradeInfo.getIso_f13()), false);
        BusinessConfig config = BusinessConfig.getInstance();
        boolean is_complete_void_card =  config.getParam(context, BusinessConfig.Key.FLAG_COMPLETE_VOID_CARD).equals("1")?true:false;
        boolean is_void_card =  config.getParam(context, BusinessConfig.Key.FLAG_VOID_CARD).equals("1")?true:false;
        if (TransCode.VOID.equals(transCode) && !is_void_card) {
            dataMap.put(iso_f2_result, oriTradeInfo.getIso_f2());
//            dataMap.put(iso_f36, encryptTrackDataTag(oriTradeInfo.getIso_f2(), oriTradeInfo.getIso_f14_forvoid(), oriTradeInfo.getIso_track2(), oriTradeInfo.getIso_track3()));
            dataMap.put(iso_f36, encryptTrackDataTag(oriTradeInfo.getIso_f2(), null, null, null));
            dataMap.put(iso_f22,"01");
//            dataMap.put(iso_f23,oriTradeInfo.getIso_f23());
        }
        if (TransCode.COMPLETE_VOID.equals(transCode) && !is_complete_void_card) {
            dataMap.put(iso_f2_result, oriTradeInfo.getIso_f2());
            dataMap.put(iso_f36, encryptTrackDataTag(oriTradeInfo.getIso_f2(), null, null, null));
            dataMap.put(iso_f22,"01");
//            dataMap.put(iso_f23,oriTradeInfo.getIso_f23());
        }
    }

    /**
     * 加密磁道数据
     *
     * PAN，卡有效期，磁道二数据，磁道三数据以TLV格式组合后，使用0x00补齐长度为8的倍数，使用DEK进行3-DES加密
     *
     *
     * @return 加密后的磁道信息
     */
    public static String encryptTrackDataTag(String cardNum,String date,String track2,String track3){
       /* if (TextUtils.isEmpty(track3)&& TextUtils.isEmpty(track2)) {
            return null;
        }*/
        IPinPadDev pinPad = CommonUtils.getPinPadDev();
        if (pinPad == null ) {
            return "FFFFFFFFFFFFFFFF";
        }
        if(null!=track2&&track2.length()>37){
            track2 = track2.substring(0,37);
        }
//        String pan = cardNum.substring(cardNum.length()-13,cardNum.length()-1);
        String pan = cardNum;
        //组TLV--长度格式不同，使用16进制格式
        String tagPan = "0A",tagDate = "0E",tagTrack2 = "C2",tagTrack3 = "C3";
        String panData = TextUtils.isEmpty(pan)?"":(tagPan+String.format("%02X",pan.length())+pan + (pan.length()%2==0?"":"0"));
        String dateData = TextUtils.isEmpty(date)?"":(tagDate + String.format("%02X",date.length())+date);
        String track2Data = TextUtils.isEmpty(track2)?"":(tagTrack2 + String.format("%02X",track2.length()) + track2 + (track2.length()%2==0?"":"0"));
        String track3Data = TextUtils.isEmpty(track3)?"":(tagTrack3 + String.format("%02X",track3.length()) + track3 + (track3.length()%2==0?"":"0"));
        //String encyptData = panData + dateData + track2Data + track3Data;
        StringBuffer stringBuffer = new StringBuffer();
        if (!StringUtils.isStrNull(panData)) {
            stringBuffer.append(panData);
        }
        if (!StringUtils.isStrNull(dateData)) {
            stringBuffer.append(dateData);
        }
        if (!StringUtils.isStrNull(track2Data)) {
            stringBuffer.append(track2Data);
        }
        if (!StringUtils.isStrNull(track3Data)) {
            stringBuffer.append(track3Data);
        }
        int length = stringBuffer.length();
        if (length <= 0) {
            return null;
        }
        String encyptData = stringBuffer.toString();
        //填充8的倍数加密
        int len  = (encyptData.length()+15)&0xFFF0;
        encyptData +="0000000000000000".substring(0,len-encyptData.length());
//        byte[] encryByte = pinPad.encryData(EnumDataEncryMode.ECB, HexUtil.hexStringToByte(encyptData),"");
        byte[] encryByte = pinPad.encryData(null, null, encyptData);
        encyptData = HexUtils.bcd2str(encryByte);
        return encyptData;

    }
    public void onConfirmClick(View view) {
        if (CommonUtils.isOneFastClick()) {
            logger.debug("==>快速点击事件，不响应！");
            return;
        }
        jumpToNext();
    }


    @Override
    protected void jumpToNext() {
        BusinessConfig config = BusinessConfig.getInstance();
        boolean is_complete_void_psw =  config.getParam(context, BusinessConfig.Key.FLAG_COMPLETE_VOID_PSW).equals("1")?true:false;
        boolean is_complete_void_card =  config.getParam(context, BusinessConfig.Key.FLAG_COMPLETE_VOID_CARD).equals("1")?true:false;
        boolean is_void_psw =  config.getParam(context, BusinessConfig.Key.FLAG_VOID_PSW).equals("1")?true:false;
        boolean is_void_card =  config.getParam(context, BusinessConfig.Key.FLAG_VOID_CARD).equals("1")?true:false;
        if (transCode.equals(TransCode.COMPLETE_VOID)) {
            if (!is_complete_void_psw && !is_complete_void_card) {
                super.jumpToNext("1");
            } else if (!is_complete_void_card) {
                super.jumpToNext("2");
            } else {
                super.jumpToNext("3");
            }
        } else if (transCode.equals(TransCode.VOID)) {
            if (!is_void_psw && !is_void_card) {
                super.jumpToNext("1");
            } else if (!is_void_card) {
                super.jumpToNext("2");
            } else {
                super.jumpToNext("3");
            }
        } else {
            super.jumpToNext("3");
        }
    }

    private void addItemView(String key, String value, boolean addDivider) {
        View view = getLayoutInflater().inflate(R.layout.result_info_item, null);
        TextView keyShow = (TextView) view.findViewById(R.id.item_name_show);
        TextView valueShow = (TextView) view.findViewById(R.id.item_value_show);
        keyShow.setText(key);
        valueShow.setText(value);
        itemContainer.addView(view, -1, -1);
        if (addDivider) {
            float size = getResources().getDimension(R.dimen.common_divider_size);
            if (size < 1) {
                size = 1;
            }
            View divider = new View(context);
            divider.setBackgroundColor(getResources().getColor(R.color.common_divider));
            itemContainer.addView(divider, -1, (int) size);
        }
    }
}
