package com.centerm.jnbank.common;


import com.centerm.jnbank.R;

/**
 * author:ysd</br>
 * date:2016/10/27</br>
 */
public enum PrinterParamEnum {
    SHOP_HEADER("9F01", R.string.printer_header),
    SHOP_NAME("9F02", R.string.printer_name),
    SHOP_NUM("9F03", R.string.printer_num),
    SHOP_TERM_NUM("9F04", R.string.printer_term_num),
    SHOP_SEND_CARD_BRANK("9F05", R.string.printer_send_card_brank),
    SHOP_RECEIVE_BRANK("9F06", R.string.printer_receive_brank),
    SHOP_CARD_NUM("9F07", R.string.printer_card_num),
    SHOP_BATCH_NUM("9F08", R.string.printer_batch_num),
    SHOP_TRAN_FLOW_NUM("9F09", R.string.printer_tran_flow_num),
    SHOP_PERMISION_CODE("9F10", R.string.printer_permision_code),
    SHOP_REFERENCE_CODE("9F11", R.string.printer_reference_code),
    SHOP_DATE_TIME("9F12", R.string.printer_date_time),
    SHOP_AMOUNT("9F13", R.string.printer_amount),
    SHOP_COMMENT("9F14", R.string.printer_comment),
    SHOP_DESCRIBE("9F15", R.string.printer_describe),
    SHOP_NOT_USED1("9F16", R.string.printer_not_used1),
    SHOP_NOT_USED2("9F17", R.string.printer_not_used2),
    SHOP_NOT_USED3("9F18", R.string.printer_not_used3),
    SHOP_NOT_USED4("9F19", R.string.printer_not_used4),
    SHOP_NOT_USED5("9F20", R.string.printer_not_used5),

    PERSON_HEADER("9F51", R.string.printer_header),
    PERSON_NAME("9F52", R.string.printer_name),
    PERSON_NUM("9F53", R.string.printer_num),
    PERSON_TERM_NUM("9F54", R.string.printer_term_num),
    PERSON_SEND_CARD_BRANK("9F55", R.string.printer_send_card_brank),
    PERSON_RECEIVE_BRANK("9F56", R.string.printer_receive_brank),
    PERSON_CARD_NUM("9F57", R.string.printer_card_num),
    PERSON_BATCH_NUM("9F58", R.string.printer_batch_num),
    PERSON_TRAN_FLOW_NUM("9F59", R.string.printer_tran_flow_num),
    PERSON_PERMISION_CODE("9F60", R.string.printer_permision_code),
    PERSON_REFERENCE_CODE("9F61", R.string.printer_reference_code),
    PERSON_DATE_TIME("9F62", R.string.printer_date_time),
    PERSON_AMOUNT("9F63", R.string.printer_amount),
    PERSON_COMMENT("9F64", R.string.printer_comment),
    PERSON_DESCRIBE("9F65", R.string.printer_describe),
    PERSON_NOT_USED1("9F66", R.string.printer_not_used1),
    PERSON_NOT_USED2("9F67", R.string.printer_not_used2),
    PERSON_NOT_USED3("9F68", R.string.printer_not_used3),
    PERSON_NOT_USED4("9F69", R.string.printer_not_used4),
    PERSON_NOT_USED5("9F70", R.string.printer_not_used5);
    private String paramId;
    private int paramTip;

    PrinterParamEnum(String paramId, int paramTip) {
        this.paramId = paramId;
        this.paramTip = paramTip;
    }

    public String getParamId() {
        return paramId;
    }

    public void setParamId(String paramId) {
        this.paramId = paramId;
    }

    public int getParamTip() {
        return paramTip;
    }

    public void setParamTip(int paramTip) {
        this.paramTip = paramTip;
    }
}
