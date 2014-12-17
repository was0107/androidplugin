package com.example.fragmentloader;

import android.app.ListFragment;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by boguang on 14/12/16.
 */
public class ListAPKFragment extends ListFragment {

    private List<Map<String,String>> list = new ArrayList<Map<String, String>>();

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AssetManager assetManager = getActivity().getAssets();

        try {
            for (String apk : assetManager.list("plugintest"))
            {
                addItem(apk, "plugintest/" + apk);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        SimpleAdapter adapter = new SimpleAdapter(getActivity(), list,
                android.R.layout.simple_list_item_1,
                new String[]{"title"},
                new int[]{android.R.id.title});
        setListAdapter(adapter);
    }



    private void addItem(String title, String path){

        Map<String,String> map = new HashMap<String, String>();
        map.put("title",title);
        map.put("path",path);
        list.add(map);
    }



    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        Map<String, String> map = list.get(position);
        Intent intent = new Intent("com.b5m.fragment.intent.action.LOAD_FRAGMENT");
        intent.putExtra("path",map.get("path"));
        intent.putExtra("class", "com.b5m.fragment.DevelopFragment");
        startActivity(intent);
    }
}
