package com.centerm.jnbank.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.centerm.jnbank.bean.BinData;
import com.centerm.jnbank.bean.ElecSignInfo;
import com.centerm.jnbank.bean.Employee;
import com.centerm.jnbank.bean.PrinterItem;
import com.centerm.jnbank.bean.QpsBinData;
import com.centerm.jnbank.bean.QpsBlackBinData;
import com.centerm.jnbank.bean.ReverseInfo;
import com.centerm.jnbank.bean.TradeInfo;
import com.centerm.jnbank.bean.TradePrintData;
import com.centerm.jnbank.bean.iso.Iso62Aid;
import com.centerm.jnbank.bean.iso.Iso62Capk;
import com.centerm.jnbank.bean.iso.Iso62Qps;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import config.Config;


/**
 * author:wanliang527</br>
 * date:2016/10/22</br>
 */

public class DbHelper extends OrmLiteSqliteOpenHelper {
    // 数据库版本号
    private final static int VERSION = Config.DB_VERSION;
    // 数据库名
    private final static String DB_NAME = Config.DB_NAME;
    private List<Class> clses;

    private Map<String, Dao> daos = new HashMap<>();


    public DbHelper(Context mCtx) {
        super(mCtx.getApplicationContext(), DB_NAME, null, VERSION);
        clses = new ArrayList<>();
        clses.add(Employee.class);
        clses.add(PrinterItem.class);
        clses.add(ReverseInfo.class);
        clses.add(TradeInfo.class);
        clses.add(ElecSignInfo.class);
        clses.add(TradePrintData.class);
        clses.add(BinData.class);
        clses.add(QpsBinData.class);
        clses.add(QpsBlackBinData.class);
        clses.add(Iso62Capk.class);
        clses.add(Iso62Aid.class);
        clses.add(Iso62Qps.class);
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            for (Class cls : clses) {
                TableUtils.createTable(connectionSource, cls);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        database.beginTransaction();
        try {
            final String sql = "INSERT INTO tb_employee (code, password) VALUES ('%s', '%s')";
            database.execSQL(String.format(sql, Config.DEFAULT_ADMIN_ACCOUNT, Config.DEFAULT_ADMIN_PWD));
            database.execSQL(String.format(sql, Config.DEFAULT_MSN_ACCOUNT, Config.DEFAULT_MSN_PWD));
            //for (int i = 0; i < 5; i++) {
            for (int i = 0; i < 1; i++) {
                database.execSQL(String.format(sql, "0" + (i + 1), "0000"));
            }
            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.endTransaction();
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        //升级数据库版本，需要保存已有数据，创建新建的表，针对表项内容发生变化的表，需要单独进行处理
        //注意：要兼容不同版本的数据库
    }

    public synchronized Dao getDao(Class clazz) throws SQLException {
        String className = clazz.getSimpleName();
        Dao dao = null;
        if (daos.containsKey(className)) {
            dao = daos.get(className);
        }
        if (dao == null) {
            dao = super.getDao(clazz);
            daos.put(className, dao);
        }
        return dao;
    }

    public synchronized void removeDao(Class clazz) {
        String className = clazz.getSimpleName();
        if (daos.containsKey(className)) {
            daos.remove(className);
        }
    }

    @Override
    public void close() {
        super.close();
        daos.clear();
    }

}
