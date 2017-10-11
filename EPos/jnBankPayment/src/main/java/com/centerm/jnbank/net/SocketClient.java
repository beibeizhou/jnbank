package com.centerm.jnbank.net;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.centerm.cpay.midsdk.dev.common.utils.HexUtils;
import com.centerm.jnbank.common.Settings;
import com.centerm.jnbank.common.StatusCode;
import com.centerm.jnbank.common.TransCode;
import com.centerm.jnbank.utils.CheckFactory;
import com.centerm.jnbank.utils.UseMobileNetwork;

import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * author:wanliang527</br>
 * date:2016/10/27</br>
 */

public class SocketClient {
    private final static String KEY_STATUS_CODE = "KEY_STATUS_CODE";
    private final static String KEY_ERROR_INFO = "KEY_ERROR_INFO";
    private final static String KEY_RETURN_DATA = "KEY_RETURN_DATA";
    private final static String KEY_THROWABLE = "KEY_THROWABLE";
    private static String TRANSCODE = "";

    private final static int MSG_SUCCESS = 0x100;
    private final static int MSG_FAILED = 0x101;
//    private final static int MSG_CONNECT_SUCCESS = 0x102;
//    private final static int MSG_CONNECT_SUCCESS = 0x103;

    private Logger logger = Logger.getLogger(SocketClient.class);
    private Context context;
    private static SocketClient instance;
    private Socket showCodeCocket = null;
    private SocketClient() {
    }

    public static SocketClient getInstance(Context context) {
        if (instance == null) {
            synchronized (SocketClient.class) {
                if (instance == null) {
                    instance = new SocketClient();
                }
            }
        }
        instance.context = context.getApplicationContext();
        return instance;
    }

    public void sendData(byte[] data, ResponseHandler handler) {
        TRANSCODE = "";
        new SocketThread(new InnerHandler(context.getMainLooper(), handler), data).start();
    }

    public void sendData(byte[] data, ResponseHandler handler, String transCode) {
        TRANSCODE = transCode;
        new SocketThread(new InnerHandler(context.getMainLooper(), handler), data).start();
    }
    public void syncSendData(byte[] data, ResponseHandler handler) {
        syncSendData(data, handler,"");
    }
    public void syncSendData(byte[] data, ResponseHandler handler,String transCode) {
        TRANSCODE = transCode;
        if ("SHOWCODE".equals(TRANSCODE)) {
            int port;
            String ip;
            try {
                if(TRANSCODE.equals(TransCode.INIT_TERMINAL)||TRANSCODE.equals(TransCode.LOAD_PARAM)) {
                    String domainName = Settings.getDomainName(context);
                    InetAddress netAddress = InetAddress.getByName(domainName);
                    ip =netAddress.getHostAddress();
                    //ip = Settings.getParamIp(context);
                    //port = Settings.getCommonPortParam(context);
                    port = 7005;
                } else {
                    ip = Settings.getCommonIp1(context);
                    port = Settings.getCommonPort1(context);
                }
                logger.info("[开始发送数据]==>IP==>" + ip + "==>PORT==>" + port);
                InetSocketAddress address = new InetSocketAddress(ip, port);
                showCodeCocket = new Socket();

                int respTimeout = Settings.getRespTimeout(context);
                int connectTimeout = Settings.getConnectTimeout(context);
                logger.debug("[正在连接中...]==>连接超时==>" + connectTimeout + "==>响应超时==>" + respTimeout);
                int retryCount = 0;
                int CONNECT_TIMES_MAX = 2;
                while (retryCount < CONNECT_TIMES_MAX) {
                    try {
                        showCodeCocket.connect(address, connectTimeout*1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (!(TRANSCODE.equals(TransCode.INIT_TERMINAL) || TRANSCODE.equals(TransCode.LOAD_PARAM))) {
                            logger.info("第一次连接失败，更换IP2继续连接");
                            ip = Settings.getCommonIp2(context);
                            port = Settings.getCommonPort2(context);
                            address = new InetSocketAddress(ip, port);
                        } else {
                            logger.info("域名连接失败，更换IP继续连接");
                            ip = Settings.getParamIp(context);
                            port = Settings.getCommonPortParam(context);
                            address = new InetSocketAddress(ip, port);
                        }
                        retryCount++;
                        cancel(showCodeCocket);
                        showCodeCocket = new Socket();
                        if (retryCount >= CONNECT_TIMES_MAX) {
                            logger.info("两次连接失败！！");
                            logger.warn(e.toString());
                            handler.onFailure(StatusCode.SOCKET_TIMEOUT.getStatusCode(), getString(StatusCode.SOCKET_TIMEOUT), e);
                            cancel(showCodeCocket);
                            return;
                        }
                        continue;
                    }
                    break;
                }
                showCodeCocket.setSoTimeout(respTimeout*1000);
                logger.debug("[连接成功]");
                InputStream is = showCodeCocket.getInputStream();
                OutputStream os = showCodeCocket.getOutputStream();
                os.write(data);
                os.flush();
                logger.debug("[正在发送数据...]==>" + HexUtils.bytesToHexString(data));
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len;
                while ((len = is.read(buffer)) > 0) {
                    baos.write(buffer, 0, len);
                    if (CheckFactory.isTradeDataReceivedComplete(baos.toByteArray()))
                        break;
                }
                byte[] returnData = baos.toByteArray();
                baos.close();
                if (data == null || data.length == 0) {
                    logger.warn("[接收数据为空]");
                    throw new SocketTimeoutException();
                }
                logger.debug("[接收到数据]==>" + HexUtils.bytesToHexString(returnData));
                cancelShowCodeSocket();
                handler.onSuccess(StatusCode.SUCCESS.getStatusCode(), getString(StatusCode.SUCCESS), returnData);
            } catch (UnknownHostException e) {
                logger.warn(e.toString());
                handler.onFailure(StatusCode.UNKNOWN_HOST.getStatusCode(), getString(StatusCode.UNKNOWN_HOST), e);
                cancelShowCodeSocket();
            } catch (SocketTimeoutException e) {
                logger.warn(e.toString());
                handler.onFailure(StatusCode.SOCKET_TIMEOUT.getStatusCode(), getString(StatusCode.SOCKET_TIMEOUT), e);
                cancelShowCodeSocket();
            } catch (SocketException e) {
                logger.warn(e.toString());
                handler.onFailure(StatusCode.CONNECTION_EXCEPTION.getStatusCode(), getString(StatusCode.CONNECTION_EXCEPTION), e);
                cancelShowCodeSocket();
            } catch (Exception e) {
                logger.warn(e.toString());
                handler.onFailure(StatusCode.UNKNOWN_REASON.getStatusCode(), getString(StatusCode.UNKNOWN_REASON), e);
                cancelShowCodeSocket();
            }
        } else {
            Socket socket = null;
            int port;
            String ip;
            try {
                if(TRANSCODE.equals(TransCode.INIT_TERMINAL)||TRANSCODE.equals(TransCode.LOAD_PARAM)) {
                    String domainName = Settings.getDomainName(context);
                    InetAddress netAddress = InetAddress.getByName(domainName);
                    ip =netAddress.getHostAddress();
                    //ip = Settings.getParamIp(context);
                    //port = Settings.getCommonPortParam(context);
                    port = 7005;
                } else {
                    ip = Settings.getCommonIp1(context);
                    port = Settings.getCommonPort1(context);
                }
                logger.info("[开始发送数据]==>IP==>" + ip + "==>PORT==>" + port);
                InetSocketAddress address = new InetSocketAddress(ip, port);
                socket = new Socket();

                int respTimeout = Settings.getRespTimeout(context);
                int connectTimeout = Settings.getConnectTimeout(context);
                logger.debug("[正在连接中...]==>连接超时==>" + connectTimeout + "==>响应超时==>" + respTimeout);
                int retryCount = 0;
                int CONNECT_TIMES_MAX = 2;
                while (retryCount < CONNECT_TIMES_MAX) {
                    try {
                        socket.connect(address, connectTimeout*1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (!(TRANSCODE.equals(TransCode.INIT_TERMINAL) || TRANSCODE.equals(TransCode.LOAD_PARAM))) {
                            logger.info("第一次连接失败，更换IP2继续连接");
                            ip = Settings.getCommonIp2(context);
                            port = Settings.getCommonPort2(context);
                            address = new InetSocketAddress(ip, port);
                        } else {
                            logger.info("域名连接失败，更换IP继续连接");
                            ip = Settings.getParamIp(context);
                            port = Settings.getCommonPortParam(context);
                            address = new InetSocketAddress(ip, port);
                        }
                        retryCount++;
                        cancel(socket);
                        socket = new Socket();
                        if (retryCount >= CONNECT_TIMES_MAX) {
                            logger.info("两次连接失败！！");
                            logger.warn(e.toString());
                            handler.onFailure(StatusCode.SOCKET_TIMEOUT.getStatusCode(), getString(StatusCode.SOCKET_TIMEOUT), e);
                            cancel(socket);
                            return;
                        }
                        continue;
                    }
                    break;
                }
                socket.setSoTimeout(respTimeout*1000);
                logger.debug("[连接成功]");
                InputStream is = socket.getInputStream();
                OutputStream os = socket.getOutputStream();
                os.write(data);
                os.flush();
                logger.debug("[正在发送数据...]==>" + HexUtils.bytesToHexString(data));
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len;
                while ((len = is.read(buffer)) > 0) {
                    baos.write(buffer, 0, len);
                    if (CheckFactory.isTradeDataReceivedComplete(baos.toByteArray()))
                        break;
                }
                byte[] returnData = baos.toByteArray();
                baos.close();
                if (data == null || data.length == 0) {
                    logger.warn("[接收数据为空]");
                    throw new SocketTimeoutException();
                }
                logger.debug("[接收到数据]==>" + HexUtils.bytesToHexString(returnData));
                cancel(socket);
                handler.onSuccess(StatusCode.SUCCESS.getStatusCode(), getString(StatusCode.SUCCESS), returnData);
            } catch (UnknownHostException e) {
                logger.warn(e.toString());
                handler.onFailure(StatusCode.UNKNOWN_HOST.getStatusCode(), getString(StatusCode.UNKNOWN_HOST), e);
                cancel(socket);
            } catch (SocketTimeoutException e) {
                logger.warn(e.toString());
                handler.onFailure(StatusCode.SOCKET_TIMEOUT.getStatusCode(), getString(StatusCode.SOCKET_TIMEOUT), e);
                cancel(socket);
            } catch (SocketException e) {
                logger.warn(e.toString());
                handler.onFailure(StatusCode.CONNECTION_EXCEPTION.getStatusCode(), getString(StatusCode.CONNECTION_EXCEPTION), e);
                cancel(socket);
            } catch (Exception e) {
                e.printStackTrace();
                handler.onFailure(StatusCode.UNKNOWN_REASON.getStatusCode(), getString(StatusCode.UNKNOWN_REASON), e);
                cancel(socket);
            }
        }
    }

    public void sendSequenceData(String firstTag, byte[] firstData, SequenceHandler handler) {
        if (handler == null) {
            logger.warn("回调接收器为空，不发送数据");
            return;
        }
        handler.bindClient(this, false);
        handler.sendNext(firstTag, firstData);
    }

    public void syncSendSequenceData(String firstTag, byte[] firstData, SequenceHandler handler) {
        if (handler == null) {
            logger.warn("回调接收器为空，不发送数据");
            return;
        }
        handler.bindClient(this, true);
        handler.sendNext(firstTag, firstData);
    }

    private String getString(StatusCode error) {
        return context.getResources().getString(error.getMsgId());
    }

    private void cancel(Socket socket) {
        if (socket != null) {
            try {
                logger.debug("[关闭Socket]");
                socket.close();
                socket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void cancelShowCodeSocket(){
        if (showCodeCocket != null) {
            try {
                logger.debug("[关闭showCodeCocket]");
                showCodeCocket.close();
                showCodeCocket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private class SocketThread extends Thread {
        private Handler handler;
        private byte[] data;
        private Socket socket;

        public SocketThread(Handler handler, byte[] data) {
            this.handler = handler;
            this.data = data;
        }

        @Override
        public void run() {
            super.run();
            int port;
            String ip;
            logger.info("bbbbbbbbbbbb");
            try {
            if(TRANSCODE.equals(TransCode.INIT_TERMINAL)||TRANSCODE.equals(TransCode.LOAD_PARAM)) {
                String domainName = Settings.getDomainName(context);
                InetAddress netAddress = InetAddress.getByName(domainName);
                ip =netAddress.getHostAddress();
                //ip = Settings.getParamIp(context);
                //port = Settings.getCommonPortParam(context);
                port = 7005;
            } else {
                ip = Settings.getCommonIp1(context);
                port = Settings.getCommonPort1(context);
            }
            logger.info("[开始发送数据]==>IP==>" + ip + "==>PORT==>" + port);
            InetSocketAddress address = new InetSocketAddress(ip, port);
            socket = new Socket();
                int respTimeout = Settings.getRespTimeout(context);
                int connectTimeout = Settings.getConnectTimeout(context);
                logger.debug("[正在连接中...]==>连接超时==>" + connectTimeout + "==>响应超时==>" + respTimeout);
                int retryCount = 0;
                int CONNECT_TIMES_MAX = 2;
                while (retryCount < CONNECT_TIMES_MAX) {
                    try {
//                        connectTimeout = 5000;
                        socket.connect(address, connectTimeout*1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                        if(!(TRANSCODE.equals(TransCode.INIT_TERMINAL)||TRANSCODE.equals(TransCode.LOAD_PARAM))) {
                            logger.info("第一次连接失败，更换IP2继续连接");
                            ip = Settings.getCommonIp2(context);
                            port = Settings.getCommonPort2(context);
                            address = new InetSocketAddress(ip, port);
                        }else {
                            logger.info("域名连接失败，更换IP继续连接");
                            ip = Settings.getParamIp(context);
                            port = Settings.getCommonPortParam(context);
                            address = new InetSocketAddress(ip, port);
                        }
                            cancel(socket);
                            socket = new Socket();
                            retryCount++;
                            if(retryCount >= CONNECT_TIMES_MAX){
                                sendMessage(handler, MSG_FAILED, StatusCode.SOCKET_TIMEOUT.getStatusCode(), getString(StatusCode.SOCKET_TIMEOUT), null, e);
                                cancel(socket);
                                return;
                            }
                            continue;
                    }
                    break;
                }

                socket.setSoTimeout(respTimeout*1000);
                logger.debug("[连接成功]");
                InputStream is = socket.getInputStream();
                OutputStream os = socket.getOutputStream();
                os.write(data);
                os.flush();
                logger.debug("[正在发送数据...]==>" + HexUtils.bytesToHexString(data));
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len;
                if ((len = is.read(buffer)) > 0) {
                    baos.write(buffer, 0, len);
                }
                byte[] data = baos.toByteArray();
                baos.close();
                if (data == null || data.length == 0) {
                    logger.warn("[接收数据为空]");
                    throw new SocketTimeoutException();
                }
                logger.debug("[接收到数据]==>" + HexUtils.bytesToHexString(data));
                sendMessage(handler, MSG_SUCCESS, StatusCode.SUCCESS.getStatusCode(), getString(StatusCode.SUCCESS), data, null);
                cancel(socket);
            } catch (UnknownHostException e) {
                e.printStackTrace();
                sendMessage(handler, MSG_FAILED, StatusCode.UNKNOWN_HOST.getStatusCode(), getString(StatusCode.UNKNOWN_HOST), null, e);
                cancel(socket);
            } catch (SocketTimeoutException e) {
                e.printStackTrace();
                sendMessage(handler, MSG_FAILED, StatusCode.SOCKET_TIMEOUT.getStatusCode(), getString(StatusCode.SOCKET_TIMEOUT), null, e);
                cancel(socket);
            } catch (ConnectException e) {
                e.printStackTrace();
                sendMessage(handler, MSG_FAILED, StatusCode.CONNECTION_EXCEPTION.getStatusCode(), getString(StatusCode.CONNECTION_EXCEPTION), null, e);
                cancel(socket);
            } catch (Exception e) {
                e.printStackTrace();
                sendMessage(handler, MSG_FAILED, StatusCode.UNKNOWN_REASON.getStatusCode(), getString(StatusCode.UNKNOWN_REASON), null, e);
                cancel(socket);
            }
        }

        private void sendMessage(Handler handler, int what, String statusCode, String errorInfo, byte[] returnData, Throwable error) {
            Message msg = new Message();
            msg.what = what;
            Bundle bundle = new Bundle();
            bundle.putString(KEY_STATUS_CODE, statusCode);
            bundle.putString(KEY_ERROR_INFO, errorInfo);
            bundle.putByteArray(KEY_RETURN_DATA, returnData);
            bundle.putSerializable(KEY_THROWABLE, error);
            msg.setData(bundle);
            handler.sendMessage(msg);
        }

    }

    private class InnerHandler extends Handler {
        private ResponseHandler outterHandler;

        public InnerHandler(Looper looper, ResponseHandler handler) {
            super(looper);
            this.outterHandler = handler;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String statusCode = data.getString(KEY_STATUS_CODE);
            String info = data.getString(KEY_ERROR_INFO);
            byte[] returnData = data.getByteArray(KEY_RETURN_DATA);
            Throwable throwable = (Throwable) data.getSerializable(KEY_THROWABLE);
            switch (msg.what) {
                case MSG_SUCCESS:
                    if (outterHandler != null) {
                        outterHandler.onSuccess(statusCode, info, returnData);
                    }
                    break;
                case MSG_FAILED:
                    if (outterHandler != null) {
                        outterHandler.onFailure(statusCode, info, throwable);
                    }
                    break;
            }
        }
    }
}
