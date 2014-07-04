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

import android.app.Fragment;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.parse.FindCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SendCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends ListActivity {

    protected ParseRelation<ParseUser> mFriendsRelation;
    protected ParseUser mCurrentUser;
    protected List<ParseUser> mFriends;
    protected FriendsAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ParseAnalytics.trackAppOpened(getIntent());
        //Check if user is logged in
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser == null) {
            //Not logged in, take user to intro
            Intent intent = new Intent(MainActivity.this, IntroActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {
            mFriends = new ArrayList<ParseUser>();
            //Menu button
            findViewById(R.id.menu).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    OyUtils.launchActivity(MainActivity.this, AccountActivity.class, R.anim.slide_in_bottom, R.anim.slide_out_top);
                }
            });
            //Invite button
            findViewById(R.id.invite).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    OyUtils.sendInvite(MainActivity.this);
                }
            });
            //Setup the [+] button
            setupAddFriendsView();
            // Store username in installation
            ParseInstallation installation = ParseInstallation.getCurrentInstallation();
            installation.put(ParseConstants.KEY_USERNAME, ParseUser.getCurrentUser().getUsername().toString().toUpperCase());
            installation.saveInBackground();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ParseUser.getCurrentUser() != null)
            loadFriendsList();
    }

    /**
     * Load the list of added friends into mFriends
     */
    public void loadFriendsList() {
        mCurrentUser = ParseUser.getCurrentUser();
        mFriendsRelation = mCurrentUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION);
        ParseQuery<ParseUser> query = mFriendsRelation.getQuery();
        query.addAscendingOrder(ParseConstants.KEY_USERNAME);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> friends, ParseException e) {
                if (e == null) {
                    mFriends = friends;
                    mAdapter = new FriendsAdapter();
                    getListView().setAdapter(mAdapter);
                    OyUtils.setListViewHeightBasedOnChildren(getListView());
                    Log.d("friends", mFriends.size() + "");
                } else {
                    Toast.makeText(MainActivity.this, getString(R.string.error_friends), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Add a friend to the shared preferences
     *
     * @param username the friend's username
     */
    public void addFriend(final String username) {
        //Check if user already added or if its yourself
        for (ParseUser user : mFriends) {
            if (user.getUsername().toLowerCase().equals(username.toLowerCase()))
                return;
        }
        //Check if the user added themselves
        if (!username.toLowerCase().equals(ParseUser.getCurrentUser().getUsername().toLowerCase())) {
            //Add new user if they exist
            ParseQuery<ParseUser> query = ParseUser.getQuery();
            query.whereEqualTo(ParseConstants.KEY_USERNAME, username);
            query.findInBackground(new FindCallback<ParseUser>() {
                public void done(List<ParseUser> people, ParseException e) {
                    if (e == null) {
                        if (people.isEmpty()) {
                            Toast.makeText(MainActivity.this, "User " + username + " does not exist", Toast.LENGTH_SHORT).show();
                        } else {
                            //Add this user as a friend
                            final ParseUser friend = people.get(0);
                            mFriendsRelation.add(friend);
                            mCurrentUser.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e != null) {
                                        Toast.makeText(MainActivity.this, getString(R.string.error_adding), Toast.LENGTH_SHORT).show();
                                    } else {
                                        mAdapter.addItem(friend);
                                    }
                                }
                            });
                        }
                    } else {
                        Toast.makeText(MainActivity.this, getString(R.string.error_adding), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            Toast.makeText(MainActivity.this, "Oy, don't you have any friends?", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Set up the button for adding more friends [+]
     */
    public void setupAddFriendsView() {
        final ViewFlipper flipper = (ViewFlipper) findViewById(R.id.viewFlipper);
        final EditText username = (EditText) flipper.findViewById(R.id.username);
        username.setImeActionLabel("Add", KeyEvent.KEYCODE_ENTER);
        username.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                //Add user
                addFriend(username.getText().toString().toUpperCase());
                username.setText("");
                flipper.setInAnimation(MainActivity.this, R.anim.slide_in_top);
                flipper.setOutAnimation(MainActivity.this, R.anim.slide_out_bottom);
                flipper.showNext();
                return false;
            }
        });
        OyUtils.uppercaseEditText(username);
        flipper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Show edittext and keyboard
                flipper.setInAnimation(MainActivity.this, R.anim.slide_in_bottom);
                flipper.setOutAnimation(MainActivity.this, R.anim.slide_out_top);
                flipper.showNext();
                //Show keyboard after animation ends
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        username.requestFocus();
                        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                        imm.showSoftInput(username, InputMethodManager.SHOW_IMPLICIT);
                    }
                }, AnimationUtils.loadAnimation(MainActivity.this, R.anim.slide_in_bottom).getDuration());
            }
        });
        flipper.setBackgroundColor(getResources().getColor(OyUtils.getColor(new Random().nextInt(5))));
    }

    /**
     * Custom ListView adapter for displaying added friends
     */
    private class FriendsAdapter extends BaseAdapter {

        private LayoutInflater mInflater;

        public FriendsAdapter() {
            mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void addItem(final ParseUser item) {
            mFriends.add(item);
            notifyDataSetChanged();
            OyUtils.setListViewHeightBasedOnChildren(getListView());
        }

        @Override
        public int getCount() {
            findViewById(R.id.invite).setVisibility(mFriends.size() >= 5 ? View.GONE : View.VISIBLE);
            return mFriends.size();
        }

        @Override
        public String getItem(int position) {
            return mFriends.get(position).getUsername().toUpperCase();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        //TODO: Add viewpager with options
        public View getView(final int position, View convertView, ViewGroup parent) {
            final FriendsViewHolder holder;
            final ParseUser friend = mFriends.get(position);
            if (convertView == null) {
                holder = new FriendsViewHolder();
                convertView = mInflater.inflate(R.layout.fragment_user, null);
                holder.textView = (TextView) convertView.findViewById(R.id.username);
                holder.root = convertView.findViewById(R.id.root);
                holder.imageView = (ImageView) convertView.findViewById(R.id.icon);
                holder.pb = (ProgressBar) convertView.findViewById(R.id.progressBar);
                convertView.setTag(holder);
            } else {
                holder = (FriendsViewHolder) convertView.getTag();
            }

            holder.textView.setText(getItem(position));
            holder.root.setBackgroundColor(getResources().getColor(OyUtils.getColor(position)));
            holder.root.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.pb.setVisibility(View.VISIBLE);
                    holder.root.findViewById(R.id.linearLayout).setVisibility(View.INVISIBLE);
                    //Send an Oy!
                    OyUtils.sendOy(getItem(position), new SendCallback() {
                        @Override
                        public void done(ParseException e) {
                            holder.pb.setVisibility(View.GONE);
                            holder.root.findViewById(R.id.linearLayout).setVisibility(View.VISIBLE);
                        }
                    });
                }
            });
            holder.root.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    //Remove friend
                    mFriendsRelation.remove(mFriends.get(position));
                    mCurrentUser.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e != null) {
                                Toast.makeText(MainActivity.this, getString(R.string.error_deleting), Toast.LENGTH_SHORT).show();
                            } else {
                                mFriends.remove(position);
                                notifyDataSetChanged();
                            }
                        }
                    });
                    return true;
                }
            });
            String motherland = (String) friend.get(ParseConstants.KEY_MOTHERLAND);
            holder.imageView.setImageResource(getResources().getIdentifier("flag_" + motherland.toLowerCase(), "drawable", getPackageName()));
            return convertView;
        }

        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
            OyUtils.setListViewHeightBasedOnChildren(getListView());
        }
    }

    public static class FriendsViewHolder {
        public TextView textView;
        public ImageView imageView;
        public View root;
        public ProgressBar pb;
    }

    /**
     * TODO: Sliding items for each user + user motherland
     */

    public static class UserInfoFragment extends Fragment {

        private View rootView;

        public void setUsername(String username) {
            ((TextView) rootView.findViewById(R.id.username)).setText(username);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.fragment_user, container, false);
            return rootView;
        }
    }

    public static class UserOptionsFragment extends Fragment {

        private ParseUser user;

        public void setUser(ParseUser user) {
            this.user = user;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_user_options, container, false);
            rootView.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            rootView.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            rootView.findViewById(R.id.block).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            return rootView;
        }
    }
}
