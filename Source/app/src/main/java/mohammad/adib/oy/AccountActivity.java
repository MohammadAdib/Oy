/*
 * Copyright (c) 2014 Mohammad Adib
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package mohammad.adib.oy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.PushService;

public class AccountActivity extends OyUtils.TransitionActivity {

    private ParseUser mCurrentUser;
    public static final String ACTION_UPDATE_OYS = "updateOys";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        setTransitionAnimations(R.anim.slide_in_top, R.anim.slide_out_bottom);
        mCurrentUser = ParseUser.getCurrentUser();
        //Set username and user motherland
        ((TextView) findViewById(R.id.username)).setText(mCurrentUser.getUsername().toUpperCase());
        ((ImageView) findViewById(R.id.icon)).setImageResource(getResources().getIdentifier("flag_" + mCurrentUser.get(ParseConstants.KEY_MOTHERLAND).toString().toLowerCase(), "drawable", getPackageName()));
        //Invite button
        findViewById(R.id.invite).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OyUtils.sendInvite(AccountActivity.this);
            }
        });
        //Sign out
        findViewById(R.id.signout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Unsubscribe from this user's notifications
                PushService.unsubscribe(AccountActivity.this, mCurrentUser.getUsername().toUpperCase());
                //Log user out
                mCurrentUser.logOut();
                //Take them to the intro
                Intent intent = new Intent(AccountActivity.this, IntroActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });
        //Launch about activity
        findViewById(R.id.about).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OyUtils.launchActivity(AccountActivity.this, AboutActivity.class, OyUtils.ANIM_ENTER, OyUtils.ANIM_EXIT);
            }
        });
        //Done
        findViewById(R.id.done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                finish();
            }
        });
        updateOys();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(updateOysReceiver, new IntentFilter(ACTION_UPDATE_OYS));
        updateOys();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(updateOysReceiver);
    }

    /**
     * Update Oys in real time as they come in
     */
    private BroadcastReceiver updateOysReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateOys();
        }
    };

    private void updateOys() {
        try {
            if (mCurrentUser == null)
                mCurrentUser = ParseUser.getCurrentUser();
            int oys = (Integer) mCurrentUser.get(ParseConstants.KEY_OYS) + 0;
            ((TextView) findViewById(R.id.oys)).setText(getResources().getString(R.string.oys) + oys);
        } catch (Exception e) {
            ((TextView) findViewById(R.id.oys)).setText(getResources().getString(R.string.oys) + 0);
        }
    }
}
