package com.centerm.jnbank.xml.process;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;

/**
 * 交易节点实体类。
 * 包含节点ID、节点名称以及该节点所包含的所有可选下一个节点。
 * 其中，节点名称对应Activity的action，根据该Action去启动对应的Activity
 */
public class ComponentNode implements Parcelable {

    private String componentId;//组件ID
    private String componentName;//组件名称
    private Map<String, Condition> idMapCondition = new HashMap<>();//条件集合

    public String getComponentId() {
        return componentId;
    }

    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public Map<String, Condition> getIdMapCondition() {
        return idMapCondition;
    }

    public void setIdMapCondition(Map<String, Condition> idMapCondition) {
        this.idMapCondition = idMapCondition;
    }

    @Override
    public String toString() {
        return "ComponentNode{" +
                "componentId='" + componentId + '\'' +
                ", componentName='" + componentName + '\'' +
                ", idMapCondition=" + idMapCondition +
                '}';
    }

    public static final Creator<ComponentNode> CREATOR = new Creator<ComponentNode>() {

        @Override
        public ComponentNode createFromParcel(Parcel source) {
            ComponentNode mComponentNode = new ComponentNode();
            mComponentNode.componentId = source.readString();
            mComponentNode.componentName = source.readString();
            source.readMap(mComponentNode.idMapCondition, getClass().getClassLoader());
            return mComponentNode;
        }

        @Override
        public ComponentNode[] newArray(int size) {
            return new ComponentNode[size];
        }

    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(componentId);
        dest.writeString(componentName);
        dest.writeMap(idMapCondition);
    }
}
