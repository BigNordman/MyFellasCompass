package com.nordman.big.myfellowcompass;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.login.widget.ProfilePictureView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

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
        ArrayList<FbRowItem> friends = new ArrayList<>();

        try {
            friendslist = new JSONArray(jsondata);
            for (int l=0; l < friendslist.length(); l++) {
                friends.add(new FbRowItem(friendslist.getJSONObject(l).getString("id"), friendslist.getJSONObject(l).getString("name")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        MyAdapter adapter = new MyAdapter(this, R.layout.activity_listview, friends);

        ListView listView = (ListView) findViewById(R.id.list);
        listView.setAdapter(adapter);
        //listView.setSelector(R.drawable.listview_selector);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                intent.putExtra("id", ((FbRowItem)parent.getItemAtPosition(position)).id);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    public void finish(View view) {
        finish();
    }

    public class FbRowItem {
        public String id;
        public String name;

        public FbRowItem(String id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public class MyAdapter extends ArrayAdapter<FbRowItem> {
        private Context context;
        private int resource;
        private List<FbRowItem> objects;

        public MyAdapter(Context context, int resource, List<FbRowItem> objects) {
            super(context, resource, objects);
            this.context=context;
            this.resource=resource;
            this.objects=objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater=((Activity) context).getLayoutInflater();
            View row=inflater.inflate(resource, parent, false);

            TextView textView = (TextView) row.findViewById(R.id.label);
            textView.setText(objects.get(position).name);

            ProfilePictureView profilePictureView = (ProfilePictureView) row.findViewById(R.id.profilePicture);
            profilePictureView.setProfileId(objects.get(position).id);

            return row;
        }
    }
}
