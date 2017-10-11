package com.centerm.jnbank.xml;

import android.content.Context;

import com.centerm.jnbank.channels.EnumChannel;
import com.centerm.jnbank.xml.handler.MenuHandler;
import com.centerm.jnbank.xml.handler.ProcessHandler;
import com.centerm.jnbank.xml.menu.Menu;
import com.centerm.jnbank.xml.process.TradeProcess;

import org.apache.log4j.Logger;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * author:wanliang527</br>
 * date:2016/10/26</br>
 */

public class XmlParser {

    private static Logger logger = Logger.getLogger(XmlParser.class);

    private final static String MENU_PATH = "menu/";
    private final static String PROCESS_PATH = "process/";
    private final static String DEFINE_PATH = "define/";


    public static Menu parseMenu(Context context, EnumChannel channel) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            XMLReader reader = parser.getXMLReader();
            String fileDir = MENU_PATH + channel.name();
            logger.info("开始菜单文件解析：" + fileDir);
            InputSource is2 = new InputSource(context.getAssets().open(fileDir));
            MenuHandler handler = new MenuHandler();
            reader.setContentHandler(handler);
            reader.parse(is2);
            return handler.getMenu();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static TradeProcess parseProcess(Context context, String fileName) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            XMLReader reader = parser.getXMLReader();
            String fileDir = PROCESS_PATH + fileName;
            logger.info("开始流程文件解析：" + fileDir);
            InputSource is2 = new InputSource(context.getAssets().open(fileDir));
            ProcessHandler handler = new ProcessHandler();
            reader.setContentHandler(handler);
            reader.parse(is2);
            return handler.getTransaction();
        } catch (Exception e) {
            logger.info(fileName + "，文件解析异常，" + e.toString());
//            e.printStackTrace();
        }
        return null;
    }

}
