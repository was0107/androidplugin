package com.b5m.loader.model;

import android.os.Parcel;
import android.os.Parcelable;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by boguang on 14/12/23.
 */
public class FragmentSpec implements Parcelable{

    private String host;
    private String code;
    private String name;

    public FragmentSpec(String host,String code,String name) {
        this.host = host;
        this.name = name;
        this.code = code;
    }

    public FragmentSpec(JSONObject jsonObject) throws JSONException{
        this.host = jsonObject.getString("host");
        this.name = jsonObject.getString("name");
        this.code = jsonObject.getString("code");
    }

    protected FragmentSpec(Parcel in) {
        host = in.readString();
        code = in.readString();
        name = in.readString();
    }


    public String getHost() {
        return host;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(host);
        dest.writeString(code);
        dest.writeString(name);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("xxx://").append(host).append(":");
        stringBuilder.append(name).append("(");
        if (null == code)
            stringBuilder.append(".");
        else
            stringBuilder.append(code);
        stringBuilder.append(")");
        return stringBuilder.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<FragmentSpec> CREATOR =
            new Parcelable.Creator<FragmentSpec>() {

                @Override
                public FragmentSpec createFromParcel(Parcel source) {
                    return new FragmentSpec(source);
                }

                @Override
                public FragmentSpec[] newArray(int size) {
                    return new FragmentSpec[size];
                }
            };

}
