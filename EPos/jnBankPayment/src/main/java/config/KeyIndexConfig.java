package config;

import com.centerm.jnbank.channels.EnumChannel;

/**
 * 密钥索引配置文件。默认情况下，都是返回渠道序列号。
 * author:wanliang527</br>
 * date:2016/10/29</br>
 */

public class KeyIndexConfig {

    public int getIndex(EnumChannel channel, KeyType type) {
//        if (type == KeyType.PIK || type == KeyType.MAK || type == KeyType.TDK) {
//            return channel.ordinal() + 1;
//        }
        return channel.ordinal();
    }


    public enum KeyType {
        TEK, TMK, PIK, MAK, TDK
    }

}
