package com.centerm.jnbank.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.TextView;

import com.centerm.cloudsys.sdk.common.utils.NetUtils;
import com.centerm.jnbank.R;
import com.centerm.jnbank.activity.InputDirectorPwdActivity;
import com.centerm.jnbank.activity.MainActivity;
import com.centerm.jnbank.activity.TradingActivity;
import com.centerm.jnbank.adapter.MenuAbsListAdapter;
import com.centerm.jnbank.bean.ReverseInfo;
import com.centerm.jnbank.bean.TradeInfo;
import com.centerm.jnbank.channels.EnumChannel;
import com.centerm.jnbank.channels.helper.ShengPayMenuHelper;
import com.centerm.jnbank.common.Settings;
import com.centerm.jnbank.common.TransCode;
import com.centerm.jnbank.db.CommonDao;
import com.centerm.jnbank.msg.MessageFactory;
import com.centerm.jnbank.net.ResponseHandler;
import com.centerm.jnbank.net.SocketClient;
import com.centerm.jnbank.task.AsyncBatchSettleDown;
import com.centerm.jnbank.task.qrcode.AsyncQueryLastData;
import com.centerm.jnbank.utils.CommonUtils;
import com.centerm.jnbank.utils.DialogFactory;
import com.centerm.jnbank.utils.ViewUtils;
import com.centerm.jnbank.view.AlertDialog;
import com.centerm.jnbank.view.GridViewPager;
import com.centerm.jnbank.xml.XmlParser;
import com.centerm.jnbank.xml.XmlTag;
import com.centerm.jnbank.xml.menu.Menu;
import com.centerm.jnbank.xml.menu.MenuItem;
import com.centerm.jnbank.xml.process.TradeProcess;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import config.BusinessConfig;

import static com.centerm.jnbank.common.TransCode.DISCOUNT_INTERGRAL;
import static com.centerm.jnbank.xml.XmlTag.MenuTag.INIT_TERMINAL;


/**
 * 菜单界面
 * author:wanliang527</br>
 * date:2016/10/26</br>
 */

public class MenuActivity extends BaseActivity {
    protected Menu menu;
    private MenuAbsListAdapter absListAdapter;
    private GridViewPager.GridPagerAdapter gridPagerAdapter;
    private MenuItem waitForExecuteItem;
    private CommonDao<ReverseInfo> reverseDao;
    private CommonDao<TradeInfo> tradeDao;
    private BusinessConfig config = BusinessConfig.getInstance();
    private List<Map<String, ?>> data = new ArrayList<>();
    private String TEXT = "text";
    private String ICON = "icon";
    private String ITEMS = "items";
    private View view;
    private RefashReceive refashReceive;

    @Override
    protected void onResume() {
        super.onResume();
        updateReverseFlag();
    }


    @Override
    public boolean isOpenDataBase() {
        return true;
    }


    @Override
    public void onInitLocalData(Bundle savedInstanceState) {
        super.onInitLocalData(savedInstanceState);
        if (null == refashReceive) {
            refashReceive = new RefashReceive();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("refresh");
            registerReceiver(refashReceive, intentFilter);
        }
        selectMenu();
        setCodeResponder(new SecretCodeResponder() {
            @Override
            public void onResponse() {
                Intent intent = new Intent(MenuActivity.this, ImportKEKActivity.class);
                startActivity(intent);
            }
        });
    }

    private void selectMenu() {
        Intent intent = getIntent();
        menu = intent.getParcelableExtra(KEY_MENU);
        int acctFlag = intent.getIntExtra(KEY_USER_FLAG, 0);
        if (menu == null) {
            logger.warn(this.getClass().getSimpleName() + "，Menu对象需要从文件中解析");
            //MainActivity的menu对象由XML解析
            EnumChannel channel = EnumChannel.valueOf(Settings.getPosChannel(this));
            menu = XmlParser.parseMenu(context, channel);

            if (this instanceof MainActivity) {
                boolean shengFlag = config.getParam(context, BusinessConfig.Key.FLAG_SHENG_SWITH).equals("1") ? true : false;
                boolean weiFlag = config.getParam(context, BusinessConfig.Key.FLAG_WEI_SWITH).equals("1") ? true : false;
                boolean aliFlag = config.getParam(context, BusinessConfig.Key.FLAG_ALI_SWITH).equals("1") ? true : false;
                //消费
                boolean saleSwitch = config.getParam(context, BusinessConfig.Key.FLAG_SALE_SWITH).equals("1") ? true : false;
                //消费撤销
                boolean voidSwitch = config.getParam(context, BusinessConfig.Key.FLAG_VOID_SWITH).equals("1") ? true : false;
                //退货
                boolean refundSwitch = config.getParam(context, BusinessConfig.Key.FLAG_REFUND_SWITH).equals("1") ? true : false;
                //预授权
                boolean authSwitch = config.getParam(context, BusinessConfig.Key.FLAG_AUTH_SWITH).equals("1") ? true : false;
                //预授权完成
                boolean completeSwitch = config.getParam(context, BusinessConfig.Key.FLAG_COMPLETE_SWITH).equals("1") ? true : false;
                //预授权撤销
                boolean cancelSwitch = config.getParam(context, BusinessConfig.Key.FLAG_CANCEL_SWITH).equals("1") ? true : false;
                //预授权完成撤销
                boolean completeVoidSwitch = config.getParam(context, BusinessConfig.Key.FLAG_COMPLETE_VOID_SWITH).equals("1") ? true : false;

                //屏蔽管理员入口
                menu.removeItem("SUPER_MANAGEMENT");
                menu.removeItem("SYS_MANAGEMENT");
                menu.removeItem("SALE_OTHER");
                menu.removeItem("SCAN_WEI_OTHER");
                menu.removeItem("SCAN_ALI_OTHER");
                menu.removeItem("SCAN_SFT_OTHER");
                if (!shengFlag) {
                    menu.removeItem("SCAN_PAY_SFT");
                }
                if (!weiFlag) {
                    menu.removeItem("SCAN_PAY_WEI");
                }
                if (!aliFlag) {
                    menu.removeItem("SCAN_PAY_ALI");
                }
                if (!saleSwitch) {
                    menu.removeItem("SALE");
                }
                if (!voidSwitch) {
                    menu.removeItem("VOID");
                }
                if (!refundSwitch) {
                    menu.removeItem("REFUND");
                }
                if (!authSwitch && !completeSwitch && !cancelSwitch && !completeVoidSwitch) {
                    menu.removeItem("AUTH");
                }

            } else {
                if (acctFlag == 1) {
                    //系统管理员菜单界面
                    menu = (Menu) findMenuItem("SYS_MANAGEMENT");
                } else if (acctFlag == 2) {
                    //主管操作员菜单界面
                    menu = (Menu) findMenuItem("SUPER_MANAGEMENT");
                } else if (acctFlag == 3) {
                    //交易业务中的其它业务
                    menu = (Menu) findMenuItem("SALE_OTHER");
                    boolean balanceSwitch = config.getParam(context, BusinessConfig.Key.FLAG_BALANCE_SWITH).equals("1") ? true : false;
                    boolean saleSwitch = config.getParam(context, BusinessConfig.Key.FLAG_SALE_SWITH).equals("1") ? true : false;
                    boolean voidSwitch = config.getParam(context, BusinessConfig.Key.FLAG_VOID_SWITH).equals("1") ? true : false;
                    boolean authSwitch = config.getParam(context, BusinessConfig.Key.FLAG_AUTH_SWITH).equals("1") ? true : false;
                    boolean cancelSwitch = config.getParam(context, BusinessConfig.Key.FLAG_CANCEL_SWITH).equals("1") ? true : false;
                    boolean completeSwitch = config.getParam(context, BusinessConfig.Key.FLAG_COMPLETE_SWITH).equals("1") ? true : false;
                    boolean completeVoidSwitch = config.getParam(context, BusinessConfig.Key.FLAG_COMPLETE_VOID_SWITH).equals("1") ? true : false;
                    boolean refundSwitch = config.getParam(context, BusinessConfig.Key.FLAG_REFUND_SWITH).equals("1") ? true : false;
                    if (!balanceSwitch) {
                        menu.removeItem("BALANCE");
                    }
                    if (!saleSwitch) {
                        menu.removeItem("SALE");
                    }
                    if (!voidSwitch) {
                        menu.removeItem("VOID");
                    }
                    if (!refundSwitch) {
                        menu.removeItem("REFUND");
                    }
                    if (!authSwitch) {
                        menu.removeAuthItem("AUTH");
                    }
                    if (!cancelSwitch) {
                        menu.removeAuthItem("CANCEL");
                    }
                    if (!completeSwitch) {
                        menu.removeAuthItem("AUTH_COMPLETE");
                    }
                    if (!completeVoidSwitch) {
                        menu.removeAuthItem("COMPLETE_VOID");
                    }
                    //如果预授权中四个子菜单都关闭，那么父菜单也不显示
                    if (!authSwitch && !cancelSwitch && !completeSwitch && !completeVoidSwitch) {
                        menu.removeItem("AUTH");
                    }
                } else if (acctFlag == 4) {
                    menu = (Menu) findMenuItem("SCAN_WEI_OTHER");
                } else if (acctFlag == 5) {
                    menu = (Menu) findMenuItem("SCAN_ALI_OTHER");
                } else if (acctFlag == 6) {
                    menu = (Menu) findMenuItem("SCAN_SFT_OTHER");
                } else if (acctFlag == 7) {
                    menu = (Menu) findMenuItem("PRINT");
                } else if (acctFlag == 8) {
                    menu = (Menu) findMenuItem("SALE_OTHER");
                    menu = (Menu) findMenuItem("AUTH");
                } else {

                }
            }
        }
    }

    @Override
    public int onLayoutId() {
        int id = R.layout.activity_menu_list;
        if (menu != null) {
            id = menu.getStructure().equals(Menu.ViewStructure.GRID) ? R.layout
                    .activity_menu_grid : id;
        }
        return id;
    }

    @Override
    public void onInitView() {
        View rootView = findViewById(R.id.root_view);
        hideTitleBar();
        if (this instanceof MainActivity) {
            //设置背景
            rootView.setBackgroundColor(getResources().getColor(R.color.secondary_bg));
        }
        TextView txtvw = (TextView) findViewById(R.id.txtvw_title);
        if (txtvw != null) {
            if (!TextUtils.isEmpty(menu.getTextResName())) {
                int resId = getResources().getIdentifier(menu.getTextResName(), "string",
                        getPackageName());
                if (resId > 1)
                    txtvw.setText(resId);
                else
                    txtvw.setText(menu.getChnTag());
            } else
                txtvw.setText(menu.getChnTag());
        }
        //余额查询
        boolean balanceSwitch = config.getParam(context, BusinessConfig.Key.FLAG_BALANCE_SWITH).equals("1") ? true : false;
        //预授权
        boolean authSwitch = config.getParam(context, BusinessConfig.Key.FLAG_AUTH_SWITH).equals("1") ? true : false;
        //预授权完成
        boolean completeSwitch = config.getParam(context, BusinessConfig.Key.FLAG_COMPLETE_SWITH).equals("1") ? true : false;
        //预授权撤销
        boolean cancelSwitch = config.getParam(context, BusinessConfig.Key.FLAG_CANCEL_SWITH).equals("1") ? true : false;
        //预授权完成撤销
        boolean completeVoidSwitch = config.getParam(context, BusinessConfig.Key.FLAG_COMPLETE_VOID_SWITH).equals("1") ? true : false;

        view = findViewById(R.id.menu_view);
        if (view instanceof AbsListView) {
            //余额查询开关控制
            if ("OTHER".equals(menu.getEntag())) {
                for (int i = 0; i < menu.getItemList().size(); i++) {
                    if ("BALANCE".equals(menu.getItem(i).getEntag())) {
                        if (!balanceSwitch) {
                            menu.removeItem("BALANCE");
                        }
                    }
                }
            }
            //预授权开关控制
            if ("AUTH".equals(menu.getEntag())) {
                for (int i = 0; i < menu.getItemList().size(); i++) {
                    if ("AUTH".equals(menu.getItem(i).getEntag())) {
                        if (!authSwitch) {
                            menu.removeItem("AUTH");
                        }
                    }
                    if ("AUTH_COMPLETE".equals(menu.getItem(i).getEntag())) {
                        if (!completeSwitch) {
                            menu.removeItem("AUTH_COMPLETE");
                        }
                    }
                    if ("CANCEL".equals(menu.getItem(i).getEntag())) {
                        if (!cancelSwitch) {
                            menu.removeItem("CANCEL");
                        }
                    }
                    if ("COMPLETE_VOID".equals(menu.getItem(i).getEntag())) {
                        if (!completeVoidSwitch) {
                            menu.removeItem("COMPLETE_VOID");
                        }
                    }
                }
            }
            absListAdapter = new MenuAbsListAdapter(context, menu);
            ((AbsListView) view).setAdapter(absListAdapter);
            ((AbsListView) view).setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    MenuItem item = absListAdapter.getItem(position);
                    onMenuItemClick(view, item);
                }
            });
            if ("MANAGEMENT".equals(menu.getEntag())
                    || "AUTH".equals(menu.getEntag())
                    || "SALE_OTHER".equals(menu.getEntag())
                    || "SCAN_WEI_OTHER".equals(menu.getEntag())
                    || "SCAN_ALI_OTHER".equals(menu.getEntag())
                    || "SCAN_SFT_OTHER".equals(menu.getEntag())
                    || "OTHER".equals(menu.getEntag())
                    || "PRINT".equals(menu.getEntag())
                    || "SIGN_IN".equals(menu.getEntag())
                    || "QUICK_PAY_NEED_PASWD".equals(menu.getEntag())
                    || "TRANS_SETTINGS".equals(menu.getEntag())
                    || "DOWNLOAD".equals(menu.getEntag())) {
                findViewById(R.id.top_banner).setVisibility(View.GONE);
                showTitleBar();
            }
        } else if (view instanceof GridViewPager) {
            loadData();
            gridPagerAdapter = new GridViewPager.GridPagerAdapter(this, R.layout
                    .common_menu_grid_item, new String[]{TEXT, ICON},
                    new int[]{R.id.menu_text_show, R.id.menu_icon_show}, data);
            ((GridViewPager) view).setAdapter(gridPagerAdapter);
            ((GridViewPager) view).setOnItemClickListener(new GridViewPager.ItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    MenuItem item = menu.getItem(position);
                    onMenuItemClick(view, item);
                }
            });
        }
    }

    private void loadData() {
        data.clear();
        int len = menu.getCounts();
        for (int i = 1; i < len; i++) {
            MenuItem tempMenu = menu.getItem(i);
            Map<String, Object> map = new HashMap<>();
            if (!TextUtils.isEmpty(tempMenu.getTextResName())) {
                int resId = getResources().getIdentifier(tempMenu.getTextResName(), "string",
                        getPackageName());
                if (resId > 0) {
                    map.put(TEXT, getString(resId));
                    if (i == 2) {
                        map.put(TEXT, tempMenu.getChnTag());
                    }
                } else
                    map.put(TEXT, tempMenu.getChnTag());
            } else
                map.put(TEXT, tempMenu.getChnTag());
            if (!TextUtils.isEmpty(tempMenu.getIconResName())) {
                int resId = getResources().getIdentifier(tempMenu.getIconResName(),
                        "drawable", getPackageName());
                if (resId > 0) {
                    map.put(ICON, resId);
                } else map.put(ICON, R.drawable.ic_launcher);
            } else map.put(ICON, R.drawable.ic_launcher);
            map.put(ITEMS, tempMenu);
            data.add(map);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        logger.info("onActivityResult==>请求码：" + Integer.toHexString(requestCode) + "==>返回码：" +
                resultCode);
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_INPUT_DIREOTOR_PWD:
                if (resultCode == RESULT_OK) {
                    jumpToMenu((Menu) waitForExecuteItem);
                    waitForExecuteItem = null;
                }
                break;
        }

    }

    @Override
    public void onBackPressed() {
        //BusinessConfig.getInstance().setValue(context, BusinessConfig.Key.KEY_OPER_ID,"01");
        super.onBackPressed();
    }

    private MenuItem findMenuItem(String enTag) {
        for (MenuItem item : menu.getItemList()) {
            if (item.getEntag().equals(enTag)) {
                return item;
            }
        }
        return null;
    }

    private void onMenuItemClick(View view, final MenuItem item) {
        if (CommonUtils.isFastClick()) {
            logger.debug("==>重复的onBackPressed事件，不响应！");
            return;
        }
        BusinessConfig config = BusinessConfig.getInstance();
        String today = new SimpleDateFormat("MMdd").format(new Date());
        String lastSignDate = config.getValue(context, BusinessConfig.Key.KEY_LAST_SIGNIN_DATE);
        logger.warn("上次签到日期：" + lastSignDate);
        //每日强制签到
        if (!today.equals(lastSignDate)) {
            config.setFlag(context, BusinessConfig.Key.FLAG_SIGN_IN, false);
        }
        logger.debug("点击：" + item.getChnTag());
        //是否属于子菜单项
        if (item instanceof Menu) {
            logger.debug("点击：" + "是否属于Menu");
            if (XmlTag.MenuTag.SUPER_MANAGEMENT.equals(item.getEntag())) {
                //要求输入主管密码
                Intent intent = new Intent(context, InputDirectorPwdActivity.class);
                waitForExecuteItem = item;
                startActivityForResult(intent, REQ_INPUT_DIREOTOR_PWD);
                return;
            }
            //跳转到下一级菜单
            boolean success = jumpToMenu((Menu) item);
            if (!success) {
                ViewUtils.showToast(context, R.string.tip_menu_undefined);
            }
        } else {
            if (!isDeviceReady()) {
                ViewUtils.showToast(context, R.string.tip_device_not_ready);
                return;
            }
            if (!XmlTag.MenuTag.OFFLINE_MENU.contains(item.getEntag())) {
                //清除交易流水可以在无网络下操作，故排除
                if (!NetUtils.isNetConnected(context) && !item.getEntag().equals(XmlTag.MenuTag
                        .CLEAR_TRADE_SERIAL)) {
                    ViewUtils.showToast(context, R.string.tip_network_unavailable);
                    return;
                }
                if (!CommonUtils.isOnCharging(context) && CommonUtils.getBatteryPercent(context)
                        < 0.15f) {
                    DialogFactory.showMessageDialog(context, "电量不足", "请连接电源后进行交易", new
                            AlertDialog.ButtonClickListener() {

                                @Override
                                public void onClick(AlertDialog.ButtonType button, View v) {
                                }
                            });
                    return;
                }
            }
            boolean[] bArray = isNeedLoadOrSignin(item.getEntag());
            if (bArray[0]) {
//                ViewUtils.showToast(context, R.string.tip_init_terminal);
//                return;
            } else if (bArray[1]) {
                ViewUtils.showToast(context, R.string.tip_download_tmk);
//                return;
            } else if (bArray[2]) {
                DialogFactory.showSelectDialog(context, null, "请签到后开始交易", new AlertDialog
                        .ButtonClickListener() {

                    @Override
                    public void onClick(AlertDialog.ButtonType button, View v) {
                        switch (button) {
                            case POSITIVE:
                                beginProcess(TransCode.SIGN_IN, "SIGN_IN");
                                break;
                        }
                    }
                });
//                return;
            }

            if (isNeedSignOut(item.getEntag())) {
                DialogFactory.showSelectDialog(context, null, "批结算完成，请签退！", new AlertDialog
                        .ButtonClickListener() {
                    @Override
                    public void onClick(AlertDialog.ButtonType button, View v) {
                        switch (button) {
                            case POSITIVE:
                                Intent intent = new Intent(context, TradingActivity.class);
                                intent.putExtra(KEY_TRANSCODE, TransCode.SIGN_OUT);
                                context.startActivity(intent);
                                break;
                        }
                    }
                });
                return;
            }

            //流水超上限，先批结算后再交易。
            if (XmlTag.MenuTag.TRADING_AFTER_SETTLEMENT_MENU.contains(item.getEntag())
                    && BusinessConfig.getInstance().getFlag(context, BusinessConfig.Key
                    .FLAG_TRADE_STORAGE_WARNING)) {
                DialogFactory.showSelectDialog(context, null, "请结算后开始交易", new AlertDialog
                        .ButtonClickListener() {
                    @Override
                    public void onClick(AlertDialog.ButtonType button, View v) {
                        switch (button) {
                            case POSITIVE:
                                Intent intent = new Intent(context, TradingActivity.class);
                                intent.putExtra(KEY_TRANSCODE, TransCode.SETTLEMENT);
                                context.startActivity(intent);
                                break;
                        }
                    }
                });
                return;
            }
            boolean isSale = BusinessConfig.getInstance().getParam(context, BusinessConfig.Key.FLAG_SALE_SWITH).equals("1") ? true : false;
            if (XmlTag.MenuTag.SALE.equals(item.getEntag()) && !isSale) {
                ViewUtils.showToast(context, "该交易入口已被关闭");
                return;
            }
            if (XmlTag.MenuTag.SCAN_LAST_SERCH.equals(item.getEntag())) {
                new AsyncQueryLastData(context) {
                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        DialogFactory.showLoadingDialog(MenuActivity.this, context.getString(R.string
                                .tip_query_flow));
                    }

                    @Override
                    public void onFinish(Object o) {
                        super.onFinish(o);
                        if (o instanceof Boolean && (Boolean) o) {
                            DialogFactory.hideAll();
                            onProcess(item);
                        } else {
                            ViewUtils.showToast(context, "暂无扫码交易");
                            DialogFactory.hideAll();
                        }
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                return;
            }
            if (INIT_TERMINAL.equals(item.getEntag())) {
                String batchStatus = Settings.getValue(context, Settings.KEY.BATCH_SEND_STATUS, "-1");
                if ("0".equals(batchStatus)) {
                    new AsyncBatchSettleDown(context) {
                        @Override
                        protected void onPreExecute() {
                            super.onPreExecute();
                            DialogFactory.showLoadingDialog(MenuActivity.this, MenuActivity.this.getString(R.string
                                    .tip_wait));
                        }

                        @Override
                        public void onFinish(Object o) {
                            super.onFinish(o);
                            if (o instanceof Boolean && (Boolean) o) {
                                ViewUtils.showToast(MenuActivity.this, MenuActivity.this.getString(R.string.tip_please_batch_first));
                                DialogFactory.hideAll();
                            } else {
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        DialogFactory.hideAll();
                                    }
                                }, 300);
                                onProcess(item);
                            }
                        }
                    }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else if ("1".equals(batchStatus)) {
                    ViewUtils.showToast(MenuActivity.this, MenuActivity.this.getString(R.string.tip_batch_fail_please_continue));
                } else {
                    onProcess(item);
                }
                return;
            }
//            if (DISCOUNT_INTERGRAL.equals(item.getEntag())) {
//                Map<String, String> map = new HashMap<String, String>();
//                final MessageFactory factory = new MessageFactory(context);
//                Object pack = factory.pack(TransCode.DISCOUNT_INTERGRAL, map);
//
//                byte[] bytes = (byte[]) pack;
//                ResponseHandler handler = new ResponseHandler() {
//                    @Override
//                    public void onSuccess(String statusCode, String msg, byte[] data) {
//                        logger.info(msg);
//                        Map<String, String> mapData = factory.unpack(TransCode.DISCOUNT_INTERGRAL, data);
//                        logger.info("mapData:" + mapData);
//
//                    }
//                    @Override
//                    public void onFailure(String code, String msg, Throwable error) {
//                        logger.info(msg);
//                    }
//                };
//
//                SocketClient instance = SocketClient.getInstance(context);
//                instance.sendData(bytes, handler, TransCode.DISCOUNT_INTERGRAL);

//            }

       /*     // TODO: 2016/11/22 脚本结果通知和冲正不要放在这里执行，应该放到TradingActivity执行，执行完成后继续执行原交易
            if (reverseFlag && !(TransCode.SIGN_IN.equals(item.getTransCode()))) {
                //签到之前不进行自动冲正
                Intent intent = new Intent(context, TradingActivity.class);
                intent.putExtra(KEY_TRANSCODE, TransCode.REVERSE);
                startActivityForResult(intent, REQ_REVERSE);
                return;
            }*/
            //特例：如果Item中含有开关，UI需要在这里进行控制，业务控制在onProcess中
            onToggleIfExists(view);
            onProcess(item);
        }
    }

    private void onToggleIfExists(View view) {
        CheckBox toggleView = (CheckBox) view.findViewById(R.id.toggle);
        if (toggleView != null) {
            boolean t = toggleView.isChecked();
            toggleView.setChecked(!t);
        }
    }

    private void onProcess(MenuItem item) {
        if (item == null) {
            return;
        }
        //进入具体业务流程
        String processFile = item.getProcessFile();
        //优先响应有流程定义的事件
        boolean success = beginProcess(item.getTransCode(), processFile);
        logger.debug("onProcess:" + success);
        if (!success) {
            //没有流程定义，继续寻找事件响应
            success = onNoProcessDefine(item);
        }
        if (!success) {
            ViewUtils.showToast(context, R.string.tip_process_undefined);
        }
    }

    /**
     * 启动交易流程
     *
     * @param processFile 流程定义文件
     * @return 启动成功返回true，失败返回false
     */
    protected boolean beginProcess(String transCode, String processFile) {
        TradeProcess process = XmlParser.parseProcess(context, processFile);
        if (process != null) {
            final Intent intent = new Intent();
            //是否需要密码，需要就跳到第一个节点，不需要跳到第二个节点
            if (transCode.equals(TransCode.VOID) || transCode.equals(TransCode.REFUND) || transCode.equals(TransCode.CANCEL)
                    || transCode.equals(TransCode.COMPLETE_VOID) || transCode.equals(TransCode.SCAN_CANCEL)
                    || transCode.equals(TransCode.SCAN_REFUND_W) || transCode.equals(TransCode.SCAN_REFUND_Z) || transCode.equals(TransCode.SCAN_REFUND_S)) {
                boolean isNeedPsw = BusinessConfig.getInstance().getParam(context, BusinessConfig.Key.FLAG_REFUND_VOID_NEED_PSW).equals("1") ? true : false;
                if (isNeedPsw) {
                    intent.setAction(process.getFirstComponentNode().getComponentName());
                } else {
                    intent.setAction(process.getSecondComponentNode().getComponentName());
                }
            } else {
                intent.setAction(process.getFirstComponentNode().getComponentName());
            }
            intent.putExtra(KEY_PROCESS, process);
            if (transCode.equals(XmlTag.MenuTag.SALE_BY_INSERT)) {
                //插卡消费
                intent.putExtra(KEY_TRANSCODE, TransCode.SALE);
                intent.putExtra(KEY_INSERT_SALE_FLAG, true);
            } else if (transCode.equals(XmlTag.MenuTag.QUICK_SALE_NEED_PASWD)) {
                //消费凭密
                intent.putExtra(KEY_TRANSCODE, TransCode.SALE);
                intent.putExtra(KEY_CLSS_FORCE_PIN_FLAG, true);
            } else if (transCode.equals(XmlTag.MenuTag.QUICK_AUTH_NEED_PASWD)) {
                //预授权凭密
                intent.putExtra(KEY_TRANSCODE, TransCode.AUTH);
                intent.putExtra(KEY_CLSS_FORCE_PIN_FLAG, true);
            } else {
                intent.putExtra(KEY_TRANSCODE, transCode);
            }
            if (transCode.equals(XmlTag.MenuTag.SCAN_SERCH)) {
                try {
                    tradeDao = new CommonDao<>(TradeInfo.class, dbHelper);
                    List<TradeInfo> tradeInfos = tradeDao.queryBuilder().orderBy("iso_f11", false)
                            .where().eq("transCode", TransCode.SCAN_PAY_WEI)
                            .or().eq("transCode", TransCode.SCAN_PAY_SFT)
                            .or().eq("transCode", TransCode.SCAN_PAY_ALI)
                            .query();
                    if (null != tradeInfos && tradeInfos.size() > 0) {
                        startActivity(intent);
                    } else {
                        ViewUtils.showToast(context, context.getString(R.string
                                .tip_no_trade_info_scan));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                startActivity(intent);
            }
            return true;
        }
        logger.warn(processFile + ", 流程未定义");
        return false;
    }

    /**
     * 跳转到子菜单
     *
     * @param menu 菜单实体对象
     * @return 跳转成功返回ture，失败返回fale
     */
    protected boolean jumpToMenu(Menu menu) {
        if (menu == null) {
            logger.warn("无法跳转菜单，菜单为空");
            return false;
        }
        Intent intent = new Intent(this, MenuActivity.class);
        intent.putExtra(KEY_MENU, menu);
        startActivity(intent);
        return true;
    }

    /**
     * 没有进行流程定义的菜单项在此方法中统一调度和处理
     *
     * @param item 菜单项
     * @return 处理成功返回true，否则返回false
     */
    protected boolean onNoProcessDefine(MenuItem item) {
        EnumChannel posChannel = EnumChannel.valueOf(Settings.getPosChannel(context));
        switch (posChannel) {
            case SHENGPAY:
                return new ShengPayMenuHelper().onTriggerMenuItem(this, item);
        }
        return false;
    }

    /**
     * 更新冲正标识
     *
     * @return true代表是，则需要先进行冲正交易才能进行其它交易；否则为false
     */
    private void updateReverseFlag() {
        if (reverseDao == null) {
            reverseDao = new CommonDao<>(ReverseInfo.class, dbHelper);
        }
        long counts = reverseDao.countOf();
        if (counts > 0) {
            BaseActivity.reverseFlag = true;
        } else {
            BaseActivity.reverseFlag = false;
        }
        logger.info("==>查询冲正表记录数==>" + counts + "==>更新冲正标志==>" + reverseFlag);
    }

    class RefashReceive extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("refresh")) {
                selectMenu();
                loadData();
                if (view instanceof GridViewPager) {
                    GridViewPager pager = (GridViewPager) view;
                    pager.reFrashData(data);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != refashReceive) {
            unregisterReceiver(refashReceive);
        }
    }
}
