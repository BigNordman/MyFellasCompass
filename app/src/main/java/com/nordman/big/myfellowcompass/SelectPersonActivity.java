package com.nordman.big.myfellowcompass;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
        ArrayList<String> friendsStr = new ArrayList<>();
        ArrayList<fbRow> friends = new ArrayList<>();

        try {
            friendslist = new JSONArray(jsondata);
            for (int l=0; l < friendslist.length(); l++) {
                friendsStr.add(friendslist.getJSONObject(l).getString("name"));
                friends.add(new fbRow(friendslist.getJSONObject(l).getString("id"), friendslist.getJSONObject(l).getString("name")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ArrayAdapter adapter = new fbArrayAdapter(this, R.layout.activity_listview, R.id.label, friends, friendsStr); // simple textview for list item
        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);

    }

    public class fbArrayAdapter extends ArrayAdapter<String> {
        private final Context context;
        private final List<fbRow> fbRows;


        public fbArrayAdapter(Context context, int resource, int textViewResourceId, List<fbRow> objects, List<String> strings) {
            super(context, resource, textViewResourceId, strings);
            this.context = context;
            this.fbRows = objects;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.activity_listview, parent, false);
            TextView textView = (TextView) rowView.findViewById(R.id.label);
            textView.setText(fbRows.get(position).name);
            ProfilePictureView profilePictureView = (ProfilePictureView) rowView.findViewById(R.id.profilePicture);
            profilePictureView.setProfileId(fbRows.get(position).id);

            return rowView;
        }
    }

    public class fbRow {
        public String id;
        public String name;

        public fbRow(String id, String name) {
            this.id = id;
            this.name = name;
        }
    }
}
