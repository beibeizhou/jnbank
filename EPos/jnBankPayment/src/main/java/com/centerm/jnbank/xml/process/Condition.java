package com.centerm.jnbank.xml.process;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 节点跳转条件实体类。
 * 由条件ID去映射流程中需要跳转的下一个节点ID
 */
public class Condition implements Parcelable {

    private String id;                 //条件ID
    private String nextComponentNodeId;//下一节点ID

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNextComponentNodeId() {
        return nextComponentNodeId;
    }

    public void setNextComponentNodeId(String nextComponentNodeId) {
        this.nextComponentNodeId = nextComponentNodeId;
    }

    @Override
    public String toString() {
        return "Condition{" +
                "id='" + id + '\'' +
                ", nextComponentNodeId='" + nextComponentNodeId + '\'' +
                '}';
    }

    public static final Creator<Condition> CREATOR = new Creator<Condition>() {

        @Override
        public Condition createFromParcel(Parcel source) {
            Condition mCondition = new Condition();
            mCondition.id = source.readString();
            mCondition.nextComponentNodeId = source.readString();
            return mCondition;
        }

        @Override
        public Condition[] newArray(int size) {
            return new Condition[size];
        }

    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(nextComponentNodeId);
    }
}
