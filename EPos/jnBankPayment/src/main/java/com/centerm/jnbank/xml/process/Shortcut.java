package com.centerm.jnbank.xml.process;

import android.content.Context;
import android.content.Intent;

import com.centerm.jnbank.xml.handler.ProcessHandler;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * 菜单快捷方式
 */
public class Shortcut {

    public final static String KEY_TRANSACTION_PROCESS = "KEY_TRANSACTION_PROCESS";

    private int iconId;                               //图标资源id
    private int textId;                               //字体资源id
    private String xmlFile;                           //流程定义xml文件路径
    private Intent intent;                            //快捷方式启动Intent
    private TradeProcess transaction;                  //交易流程
    private Context context;                          //Context

    public Shortcut(int iconId, int textId) {
        this.iconId = iconId;
        this.textId = textId;
    }

    public Shortcut(int iconId, int textId, Intent intent) {
        this.iconId = iconId;
        this.textId = textId;
        this.intent = intent;
    }

    public Shortcut(Context context, int iconId, int textId) {
        this.iconId = iconId;
        this.textId = textId;
        this.context = context;
    }

    public Shortcut(Context context, int iconId, int textId, String xmlFile) {
        this.iconId = iconId;
        this.textId = textId;
        this.xmlFile = xmlFile;
        this.context = context;
    }

    public Shortcut(Context context, String xmlFile) {
        this.xmlFile = xmlFile;
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public int getIconId() {
        return iconId;
    }

    public void setIconId(int iconId) {
        this.iconId = iconId;
    }

    public int getTextId() {
        return textId;
    }

    public void setTextId(int textId) {
        this.textId = textId;
    }

    public TradeProcess getTransaction() {
        if (xmlFile != null && transaction == null) {
            transaction = parseTransaction();
        }
        return transaction;
    }

    public void setTransaction(TradeProcess mTransaction) {
        this.transaction = mTransaction;
    }

    public Intent getIntent() {
        if (xmlFile != null && intent == null) {
            intent = new Intent();
            intent.setAction(getTransaction().getFirstComponentNode().getComponentName());
            intent.putExtra(KEY_TRANSACTION_PROCESS, getTransaction());
        }
        return intent;
    }

    public void setIntent(Intent mIntent) {
        this.intent = mIntent;
    }

    private TradeProcess parseTransaction() {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            XMLReader reader = parser.getXMLReader();
            ProcessHandler handler = new ProcessHandler();
            reader.setContentHandler(handler);
            InputSource is = new InputSource(this.context.getAssets().open(xmlFile));
            reader.parse(is);
            return handler.getTransaction();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
