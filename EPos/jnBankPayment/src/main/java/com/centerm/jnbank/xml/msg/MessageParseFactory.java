package com.centerm.jnbank.xml.msg;

import android.content.Context;

import com.centerm.jnbank.channels.EnumChannel;

/**
 * 报文解析工厂
 * author:wanliang527</br>
 * date:2016/10/17</br>
 */

public class MessageParseFactory {
    private final static String DEFINE_PATH = "msg/define";//接口定义的路径
    private final static String ADAPTER_PATH = "msg/adapter";//接口适配的路径

    private Context context;
    private EnumChannel channel;

    public MessageParseFactory(Context context, EnumChannel channel) {
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }
        if (channel == null) {
            throw new IllegalArgumentException("Please assgin a channle type for generate message");
        }
        this.context = context;
        this.channel = channel;
    }


}
