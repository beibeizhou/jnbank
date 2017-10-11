package com.centerm.jnbank.bean.iso;

import com.centerm.jnbank.utils.DataHelper;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.HashMap;
import java.util.Map;

/**
 * 小额免密免签参数实体类
 * <p>
 * author:wanliang527</br>
 * date:2016/11/24</br>
 */

@DatabaseTable(tableName = "tb_qps_params")
public class Iso62Qps {
    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField
    private String value;
    /**
     * 非接交易通道开关，0-优先联机借贷记；1-优先电子现金
     * N1
     */
    @DatabaseField
    private String FF805D;
    /**
     * 闪卡当笔重刷处理时间,默认值10秒
     * N3
     */
    @DatabaseField
    private String FF803A;
    /**
     * 闪卡记录可处理时间，默认值60秒
     * N3
     */
    @DatabaseField
    private String FF803C;
    /**
     * 非接快速业务（QPS）免密限额，终端使用此数据元作为条件之一判断非接联机交易是否请求持卡人验证方法，默认值300元，精确至小数点后两位
     * N12
     */
    @DatabaseField(defaultValue = "000000030000")
    private String FF8058;
    /**
     * 非接快速业务标识，终端使用此数据元作为是否开启非接快速功能的判断条件。1-启用0-关闭
     * N1
     */
    @DatabaseField
    private String FF8054;
    /**
     * BIN表A标识，终端使用此数据元作为是否将BIN表A作为免密的判断条件，启用该标识意味着非接快速业务处于试点阶段。1-启用，0-关闭
     * N1
     */
    @DatabaseField
    private String FF8055;
    /**
     * BIN表B标识，在终端启用此数据元意味着非接快速业务试点结束，但仍处于试点结束后的初期阶段，即贷记卡实现全面支持，但此时境内借记卡尚未实现全面支持，借记卡依然根据BIN表判断。1-启用，0-关闭
     * N1
     */
    @DatabaseField
    private String FF8056;
    /**
     * CDCVM标识，终端使用此数据元作为是否将卡片CDCVM执行情况作为免密的判断条件。1-启用，0-关闭
     * N1
     */
    @DatabaseField(defaultValue = "000000030000")
    private String FF8057;
    /**
     * 免签限额，终端使用此数据元作为判断交易凭证是否需要进行免签处理，默认值为300元，精确至小数点后两位
     * N12
     */
    @DatabaseField
    private String FF8059;
    /**
     * 免签标识，终端使用此数据元作为是否支持交易凭证免签处理的判断条件，1-启用，0-关闭
     * N1
     */
    @DatabaseField
    private String FF805A;

    public Iso62Qps() {
    }

    public Iso62Qps(String value) {
        this.value = value;
        if (value != null) {
            Map<String, String> tlvMap = new HashMap<>();
            int index = 0;
            while (index < value.length()) {
                String tag = value.substring(index, index + 6);
                index += tag.length();
                String len = value.substring(index, index + 2);
                index += len.length();
                int intLen = Integer.parseInt(len);
                String v = value.substring(index, index + intLen * 2);
                index += v.length();
                tlvMap.put(tag, v);
            }
            FF805D = tlvMap.get("FF805D");
            FF803A = tlvMap.get("FF803A");
            FF803C = tlvMap.get("FF803C");
            FF8058 = tlvMap.get("FF8058");
            FF8054 = tlvMap.get("FF8054");
            FF8055 = tlvMap.get("FF8055");
            FF8056 = tlvMap.get("FF8056");
            FF8057 = tlvMap.get("FF8057");
            FF8059 = tlvMap.get("FF8059");
            FF805A = tlvMap.get("FF805A");
        }
    }
    /**
     * 获取小额免密免签推广阶段
     *
     * @return 返回3代表全面推广，2代表试点阶段二，1代表试点阶段一，0代表不支持非接快速
     */
    public int getPromotionStage() {
        if ("1".equals(getFF8054())) {
            if ("1".equals(getFF8056())) {//BIN表B标识
                return 2;
            } else if ("1".equals(getFF8055())) {//BIN表A标识
                return 1;
            } else  {
                return 3;
            }
        } else {
            return 0;
        }
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getFF805D() {
        return FF805D;
    }

    public void setFF805D(String FF805D) {
        this.FF805D = FF805D;
    }

    public String getFF803A() {
        return FF803A;
    }

    public void setFF803A(String FF803A) {
        this.FF803A = FF803A;
    }

    public String getFF803C() {
        return FF803C;
    }

    public void setFF803C(String FF803C) {
        this.FF803C = FF803C;
    }

    public String getFF8058() {
        return FF8058;
    }

    public void setFF8058(String FF8058) {
        this.FF8058 = FF8058;
    }

    public String getFF8054() {
        return FF8054;
    }

    public void setFF8054(String FF8054) {
        this.FF8054 = FF8054;
    }

    public String getFF8055() {
        return FF8055;
    }

    public void setFF8055(String FF8055) {
        this.FF8055 = FF8055;
    }

    public String getFF8056() {
        return FF8056;
    }

    public void setFF8056(String FF8056) {
        this.FF8056 = FF8056;
    }

    public String getFF8057() {
        return FF8057;
    }

    public void setFF8057(String FF8057) {
        this.FF8057 = FF8057;
    }

    public String getFF8059() {
        return FF8059;
    }

    public void setFF8059(String FF8059) {
        this.FF8059 = FF8059;
    }

    public String getFF805A() {
        return FF805A;
    }

    public void setFF805A(String FF805A) {
        this.FF805A = FF805A;
    }

    public boolean isNoPinOn() {
        return "1".equals(FF8054);
    }

    public double getNoPinLimit() {
        try {
            return DataHelper.parseIsoF4(FF8058);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 300;
    }
    public boolean isNoSignOn() {
        return "1".equals(FF805A);
    }

    public double getNoSignLimit() {
        try {
            return DataHelper.parseIsoF4(FF8059);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 300;
    }

    @Override
    public String toString() {
        return "Iso62Qps{" +
                "id=" + id +
                ", isNoPinOn='" + isNoPinOn() + '\'' +
                ", isNoSignOn='" + isNoSignOn() + '\'' +
                ", noPinLimit='" + getNoPinLimit() + '\'' +
                ", noSignLimit='" + getNoSignLimit() + '\'' +
                '}';
    }
}
