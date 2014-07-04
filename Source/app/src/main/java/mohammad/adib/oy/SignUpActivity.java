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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import static mohammad.adib.oy.OyUtils.MotherlandArrayAdapter;
import static mohammad.adib.oy.OyUtils.TransitionActivity;

public class SignUpActivity extends TransitionActivity {

    private Button motherlandButton;
    private EditText username, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        //Username and password fields
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        OyUtils.uppercaseEditText(username);

        //Select motherland
        motherlandButton = (Button) findViewById(R.id.motherland);
        motherlandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeMotherlandDialog();
            }
        });

        //Sign up setup
        final Button signUpButton = (Button) findViewById(R.id.signupbutton);
        final Button takenButton = (Button) findViewById(R.id.takenButton);
        final ProgressBar pb = (ProgressBar) findViewById(R.id.progressBar);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = username.getText().toString().toUpperCase();
                String pass = password.getText().toString();
                if (user.length() > 0 && pass.length() > 0 && !motherlandButton.getText().equals(getResources().getString(R.string.motherland))) {
                    final ParseUser newUser = new ParseUser();
                    newUser.setUsername(user);
                    newUser.setPassword(pass);
                    signUpButton.setText("");
                    pb.setVisibility(View.VISIBLE);
                    newUser.signUpInBackground(new SignUpCallback() {
                        @Override
                        public void done(ParseException e) {
                            newUser.put(ParseConstants.KEY_MOTHERLAND, motherlandButton.getText().toString().toUpperCase());
                            newUser.saveInBackground();
                            if (e == null) {
                                // Success!
                                Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            } else {
                                signUpButton.setText(getResources().getString(R.string.signup));
                                pb.setVisibility(View.INVISIBLE);
                                if (e.getMessage().contains("taken")) {
                                    //Username taken, briefly flash error
                                    takenButton.setVisibility(View.VISIBLE);
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            takenButton.setVisibility(View.INVISIBLE);
                                            username.setText(username.getText() + "CURRY");
                                        }
                                    }, 2000);
                                } else
                                    Toast.makeText(SignUpActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                } else {
                    OyUtils.showEmptyWarning(SignUpActivity.this);
                }
            }
        });
    }

    /**
     * Makes a dialog to select the motherland
     */
    public void makeMotherlandDialog() {
        final String[] options = getResources().getStringArray(R.array.motherlands);
        ListView listView = new ListView(this);
        listView.setAdapter(new MotherlandArrayAdapter(this, options));
        listView.setBackgroundColor(getResources().getColor(R.color.purple));
        final Dialog dialog = new AlertDialog.Builder(this).setView(listView).create();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int index, long id) {
                motherlandButton.setText(options[index]);
                dialog.dismiss();
            }
        });

        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        dialog.show();
    }

}
