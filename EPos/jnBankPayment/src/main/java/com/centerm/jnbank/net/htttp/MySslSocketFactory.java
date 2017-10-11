package com.centerm.jnbank.net.htttp;

import com.loopj.android.http.MySSLSocketFactory;

import java.io.IOException;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLSocket;

/**
 * Created by linwanliang on 2016/3/17.
 */
public class MySslSocketFactory extends MySSLSocketFactory {
    /**
     * Creates a new SSL Socket Factory with the given KeyStore.
     *
     * @param truststore A KeyStore to create the SSL Socket Factory in context of
     * @throws NoSuchAlgorithmException  NoSuchAlgorithmException
     * @throws KeyManagementException    KeyManagementException
     * @throws KeyStoreException         KeyStoreException
     * @throws UnrecoverableKeyException UnrecoverableKeyException
     */
    public MySslSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
        super(truststore);
    }

    @Override
    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException {
        SSLSocket sslSocket = (SSLSocket) super.createSocket(socket, host, port, autoClose);
        return deleteECDH(sslSocket);
    }

    @Override
    public Socket createSocket() throws IOException {
        SSLSocket sslSocket = (SSLSocket) super.createSocket();
        return deleteECDH(sslSocket);
    }

    private SSLSocket deleteECDH(SSLSocket socket) {
        String[] suites = socket.getSupportedCipherSuites();
        List<String> noEcdh = new ArrayList<String>();
        for (String c : suites) {
            if (c.contains("_ECDH_")) {
                continue;
            }
            noEcdh.add(c);
        }
        suites = new String[noEcdh.size()];
        noEcdh.toArray(suites);
        socket.setEnabledCipherSuites(suites);
        return socket;
    }

}
