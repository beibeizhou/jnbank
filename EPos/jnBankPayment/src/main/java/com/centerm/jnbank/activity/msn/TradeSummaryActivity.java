package com.centerm.jnbank.activity.msn;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.centerm.jnbank.R;
import com.centerm.jnbank.adapter.ObjectBaseAdapter;
import com.centerm.jnbank.base.BaseActivity;
import com.centerm.jnbank.bean.TradeInfo;
import com.centerm.jnbank.db.CommonDao;
import com.centerm.jnbank.db.CommonManager;
import com.centerm.jnbank.printer.PrintTransData;
import com.centerm.jnbank.task.AsyncQueryPrintDataTask;
import com.centerm.jnbank.utils.DataHelper;
import com.centerm.jnbank.utils.DialogFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import config.BusinessConfig;

import static com.centerm.jnbank.common.TransCode.AUTH_COMPLETE;
import static com.centerm.jnbank.common.TransCode.AUTH_SETTLEMENT;
import static com.centerm.jnbank.common.TransCode.COMPLETE_VOID;
import static com.centerm.jnbank.common.TransCode.REFUND;
import static com.centerm.jnbank.common.TransCode.SALE;
import static com.centerm.jnbank.common.TransCode.SCAN_CANCEL;
import static com.centerm.jnbank.common.TransCode.SCAN_PAY_ALI;
import static com.centerm.jnbank.common.TransCode.SCAN_PAY_SFT;
import static com.centerm.jnbank.common.TransCode.SCAN_PAY_WEI;
import static com.centerm.jnbank.common.TransCode.SCAN_REFUND_S;
import static com.centerm.jnbank.common.TransCode.SCAN_REFUND_W;
import static com.centerm.jnbank.common.TransCode.SCAN_REFUND_Z;
import static com.centerm.jnbank.common.TransCode.VOID;


/**
 * 交易汇总界面
 * author:wanliang527</br>
 * date:2016/11/13</br>
 */
public class TradeSummaryActivity extends BaseActivity {

    private ListView listView1, listView2;
    private CommonDao<TradeInfo> tradeInfoDao;
    private View lableGroup;
    private List<TradeSummary> summaryList1, summaryList2;
    private TradeSummary saleSum, voidSum, authCompSum, compVoidSum, refundSum, inDebit, inCredit, outDebit, outCredit,weixiSum,zhifbSum,sftSum,codeVoid,codeRefund;
    private List<TradeInfo> jiejiList;
    private List<TradeInfo> daijiList;
    private CommonDao<TradeInfo> tradeDao;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Adapter adapter1 = new Adapter(context);
            adapter1.addAll(summaryList1);
            logger.debug(summaryList1);
            logger.debug(summaryList2);
            listView1.setAdapter(adapter1);
            Adapter adapter2 = new Adapter(context);
            adapter2.addAll(summaryList2);
            listView2.setAdapter(adapter2);
        }
    };
    private double jiejiAmount;
    private double daijiAmount;

    @Override
    public boolean isOpenDataBase() {
        return true;
    }

    @Override
    public void onInitLocalData(Bundle savedInstanceState) {
        super.onInitLocalData(savedInstanceState);
        //初始化需要汇总的交易类型
        boolean isPrintVoid = BusinessConfig.getInstance().getParam(context, BusinessConfig.Key.FLAG_PRINT_VOID_DETAIL).equals("1") ? true : false;
        tradeInfoDao = new CommonDao<>(TradeInfo.class, dbHelper);
        summaryList1 = new ArrayList<>();
        summaryList2 = new ArrayList<>();
        summaryList1.add(saleSum = new TradeSummary("消费"));
        if (isPrintVoid) {
            summaryList1.add(voidSum = new TradeSummary("消费撤销"));
        }
        summaryList1.add(authCompSum = new TradeSummary("预授权完成"));
        if (isPrintVoid) {
            summaryList1.add(compVoidSum = new TradeSummary("预授权完成撤销"));
        }
        summaryList1.add(refundSum = new TradeSummary("退货"));
        summaryList2.add(weixiSum = new TradeSummary("微信"));
        summaryList2.add(zhifbSum = new TradeSummary("支付宝"));
        if (isPrintVoid) {
            summaryList2.add(codeVoid = new TradeSummary("二维码撤销"));
        }
        summaryList2.add(codeRefund = new TradeSummary("二维码退货"));
    }

    @Override
    public int onLayoutId() {
        return R.layout.activity_trade_summary;
    }

    @Override
    public void onInitView() {
        setTitle(R.string.title_trade_summary);
        listView1 = (ListView) findViewById(R.id.list_v);
        listView2 = (ListView) findViewById(R.id.list_v2);
        lableGroup = findViewById(R.id.label_name_group);
//        ViewGroup.LayoutParams params = lableGroup.getLayoutParams();
//        params.height = getResources().getDimensionPixelSize(R.dimen.trade_record_title_height);
        ((TextView) lableGroup.findViewById(R.id.pos_serial_show)).setText(R.string.label_trans_type2);//交易类型
        ((TextView) lableGroup.findViewById(R.id.trans_type_show)).setText(R.string.label_trans_total_counts);//总笔数
        ((TextView) lableGroup.findViewById(R.id.trans_money_show)).setText(R.string.label_trans_total_amt);//总金额
    }

    @Override
    public void afterInitView() {
        super.afterInitView();
        onSummary();
    }

    private void onSummary() {
        new Thread(new SummaryThread()).start();
    }

    private class SummaryThread implements Runnable {

        @Override
        public void run() {
            long start = System.currentTimeMillis();
            CommonManager commonManager = new CommonManager(TradeInfo.class, context);
            try {
                List<TradeInfo> tradeList = commonManager.getBatchList();
//                logger.debug("查询数据结果==>" + tradeList.toString());
                for (int i = 0; i < tradeList.size(); i++) {
                    TradeInfo data = tradeList.get(i);
                    String transCode = data.getTransCode();
                    String f4 = data.getIso_f4();//金额
                    String f49 = data.getIso_f49();//货币类型
                    double amt = 0;
                    try {
                        amt = Double.valueOf(DataHelper.formatIsoF4(f4));
                    } catch (Exception e) {
                        logger.warn("格式化4域数据异常==>" + f4 + "==>交易类型：" + data.getTransCode());
                    }
                    switch (transCode) {
                        case SALE:
                            saleSum.addUpTotoalNum();
                            saleSum.addUpTotalAmt(amt);
                            break;
                        case SCAN_PAY_WEI:
                            weixiSum.addUpTotoalNum();
                            weixiSum.addUpTotalAmt(amt);
                            break;
                        case SCAN_PAY_ALI:
                            zhifbSum.addUpTotoalNum();
                            zhifbSum.addUpTotalAmt(amt);
                            break;
                        case SCAN_PAY_SFT:
                            sftSum.addUpTotoalNum();
                            sftSum.addUpTotalAmt(amt);
                            break;
                        case VOID:
                            voidSum.addUpTotoalNum();
                            voidSum.addUpTotalAmt(amt);
                            break;
                        case SCAN_CANCEL:
                            codeVoid.addUpTotoalNum();
                            codeVoid.addUpTotalAmt(amt);
                            break;
                        case AUTH_SETTLEMENT:
                        case AUTH_COMPLETE:
                            authCompSum.addUpTotoalNum();
                            authCompSum.addUpTotalAmt(amt);
                            break;
                        case COMPLETE_VOID:
                            compVoidSum.addUpTotoalNum();
                            compVoidSum.addUpTotalAmt(amt);
                            break;
                        case REFUND:
                            refundSum.addUpTotoalNum();
                            refundSum.addUpTotalAmt(amt);
                            break;
                        case SCAN_REFUND_W:
                        case SCAN_REFUND_Z:
                        case SCAN_REFUND_S:
                            codeRefund.addUpTotoalNum();
                            codeRefund.addUpTotalAmt(amt);
                            break;
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            long end = System.currentTimeMillis();
            logger.info("交易汇总数据统计完成==>耗时==>" + (end - start) / 1000.0);
            handler.obtainMessage().sendToTarget();
        }
    }


    private class Adapter extends ObjectBaseAdapter<TradeSummary> {

        public Adapter(Context mCtx) {
            super(mCtx);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TradeSummary data = getItem(position);
            int layoutId = R.layout.v_trade_record_item2;
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(layoutId, null);
            }
            TextView posSerial = (TextView) convertView.findViewById(R.id.pos_serial_show);
            TextView transType = (TextView) convertView.findViewById(R.id.trans_type_show);
            TextView transAmt = (TextView) convertView.findViewById(R.id.trans_money_show);
            posSerial.setTextColor(getResources().getColor(R.color.font_black));
            transType.setTextColor(getResources().getColor(R.color.font_black));
            transAmt.setTextColor(getResources().getColor(R.color.font_black));
            posSerial.setText(data.getTypeName());//交易类型
            transType.setText("" + data.getTotalNum());//总笔数
            transAmt.setText("" + DataHelper.saved2Decimal(data.getTotalAmt()));//总金额
            return convertView;
        }
    }

    public class TradeSummary {
        private String typeName;
        private int totalNum;
        private double totalAmt;

        public TradeSummary(String typeName) {
            this.typeName = typeName;
        }

        public TradeSummary(String typeName, int totalNum, double totalAmt) {
            this.typeName = typeName;
            this.totalNum = totalNum;
            this.totalAmt = totalAmt;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }

        public int getTotalNum() {
            return totalNum;
        }

        public void setTotalNum(int totalNum) {
            this.totalNum = totalNum;
        }

        public double getTotalAmt() {
            return totalAmt;
        }

        public void setTotalAmt(double totalAmt) {
            this.totalAmt = totalAmt;
        }

        public int addUpTotoalNum() {
            return ++totalNum;
        }

        public double addUpTotalAmt(double amt) {
            return totalAmt += amt;
        }

        @Override
        public String toString() {
            return "TradeSummary{" +
                    "typeName='" + typeName + '\'' +
                    ", totalNum=" + totalNum +
                    ", totalAmt=" + totalAmt +
                    '}';
        }
    }


    public void onPrintBtnClick(View view) {
        printTotalData();
    }

    private void printTotalData() {
        tradeDao = new CommonDao<>(TradeInfo.class, dbHelper);
        new AsyncQueryPrintDataTask(context) {
            @Override
            public void onStart() {
                super.onStart();
                DialogFactory.showLoadingDialog(TradeSummaryActivity.this,"正在查询终端数据……");
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
}
