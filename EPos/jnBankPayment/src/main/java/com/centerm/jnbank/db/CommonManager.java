package com.centerm.jnbank.db;

import android.content.Context;

import com.centerm.jnbank.bean.TradeInfo;
import com.centerm.jnbank.common.TransCode;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.Where;

import java.sql.SQLException;
import java.util.List;

import config.BusinessConfig;
import config.Config;

/**
 * Created by ysd on 2016/12/13.
 */

public class CommonManager<T extends Object> {

    private final CommonDao commonDao;
    private Context context;
    public CommonManager(Class<T> clz, Context context){
        this.context = context;
        DbHelper dbHelper = new DbHelper(context);
        commonDao = new CommonDao<>(clz,dbHelper);
    }
    //获取借记交易列表
    public List<T> getDebitList() throws SQLException {
        Where<T, String> jiejiWhere = commonDao.queryBuilder().where();
        jiejiWhere.or(
                jiejiWhere.and(jiejiWhere.ne("flag", "0"),jiejiWhere.ne("flag", "3"), jiejiWhere.eq("transCode", TransCode.SALE)),
                jiejiWhere.and(jiejiWhere.ne("flag", "0"),jiejiWhere.ne("flag", "3"), jiejiWhere.eq("transCode", TransCode.AUTH_COMPLETE)),
                jiejiWhere.and(jiejiWhere.ne("flag", "0"),jiejiWhere.ne("flag", "3"), jiejiWhere.eq("transCode", TransCode.SCAN_PAY_ALI)),
                jiejiWhere.and(jiejiWhere.ne("flag", "0"),jiejiWhere.ne("flag", "3"), jiejiWhere.eq("transCode", TransCode.SCAN_PAY_SFT)),
                jiejiWhere.and(jiejiWhere.ne("flag", "0"),jiejiWhere.ne("flag", "3"), jiejiWhere.eq("transCode", TransCode.SCAN_PAY_WEI)));
        List<T> jiejiList = jiejiWhere.query();
        return jiejiList;
    }
    //获取贷记交易列表
    public List<T> getCreditList() throws SQLException {
        Where<T, String> daijiWhere = commonDao.queryBuilder().where();
        daijiWhere.or(
                daijiWhere.and(daijiWhere.ne("flag", "0"),daijiWhere.ne("flag", "3"), daijiWhere.eq("transCode", TransCode.REFUND)),
                daijiWhere.and(daijiWhere.ne("flag", "0"),daijiWhere.ne("flag", "3"), daijiWhere.eq("transCode", TransCode.SCAN_PAY_WEI)),
                daijiWhere.and(daijiWhere.ne("flag", "0"),daijiWhere.ne("flag", "3"), daijiWhere.eq("transCode", TransCode.SCAN_REFUND_W)),
                daijiWhere.and(daijiWhere.ne("flag", "0"),daijiWhere.ne("flag", "3"), daijiWhere.eq("transCode", TransCode.SCAN_REFUND_Z)),
                daijiWhere.and(daijiWhere.ne("flag", "0"),daijiWhere.ne("flag", "3"), daijiWhere.eq("transCode", TransCode.SCAN_REFUND_S)));
        List<T> daijiList = daijiWhere.query();
        return daijiList;
    }
    //获取交易明细列表
    public List<T> getTransDetail() throws SQLException {
        Where<T, String> where = commonDao.queryBuilder().orderBy("iso_f11", true).where();
        where.or(
                where.and(where.ne("flag", "0"),where.ne("flag", "3"), where.eq("transCode", TransCode.SALE)),
                where.and(where.ne("flag", "0"),where.ne("flag", "3"), where.eq("transCode", TransCode.REFUND)),
                where.and(where.ne("flag", "0"),where.ne("flag", "3"), where.eq("transCode", TransCode.AUTH_COMPLETE)),
                where.and(where.ne("flag", "0"),where.ne("flag", "3"), where.eq("transCode", TransCode.SCAN_PAY_ALI)),
                where.and(where.ne("flag", "0"),where.ne("flag", "3"), where.eq("transCode", TransCode.SCAN_PAY_SFT)),
                where.and(where.ne("flag", "0"),where.ne("flag", "3"), where.eq("transCode", TransCode.SCAN_PAY_WEI)),
                where.and(where.ne("flag", "0"),where.ne("flag", "3"), where.eq("transCode", TransCode.SCAN_REFUND_W)),
                where.and(where.ne("flag", "0"),where.ne("flag", "3"), where.eq("transCode", TransCode.SCAN_REFUND_Z)),
                where.and(where.ne("flag", "0"),where.ne("flag", "3"), where.eq("transCode", TransCode.SCAN_REFUND_S)));
        List<T> saleDetailList = where.query();
        return saleDetailList;
    }

    //获取批结算被拒绝的交易流水
    public List<T> getRefusedList() throws SQLException {
        return commonDao.queryBuilder().orderBy("iso_f11", true).where().eq("sendCount", 99).query();
    }
    //批结算时获取上送失败的交易流水
    public List<T> getFailList() throws SQLException {
        return commonDao.queryBuilder().orderBy("iso_f11", true)
                .where().ge("sendCount", Config.BATCH_MAX_UPLOAD_TIMES).and().ne("sendCount", 99)
                .and().eq("isBatchSuccess", false).query();
    }
    //获取可以批结算的交易流水
    public List<T> getBatchList() throws SQLException {
        boolean isPrintVoidDetail = BusinessConfig.getInstance().getParam(context, BusinessConfig.Key.FLAG_PRINT_VOID_DETAIL).equals("1")?true:false;
        Where<T, String> where = commonDao.queryBuilder().where();
        if (isPrintVoidDetail) {
            where.or(
                    where.and(where.ne("flag", "0"),where.ne("flag", "3"), where.eq("transCode", TransCode.SALE)),
                    where.and(where.ne("flag", "0"),where.ne("flag", "3"), where.eq("transCode", TransCode.VOID)),
                    where.and(where.ne("flag", "0"),where.ne("flag", "3"), where.eq("transCode", TransCode.REFUND)),
                    where.and(where.ne("flag", "0"),where.ne("flag", "3"), where.eq("transCode", TransCode.AUTH_COMPLETE)),
                    where.and(where.ne("flag", "0"),where.ne("flag", "3"), where.eq("transCode", TransCode.COMPLETE_VOID)),
                    where.and(where.ne("flag", "0"),where.ne("flag", "3"), where.eq("transCode", TransCode.SCAN_PAY_ALI)),
                    where.and(where.ne("flag", "0"),where.ne("flag", "3"), where.eq("transCode", TransCode.SCAN_PAY_SFT)),
                    where.and(where.ne("flag", "0"),where.ne("flag", "3"), where.eq("transCode", TransCode.SCAN_PAY_WEI)),
                    where.and(where.ne("flag", "0"),where.ne("flag", "3"), where.eq("transCode", TransCode.SCAN_CANCEL)),
                    where.and(where.ne("flag", "0"),where.ne("flag", "3"), where.eq("transCode", TransCode.SCAN_REFUND_W)),
                    where.and(where.ne("flag", "0"),where.ne("flag", "3"), where.eq("transCode", TransCode.SCAN_REFUND_Z)),
                    where.and(where.ne("flag", "0"),where.ne("flag", "3"), where.eq("transCode", TransCode.SCAN_REFUND_S)));
        } else {
            where.or(
                    where.and(where.ne("flag", "0"),where.ne("flag", "3"),where.ne("flag", "2"), where.eq("transCode", TransCode.SALE)),
                    where.and(where.ne("flag", "0"),where.ne("flag", "3"), where.eq("transCode", TransCode.REFUND)),
                    where.and(where.ne("flag", "0"),where.ne("flag", "3"),where.ne("flag", "2"), where.eq("transCode", TransCode.AUTH_COMPLETE)),
                    where.and(where.ne("flag", "0"),where.ne("flag", "3"),where.ne("flag", "2"), where.eq("transCode", TransCode.SCAN_PAY_ALI)),
                    where.and(where.ne("flag", "0"),where.ne("flag", "3"),where.ne("flag", "2"), where.eq("transCode", TransCode.SCAN_PAY_SFT)),
                    where.and(where.ne("flag", "0"),where.ne("flag", "3"),where.ne("flag", "2"), where.eq("transCode", TransCode.SCAN_PAY_WEI)),
                    where.and(where.ne("flag", "0"),where.ne("flag", "3"), where.eq("transCode", TransCode.SCAN_REFUND_W)),
                    where.and(where.ne("flag", "0"),where.ne("flag", "3"), where.eq("transCode", TransCode.SCAN_REFUND_Z)),
                    where.and(where.ne("flag", "0"),where.ne("flag", "3"), where.eq("transCode", TransCode.SCAN_REFUND_S)));
        }

        List<T> tradeInfos = where.query();
        return tradeInfos;
    }
    //获取冲正的流水
    public List<T> getReverseList() throws SQLException {
        Where<T, String> where = commonDao.queryBuilder().where();
        where.eq("flag", "3");
        List<T> tradeInfos = where.query();
        return tradeInfos;
    }
    //获取可以批结算的交易流水个数
    public long getBatchCount() throws SQLException {
        Where infoWhere = commonDao.queryBuilder().setCountOf(true).where();
        infoWhere.or(
                infoWhere.and(infoWhere.ne("flag", "0"),infoWhere.ne("flag", "3"), infoWhere.eq("transCode", "SALE")),
                infoWhere.and(infoWhere.ne("flag", "0"),infoWhere.ne("flag", "3"), infoWhere.eq("transCode", "VOID")),
                infoWhere.and(infoWhere.ne("flag", "0"),infoWhere.ne("flag", "3"), infoWhere.eq("transCode", "REFUND")),
                infoWhere.and(infoWhere.ne("flag", "0"),infoWhere.ne("flag", "3"), infoWhere.eq("transCode", "AUTH_COMPLETE")),
                infoWhere.and(infoWhere.ne("flag", "0"),infoWhere.ne("flag", "3"), infoWhere.eq("transCode", "COMPLETE_VOID")),
                infoWhere.and(infoWhere.ne("flag", "0"),infoWhere.ne("flag", "3"), infoWhere.eq("transCode", "AUTH")),
                infoWhere.and(infoWhere.ne("flag", "0"),infoWhere.ne("flag", "3"), infoWhere.eq("transCode", TransCode.CANCEL)),
                infoWhere.and(infoWhere.ne("flag", "0"),infoWhere.ne("flag", "3"), infoWhere.eq("transCode", TransCode.SCAN_PAY_ALI)),
                infoWhere.and(infoWhere.ne("flag", "0"),infoWhere.ne("flag", "3"), infoWhere.eq("transCode", TransCode.SCAN_PAY_SFT)),
                infoWhere.and(infoWhere.ne("flag", "0"),infoWhere.ne("flag", "3"), infoWhere.eq("transCode", TransCode.SCAN_PAY_WEI)),
                infoWhere.and(infoWhere.ne("flag", "0"),infoWhere.ne("flag", "3"), infoWhere.eq("transCode", TransCode.SCAN_CANCEL)),
                infoWhere.and(infoWhere.ne("flag", "0"),infoWhere.ne("flag", "3"), infoWhere.eq("transCode", TransCode.SCAN_REFUND_S)),
                infoWhere.and(infoWhere.ne("flag", "0"),infoWhere.ne("flag", "3"), infoWhere.eq("transCode", TransCode.SCAN_REFUND_Z)),
                infoWhere.and(infoWhere.ne("flag", "0"),infoWhere.ne("flag", "3"), infoWhere.eq("transCode", TransCode.SCAN_REFUND_W)));
        PreparedQuery<TradeInfo> preparedQuery = infoWhere
                .prepare();
        return commonDao.countOf(preparedQuery);
    }
    //获取电子签名数
    public long getSignCount() throws SQLException {
        return commonDao.query().size();
    }
    //获取降序的交易记录用于打印最后一笔交易
    public List<T> getLastTransItem() throws SQLException {
        Where<T, String> infoWhere = commonDao.queryBuilder().orderBy("iso_f11", false).where();
        infoWhere.or(
                infoWhere.and(infoWhere.ne("flag", "0"),infoWhere.ne("flag", "3"), infoWhere.eq("transCode", "SALE")),
                infoWhere.and(infoWhere.ne("flag", "0"),infoWhere.ne("flag", "3"), infoWhere.eq("transCode", "VOID")),
                infoWhere.and(infoWhere.ne("flag", "0"),infoWhere.ne("flag", "3"), infoWhere.eq("transCode", "REFUND")),
                infoWhere.and(infoWhere.ne("flag", "0"),infoWhere.ne("flag", "3"), infoWhere.eq("transCode", "AUTH_COMPLETE")),
                infoWhere.and(infoWhere.ne("flag", "0"),infoWhere.ne("flag", "3"), infoWhere.eq("transCode", "COMPLETE_VOID")),
                infoWhere.and(infoWhere.ne("flag", "0"),infoWhere.ne("flag", "3"), infoWhere.eq("transCode", "AUTH")),
                infoWhere.and(infoWhere.ne("flag", "0"),infoWhere.ne("flag", "3"), infoWhere.eq("transCode", TransCode.CANCEL)),
                infoWhere.and(infoWhere.ne("flag", "0"),infoWhere.ne("flag", "3"), infoWhere.eq("transCode", TransCode.SCAN_PAY_ALI)),
                infoWhere.and(infoWhere.ne("flag", "0"),infoWhere.ne("flag", "3"), infoWhere.eq("transCode", TransCode.SCAN_PAY_SFT)),
                infoWhere.and(infoWhere.ne("flag", "0"),infoWhere.ne("flag", "3"), infoWhere.eq("transCode", TransCode.SCAN_PAY_WEI)),
                infoWhere.and(infoWhere.ne("flag", "0"),infoWhere.ne("flag", "3"), infoWhere.eq("transCode", TransCode.SCAN_CANCEL)),
                infoWhere.and(infoWhere.ne("flag", "0"),infoWhere.ne("flag", "3"), infoWhere.eq("transCode", TransCode.SCAN_REFUND_S)),
                infoWhere.and(infoWhere.ne("flag", "0"),infoWhere.ne("flag", "3"), infoWhere.eq("transCode", TransCode.SCAN_REFUND_Z)),
                infoWhere.and(infoWhere.ne("flag", "0"),infoWhere.ne("flag", "3"), infoWhere.eq("transCode", TransCode.SCAN_REFUND_W)));
        List<T> tradeInfos = infoWhere.query();
        return tradeInfos;
    }

    public List<T> getLastCode() throws  SQLException {
        Where<T, String> where = commonDao.queryBuilder().orderBy("iso_f11", false).where();
        where.or(where.eq("transCode", TransCode.SCAN_PAY_ALI),where.eq("transCode", TransCode.SCAN_PAY_SFT),where.eq("transCode", TransCode.SCAN_PAY_WEI));
/*                where.and(where.ne("flag", "0"),where.ne("flag", "3"), where.eq("transCode", TransCode.SCAN_PAY_ALI)),
                where.and(where.ne("flag", "0"),where.ne("flag", "3"), where.eq("transCode", TransCode.SCAN_PAY_SFT)),
                where.and(where.ne("flag", "0"),where.ne("flag", "3"), where.eq("transCode", TransCode.SCAN_PAY_WEI)));*/
  /*                  where.and(where.ne("flag", "0"),where.ne("flag", "3"), where.eq("transCode", TransCode.SCAN_CANCEL)),
                    where.and(where.ne("flag", "0"),where.ne("flag", "3"), where.eq("transCode", TransCode.SCAN_REFUND_W)),
                    where.and(where.ne("flag", "0"),where.ne("flag", "3"), where.eq("transCode", TransCode.SCAN_REFUND_Z)),
                    where.and(where.ne("flag", "0"),where.ne("flag", "3"), where.eq("transCode", TransCode.SCAN_REFUND_S)));*/
        List<T> tradeInfos = where.query();
        return tradeInfos;
    }

    //获取有效的批上送交易流水
    public List<T> getListForBatch() throws SQLException {
        Where<T, String> where = commonDao.queryBuilder().where();
        where.and(
                where.or(
                        where.and(where.ne("flag", "0"),where.ne("flag", "3"), where.eq("transCode", TransCode.SALE)),
                        where.and(where.ne("flag", "0"),where.ne("flag", "3"), where.eq("transCode", TransCode.VOID)),
                        where.and(where.ne("flag", "0"),where.ne("flag", "3"), where.eq("transCode", TransCode.REFUND)),
                        where.and(where.ne("flag", "0"),where.ne("flag", "3"), where.eq("transCode", TransCode.AUTH_COMPLETE)),
                        where.and(where.ne("flag", "0"),where.ne("flag", "3"), where.eq("transCode", TransCode.COMPLETE_VOID))),
                where.eq("isBatchSuccess", false),
                where.le("sendCount", Config.BATCH_MAX_UPLOAD_TIMES));
        List<T> transInfos = where.query();
        return transInfos;
    }

    //获取有效的批结算退货交易流水
    public List<T> getRefundList() throws SQLException {
      return  commonDao.queryBuilder()
                .where().eq("flag", "1")
                .and().eq("transCode", TransCode.REFUND)
                .and().eq("isBatchSuccess", false)
                .and().le("sendCount", Config.BATCH_MAX_UPLOAD_TIMES)
                .query();
    }

    //获取有效交易明细的记录数
    public long getValidCount() throws SQLException {
        Where where = commonDao.queryBuilder().setCountOf(true).where();
        where.not().eq("flag", 0).and()//交易未成功
                .not().eq("flag", 3).and()//冲正成功
                .not().eq("flag", 6).and()//冲正失败
                .not().eq("transCode", "BALANCE");
        PreparedQuery<TradeInfo> preparedQuery = where
                .prepare();
        return commonDao.countOf(preparedQuery);
    }
}
