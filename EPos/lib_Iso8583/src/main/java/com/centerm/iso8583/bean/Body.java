/**
 * copyright(c) 2013 FuJian star-net Information Corp.Ltd
 *
 * @File name:  Body.java
 * @Version : 1.0
 * @Create on:  2013-01-18
 * @Author :  Tianxiaobo
 * @ChangeList ---------------------------------------------------
 * Date         Editor              ChangeReasons
 */
package com.centerm.iso8583.bean;

import java.util.Map;

/**
 * 功能描述：这个类用于描述8583报文的报文体模块，包括报文类型和各个域的取值
 * @author Tianxiaobo
 *
 */
public class Body {
    /**存放对应的域Id和域数据格式控制对象*/
    private Map<Integer, Field> map = null;

    public Body(Map<Integer, Field> map) {
        this.map = map;
    }

    /**
     * 功能描述：获取Body对象的map属性
     * @return 获取map集合，该集合中存放了报文域ID和域数据的格式控制对象Field
     */
    public Map<Integer, Field> getMap() {
        return map;
    }

    /**
     * 功能描述：设置Body对象的map属性
     * @param map 该集合中存放了报文域ID和域数据的格式控制对象Field
     */
    public void setMap(Map<Integer, Field> map) {
        this.map = map;
    }

}
