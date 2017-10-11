package com.centerm.jnbank.bean.iso;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * 62域中终端IC卡AID参数实体类
 * author:wanliang527</br>
 * date:2016/11/22</br>
 */
@DatabaseTable(tableName = "tb_aid")
public class Iso62Aid {

    @DatabaseField(generatedId = true)
    private int id;
    @DatabaseField
    private String aid;
    @DatabaseField
    private int importTimes = 0;//导入次数

//    private Map<String, String> tlvMap;

    public Iso62Aid() {
    }

    public Iso62Aid(String aid) {
        this.aid = aid;
        initMap();
    }

    private void initMap() {
      /*  if (!TextUtils.isEmpty(aid)) {
            tlvMap = TlvUtils.tlvToMap(aid);
        } else {
            tlvMap = new HashMap<>();
        }*/
    }

    private void resetMap() {
       /* if (tlvMap == null || tlvMap.size() == 0) {
            if (!TextUtils.isEmpty(aid)) {
                tlvMap = TlvUtils.tlvToMap(aid);
        }
        if (tlvMap == null) {
            tlvMap = new HashMap<>();
        }
        }*/
    }

    public void setAid(String aid) {
        this.aid = aid;
    }

    public String getAid() {
        return aid;
    }

    public int getImportTimes() {
        return importTimes;
    }

    public void setImportTimes(int importTimes) {
        this.importTimes = importTimes;
    }
}
