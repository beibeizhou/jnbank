package com.centerm.jnbank.printer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.view.View;

import com.centerm.cloudsys.sdk.common.utils.FileUtils;
import com.centerm.cpay.midsdk.dev.common.exception.ErrorCode;
import com.centerm.jnbank.R;
import com.centerm.jnbank.activity.ResultActivity;
import com.centerm.jnbank.bean.TradePrintData;
import com.centerm.jnbank.common.TransCode;
import com.centerm.jnbank.db.CommonDao;
import com.centerm.jnbank.db.DbHelper;
import com.centerm.jnbank.task.AsyncUploadSignTask;
import com.centerm.jnbank.utils.DialogFactory;
import com.centerm.jnbank.utils.ViewUtils;
import com.centerm.jnbank.view.AlertDialog;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import config.BusinessConfig;
import config.Config;

import static com.centerm.jnbank.common.TransDataKey.iso_f11;

/**
 * Created by ysd on 2016/11/23.
 */

public class ShengPayPrinter implements PrintTransData.PrinterCallBack {
    private Logger logger = Logger.getLogger(ShengPayPrinter.class);
    private static ShengPayPrinter instance;
    private Context context;
    private Map<String, String> mapData;
    private CommonDao<TradePrintData> printDataCommonDao;
    private String transCode;
    private PrintTransData printTransData;
    private Bitmap bitmap;
    private ResultActivity activity;
    private boolean isReprint;//是否是重打印
    private String uploadTime;
    private ShengPayPrinter() {
    }

    public static ShengPayPrinter getMenuPrinter() {
        if (instance == null) {
            instance = new ShengPayPrinter();
        }
        return instance;
    }

    public void init(Context context) {
        activity = null;
        if (context instanceof ResultActivity) {
            activity = (ResultActivity) context;
        }
        this.context = context;
         uploadTime = BusinessConfig.getInstance().getParam(context, BusinessConfig.Key.PARAM_WHEN_UPLOAD);
    }

    public void printData(Map<String, String> mapData, String transCode, boolean isRePrint) {
        logger.debug("开始执行打印方法");
        this.isReprint = isRePrint;
        DbHelper dbHelper = new DbHelper(context);
        printDataCommonDao = new CommonDao<>(TradePrintData.class, dbHelper);
        this.mapData = mapData;
        this.transCode = transCode;
        try {
            printTransData = PrintTransData.getMenuPrinter();
            printTransData.open(context);
            ifElecSignThenGono();
            if ("1".equals(uploadTime)) {
                boolean isScan = false;
                if(transCode.equals(TransCode.SCAN_PAY_ALI)
                        || transCode.equals(TransCode.SCAN_PAY_SFT)
                        || transCode.equals(TransCode.SCAN_PAY_WEI)
                        || transCode.equals(TransCode.SCAN_CANCEL)
                        || transCode.equals(TransCode.SCAN_REFUND_W)
                        || transCode.equals(TransCode.SCAN_REFUND_Z)
                        || transCode.equals(TransCode.SCAN_REFUND_S))
                    isScan = true;
                if(!isReprint && !isScan)
                    requestSendBitmap();
            }
            List<TradePrintData> printDatas = null;
            try {
                printDatas = printDataCommonDao.queryBuilder().where().eq("iso_f11", mapData.get(iso_f11)).query();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            logger.debug("查询到要打印的IC数据对象为："+printDatas);
            if (null != printDatas && printDatas.size() > 0) {
                printTransData.printData(mapData,isRePrint,printDatas.get(0), context.getString(TransCode.codeMapName(transCode)),transCode, this);
            } else {

                printTransData.printData(mapData,isRePrint,null, context.getString(TransCode.codeMapName(transCode)), transCode,this);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 是否支持电子签名，且能读取到图片文件
     */
    private void ifElecSignThenGono() {
//        boolean isOpenSign = BusinessConfig.getInstance().getParam(context, BusinessConfig.Key.FLAG_IS_OPEN_SIGN).equals("1") ? true : false;
        boolean isOpenSign =true;
        if (isOpenSign) {
            String path = Config.Path.SIGN_PATH + File.separator + BusinessConfig.getInstance().getBatchNo(context) + "_" + mapData.get(iso_f11) + ".png";
            if (FileUtils.getFileSize(path) > 0) {
                logger.debug("签名图片存在");
                try {
                    FileInputStream inputStream = new FileInputStream(path);
                    bitmap = BitmapFactory.decodeStream(inputStream);
                    printTransData.setElecBitMap(bitmap);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            } else {
                logger.debug("签名图片不存在");
                printTransData.setElecBitMap(null);
            }
        } else {
            logger.debug("电子签名不开启");
            printTransData.setElecBitMap(null);
        }
    }
    @Override
    public void onPrinterFirstSuccess() {
        BusinessConfig config = BusinessConfig.getInstance();
        String printCount = config.getParam(context, BusinessConfig.Key.PARAM_PRINT_COUNT);
        switch (printCount) {
            case "1":
                DialogFactory.hideAll();
                if ("0".equals(uploadTime)) {
                    boolean isScan = false;
                    if(transCode.equals(TransCode.SCAN_PAY_ALI)
                            || transCode.equals(TransCode.SCAN_PAY_SFT)
                            || transCode.equals(TransCode.SCAN_PAY_WEI)
                            || transCode.equals(TransCode.SCAN_CANCEL)
                            || transCode.equals(TransCode.SCAN_REFUND_W)
                            || transCode.equals(TransCode.SCAN_REFUND_Z)
                            || transCode.equals(TransCode.SCAN_REFUND_S))
                        isScan = true;
                    if(!isReprint && !isScan)
                        requestSendBitmap();
                }
                ViewUtils.showToast(context, context.getString(R.string.tip_print_over));
                if (null != activity) {
                    activity.openPageTimeout(false);
                }
                break;
            case "2":
                DialogFactory.showPrintDialog(context, new AlertDialog.ButtonClickListener() {
                    @Override
                    public void onClick(AlertDialog.ButtonType button, View v) {
                        switch (button) {
                            case POSITIVE:
                                printTransData.printThird();
                                break;
                            case NEGATIVE:
                                DialogFactory.hideAll();
                                if ("0".equals(uploadTime)) {
                                    boolean isScan = false;
                                    if(transCode.equals(TransCode.SCAN_PAY_ALI)
                                            || transCode.equals(TransCode.SCAN_PAY_SFT)
                                            || transCode.equals(TransCode.SCAN_PAY_WEI)
                                            || transCode.equals(TransCode.SCAN_CANCEL)
                                            || transCode.equals(TransCode.SCAN_REFUND_W)
                                            || transCode.equals(TransCode.SCAN_REFUND_Z)
                                            || transCode.equals(TransCode.SCAN_REFUND_S))
                                        isScan = true;
                                    if(!isReprint && !isScan)
                                        requestSendBitmap();
                                }
                                ViewUtils.showToast(context, context.getString(R.string.tip_print_over));
                                if (null != activity) {
                                    activity.openPageTimeout(false);
                                }
                                break;
                        }
                    }
                });
                break;
            case "3":
                DialogFactory.showPrintDialog(context, new AlertDialog.ButtonClickListener() {
                    @Override
                    public void onClick(AlertDialog.ButtonType button, View v) {
                        switch (button) {
                            case POSITIVE:
                                printTransData.printSecond();
                                break;
                            case NEGATIVE:
                                DialogFactory.hideAll();
                                if ("0".equals(uploadTime)) {
                                    boolean isScan = false;
                                    if(transCode.equals(TransCode.SCAN_PAY_ALI)
                                            || transCode.equals(TransCode.SCAN_PAY_SFT)
                                            || transCode.equals(TransCode.SCAN_PAY_WEI)
                                            || transCode.equals(TransCode.SCAN_CANCEL)
                                            || transCode.equals(TransCode.SCAN_REFUND_W)
                                            || transCode.equals(TransCode.SCAN_REFUND_Z)
                                            || transCode.equals(TransCode.SCAN_REFUND_S))
                                        isScan = true;
                                    if(!isReprint && !isScan)
                                        requestSendBitmap();
                                }
                                ViewUtils.showToast(context, context.getString(R.string.tip_print_over));
                                if (null != activity) {
                                    activity.openPageTimeout(false);
                                }
                                break;
                        }
                    }
                });
                break;
        }
    }

    @Override
    public void onPrinterSecondSuccess() {
        DialogFactory.showPrintDialog(context, new AlertDialog.ButtonClickListener() {
            @Override
            public void onClick(AlertDialog.ButtonType button, View v) {
                switch (button) {
                    case POSITIVE:
                        printTransData.printThird();
                        break;
                    case NEGATIVE:
                        DialogFactory.hideAll();
                        if ("0".equals(uploadTime)) {
                            boolean isScan = false;
                            if(transCode.equals(TransCode.SCAN_PAY_ALI)
                                    || transCode.equals(TransCode.SCAN_PAY_SFT)
                                    || transCode.equals(TransCode.SCAN_PAY_WEI)
                                    || transCode.equals(TransCode.SCAN_CANCEL)
                                    || transCode.equals(TransCode.SCAN_REFUND_W)
                                    || transCode.equals(TransCode.SCAN_REFUND_Z)
                                    || transCode.equals(TransCode.SCAN_REFUND_S))
                                isScan = true;
                            if(!isReprint && !isScan)
                                requestSendBitmap();
                        }
                        ViewUtils.showToast(context, context.getString(R.string.tip_print_over));
                        if (null != activity) {
                            activity.openPageTimeout(false);
                        }
                        break;
                }
            }
        });
    }

    @Override
    public void onPrinterThreeSuccess() {
        DialogFactory.hideAll();
        if ("0".equals(uploadTime)) {
            boolean isScan = false;
            if(transCode.equals(TransCode.SCAN_PAY_ALI)
                    || transCode.equals(TransCode.SCAN_PAY_SFT)
                    || transCode.equals(TransCode.SCAN_PAY_WEI)
                    || transCode.equals(TransCode.SCAN_CANCEL)
                    || transCode.equals(TransCode.SCAN_REFUND_W)
                    || transCode.equals(TransCode.SCAN_REFUND_Z)
                    || transCode.equals(TransCode.SCAN_REFUND_S))
                isScan = true;
            if(!isReprint && !isScan)
                requestSendBitmap();
        }
        ViewUtils.showToast(context, context.getString(R.string.tip_print_over));
        if (null != activity) {
            activity.openPageTimeout(false);
        }
    }

    private void requestSendBitmap() {
        logger.debug("进入【电子签名】上送方法");
        if (bitmap != null && !bitmap.isRecycled()) {
            new AsyncUploadSignTask(context, bitmap, mapData) {
                @Override
                public void onFinish(String[] strings) {
                    super.onFinish(strings);
                    if (bitmap != null && !bitmap.isRecycled())
                        bitmap.recycle();
                    logger.debug("电子签名方法出来");
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, transCode);
        } else {
            logger.debug("没有电子签名图片或者被销毁");
        }
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
                new AlertDialog.ButtonClickListener() {
                    @Override
                    public void onClick(AlertDialog.ButtonType button, View v) {
                        switch (button) {
                            case POSITIVE:
                                printTransData.printFirst();
                                break;
                            case NEGATIVE:
                                DialogFactory.hideAll();
                                if ("0".equals(uploadTime)) {
                                    boolean isScan = false;
                                    if(transCode.equals(TransCode.SCAN_PAY_ALI)
                                            || transCode.equals(TransCode.SCAN_PAY_SFT)
                                            || transCode.equals(TransCode.SCAN_PAY_WEI)
                                            || transCode.equals(TransCode.SCAN_CANCEL)
                                            || transCode.equals(TransCode.SCAN_REFUND_W)
                                            || transCode.equals(TransCode.SCAN_REFUND_Z)
                                            || transCode.equals(TransCode.SCAN_REFUND_S))
                                        isScan = true;
                                    if(!isReprint && !isScan)
                                        requestSendBitmap();
                                }
                                if (bitmap != null && !bitmap.isRecycled()) {
                                    bitmap.recycle();
                                }
                                if (null != activity) {
                                    activity.openPageTimeout(false);
                                }
                                break;
                        }
                    }
                });
    }

    @Override
    public void onPrinterSecondFail(int errorCode, String errorMsg) {
        if (errorCode == ErrorCode.PRINTER_ERROR.ERR_NO_PAPER)
            errorMsg = "打印机缺纸，请放入打印纸";
        final int eCode = errorCode;
        final String eMsg = errorMsg;
        DialogFactory.showSelectPirntDialog(context,
                "提示",
                errorMsg,
                new AlertDialog.ButtonClickListener() {
                    @Override
                    public void onClick(AlertDialog.ButtonType button, View v) {
                        switch (button) {
                            case POSITIVE:
                                printTransData.printSecond();
                                break;
                            case NEGATIVE:
                                DialogFactory.hideAll();
                                boolean isScan = false;
                                if(transCode.equals(TransCode.SCAN_PAY_ALI)
                                        || transCode.equals(TransCode.SCAN_PAY_SFT)
                                        || transCode.equals(TransCode.SCAN_PAY_WEI)
                                        || transCode.equals(TransCode.SCAN_CANCEL)
                                        || transCode.equals(TransCode.SCAN_REFUND_W)
                                        || transCode.equals(TransCode.SCAN_REFUND_Z)
                                        || transCode.equals(TransCode.SCAN_REFUND_S))
                                    isScan = true;
                                if(!isReprint && !isScan)
                                requestSendBitmap();
                                if (bitmap != null && !bitmap.isRecycled()) {
                                    bitmap.recycle();
                                }
                                if (null != activity) {
                                    activity.openPageTimeout(false);
                                }
                                break;
                        }
                    }
                });
    }

    @Override
    public void onPrinterThreeFail(int errorCode, String errorMsg) {
        if (errorCode == ErrorCode.PRINTER_ERROR.ERR_NO_PAPER)
            errorMsg = "打印机缺纸，请放入打印纸";
        final int eCode = errorCode;
        final String eMsg = errorMsg;
        DialogFactory.showSelectPirntDialog(context,
                "提示",
                errorMsg,
                new AlertDialog.ButtonClickListener() {
                    @Override
                    public void onClick(AlertDialog.ButtonType button, View v) {
                        switch (button) {
                            case POSITIVE:
                                printTransData.printThird();
                                break;
                            case NEGATIVE:
                                DialogFactory.hideAll();
                                if ("0".equals(uploadTime)) {
                                    boolean isScan = false;
                                    if(transCode.equals(TransCode.SCAN_PAY_ALI)
                                            || transCode.equals(TransCode.SCAN_PAY_SFT)
                                            || transCode.equals(TransCode.SCAN_PAY_WEI)
                                            || transCode.equals(TransCode.SCAN_CANCEL)
                                            || transCode.equals(TransCode.SCAN_REFUND_W)
                                            || transCode.equals(TransCode.SCAN_REFUND_Z)
                                            || transCode.equals(TransCode.SCAN_REFUND_S))
                                        isScan = true;
                                    if(!isReprint && !isScan)
                                        requestSendBitmap();
                                }
                                if (bitmap != null && !bitmap.isRecycled()) {
                                    bitmap.recycle();
                                }
                                if (null != activity) {
                                    activity.openPageTimeout(false);
                                }
                                break;
                        }
                    }
                });
    }
}
