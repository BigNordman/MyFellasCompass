package com.nordman.big.myfellowcompass;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.blunderer.materialdesignlibrary.handlers.ActionBarHandler;
import com.facebook.share.model.AppInviteContent;
import com.facebook.share.widget.AppInviteDialog;

public class InviteFriendActivity extends com.blunderer.materialdesignlibrary.activities.AActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("LOG","invite friend dialog shoud appear");

        String appLinkUrl, previewImageUrl;

        appLinkUrl = "https://play.google.com/store/apps/details?id=com.nordman.big.smsparking2";
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

        finish();
        //setContentView(R.layout.activity_invite_friend);
    }

    @Override
    protected boolean enableActionBarShadow() {
        return false;
    }

    @Override
    protected ActionBarHandler getActionBarHandler() {
        return null;
    }
}
