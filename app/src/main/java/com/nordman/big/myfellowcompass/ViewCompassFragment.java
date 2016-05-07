package com.nordman.big.myfellowcompass;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.blunderer.materialdesignlibrary.fragments.AFragment;


public class ViewCompassFragment extends AFragment {

    public ViewCompassFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("LOG","...onCreateView...");
        return inflater.inflate(R.layout.fragment_view_compass, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView imageMap = (ImageView)getActivity().findViewById(R.id.compassMap);
        imageMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((NavigationDrawerActivity) getActivity()).performNavigationDrawerItemClick(0);
            }
        });

    }
}
