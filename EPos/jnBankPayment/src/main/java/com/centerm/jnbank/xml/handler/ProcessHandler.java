package com.centerm.jnbank.xml.handler;

import com.centerm.jnbank.xml.process.ComponentNode;
import com.centerm.jnbank.xml.process.Condition;
import com.centerm.jnbank.xml.process.TradeProcess;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;


public class ProcessHandler extends BaseHandler {

    //xml文件中标签定义
    private final String TAG_PROCESS = "process";
    private final String TAG_COMPONENT = "component";
    private final String TAG_CONDITION = "condition";
    //属性名称定义
    private final String ATTR_TRANSCODE = "transCode";
    private final String ATTR_NAME = "name";
    private final String ATTR_ID = "id";

    private StringBuilder sb = new StringBuilder();
    private TradeProcess transaction = null;
    private ComponentNode mComponentNode = null;
    private Condition mCondition = null;

    public TradeProcess getTransaction() {
        return transaction;
    }

    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
    }

    @Override
    public void startElement(String uri, String localName, String qName,
                             Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        sb.setLength(0);
        if (TAG_PROCESS.equals(localName)) {
            transaction = new TradeProcess();
            String transCode = attributes.getValue(ATTR_TRANSCODE);
            transaction.setTransCode(transCode);
        } else if (TAG_COMPONENT.equals(localName)) {
            mComponentNode = new ComponentNode();
            String nodeId = attributes.getValue(ATTR_ID);
            String nodeName = attributes.getValue(ATTR_NAME);
            mComponentNode.setComponentId(nodeId);
            mComponentNode.setComponentName(nodeName);
        } else if (TAG_CONDITION.equals(localName)) {
            mCondition = new Condition();
            String contId = attributes.getValue(ATTR_ID);
            mCondition.setId(contId);
        }
    }

    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        super.characters(ch, start, length);
        sb.append(ch, start, length);
    }

    @Override
    public void endDocument() throws SAXException {
        super.endDocument();
    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        super.endElement(uri, localName, qName);
        String value = sb.toString();
        if (TAG_COMPONENT.equals(localName)) {
            transaction.getComponentNodeList().add(mComponentNode);
        } else if (TAG_CONDITION.equals(localName)) {
            mCondition.setNextComponentNodeId(value);
            mComponentNode.getIdMapCondition().put(mCondition.getId(), mCondition);
        }
    }

}
