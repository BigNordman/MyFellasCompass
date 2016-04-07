package com.nordman.big.myfellowcompass;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class SelectPersonActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_person);
        ActionBar bar=getSupportActionBar();
        if (bar!=null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        String jsondata = intent.getStringExtra("jsondata");

        JSONArray friendslist;
        ArrayList<String> friends = new ArrayList<>();

        try {
            friendslist = new JSONArray(jsondata);
            for (int l=0; l < friendslist.length(); l++) {
                friends.add(friendslist.getJSONObject(l).getString("name"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ArrayAdapter adapter = new ArrayAdapter<>(this, R.layout.activity_select_person, friends); // simple textview for list item
        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);

    }
}
