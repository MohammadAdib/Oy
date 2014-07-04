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

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

public class LoginActivity extends OyUtils.TransitionActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Username and password fields
        final EditText username = (EditText) findViewById(R.id.username);
        final EditText password = (EditText) findViewById(R.id.password);
        OyUtils.uppercaseEditText(username);

        //Login setup
        final Button loginButton = (Button) findViewById(R.id.loginbutton);
        final Button errorButton = (Button) findViewById(R.id.errorButton);
        final ProgressBar pb = (ProgressBar) findViewById(R.id.progressBar);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = username.getText().toString().toUpperCase();
                final String pass = password.getText().toString();
                if (user.length() > 0 && pass.length() > 0) {
                    //Show loading progress bar
                    loginButton.setText("");
                    pb.setVisibility(View.VISIBLE);
                    //Login on Parse
                    ParseUser.logInInBackground(user, pass, new LogInCallback() {
                        @Override
                        public void done(ParseUser user, ParseException e) {
                            if (e == null) {
                                // Success!
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            } else {
                                loginButton.setText(getResources().getString(R.string.login));
                                pb.setVisibility(View.INVISIBLE);
                                errorButton.setVisibility(View.VISIBLE);
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        errorButton.setVisibility(View.INVISIBLE);
                                    }
                                }, 1000);
                            }
                        }
                    });
                } else {
                    OyUtils.showEmptyWarning(LoginActivity.this);
                }
            }
        });
    }
}
