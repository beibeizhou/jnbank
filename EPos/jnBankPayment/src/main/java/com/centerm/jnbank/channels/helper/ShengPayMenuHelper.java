package com.centerm.jnbank.channels.helper;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.View;
import android.widget.EditText;

import com.centerm.cloudsys.sdk.common.utils.FileUtils;
import com.centerm.cpay.midsdk.dev.common.exception.ErrorCode;
import com.centerm.jnbank.R;
import com.centerm.jnbank.activity.LoginActivity;
import com.centerm.jnbank.activity.TradingActivity;
import com.centerm.jnbank.activity.msn.QueryTradeActivity;
import com.centerm.jnbank.activity.msn.SettingMarchantActivity;
import com.centerm.jnbank.activity.msn.SettingSystemActivity;
import com.centerm.jnbank.activity.msn.SettingTransationActivity;
import com.centerm.jnbank.base.BaseActivity;
import com.centerm.jnbank.bean.ReverseInfo;
import com.centerm.jnbank.bean.TradeInfo;
import com.centerm.jnbank.bean.TradePrintData;
import com.centerm.jnbank.common.Settings;
import com.centerm.jnbank.common.TransCode;
import com.centerm.jnbank.db.CommonDao;
import com.centerm.jnbank.db.CommonManager;
import com.centerm.jnbank.db.DbHelper;
import com.centerm.jnbank.printer.PrintTransData;
import com.centerm.jnbank.printer.ShengPayPrinter;
import com.centerm.jnbank.task.AsyncBatchSettleDown;
import com.centerm.jnbank.task.AsyncClearData;
import com.centerm.jnbank.task.AsyncQueryPrintDataTask;
import com.centerm.jnbank.utils.DialogFactory;
import com.centerm.jnbank.utils.ViewUtils;
import com.centerm.jnbank.xml.XmlTag;
import com.centerm.jnbank.xml.menu.MenuItem;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import config.BusinessConfig;
import config.Config;

import static com.centerm.jnbank.base.BaseActivity.KEY_TRANSCODE;
import static com.centerm.jnbank.common.TransCode.PRINT_BATCH_SUMMARY;
import static com.centerm.jnbank.view.AlertDialog.ButtonClickListener;
import static com.centerm.jnbank.view.AlertDialog.ButtonType;
import static com.centerm.jnbank.xml.XmlTag.MenuTag.CLEAR_TRADE_SERIAL;
import static com.centerm.jnbank.xml.XmlTag.MenuTag.LOCK;
import static com.centerm.jnbank.xml.XmlTag.MenuTag.MERCHANTS_SETTINGS;
import static com.centerm.jnbank.xml.XmlTag.MenuTag.OPER_SIGN_IN;
import static com.centerm.jnbank.xml.XmlTag.MenuTag.PRINT_DETAIL;
import static com.centerm.jnbank.xml.XmlTag.MenuTag.PRINT_LAST;
import static com.centerm.jnbank.xml.XmlTag.MenuTag.PRINT_SUMMARY;
import static com.centerm.jnbank.xml.XmlTag.MenuTag.SCAN_PARAM;
import static com.centerm.jnbank.xml.XmlTag.MenuTag.SETTLE_SET;
import static com.centerm.jnbank.xml.XmlTag.MenuTag.SYSTEM_SETTINGS;
import static com.centerm.jnbank.xml.XmlTag.MenuTag.TRANS_CARD;
import static com.centerm.jnbank.xml.XmlTag.MenuTag.TRANS_OTHER;
import static com.centerm.jnbank.xml.XmlTag.MenuTag.TRANS_PSW;
import static com.centerm.jnbank.xml.XmlTag.MenuTag.TRANS_SIGN;
import static com.centerm.jnbank.xml.XmlTag.MenuTag.TRANS_SWITCH;
import static com.centerm.jnbank.xml.XmlTag.MenuTag.VISA_FREE_PARAM;

/**
 * author:wanliang527</br>
 * 没有复杂的页面跳转流程，就放这里处理
 * date:2016/10/26</br>
 */

public class ShengPayMenuHelper extends BaseMenuHelper implements PrintTransData.PrinterCallBack {
    private Context context;
    private Context mContext;
    private CommonDao<TradeInfo> tradeDao;
    private CommonDao<ReverseInfo> infoCommonDao;
    private CommonDao<TradePrintData> printDataCommonDao;
    private boolean isTrue;
    private PrintTransData printTransData;
    private List<List<String>> lists;
    private CommonManager commonManager;

    @Override
    public boolean onTriggerMenuItem(final Context context, MenuItem item) {
        this.context = context;
        this.mContext = context;
        BaseActivity baseActivity = (BaseActivity) context;
        commonManager = new CommonManager(TradeInfo.class, context);
        tradeDao = new CommonDao<>(TradeInfo.class, baseActivity.getDbHelper());
        infoCommonDao = new CommonDao<>(ReverseInfo.class, baseActivity.getDbHelper());
        printDataCommonDao = new CommonDao<>(TradePrintData.class, baseActivity.getDbHelper());
        String tag = item.getEntag();
        switch (tag) {
            case XmlTag.MenuTag.INJECT_TEK:
                onTriggerInjectTek(context);
                return true;
            case XmlTag.MenuTag.QUERY:
                jumpTo(context, QueryTradeActivity.class);
                return true;
            case XmlTag.MenuTag.SETTLEMENT:
                readyToSettleDown();
                return true;
            case XmlTag.MenuTag.SIGN_OUT:
                readyToSignOut();
                return true;
            case XmlTag.MenuTag.PRINT_LAST:
                printData(PRINT_LAST);
                return true;
            case XmlTag.MenuTag.PRINT_ANY:
                jumpTo(context, QueryTradeActivity.class);
                return true;
            case PRINT_SUMMARY:
                printData(PRINT_SUMMARY);
                return true;
            case XmlTag.MenuTag.PRINT_DETAIL:
                printData(PRINT_DETAIL);
                return true;
            case XmlTag.MenuTag.PRINT_BATCH_SUMMARY:
                printData(PRINT_BATCH_SUMMARY);
                return true;
            case MERCHANTS_SETTINGS:
                /*if (Settings.hasInit(context)) {
                    jumpTo(context, SettingMarchantActivity.class);
                } else {
                    ViewUtils.showToast(context, context.getString(R.string.tip_init_first));
                }*/
                jumpTo(context, SettingMarchantActivity.class);
                return true;
            case SYSTEM_SETTINGS:
                jumpTo(context, SettingSystemActivity.class);
                return true;
            case TRANS_SWITCH:
                Intent intent = new Intent(context,SettingTransationActivity.class);
                intent.putExtra("TYPE", "TRANS_SWITCH");
                context.startActivity(intent);
                return true;
            case TRANS_PSW:
                Intent intent1 = new Intent(context,SettingTransationActivity.class);
                intent1.putExtra("TYPE", "TRANS_PSW");
                context.startActivity(intent1);
                return true;
            case TRANS_CARD:
                Intent intent2 = new Intent(context,SettingTransationActivity.class);
                intent2.putExtra("TYPE", "TRANS_CARD");
                context.startActivity(intent2);
                return true;
            case SETTLE_SET:
                Intent intent3 = new Intent(context,SettingTransationActivity.class);
                intent3.putExtra("TYPE", "SETTLE_SET");
                context.startActivity(intent3);
                return true;
            case TRANS_OTHER:
                Intent intent4 = new Intent(context,SettingTransationActivity.class);
                intent4.putExtra("TYPE", "TRANS_OTHER");
                context.startActivity(intent4);
                return true;
            case TRANS_SIGN:
                Intent intent5 = new Intent(context,SettingTransationActivity.class);
                intent5.putExtra("TYPE", "TRANS_SIGN");
                context.startActivity(intent5);
                return true;
            case VISA_FREE_PARAM:
                Intent intent10 = new Intent(context,SettingTransationActivity.class);
                intent10.putExtra("TYPE", "VISA_FREE_PARAM");
                context.startActivity(intent10);
                return true;
            case SCAN_PARAM:
                Intent intent6 = new Intent(context,SettingTransationActivity.class);
                intent6.putExtra("TYPE", "SCAN_PARAM");
                context.startActivity(intent6);
                return true;
            case OPER_SIGN_IN:
                jumpTo(context, LoginActivity.class);
                return true;
            case LOCK:
                BusinessConfig.getInstance().setFlag(context, BusinessConfig.Key.KEY_IS_LOCK, true);
                DialogFactory.showLockDialog(context);
                return true;
            case CLEAR_TRADE_SERIAL:
                new AsyncClearData(context) {
                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        DialogFactory.showLoadingDialog(mContext, mContext.getString(R.string
                                .tip_query_flow));
                    }

                    @Override
                    public void onFinish(Object o) {
                        super.onFinish(o);
                        if (o instanceof Boolean && (Boolean) o) {
                            DialogFactory.showSelectDialog(mContext, mContext.getString(R.string
                                    .tip_notification), mContext.getString(R.string.tip_clear_trans_data), new
                                    ButtonClickListener() {
                                        @Override
                                        public void onClick(ButtonType button, View v) {
                                            switch (button) {
                                                case POSITIVE:
                                                    boolean delSuccess = clearTransData();
                                                    if (delSuccess) {
                                                        //清空电子签名的文件夹
                                                        String path = Config.Path.SIGN_PATH;
                                                        if (FileUtils.getFileSize(path) > 0) {
                                                            FileUtils.deleteAllFiles(path);
                                                        }
                                                        ViewUtils.showToast(mContext, mContext.getString(R.string
                                                                .tip_clear_data_over));
                                                    } else {
                                                        ViewUtils.showToast(mContext, mContext.getString(R.string
                                                                .tip_clear_data_error));
                                                    }
                                                    break;
                                            }
                                        }
                                    });
                        } else {
                            ViewUtils.showToast(mContext, "当前无交易流水");
                            DialogFactory.hideAll();
                        }
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                return true;
        }
        return false;
    }

    /**
     * 清除交易流水
     *
     * @return
     */
    private boolean clearTransData() {
        if (tradeDao.deleteByWhere("1=1")) {
            if (infoCommonDao.deleteByWhere("1=1")) {
                if (printDataCommonDao.deleteByWhere("1=1")) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * 批结算
     */
    private void readyToSettleDown() {
        logger.debug("点击结算1111111111");
        String isBatch = Settings.getValue(context, Settings.KEY.BATCH_SEND_STATUS, "0");
        boolean isSignIn = BusinessConfig.getInstance().getFlag(context, BusinessConfig.Key
                .FLAG_SIGN_IN);
        logger.debug("点击结算22222222");
        if (isSignIn) {
            logger.debug("点击结算333333333");
            if ("2".equals(isBatch)) {
                logger.debug("点击结算，已经完成批结算，请签退");
                ViewUtils.showToast(context, context.getString(R.string
                        .tip_batch_over_please_sign_out));
            } else {
                logger.debug("点击结算4444444444");
                new AsyncBatchSettleDown(context) {
                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        DialogFactory.showLoadingDialog(mContext, mContext.getString(R.string
                                .tip_query_flow));
                    }

                    @Override
                    public void onFinish(Object o) {
                        logger.debug("点击结算执行完成AsyncBatchSettleDown1111111");
                        super.onFinish(o);
//                        if (o instanceof Boolean && (Boolean) o) {
                            logger.debug("点击结算执行完成AsyncBatchSettleDown22222222");
                            DialogFactory.showSelectDialog(mContext, mContext.getString(R.string
                                    .tip_notification), mContext.getString(R.string
                                    .tip_comfirm_batch), new ButtonClickListener() {
                                @Override
                                public void onClick(ButtonType button, View v) {
                                    switch (button) {
                                        case POSITIVE:
                                            logger.debug("点击结算执行完成AsyncBatchSettleDown3333333333");
                                            Intent intent = new Intent(context, TradingActivity
                                                    .class);
                                            intent.putExtra(KEY_TRANSCODE, TransCode.SETTLEMENT);
                                            mContext.startActivity(intent);
                                            break;
                                    }
                                }
                            });
//                        } else {
//                            DialogFactory.hideAll();
//                            ViewUtils.showToast(mContext, mContext.getString(R.string
//                                    .tip_no_trans_flow));
//                        }
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                logger.debug("点击结算555555555555");
            }
        } else {
            ViewUtils.showToast(context, context.getString(R.string.tip_no_sign_in_status));
        }
    }


    /**
     * 触发TEK注入的业务
     */
    private void onTriggerInjectTek(Context context) {
        Dialog dialog = new AlertDialog.Builder(context, android.R.style
                .Theme_Material_Light_Dialog_Alert)
                .setTitle("请写入TEK明文")
                .setView(new EditText(context))
                .create();
        dialog.show();
    }

    /**
     * 最后一笔交易及明细打印处理
     *
     * @param transCode
     */
    private void printData(String transCode) {
        try {
            DbHelper dbHelper = OpenHelperManager.getHelper(context, DbHelper.class);
            Dao<TradeInfo, String> tradeDao = dbHelper.getDao(TradeInfo.class);
            ShengPayPrinter shengPayPrinter = ShengPayPrinter.getMenuPrinter();
            shengPayPrinter.init(context);
            printTransData = PrintTransData.getMenuPrinter();
            printTransData.open(context);
            switch (transCode) {
                case TransCode.PRINT_LAST:
                    List<TradeInfo> tradeInfos = commonManager.getLastTransItem();
                    if (null != tradeInfos && tradeInfos.size() > 0) {
                        TradeInfo info = tradeInfos.get(0);
                        Map<String, String> mapData = info.convert2Map();
                        shengPayPrinter.printData(mapData, info.getTransCode(), true);
                    } else {
                        ViewUtils.showToast(context, context.getString(R.string.tip_no_trade_info));
                    }
                    break;
                case TransCode.PRINT_DETAIL:
                    new AsyncQueryPrintDataTask(mContext) {
                        @Override
                        public void onStart() {
                            super.onStart();
                            DialogFactory.showLoadingDialog(mContext, mContext.getString(R.string
                                    .tip_query_flow));
                        }

                        @Override
                        public void onFinish(List<List<TradeInfo>> lists) {
                            super.onFinish(lists);
                            List<TradeInfo> infoDetail = lists.get(5);
                            if (null != infoDetail && infoDetail.size() > 0) {
                                printTransData.printDetails(infoDetail);
                            } else {
                                ViewUtils.showToast(mContext, mContext.getString(R.string
                                        .tip_no_trade_info));
                                DialogFactory.hideAll();
                            }
                        }
                    }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                    break;
                case TransCode.PRINT_BATCH_SUMMARY:
                        String s = Settings.getValue(context, Settings.KEY.PREV_BATCH_TOTAL, "");
                        if (!"".equals(s)) {
                            printTransData.setBatchListener(this);
                            printTransData.printLastTotalData(s);
                        } else {
                            ViewUtils.showToast(context, context.getString(R.string
                                    .tip_no_pre_batch_total));
                        }
                    break;
                case TransCode.PRINT_SUMMARY:
                    printTotalData();
                    break;
                default:
                    logger.warn("打印==>未知交易码：" + transCode);
                    break;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void printTotalData() {
        new AsyncQueryPrintDataTask(context) {
            @Override
            public void onStart() {
                super.onStart();
                DialogFactory.showLoadingDialog(mContext, mContext.getString(R.string
                        .tip_query_flow));
            }

            @Override
            public void onFinish(List<List<TradeInfo>> lists) {
                super.onFinish(lists);
                beginToPrint(lists);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    private void beginToPrint(List<List<TradeInfo>> lists) {
        PrintTransData printTransData = PrintTransData.getMenuPrinter();
        printTransData.open(context);
        printTransData.printDataALLTrans(lists);
    }
    /**
     * 签退
     */
    private void readyToSignOut() {
        String isBatchSucc = Settings.getValue(context, Settings.KEY.BATCH_SEND_STATUS, "0");
        final boolean isSignIn = BusinessConfig.getInstance().getFlag(context, BusinessConfig.Key
                .FLAG_SIGN_IN);
        if ("2".equals(isBatchSucc)) {
            if (isSignIn) {
                Intent signOut = new Intent(context, TradingActivity.class);
                signOut.putExtra(KEY_TRANSCODE, TransCode.SIGN_OUT);
                context.startActivity(signOut);
            } else {
                ViewUtils.showToast(context, "未签到状态，无需签退！");
            }
        } else if ("0".equals(isBatchSucc)) {
            new AsyncBatchSettleDown(context) {
                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    DialogFactory.showLoadingDialog(mContext, mContext.getString(R.string
                            .tip_query_flow));
                }

                @Override
                public void onFinish(Object o) {
                    super.onFinish(o);
                    if (o instanceof Boolean && (Boolean) o) {
                        DialogFactory.hideAll();
                        ViewUtils.showToast(mContext, "请先完成批结算！");
                    } else {
                        if (isSignIn) {
                            signOut();
                        } else {
                            ViewUtils.showToast(mContext, "未签到状态，无需签退！");
                        }
                        DialogFactory.hideAll();
                    }
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            ViewUtils.showToast(context, "请先完成批结算！");
        }
    }

    private void signOut() {
        Intent signOut = new Intent(context, TradingActivity.class);
        signOut.putExtra(KEY_TRANSCODE, TransCode.SIGN_OUT);
        context.startActivity(signOut);
    }

    @Override
    public void onPrinterFirstSuccess() {
        DialogFactory.hideAll();
    }

    @Override
    public void onPrinterSecondSuccess() {

    }

    @Override
    public void onPrinterThreeSuccess() {

    }

    @Override
    public void onPrinterFirstFail(int errorCode, String errorMsg) {
        if (errorCode == ErrorCode.PRINTER_ERROR.ERR_NO_PAPER)
            errorMsg = "打印机缺纸，请放入打印纸";
        final int eCode = errorCode;
        final String eMsg = errorMsg;
        DialogFactory.showSelectPirntDialog(context,
                "提示",
                errorMsg,
                new com.centerm.jnbank.view.AlertDialog.ButtonClickListener() {
                    @Override
                    public void onClick(com.centerm.jnbank.view.AlertDialog.ButtonType button, View
                            v) {
                        switch (button) {
                            case POSITIVE:
                                String s = Settings.getValue(context, Settings.KEY.PREV_BATCH_TOTAL, "");
                                if (!"".equals(s)) {
                                    printTransData.printLastTotalData(s);
                                } else {
                                    ViewUtils.showToast(context, context.getString(R.string
                                            .tip_no_pre_batch_total));
                                }
                                break;
                            case NEGATIVE:
                                DialogFactory.hideAll();
                                break;
                        }
                    }
                });
    }

    @Override
    public void onPrinterSecondFail(int errorCode, String errorMsg) {

    }

    @Override
    public void onPrinterThreeFail(int errorCode, String errorMsg) {

    }
}
