package com.centerm.jnbank.bean.iso;

import android.text.TextUtils;

import com.centerm.cpay.midsdk.dev.common.utils.TlvUtils;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.HashMap;
import java.util.Map;

/**
 * 62域中终端公钥参数实体类
 * author:wanliang527</br>
 * date:2016/11/20</br>
 */

@DatabaseTable(tableName = "tb_capk")
public class Iso62Capk {
    @DatabaseField(generatedId = true)
    private int id;
    @DatabaseField
    private String capk;
    @DatabaseField
    private int importTimes = 0;//导入次数

    private Map<String, String> tlvMap;

    public Iso62Capk() {
    }

    public Iso62Capk(String capk) {
        this.capk = capk;
        initMap();
    }

    private void initMap() {
        if (!TextUtils.isEmpty(capk)) {
            tlvMap = TlvUtils.tlvToMap(capk);
        } else {
            tlvMap = new HashMap<>();
        }
    }

    private void resetMap() {
        if (tlvMap == null || tlvMap.size() == 0) {
            if (!TextUtils.isEmpty(capk)) {
                tlvMap = TlvUtils.tlvToMap(capk);
            }
            if (tlvMap == null) {
                tlvMap = new HashMap<>();
            }
        }
    }

    public void setCapk(String capk) {
        this.capk = capk;
    }

    public String getCapk() {
        return capk;
    }

    /**
     * 获取RID。tag值：9F06，长度：5
     *
     * @return
     */
    public String getRID() {
        resetMap();
        return "9F0605" + tlvMap.get("9F06");
    }

    /**
     * 认证中心公钥索引。tag值：9F22，长度：1
     *
     * @return 认证中心公钥索引
     */
    public String getIndex() {
        resetMap();
        return "9F2201" + tlvMap.get("9F22");
    }

    /**
     * 认证中心公钥有效期。tag值：DF05，长度：8
     *
     * @return 认证中心公钥有效期
     */
    public String getValidity() {
        resetMap();
        return "DF0508" + tlvMap.get("DF05");
    }

    public int getImportTimes() {
        return importTimes;
    }

    public void setImportTimes(int importTimes) {
        this.importTimes = importTimes;
    }
}
