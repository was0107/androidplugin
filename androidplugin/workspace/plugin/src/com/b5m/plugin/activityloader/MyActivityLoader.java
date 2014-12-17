package com.b5m.plugin.activityloader;

import android.app.ListActivity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import dalvik.system.DexClassLoader;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by boguang on 14-12-8.
 */
public class MyActivityLoader extends ListActivity {
    protected List<Map<String, String>> data = new ArrayList<Map<String, String>>();

    private void addItem(String title, String path) {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("title", title);
        map.put("path", path);
        data.add(map);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addItem("[Launch SampleActivity]", null);
        addItem("[Launch Default]", null);

        AssetManager assets = getAssets();
        try {
            for (String s : assets.list("plugintest")) {
                addItem(s, "plugintest/" + s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        SimpleAdapter adapter = new SimpleAdapter(this, data,
                android.R.layout.simple_list_item_1, new String[]{"title"},
                new int[]{android.R.id.text1});
        setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

        if (0 == position) {
            Intent intent = new Intent("com.b5m.intent.action.MYACTIVITY");
            startActivity(intent);
            return;
        }

        if (1 == position) {
            MyApplication.CUSTOM_LOADER = null;
            return;
        }

        Map<String, String> map = data.get(position);
        String title = map.get("title");
        String path = map.get("path");

        try {
            File dex = getDir("dex", MODE_PRIVATE);
            dex.mkdir();

            File inFile = new File(dex, title);

            InputStream inputStream = getAssets().open(path);
            OutputStream outputStream = new FileOutputStream(inFile);

            byte buffer[] = new byte[0xFF];
            int length;
            while ((length = inputStream.read(buffer)) > 0) outputStream.write(buffer, 0, length);
            inputStream.close();
            outputStream.close();

            File fo = getDir("outdex", MODE_PRIVATE);
            fo.mkdir();

            DexClassLoader dexClassLoader = new DexClassLoader(inFile.getAbsolutePath(),
                    fo.getAbsolutePath(), null, MyApplication.ORIGINAL_LOADER.getParent());
            MyApplication.CUSTOM_LOADER = dexClassLoader;
            Toast.makeText(this, title + " loaded, try launch again",
                    Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            Toast.makeText(this, "Unable to load " + title, Toast.LENGTH_SHORT)
                    .show();
            e.printStackTrace();
            MyApplication.CUSTOM_LOADER = null;
        }

    }

}

