package com.centerm.jnbank.xml.process;

import android.os.Parcel;
import android.os.Parcelable;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class TradeProcess implements Parcelable {
    private Logger logger = Logger.getLogger(TradeProcess.class);
    private String transCode;                                                     //当前交易码
    private ComponentNode curNode;
    private LinkedList<ComponentNode> componentNodeList = new LinkedList<>();     //当前交易涉及的所有节点集
    private Map<String, String> dataMap = new HashMap<>();                        //交易流程中存储必要数据，用于组包或者持久化保存
    private Map<String, String> tempMap = new HashMap<>();                        //交易流程中存储临时数据，用于逻辑判断

    public String getTransCode() {
        return transCode;
    }

    public void setTransCode(String transCode) {
        this.transCode = transCode;
    }

    public List<ComponentNode> getComponentNodeList() {
        return componentNodeList;
    }

    public Map<String, String> getDataMap() {
        return dataMap;
    }

    public void setDataMap(Map<String, String> dataMap) {
        this.dataMap = dataMap;
    }

    public Map<String, String> getTempMap() {
        return tempMap;
    }

    public void setTempMap(Map<String, String> tempMap) {
        this.tempMap = tempMap;
    }

    public ComponentNode getFirstComponentNode() {
        return componentNodeList.get(0);
    }
    public ComponentNode getSecondComponentNode() {
        return componentNodeList.get(1);
    }
    public ComponentNode getThirdComponentNode() {
        return componentNodeList.get(2);
    }
    public ComponentNode getForthComponentNode() {
        return componentNodeList.get(3);
    }
    public ComponentNode getCurNode() {
        return curNode;
    }

    public void setCurNode(ComponentNode curNode) {
        this.curNode = curNode;
    }

    /**
     * 获取下一个节点对象。如果当前节点对象为空，则默认返回第一个节点；如果当前节点已经是最后一个，则返回null
     *
     * @param conditionId 条件ID
     * @return 节点对象
     */
    public ComponentNode getNextComponentNode(String conditionId) {
        if (curNode == null) {
            if (componentNodeList.isEmpty()) {
                return null;
            }
            ComponentNode node = componentNodeList.getFirst();
            logger.warn("当前节点为空，返回第一个节点, [" + node.getComponentName() + "]");
            return node;
        }
        Condition condition = curNode.getIdMapCondition().get(conditionId);
        String nextId = condition.getNextComponentNodeId();
        for (int i = 0; i < componentNodeList.size(); i++) {
            ComponentNode node = componentNodeList.get(i);
            if (node.getComponentId().equals(nextId)) {
                return node;
            }
        }
        return null;
    }

    public static final Creator<TradeProcess> CREATOR = new Creator<TradeProcess>() {

        @SuppressWarnings("unchecked")
        @Override
        public TradeProcess createFromParcel(Parcel source) {
            TradeProcess mTransaction = new TradeProcess();
            mTransaction.transCode = source.readString();
            mTransaction.dataMap = (Map<String, String>) source.readHashMap(getClass().getClassLoader());
            mTransaction.tempMap = (Map<String, String>) source.readHashMap(getClass().getClassLoader());
            source.readList(mTransaction.componentNodeList, getClass().getClassLoader());
            return mTransaction;
        }

        @Override
        public TradeProcess[] newArray(int size) {
            return new TradeProcess[size];
        }

    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(transCode);
        dest.writeMap(dataMap);
        dest.writeMap(tempMap);
        dest.writeList(componentNodeList);
    }

}
