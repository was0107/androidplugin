package com.b5m.loader.model;

import android.os.Parcel;
import android.os.Parcelable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

/**
 * Created by boguang on 14/12/23.
 */
public class FileSpec implements Parcelable {

    /*
    * Download when required
    * */
    public static final int DOWN_NONE = 0;
    /*
    * Try to download in background if wifi or faster network is available
    */
    public static final int DOWN_WIFI = 1;
    /*
    * Try to download in background if 3G or faster network is available
    * */
    public static final int DOWN_3G = 2;
    /*
    * Try to download in background
    * */
    public static final int DOWN_ALWAYS = 5;

    private String id;
    private String url;
    private String md5;
    private int down;
    private int length;
    private String[] deps;
    private Parcel out;
    private int flags;


    public FileSpec(String id, String url, String md5, int down,int length, String[] deps ){
        this.id = id;
        this.url = url;
        this.md5 = md5;
        this.down = down;
        this.length = length;
        this.deps = deps;
    }

    public FileSpec(JSONObject jsonObject) throws JSONException {
        this.id = jsonObject.getString("id");
        this.url = jsonObject.getString("url");
        this.md5 = jsonObject.getString("md5");
        this.down = jsonObject.optInt("down", 0);
        this.length = jsonObject.optInt("length", 0);

        JSONArray jsonArray = jsonObject.getJSONArray("deps");
        if (null != jsonArray) {
            deps = new String[jsonArray.length()];
            for (int i = 0 ; i < deps.length; i++) {
                deps[i] = jsonArray.getString(i);
            }
        }
    }

    protected FileSpec(Parcel parcel) {
        id = parcel.readString();
        url = parcel.readString();
        md5 = parcel.readString();
        down = parcel.readInt();
        length = parcel.readInt();
        deps = parcel.createStringArray();
    }

    public String getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public String getMd5() {
        return md5;
    }

    public int getDown() {
        return down;
    }

    public int getLength() {
        return length;
    }

    public String[] getDeps() {
        return deps;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(id);
        out.writeString(url);
        out.writeString(md5);
        out.writeInt(down);
        out.writeInt(length);
        out.writeStringArray(deps);
    }


    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(id);
        if (null != deps && deps.length > 0) {
            stringBuilder.append(":").append(deps[0]);
            for (int i = 1 ; i < deps.length ; i++ ) {
                stringBuilder.append(",").append(deps[i]);
            }
        }

        return stringBuilder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FileSpec)) return false;

        FileSpec fileSpec = (FileSpec) o;

        if (!Arrays.equals(deps, fileSpec.deps)) return false;
        if (id != null ? !id.equals(fileSpec.id) : fileSpec.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<FileSpec> CREATOR = new Parcelable.Creator<FileSpec>(){
        @Override
        public FileSpec createFromParcel(Parcel source) {
            return new FileSpec(source);
        }

        @Override
        public FileSpec[] newArray(int size) {
            return new FileSpec[size];
        }
    };

}

