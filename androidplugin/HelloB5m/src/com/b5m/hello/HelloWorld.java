package com.b5m.hello;

import android.app.Fragment;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.b5m.loader.MyResources;

/**
 * Created by boguang on 14/12/29.
 */
public class HelloWorld extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // MyResources manages the resources in specific package.
        // Using a Class object to obtain an instance of MyResources.

        // In this case, hello.xml is in the same package as HelloFragment class
        //TODO: use this plugin to debug

        /*
        MyResources res = null;
        View view = null;
        try {
            res = MyResources.getResource(HelloWorld.class);
            view = res.inflate(getActivity(), R.layout.hello, container, false);
        } catch (Exception e) {
            view = View.inflate(getActivity(),R.layout.hello, null);
        }
        return view;

        */

        //TODO: debug this plugin on main application
        Log.i("HelloWorld","onCreateView start");
        MyResources res = MyResources.getResource(HelloWorld.class);
        Log.i("HelloWorld","onCreateView middle" + res.toString());
        View view = res.inflate(getActivity(), R.layout.hello, container, false);
        // Using MyResources.inflate() if you want to inflate some layout in
        // this package.
        Log.i("HelloWorld","onCreateView ended" + view.toString());
        return view;
    }
}
