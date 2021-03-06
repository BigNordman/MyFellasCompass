package com.nordman.big.myfellowcompass;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.blunderer.materialdesignlibrary.handlers.ActionBarDefaultHandler;
import com.blunderer.materialdesignlibrary.handlers.ActionBarHandler;
import com.facebook.login.widget.ProfilePictureView;
import com.facebook.share.model.AppInviteContent;
import com.facebook.share.widget.AppInviteDialog;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class SelectPerson2Activity extends com.blunderer.materialdesignlibrary.activities.ListViewActivity {
    ArrayList<FbRowItem> friends;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        String jsondata = intent.getStringExtra("jsondata");

        JSONArray friendslist;
        friends = new ArrayList<>();

        try {
            friendslist = new JSONArray(jsondata);
            for (int l=0; l < friendslist.length(); l++) {
                friends.add(new FbRowItem(friendslist.getJSONObject(l).getString("id"), friendslist.getJSONObject(l).getString("name")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        super.onCreate(savedInstanceState);
    }

    @Override
    protected boolean enableActionBarShadow() {
        return false;
    }

    @Override
    protected ActionBarHandler getActionBarHandler() {
        return new ActionBarDefaultHandler(this);
    }

    @Override
    public ListAdapter getListAdapter() {
        return new MyAdapter(this, R.layout.listview_row, friends);
    }

    @Override
    public boolean useCustomContentView() {
        return true;
    }

    @Override
    public int getCustomContentView() {
        return R.layout.activity_select_person2;
    }

    @Override
    public boolean pullToRefreshEnabled() {
        return false;
    }

    @Override
    public int[] getPullToRefreshColorResources() {
        return new int[0];
    }

    @Override
    public void onRefresh() {

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        Log.d("LOG","...selected item id = " + ((FbRowItem)adapterView.getItemAtPosition(position)).id);

        Intent intent = new Intent();
        intent.putExtra("id", ((FbRowItem)adapterView.getItemAtPosition(position)).id);
        intent.putExtra("name", ((FbRowItem)adapterView.getItemAtPosition(position)).name);
        setResult(RESULT_OK, intent);

        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor ed = prefs.edit();
        ed.putString("personId", ((FbRowItem)adapterView.getItemAtPosition(position)).id);
        ed.putString("personName", ((FbRowItem)adapterView.getItemAtPosition(position)).name);
        ed.apply();

        finish();

    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
        return false;
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

    public void onInviteClick(View view) {
        /*
        Intent intent = new Intent(this, InviteFriendActivity.class);
        startActivity(intent);
        */
        String appLinkUrl, previewImageUrl;

        appLinkUrl = getString(R.string.fb_app_link);
        //previewImageUrl = "https://www.mydomain.com/my_invite_image.jpg";

        if (AppInviteDialog.canShow()) {
            AppInviteContent content = new AppInviteContent.Builder()
                    .setApplinkUrl(appLinkUrl)
                    //.setPreviewImageUrl(previewImageUrl)
                    .build();
            AppInviteDialog.show(this, content);
        } else {
            Log.d("LOG","can't show invite dialog");
        }

    }
}
