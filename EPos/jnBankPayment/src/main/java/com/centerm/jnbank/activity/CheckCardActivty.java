package com.centerm.jnbank.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.centerm.cloudsys.sdk.common.utils.StringUtils;
import com.centerm.cpay.midsdk.dev.DeviceFactory;
import com.centerm.cpay.midsdk.dev.define.ICardReaderDev;
import com.centerm.cpay.midsdk.dev.define.IPbocService;
import com.centerm.cpay.midsdk.dev.define.cardreader.CardInfo;
import com.centerm.cpay.midsdk.dev.define.cardreader.CardReaderListener;
import com.centerm.cpay.midsdk.dev.define.cardreader.EnumReadCardType;
import com.centerm.cpay.midsdk.dev.define.pboc.EnumPbocFlow;
import com.centerm.cpay.midsdk.dev.define.pboc.EnumPbocSlot;
import com.centerm.jnbank.R;
import com.centerm.jnbank.base.BaseTradeActivity;
import com.centerm.jnbank.bean.BinData;
import com.centerm.jnbank.common.TransCode;
import com.centerm.jnbank.common.TransDataKey;
import com.centerm.jnbank.db.CommonDao;
import com.centerm.jnbank.utils.CommonUtils;
import com.centerm.jnbank.utils.DataHelper;
import com.centerm.jnbank.utils.DialogFactory;
import com.centerm.jnbank.utils.ViewUtils;
import com.centerm.jnbank.view.AlertDialog;
import com.centerm.jnbank.view.InputCardNumberDialog;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import config.BusinessConfig;

import static com.centerm.cpay.midsdk.dev.define.pboc.PbocEventAction.getAllActions;
import static com.centerm.jnbank.common.TransCode.DISCOUNT_INTERGRAL;
import static com.centerm.jnbank.common.TransCode.SALE;
import static com.centerm.jnbank.common.TransDataKey.FLAG_IMPORT_AMOUNT;
import static com.centerm.jnbank.common.TransDataKey.FLAG_IMPORT_CARD_CONFIRM_RESULT;
import static com.centerm.jnbank.common.TransDataKey.FLAG_REQUEST_ONLINE;
import static com.centerm.jnbank.common.TransDataKey.KEY_HOLDER_NAME;
import static com.centerm.jnbank.common.TransDataKey.iso_f14;
import static com.centerm.jnbank.common.TransDataKey.iso_f14_forvoid;
import static com.centerm.jnbank.common.TransDataKey.iso_f14_result;
import static com.centerm.jnbank.common.TransDataKey.iso_f2;
import static com.centerm.jnbank.common.TransDataKey.iso_f22;
import static com.centerm.jnbank.common.TransDataKey.iso_f2_result;
import static com.centerm.jnbank.common.TransDataKey.iso_f35;
import static com.centerm.jnbank.common.TransDataKey.iso_f36;
import static com.centerm.jnbank.common.TransDataKey.iso_f4;
import static com.centerm.jnbank.common.TransDataKey.iso_track2;
import static com.centerm.jnbank.common.TransDataKey.iso_track3;
import static com.centerm.jnbank.common.TransDataKey.keyFlagFallback;
import static com.centerm.jnbank.utils.CommonUtils.encryptTrackDataTag;
import static config.BusinessConfig.Key.FLAG_USE_DISCOUNT;
import static config.BusinessConfig.Key.FLAG_USE_INTEGRAL;

/**
 * 检卡界面。读取卡片信息，用于联机交易。
 * 消费、余额查询、预授权等业务需要在此界面开启PBOC流程
 * <p>
 * author:wanliang527</br>
 * date:2016/10/25</br>
 */

public class CheckCardActivty extends BaseTradeActivity {
    private final static String ALL = "ALL";
    private final static String SWIPE_INSERT = "SWIPE_INSERT";
    private final static String SWIPE_SWING = "SWIPE_SWING";
    private final static String INSERT = "INSERT";
    private final static String SWING = "SWING";
    private final static String SWIPE = "SWIPE";
    private int retryTimes;
    private EnumReadCardType cardType;
    private String showType;
    private Handler retryHandler;
    private Runnable retryThread;
    private boolean isFallback;//是否降级
    private boolean isForceIc;//是否强制IC卡
    private boolean onlyInsertIc;//是否只支持插卡，用于插卡消费业务
    private boolean clssPrefer;//是否挥卡优先
    private boolean flag_feijie;//是否支持非接
    private boolean gotTerminatedEvent;
    private ImageView tipPic1, tipPic2, tipPic3;//检卡提示图
    private CommonDao<BinData> dao;
    private boolean is_void_psw = true;
    private boolean is_cancel_psw = true;
    private boolean is_complete_psw = true;
    private boolean is_complete_void_psw = true;
    private InputCardNumberDialog dialog;
    private String cardBin;
    private CardInfo cardInfo1;

    @Override
    public boolean isOpenDataBase() {
        return true;
    }

    @Override
    public void onInitLocalData(Bundle savedInstanceState) {
        super.onInitLocalData(savedInstanceState);
        onlyInsertIc = getIntent().getBooleanExtra(KEY_INSERT_SALE_FLAG, false);//插卡消费的标识
        if (onlyInsertIc) {
            //插卡消费
            cardType = EnumReadCardType.INSERT;
            showType = INSERT;
        } else if (clssForcePin) {
            //闪付凭密
            cardType = EnumReadCardType.SWING;
            showType = SWING;
        } else {
            //挥卡优先
            clssPrefer = BusinessConfig.getInstance().getParam(context, BusinessConfig.Key.FLAG_PREFER_CLSS).equals("1") ? true : false;
            flag_feijie = BusinessConfig.getInstance().getParam(context, BusinessConfig.Key.FLAG_FEIJIE).equals("1") ? true : false;
            if ((TransCode.SALE.equals(transCode))) {
                if (flag_feijie) {
                    if (clssPrefer) {
                        cardType = EnumReadCardType.ALL;
                        showType = SWIPE_SWING;
                    } else {
                        cardType = EnumReadCardType.ALL;
                        showType = ALL;
                    }
                } else {
                    if (clssPrefer) {
                        cardType = EnumReadCardType.SWIPE_INSERT;
                        showType = SWIPE;
                    } else {
                        cardType = EnumReadCardType.SWIPE_INSERT;
                        showType = SWIPE_INSERT;
                    }
                }
            } else {
                if (flag_feijie) {
                    cardType = EnumReadCardType.ALL;
                    showType = ALL;
                } else {
                    cardType = EnumReadCardType.SWIPE_INSERT;
                    showType = SWIPE_INSERT;
                }
            }
        }
        dao = new CommonDao<>(BinData.class, dbHelper);
    }

    @Override
    public int onLayoutId() {
        return R.layout.activity_check_card;
    }

    @Override
    public void onInitView() {
        LinearLayout tipLayout1 = (LinearLayout) findViewById(R.id.tip1);
        LinearLayout tipLayout2 = (LinearLayout) findViewById(R.id.tip2);
        LinearLayout tipLayout3 = (LinearLayout) findViewById(R.id.tip3);
        switch (showType) {
            case ALL:
                fill(tipLayout1, R.string.tip_clss_card, R.drawable.pic_feijieka);
                fill(tipLayout2, R.string.tip_insert_card, R.drawable.pic_xinpianka);
                fill(tipLayout3, R.string.tip_mag_card, R.drawable.pic_citiaoka);
                tipLayout3.setVisibility(View.VISIBLE);
                break;
            case INSERT:
                tipLayout1.setVisibility(View.VISIBLE);
                tipLayout2.setVisibility(View.INVISIBLE);
                tipLayout3.setVisibility(View.INVISIBLE);
                fill(tipLayout1, R.string.tip_insert_card2, R.drawable.pic_xinpianka);
                break;
            case SWIPE:
                tipLayout1.setVisibility(View.VISIBLE);
                tipLayout2.setVisibility(View.INVISIBLE);
                tipLayout3.setVisibility(View.INVISIBLE);
                fill(tipLayout1, R.string.tip_mag_card, R.drawable.pic_citiaoka);
                break;
            case SWING:
                tipLayout1.setVisibility(View.VISIBLE);
                tipLayout2.setVisibility(View.INVISIBLE);
                tipLayout3.setVisibility(View.INVISIBLE);
                fill(tipLayout1, R.string.tip_clss_card, R.drawable.pic_feijieka);
                break;
            case SWIPE_SWING:
                fill(tipLayout1, R.string.tip_clss_card, R.drawable.pic_feijieka);
                fill(tipLayout2, R.string.tip_mag_card, R.drawable.pic_citiaoka);
                break;
            case SWIPE_INSERT:
                fill(tipLayout1, R.string.tip_mag_card, R.drawable.pic_citiaoka);
                fill(tipLayout2, R.string.tip_insert_card, R.drawable.pic_xinpianka);
                break;
        }
        boolean ishandCardNum = BusinessConfig.getInstance().getParam(context, BusinessConfig.Key.FLAG_AUTH_HAND_CARD).equals("1") ? true : false;
        if (ishandCardNum) {
            if (transCode.equals(TransCode.AUTH_COMPLETE) || transCode.equals(TransCode.CANCEL)) {
                if (null == dialog) {
                    dialog = new InputCardNumberDialog(context);
                    dialog.setClickListener(new InputCardNumberDialog.ButtonClickListener() {
                        @Override
                        public void onClick(InputCardNumberDialog.ButtonType button, View v) {
                            switch (button) {
                                case POSITIVE:
                                    String cardNo = dialog.getInputText();
                                    //ViewUtils.showToast(context, cardNo);
                                    dialog.dismiss();
                                    dataMap.put(iso_f2, cardNo);
                                    dataMap.put(iso_f2_result, cardNo);
                                    dataMap.put(iso_f22, "01");
                                    finishCheck();
                                    //如果是预授权完成和预授权取消要跳转输入卡有效期的界面
                                    if (transCode.equals(TransCode.AUTH_COMPLETE) || transCode.equals(TransCode.CANCEL)) {
                                        jumpToNext("2");
                                    } else {
                                        commNext(false);
                                    }
                                    break;
                                case NEGATIVE:
                                    dialog.dismiss();
                                    //finishCheck();
                                    break;
                            }
                        }
                    });
                    dialog.show();
                }
            }
        }
    }

    private void fill(LinearLayout tipContainer, int textId, int drawableId) {
        if (tipContainer.getId() == R.id.tip1) {
            tipPic1 = (ImageView) tipContainer.findViewById(R.id.hint_icon_show);
            tipPic1.setImageResource(drawableId);
//            AnimationDrawable drawable = (AnimationDrawable) tipPic1.getDrawable();
//            drawable.start();
        } else if (tipContainer.getId() == R.id.tip2) {
            tipPic2 = (ImageView) tipContainer.findViewById(R.id.hint_icon_show);
            tipPic2.setImageResource(drawableId);
//            AnimationDrawable drawable = (AnimationDrawable) tipPic2.getDrawable();
//            drawable.start();
        } else if (tipContainer.getId() == R.id.tip3) {
            tipPic3 = (ImageView) tipContainer.findViewById(R.id.hint_icon_show);
            tipPic3.setImageResource(drawableId);
//            AnimationDrawable drawable = (AnimationDrawable) tipPic3.getDrawable();
//            drawable.start();
        }

        TextView hintText = (TextView) tipContainer.findViewById(R.id.hint_text_show);
        hintText.setText(textId);
    }

    @Override
    public void afterInitView() {
        super.afterInitView();
        beginSearchCard();
        receiver = new MyReceiver();
        LocalBroadcastManager.getInstance(context).registerReceiver(receiver, getAllActions());
        BusinessConfig config = BusinessConfig.getInstance();
        is_void_psw = config.getParam(context, BusinessConfig.Key.FLAG_VOID_PSW).equals("1") ? true : false;
        is_cancel_psw = config.getParam(context, BusinessConfig.Key.FLAG_CANCEL_PSW).equals("1") ? true : false;
        is_complete_psw = config.getParam(context, BusinessConfig.Key.FLAG_COMPLETE_PSW).equals("1") ? true : false;
        is_complete_void_psw = config.getParam(context, BusinessConfig.Key.FLAG_COMPLETE_VOID_PSW).equals("1") ? true : false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        /*if (tipPic1 != null && tipPic1.getDrawable() instanceof AnimationDrawable) {
            ((AnimationDrawable) tipPic1.getDrawable()).stop();
        }
        if (tipPic2 != null && tipPic2.getDrawable() instanceof AnimationDrawable) {
            ((AnimationDrawable) tipPic2.getDrawable()).stop();
        }
        if (tipPic3 != null && tipPic3.getDrawable() instanceof AnimationDrawable) {
            ((AnimationDrawable) tipPic3.getDrawable()).stop();
        }*/

    }

    @Override
    public void onBackPressed() {
        finishCheck();
    }

    private void finishCheck() {
        activityStack.backTo(MainActivity.class);
        if (retryHandler != null) {
            retryHandler.removeCallbacks(retryThread);
        }
        try {
            ICardReaderDev cardReaderDev = DeviceFactory.getInstance().getCardReaderDev();
            cardReaderDev.stopReadCard();
            IPbocService pbocService = DeviceFactory.getInstance().getPbocService();
            pbocService.abortProcess();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 开始读卡
     */
    private void beginSearchCard() {
        ICardReaderDev cardReaderDev = null;
        try {
            cardReaderDev = DeviceFactory.getInstance().getCardReaderDev();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (cardReaderDev != null) {
            CardReaderListener listener = new CardReaderListener() {
                @Override
                public void onSuccess(final CardInfo cardInfo) {
                    cardInfo1 = cardInfo;
                    switch (cardInfo.getCardType()) {
                        case MAG_CARD:
                            Log.e("zhouwenzheng","MAG_CARD");
                            String cardNo = cardInfo.getCardNo();
                            if ((TransCode.SALE.equals(transCode) || TransCode.AUTH.equals(transCode)) && isUnionPayInternationalCard(cardNo)) {
                                DialogFactory.showSelectDialog(context, "提示", "银联国际卡交易手续费高！\n确定继续", new AlertDialog.ButtonClickListener() {
                                    @Override
                                    public void onClick(AlertDialog.ButtonType button, View v) {
                                        switch (button) {
                                            case POSITIVE:
                                                onSwipeCardSuccess(cardInfo, true);
                                                break;
                                            case NEGATIVE:
                                                onBackPressed();
                                                break;
                                        }
                                    }
                                });
                            } else if (TextUtils.isEmpty(cardNo)) {
                                ViewUtils.showToast(context, R.string.tip_read_card_failed);
                                onRetry(true);
                            } else {
                                onSwipeCardSuccess(cardInfo, false);
                            }
                            break;
                        case IC_CARD:
                            Log.e("zhouwenzheng","IC_CARD");
                            dataMap.put(iso_f22, "05");
                            pbocParams.setFlow(EnumPbocFlow.PBOC_FLOW);
                            pbocParams.setSlot(EnumPbocSlot.SLOT_IC);
                            //退货流程需输完金额才开启PBOC流程，在TradingActivity中开启
                            if (TransCode.REFUND.equals(transCode)
                                    || TransCode.AUTH_COMPLETE.equals(transCode)
                                    || TransCode.CANCEL.equals(transCode)) {
                                //消费，先输金额再检卡
                                //预授权/预授权撤销/预授权完成，先检卡再输金额
                                //预授权按照客户需求，改为跟消费一致的流程
                                pbocParams.setRequestAmtAfterCardNo(true);
                            }
                            beginPbocProcess();//开启PBOC流程
                            DialogFactory.showLoadingDialog(context, getString(R.string.tip_ic_on_processing));
                            break;
                        case RF_CARD:
                            Log.e("zhouwenzheng","RF_CARD");
                            dataMap.put(iso_f22, "07");
                            pbocParams.setFlow(EnumPbocFlow.QPBOC_FLOW);
                            pbocParams.setSlot(EnumPbocSlot.SLOT_RF);
                            //退货流程需输完金额才开启PBOC流程，在TradingActivity中开启
                            if (TransCode.AUTH.equals(transCode)
                                    || TransCode.REFUND.equals(transCode)
                                    || TransCode.AUTH_COMPLETE.equals(transCode)
                                    || TransCode.CANCEL.equals(transCode)) {
                                //消费，在卡号确认前输入金额
                                //预授权，在卡号确认后输入金额
                                pbocParams.setRequestAmtAfterCardNo(true);
                            }
                            beginPbocProcess();//开启PBOC流程
                            break;
                    }
                }

                @Override
                public void onFailure() {
                    //提示并且重试，重试次数以BusinessConfig当中的为准
                    ViewUtils.showToast(context, R.string.tip_read_card_failed);
                    onRetry(true);
                }

                @Override
                public void onTimeout() {
                    ViewUtils.showToast(context, R.string.tip_read_card_timeout);
                    //检卡超时，退回到主界面
                    activityStack.backTo(MainActivity.class);
                }

                @Override
                public void onCanceled() {
                    //这里暂时无需处理
                }

                @Override
                public void onError(int i, String s) {
                    //提示并且重试，重试次数以BusinessConfig当中的为准
                    ViewUtils.showToast(context, R.string.tip_read_card_failed);
                    onRetry(true);
                }
            };
            cardReaderDev.beginReadCard(cardType, BusinessConfig.CHECK_CARD_TIMEOUT, listener);
        }
    }

    private void onSwipeCardSuccess(CardInfo cardInfo, boolean isUnionInternationalCard) {
        dataMap.put(iso_f22, "02");
        String track1 = cardInfo.getTrack1();
        dataMap.put(KEY_HOLDER_NAME, DataHelper.extractName(track1));
        String track2 = cardInfo.getTrack2();
        if (!StringUtils.isStrNull(track2)) {
            if (track2.endsWith("F") || track2.endsWith("f")) {
                track2 = track2.substring(0, track2.length() - 1);
            }
        }
        String track3 = cardInfo.getTrack3();
        if (BusinessConfig.FLAG_ENCRYPT_TRACK_DATA) {
            //如果报文头加密标志为true，则2/14/35域置空，但是卡号开始要存储的,用于显示
            dataMap.put(iso_f2_result, cardInfo.getCardNo());
        } else {
            dataMap.put(iso_f35, track2);
            dataMap.put(iso_f2, cardInfo.getCardNo());
            dataMap.put(iso_f14, cardInfo.getExpDate());
        }
       /* String f14 = dataMap.get(iso_f14);
        if (f14 != null && f14.length() >= 4) {
            dataMap.put(iso_f14, f14.substring(0, 4));
        }*/
        dataMap.put(iso_f14_result, cardInfo.getExpDate());
        dataMap.put(iso_f14_forvoid, cardInfo.getExpDate());
        if (BusinessConfig.FLAG_ENCRYPT_TRACK_DATA) {
            dataMap.put(iso_track2, track2);
            dataMap.put(iso_track3, track3);
            dataMap.put(iso_f36, encryptTrackDataTag(cardInfo.getCardNo(), cardInfo.getExpDate(), track2, track3));
        } else {
            if (!TextUtils.isEmpty(track3)) {
                dataMap.put(iso_f36, track3);
            }
        }
        if (isFallback) {
            //注明降级交易
            dataMap.put(keyFlagFallback, "1");
        }
        if (!isFallback && CommonUtils.isIcCard(track2)) {
            if (BusinessConfig.getInstance().getParam(context, BusinessConfig.Key.FLAG_PREFER_CLSS).equals("1") ? true : false) {
                flag_feijie = BusinessConfig.getInstance().getParam(context, BusinessConfig.Key.FLAG_FEIJIE).equals("1") ? true : false;
                if (flag_feijie) {
                    ViewUtils.showToast(context, R.string.tip_force_clss);
                } else {
                    ViewUtils.showToast(context, R.string.tip_huanka);
                }
            } else {
                ViewUtils.showToast(context, R.string.tip_force_ic);
            }
            isForceIc = true;//强制IC卡消费，不允许刷卡
            onRetry(false);
        } else {
            //进入下一个界面
            commNext(isUnionInternationalCard);
        }
    }

    private void commNext(boolean isUnionInternationalCard) {
        if (TransCode.VOID.equals(transCode) && is_void_psw) {
            //消费撤销可能无需输入密码
            jumpToNext("2");
        } else if (TransCode.COMPLETE_VOID.equals(transCode) && is_complete_void_psw) {
            //消费撤销可能无需输入密码
            jumpToNext("2");
        } else if (isUnionInternationalCard && (TransCode.SALE.equals(transCode) || TransCode.AUTH.equals(transCode))) {
            jumpToNext("3");//银联国际卡要求输入主管密码
        } else {
            jumpToNext();
        }
    }

    /**
     * 根据卡号判断是否属于银联国际卡。银联国际卡需要进行风险提示
     *
     * @param cardNo 卡号
     * @return 属于银联国际卡返回true，否则返回false
     */
    private boolean isUnionPayInternationalCard(String cardNo) {
        if (cardNo == null) {
            return false;
        }
        String bin = cardNo.length() > 6 ? cardNo.substring(0, 6) : cardNo;
        try {
            List<BinData> binList = dao.queryBuilder().where().like("cardBin", bin + "%").query();
            if (binList != null && binList.size() > 0) {
                for (int i = 0; i < binList.size(); i++) {
                    BinData item = binList.get(i);
                    if (cardNo.startsWith(item.getCardBin())) {
                        //卡组织：1-银联；2-银联国际卡；3-JCB；4-VISA；5-MASTER
                        if ("2".equals(item.getCardOrg().trim())) {
                            return true;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    private void onRetry(final boolean addTimes) {
        if (retryHandler == null) {
            retryHandler = new Handler();
        }
        retryThread = null;
        retryThread = new Runnable() {
            @Override
            public void run() {
                if (addTimes) {
                    retryTimes++;
                }
                if (retryTimes > BusinessConfig.CHECK_CARD_RETRY_TIMES) {
                    ViewUtils.showToast(context, R.string.tip_card_retry_limited);
                    activityStack.backTo(MainActivity.class);
                    DialogFactory.hideAll();
                } else {
                    beginSearchCard();
                }
            }
        };
        retryHandler.postDelayed(retryThread, 1500);
    }

    private class MyReceiver extends PbocEventReceiver {

        @Override
        public void onImportAmount() {
            //金额是在卡号之前输入的交易，需要先导入金额，否则内核无法上报卡号确认事件
            if (TransCode.SALE.equals(transCode)
                    || TransCode.VOID.equals(transCode)
                    || TransCode.AUTH.equals(transCode)) {
                String amt = dataMap.get(iso_f4);
                if (amt.length() == 12) {
                    amt = DataHelper.formatIsoF4(amt);
                }
                pbocService.importAmount(amt);
            } else {
                dataMap.put(FLAG_IMPORT_AMOUNT, "1");
                jumpToNext();
            }
        }

        @Override
        public void onImportPin() {
            pbocService.importPIN(false, null);
            //非接可能会在请求导入金额之后，上报该事件，因此这里需要处理掉该事件
            //处理完该事件才能，进入到请求联机的事件
        }


        @Override
        public void onRequestOnline() {
            dataMap.put(FLAG_REQUEST_ONLINE, "1");
            if (TransCode.VOID.equals(transCode) && is_void_psw) {
                //消费撤销可能无需输入密码
                jumpToNext("2");
            } else if (TransCode.COMPLETE_VOID.equals(transCode) && is_complete_void_psw) {
                //消费撤销可能无需输入密码
                jumpToNext("2");
                //} else if (TransCode.SALE.equals(transCode) || TransCode.AUTH.equals(transCode)) {
            } else if (TransCode.SALE.equals(transCode)) {
                //消费业务，输入金额在检卡之前，判断是否属于小额免密业务，小额免密可以直接进行联机
                boolean[] qpsCondition = getQpsCondition();
                if (qpsCondition[0]) {
                    dataMap.put(TransDataKey.KEY_QPS_FLAG, "true");//小额免密标识，用于组59域报文
                    jumpToNext("2");
                } else {
                    dataMap.put(TransDataKey.KEY_QPS_FLAG, "false");//小额免密标识，用于组59域报文
                    String[] jncardbin = BusinessConfig.JNCARDBIN;//江南银行card bin信息
                    List<String> list = Arrays.asList(jncardbin);
                    String cardNo = DataHelper.shieldCardNo(dataMap.get(iso_f2));
                    if (cardNo != null) {
                        String cardNo1 = cardNo.substring(0, 6);
                        logger.info("卡号前6位为:" + cardNo1);
                        if (list.contains(cardNo1)) {
                            //该卡是江南银行行方卡
                            transCode = DISCOUNT_INTERGRAL;
                            boolean isUseIntegral = BusinessConfig.getInstance().getParam(context, FLAG_USE_INTEGRAL).equals("1") ? true : false;
                            boolean isUseDiscount = BusinessConfig.getInstance().getParam(context, FLAG_USE_DISCOUNT).equals("1") ? true : false;
                            if (isUseDiscount==false && isUseIntegral==false) {
                                transCode =SALE;
                                jumpToNext();
                            }else {
                                jumpToNext("4");
                            }
                        } else {
                            jumpToNext();
                        }
                    }
                }
            } else {
                jumpToNext();
            }
        }

        @Override
        public void onConfirmCardNo(String cardNo) {
            cardBin = cardNo;
            dataMap.put(iso_f2_result, cardNo);
            dataMap.put(FLAG_IMPORT_CARD_CONFIRM_RESULT, "1");
            if (!gotTerminatedEvent) {
                if (TransCode.VOID.equals(transCode) && is_void_psw) {
                    //消费撤销可能无需输入密码
                    jumpToNext("2");
                } else if (TransCode.COMPLETE_VOID.equals(transCode) && is_complete_void_psw) {
                    //消费撤销可能无需输入密码
                    jumpToNext("2");
                } else {
//                    jumpToNext();
                    String[] jncardbin = BusinessConfig.JNCARDBIN;//江南银行card bin信息
                    List<String> list = Arrays.asList(jncardbin);
                    if (cardNo != null) {
                        String cardNo1 = cardNo.substring(0, 6);
                        logger.info("卡号前6位为:" + cardNo1);
                        if (list.contains(cardNo1)) {
                            //该卡是江南银行行方卡
                            transCode = DISCOUNT_INTERGRAL;
                            boolean isUseIntegral = BusinessConfig.getInstance().getParam(context, FLAG_USE_INTEGRAL).equals("1") ? true : false;
                            boolean isUseDiscount = BusinessConfig.getInstance().getParam(context, FLAG_USE_DISCOUNT).equals("1") ? true : false;
                            if (isUseDiscount==false && isUseIntegral==false) {
                                transCode =SALE;
                                jumpToNext();
                            }else {
                                jumpToNext("4");
                            }
                        }else {
                            jumpToNext();
                        }
                    }
                }
            }
        }

        @Override
        public boolean onTradeTerminated() {
            gotTerminatedEvent = true;
            return false;
        }

        @Override
        public boolean onFallback() {
            isFallback = true;
            ViewUtils.showToast(context, R.string.tip_trade_fallback);
            pbocService.stopProcess();
            onRetry(true);
            DialogFactory.hideAll();
            return true;
        }

        @Override
        public boolean onNeedChangeUserFaces() {
            isFallback = true;
            ViewUtils.showToast(context, R.string.tip_read_card_failed);
            pbocService.stopProcess();
            DialogFactory.hideAll();
            activityStack.pop();
//            onRetry(true);
//            DialogFactory.hideAll();
            return true;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}
