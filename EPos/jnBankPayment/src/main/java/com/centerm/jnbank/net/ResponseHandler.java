package com.centerm.jnbank.net;


public interface ResponseHandler {
    /**
     * 请求成功。
     *
     * @param statusCode 状态码
     * @param msg        返回的字符内容
     * @param data       数据
     */
    void onSuccess(String statusCode, String msg, byte[] data);

    /**
     * 请求失败
     *
     * @param code  状态码
     * @param msg   状态信息
     * @param error 异常对象
     */
    void onFailure(String code, String msg, Throwable error);
}
