package com.centerm.jnbank.task;

import android.content.Context;
import android.text.TextUtils;

import com.centerm.cpay.midsdk.dev.DeviceFactory;
import com.centerm.cpay.midsdk.dev.define.IPbocService;
import com.centerm.cpay.midsdk.dev.define.pboc.EnumAidCapkOperation;
import com.centerm.jnbank.bean.QpsBinData;
import com.centerm.jnbank.common.Settings;
import com.centerm.jnbank.db.CommonDao;
import com.centerm.jnbank.db.DbHelper;
import com.j256.ormlite.table.TableUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import config.BusinessConfig;
import jxl.Sheet;
import jxl.Workbook;

/**
 * 程序初始化的异步任务。需要执行的任务为：初始化默认的IC卡公钥参数、AID参数；导入默认的卡BIN表
 * author:wanliang527</br>
 * date:2016/12/1</br>
 */

public abstract class AsyncAppInitTask extends BaseAsyncTask {

    private final static int taskCounts = 3;
    private final static int MAX_RETRY_TIMES = 3;
    private int retryTimes = 0;

    public AsyncAppInitTask(Context context) {
        super(context);
    }

    @Override
    protected Object doInBackground(Object[] params) {
        logger.debug("应用初始化==>导入卡BIN表");
        sleep(LONG_SLEEP);
        //CommonUtils.readExcelToDB(context);
        logger.debug("应用初始化==>导入卡BIN表完成");
        logger.debug("应用初始化==>导入小额免密免签业务卡BIN表");
        sleep(MEDIUM_SLEEP);
        doImportQpsBinSheet();
        logger.debug("应用初始化==>导入小额免密免签业务卡BIN表完成");
        retryTimes = 0;
        while (retryTimes < MAX_RETRY_TIMES) {
            retryTimes++;
            logger.debug("应用初始化==>尝试导入AID参数第" + retryTimes + "次");
            sleep(LONG_SLEEP);
            if (doImportDefaultAid()) {
                break;
            }
            logger.warn("应用初始化==>尝试导入AID参数失败");
        }
        retryTimes = 0;
        while (retryTimes < MAX_RETRY_TIMES) {
            retryTimes++;
            logger.debug("应用初始化==>尝试导入公钥参数第" + retryTimes + "次");
            sleep(LONG_SLEEP);
            if (doImportDefaultCapk()) {
                break;
            }
            logger.warn("应用初始化==>尝试导入公钥参数失败");
        }
        Settings.setValue(context, Settings.KEY.FIRST_TIME_LOADING, false);
        return null;
    }


    private boolean doImportDefaultAid() {
        try {
            IPbocService pbocService = DeviceFactory.getInstance().getPbocService();
            logger.info("正在导入默认AID参数");
            for (int i = 0; i < BusinessConfig.AID.length; i++) {
                String aidValue = BusinessConfig.AID[i];
                pbocService.updateAID(EnumAidCapkOperation.UPDATE, aidValue);
            }
            Settings.setValue(context, Settings.KEY.IC_AID_VERSION, "000000");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean doImportDefaultCapk() {
        try {
            IPbocService pbocService = DeviceFactory.getInstance().getPbocService();
            logger.info("正在导入默认CAPK参数");
            for (int i = 0; i < BusinessConfig.CAPK.length; i++) {
                String capk = BusinessConfig.CAPK[i];
                pbocService.updateCAPK(EnumAidCapkOperation.UPDATE, capk);
            }
            Settings.setValue(context, Settings.KEY.IC_CAPK_VERSION, "000000");
            logger.info("导入默认CAPK参数成功");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void doImportQpsBinSheet() {
        try {
            List<QpsBinData> datas = new ArrayList<>();
            DbHelper dbHelper = new DbHelper(context);
            TableUtils.dropTable(dbHelper.getDao(QpsBinData.class),true);
            TableUtils.createTable(dbHelper.getDao(QpsBinData.class));
            CommonDao<QpsBinData> commonDao = new CommonDao<>(QpsBinData.class, dbHelper);
            InputStream is = context.getAssets().open("QPS_BIN_DATA.xls");
            Workbook book = Workbook.getWorkbook(is);
            book.getNumberOfSheets();
            //第一个工作表时BIN表A
            Sheet sheetA = book.getSheet(0);
            int rows = sheetA.getRows();
            logger.debug("BIN表A共有数据" + rows + "条");
            for (int i = 1; i < rows; ++i) {
                String content = sheetA.getCell(1, i).getContents();
                if (TextUtils.isEmpty(content) || content.length() < 8) {
                    logger.warn("BIN表A中含有非法数据==>" + content);
                    continue;
                }
                String type = "A";
                int cardLen = Integer.valueOf(content.substring(0, 2));
                String cardBin = content.substring(2, content.length());
                QpsBinData bin = new QpsBinData(type, cardBin, cardLen);
                datas.add(bin);
                if (i % 50 == 0 || i == rows - 1) {
                    commonDao.save(datas);
                    datas.clear();
                }
            }
            //第二个工作表是BIN表B
            Sheet sheetB = book.getSheet(1);
            rows = sheetB.getRows();
            logger.debug("BIN表B共有数据" + rows + "条");
            for (int i = 1; i < rows; ++i) {
                String content = sheetB.getCell(1, i).getContents();
                if (TextUtils.isEmpty(content) || content.length() < 8) {
                    logger.warn("BIN表B中含有非法数据==>" + content);
                    continue;
                }
                String type = "B";
                int cardLen = Integer.valueOf(content.substring(0, 2));
                String cardBin = content.substring(2, content.length());
                QpsBinData bin = new QpsBinData(type, cardBin, cardLen);
                datas.add(bin);
                if (i % 50 == 0 || i == rows - 1) {
                    commonDao.save(datas);
                    datas.clear();
                }
            }
            datas.clear();
            book.close();
            //设置免密免签业务卡BIN表存在的标识
            Settings.setValue(context, Settings.KEY.QPS_BIN_EXISTS, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
