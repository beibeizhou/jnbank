package com.centerm.jnbank.xml.handler;


import com.centerm.jnbank.xml.XmlAttrs;
import com.google.gson.JsonObject;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * 本地自定义报文解析器
 * author:wanliang527</br>
 * date:2016/10/18</br>
 */

public class LocalInterfaceHandler extends BaseHandler {

    private Map<String, String> valueMap;
    private Map<String, Map<String, String>> tagMapAttrs;
    private String rootTag;
    private JsonObject rootJson;
    private int tagCounts;
    private Stack<String> tagStack = new Stack<>();
    private Stack<JsonObject> jsonStack = new Stack<>();

    public LocalInterfaceHandler() {
        tagMapAttrs = new HashMap<>();
    }

    public LocalInterfaceHandler(Map<String, String> valueMap) {
        this();
        this.valueMap = valueMap;
    }

    @Override
    public void startDocument() throws SAXException {
        rootJson = new JsonObject();
        jsonStack.push(rootJson);
        super.startDocument();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
//        logger.debug("startElement == " + localName);
        super.startElement(uri, localName, qName, attributes);
        tagStack.push(localName);
        tagMapAttrs.put(localName, attr2Map(attributes));
        if (++tagCounts == 1) {
            rootTag = tagStack.pop();
        }
        if (jsonStack.size() < tagStack.size()) {
            jsonStack.push(new JsonObject());
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
//        logger.debug("endElement == " + localName);
        super.endElement(uri, localName, qName);
        //针对根节点做特殊处理
        if (localName.equals(rootTag)) {
            return;
        }
        int tagSize = tagStack.size();
        int jsonSize = jsonStack.size();
        String topTag = tagStack.pop();
        String value = getValueIfExists(topTag);
        if (tagSize == jsonSize) {
            if (value != null) {
                jsonStack.peek().addProperty(topTag, value);
            }
        } else if (tagSize < jsonSize) {
            JsonObject topJson = jsonStack.pop();
            if (jsonSize == 1) {
                if (value != null) {
                    jsonStack.peek().addProperty(topTag, value);
                }
            } else if (jsonSize > 1) {
                jsonStack.peek().add(topTag, topJson);
            }


        }
/*        else if (tagSize > jsonSize) {
            if (value != null) {
                JsonObject childNode = new JsonObject();
                childNode.addProperty(topTag, "");
                jsonStack.push(childNode);
            }
        }*/
    }

    @Override
    public void endDocument() throws SAXException {
        super.endDocument();
//        logger.warn(getJson().toString());
    }

    public JsonObject getJson() {
        return rootJson;
    }

    private String getValueIfExists(String tag) {
        if (valueMap == null) {
            return "";
        }
        Map<String, String> attrs = tagMapAttrs.get(tag);
        boolean canBeNull = false;
        String value = valueMap.get(tag);
        if (attrs != null && attrs.size() > 0) {
            canBeNull = "true".equals(attrs.get(XmlAttrs.MessageAttrs.canBeNull));
        }
        if (value == null && canBeNull) {
            return null;
        }
        return value == null ? "" : value;
    }

    private Map<String, String> attr2Map(Attributes attrs) {
        Map<String, String> map = new HashMap<>();
        if (attrs != null) {
            for (int i = 0; i < attrs.getLength(); i++) {
                map.put(attrs.getLocalName(i), attrs.getValue(i));
            }
        }
        return map;
    }

}
