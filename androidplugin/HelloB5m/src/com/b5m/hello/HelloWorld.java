package com.b5m.hello;

import android.app.Fragment;
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

        Log.e("HelloWorld","onCreateView start");

        MyResources res = MyResources.getResource(HelloWorld.class);
        Log.e("HelloWorld","onCreateView middle" + res.toString());
        View view = res.inflate(getActivity(), R.layout.hello, container, false);
        // Using MyResources.inflate() if you want to inflate some layout in
        // this package.
        Log.e("HelloWorld","onCreateView ended" + view.toString());
        return view;

    }
}
