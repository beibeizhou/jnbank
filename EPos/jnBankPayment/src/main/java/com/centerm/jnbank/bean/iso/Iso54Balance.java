package com.centerm.jnbank.bean.iso;

import com.centerm.jnbank.utils.DataHelper;

/**
 * author:wanliang527</br>
 * date:2016/11/1</br>
 */

public class Iso54Balance {
    private String accType;//账户类型，10-储蓄账户、20-支票账户、30-信用卡账户、90-积分账户
    private String amtType;//余额类型，02-可用余额
    private String currencyCode;//货币代码，156-人民币、999-积分账户、其它币种依据ISO
    private String amtSign;//余额符号，C-正值
    private String amount;//余额，若交易不成功为全零

    public Iso54Balance(String iso54Data) {
        if (iso54Data.length() == 20 || iso54Data.length() == 40) {
            accType = iso54Data.substring(0, 2);
            amtType = iso54Data.substring(2, 4);
            currencyCode = iso54Data.substring(4, 7);
            amtSign = iso54Data.substring(7, 8);
            amount = DataHelper.formatAmountForShow(iso54Data.substring(8, 20));
        }
    }

    public String getAccType() {
        return accType;
    }

    public String getAmtType() {
        return amtType;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public String getAmtSign() {
        return amtSign;
    }

    public String getAmount() {
        return amount;
    }

    @Override
    public String toString() {
        return "Iso54Balance{" +
                "accType='" + accType + '\'' +
                ", amtType='" + amtType + '\'' +
                ", currencyCode='" + currencyCode + '\'' +
                ", amtSign='" + amtSign + '\'' +
                ", amount='" + amount + '\'' +
                '}';
    }
}
