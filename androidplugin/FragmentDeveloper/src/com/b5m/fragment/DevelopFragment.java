package com.b5m.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.b5m.fragmentdeveloper.R;

/**
 * Created by boguang on 14/12/16.
 */
public class DevelopFragment  extends Fragment{

    Button button;
    int counter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.developer_fragment,container,false);

        button = (Button)view.findViewById(R.id.btn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button.setText(String.valueOf(++counter));
            }
        });

        if (null != savedInstanceState)
        {
            counter = savedInstanceState.getInt("counter");
        }

        button.setText(String.valueOf(counter));

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("counter",counter);
    }
}
