package com.centerm.jnbank.msg;

import android.content.Context;

import com.centerm.jnbank.channels.EnumChannel;
import com.centerm.jnbank.common.Settings;

import java.util.Map;

/**
 * 报文工厂
 * author:wanliang527</br>
 * date:2016/10/27</br>
 */

public class MessageFactory {
    private Context context;
    private EnumChannel channel;

    public MessageFactory(Context context) {
        this.context = context;
        channel = EnumChannel.valueOf(Settings.getPosChannel(context));
    }

    public Object pack(String transCode, Map<String, String> data) {
        switch (channel) {
            case SHENGPAY:
                return new ShengPayMsgFactory().pack(context, transCode, data);
        }
        return null;
    }

    public Map<String, String> unpack(String transCode, Object data) {
        switch (channel) {
            case SHENGPAY:
                try {
                    return new ShengPayMsgFactory().unPack(context, transCode, data);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
        }
        return null;
    }


}
