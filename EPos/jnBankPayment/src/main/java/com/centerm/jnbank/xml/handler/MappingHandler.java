package com.centerm.jnbank.xml.handler;

import com.centerm.jnbank.channels.EnumChannel;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.Stack;

/**
 * 报文映射解析器。支持本地 <--> 渠道之间报文互转。
 * author:wanliang527</br>
 * date:2016/10/22</br>
 */

public class MappingHandler extends BaseHandler {

    private StringBuilder sBuilder;
    private EnumChannel channel;
    private MessageType msgType;
    private JsonObject inJson;//输入Json报文
    private JsonObject outJson;//输出Json报文

    private String rootTag;
    private JsonObject rootJson;
    private int tagCounts;
    private Stack<String> tagStack = new Stack<>();
    private Stack<JsonObject> jsonStack = new Stack<>();

    private boolean stopFlag;

    /**
     * 构造函数
     *
     * @param channel 渠道类型
     * @param msgType 报文类型
     * @param in      原报文数据。如果是请求报文，则为本地报文==》渠道报文的映射；反之则为渠道报文==》本地报文的映射。
     */
    public MappingHandler(EnumChannel channel, MessageType msgType, JsonObject in) {
        sBuilder = new StringBuilder();
        this.channel = channel;
        this.msgType = msgType;
        this.inJson = in;
    }

    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
        switch (msgType) {
            case REQUEST:
                jsonStack.push(new JsonObject());
                break;
            case RESPONSE:
                break;
        }
    }

    @Override
    public void endDocument() throws SAXException {
        super.endDocument();
        outJson = jsonStack.pop();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        sBuilder.setLength(0);
        switch (msgType) {
            case REQUEST:
                if (stopFlag) {
                    return;
                }
                tagStack.push(localName);
                if (++tagCounts == 1) {
                    rootTag = localName;
                }
                break;
            case RESPONSE:
                break;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);
        if (stopFlag) {
            return;
        }
        switch (msgType) {
            case REQUEST:
                onEndElementOfRequest(localName);
                if ("req".equals(localName)) {
                    stopFlag = true;
                }
                break;
            case RESPONSE:
                break;
        }
    }

    private void onEndElementOfRequest(String localName) {
        String topTag = tagStack.pop();
        int tagSize = tagStack.size();
        int jsonSize = jsonStack.size();
        //针对根节点做特殊处理
        if (localName.equals(rootTag)) {
            if (tagCounts == 1) {
                rootJson = jsonStack.pop();
                rootJson.addProperty(rootTag, "");
            } else {
                rootJson = new JsonObject();
                rootJson.add(rootTag, jsonStack.pop());
            }
            return;
        }
        JsonElement value = getValueIfExists(topTag);
        if (tagSize == jsonSize) {
            if (value != null) {
                jsonStack.peek().add(localName,value);
            }
        } else if (tagSize < jsonSize) {
            JsonObject leafNode = jsonStack.pop();
            jsonStack.peek().add(topTag, leafNode);
        } else if (tagSize > jsonSize) {
            if (value != null) {
                JsonObject childNode = new JsonObject();
                childNode.addProperty(topTag, "");
                jsonStack.push(childNode);
            }
        }
    }

    private JsonElement getValueIfExists(String tag) {
        if (inJson == null) {
            return null;
        }
        String mapValue = sBuilder.toString();
        String[] keys = mapValue.split("#");
        if (keys.length == 1) {
            JsonElement element = inJson.get(keys[0]);
            if (element != null) {
                return element;
            }
        } else {
            JsonObject temp = null;
            for (int i = 0; i < keys.length; i++) {
                if (temp != null && i == keys.length - 1) {
                    JsonElement element = temp.get(keys[i]);
                    if (element != null) {
                        return element;
                    }
                }
                temp = inJson.getAsJsonObject(keys[i]);
            }
        }
        return null;
    }


    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);
        sBuilder.append(ch, start, length);
    }

    public JsonObject getOutput() {
        switch (msgType) {
            case REQUEST:
                return outJson.getAsJsonObject("req");
            case RESPONSE:
                break;
        }
        return outJson;
    }

    /**
     * 报文类型枚举。请求报文或响应报文。
     */
    public enum MessageType {
        REQUEST, RESPONSE;
    }

}
