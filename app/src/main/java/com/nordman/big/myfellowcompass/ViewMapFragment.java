package com.nordman.big.myfellowcompass;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blunderer.materialdesignlibrary.fragments.AFragment;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;


/**
 * A simple {@link Fragment} subclass.
 *
 */
public class ViewMapFragment extends AFragment implements OnMapReadyCallback {
    private GoogleMap mMap;
    private PersonOnMap me = null;
    private PersonOnMap him = null;
    private PersonOnMap toDraw = null;


    public ViewMapFragment() {
        Log.d("LOG","...ViewMapFragment constructor...");
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("LOG","...onCreateView...");
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_view_map, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("LOG","...onViewCreated...");
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            /*
            mMap = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map)).getMap();
            // Check if we were successful in obtaining the map.

            if (mMap != null) {
                setUpMap();
            }
            */
            Log.d("LOG","...map should be loaded...");
            SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        } else {
            Log.d("LOG","...map is already loaded...");
        }
    }
/*
    private void setUpMap() {
        mMap.setMyLocationEnabled(true);//выводим индикатор своего местоположения
    }
*/

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        Log.d("LOG","...onMapReady...");
        //mMap.setMyLocationEnabled(true);//выводим индикатор своего местоположения
    }
}
