package com.b5m.loader.model;

import android.os.Parcel;
import android.os.Parcelable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by boguang on 14/12/23.
 */
public class SiteSpec implements Parcelable{

    private String id ;
    private String version;
    private FileSpec fileSpec[];
    private FragmentSpec fragmentSpec[];

    public SiteSpec(String id, String version, FileSpec[] fileSpec, FragmentSpec[] fragmentSpec) {
        this.id = id;
        this.version = version;
        this.fileSpec = fileSpec;
        this.fragmentSpec = fragmentSpec;
    }

    public SiteSpec(JSONObject jsonObject) throws JSONException {
        this.id = jsonObject.getString("id");
        this.version = jsonObject.getString("version");
        JSONArray jsonArray = jsonObject.getJSONArray("files");
        fileSpec = new FileSpec[jsonArray.length()];
        for (int i = 0 ; i < fileSpec.length; i++)
        {
            JSONObject ob = jsonArray.getJSONObject(i);
            FileSpec fileSpec1 = new FileSpec(ob);
            fileSpec[i] = fileSpec1;
        }
        jsonArray = jsonObject.getJSONArray("fragments");
        fragmentSpec = new FragmentSpec[jsonArray.length()];
        for (int i = 0 ; i < fileSpec.length; i++)
        {
            JSONObject ob = jsonArray.getJSONObject(i);
            FragmentSpec fragmentSpec1 = new FragmentSpec(ob);
            fragmentSpec[i] = fragmentSpec1;
        }
    }

    protected SiteSpec(Parcel in) {
        id = in.readString();
        fileSpec = in.createTypedArray(FileSpec.CREATOR);
        fragmentSpec = in.createTypedArray(FragmentSpec.CREATOR);
    }


    public String id() {
        return id;
    }

    public String version() {
        return version;
    }

    public FileSpec[] files() {
        return fileSpec;
    }

    public FragmentSpec[] fragments() {
        return fragmentSpec;
    }

    public FragmentSpec getFragment(String host) {
        for (FragmentSpec f : fragmentSpec) {
            if (host.equalsIgnoreCase(f.getHost())) {
                return f;
            }
        }
        return null;
    }

    public FileSpec getFile(String id) {
        for (FileSpec f : fileSpec) {
            if (id.equals(f.getId())) {
                return f;
            }
        }
        return null;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeTypedArray(fileSpec,0);
        dest.writeTypedArray(fragmentSpec,0);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(id);
        if (!id.contains(version)) {
            sb.append(" v").append(version);
        }
        sb.append(" (").append(fileSpec.length).append(" files, ")
                .append(fragmentSpec.length).append(" fragments)");
        return sb.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }
    public static final Parcelable.Creator<SiteSpec> CREATOR = new Parcelable.Creator<SiteSpec>() {
        public SiteSpec createFromParcel(Parcel in) {
            return new SiteSpec(in);
        }

        public SiteSpec[] newArray(int size) {
            return new SiteSpec[size];
        }
    };

}
