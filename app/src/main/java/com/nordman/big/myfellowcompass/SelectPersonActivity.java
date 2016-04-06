package com.nordman.big.myfellowcompass;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SelectPersonActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_person);
        ActionBar bar=getSupportActionBar();
        if (bar!=null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
