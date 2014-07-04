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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParsePush;
import com.parse.ParseUser;
import com.parse.SendCallback;

import org.json.JSONObject;

public class OyUtils {

    public static final int ANIM_ENTER = R.anim.slide_in_right;
    public static final int ANIM_EXIT = R.anim.slide_out_left;

    public static void sendOy(String username, SendCallback callback) {
        try {
            String message = "From " + ParseUser.getCurrentUser().getUsername().toLowerCase();
            JSONObject data = new JSONObject("{\"action\": \"mohammad.adib.oy.UPDATE_STATUS\",\"alert\": \"" + message + "\"}");
            // Send push notification to query
            ParsePush push = new ParsePush();
            push.setChannel(username);
            push.setData(data);
            push.sendInBackground(callback);
        } catch (Exception e) {

        }
    }

    public static void launchActivity(Activity activity, Class activityClass, int enterAnim, int exitAnim) {
        activity.startActivity(new Intent(activity, activityClass));
        activity.overridePendingTransition(enterAnim, exitAnim);
    }

    public static void showEmptyWarning(Context context) {
        Toast.makeText(context, context.getResources().getString(R.string.empty), Toast.LENGTH_SHORT).show();
    }

    public static class TransitionActivity extends Activity {

        public int anim_in = R.anim.slide_in_left, anim_out = R.anim.slide_out_right;

        public void setTransitionAnimations(int anim_in, int anim_out) {
            this.anim_in = anim_in;
            this.anim_out = anim_out;
        }

        @Override
        public void onBackPressed() {
            super.onBackPressed();
            overridePendingTransition(anim_in, anim_out);
        }
    }

    public static void uppercaseEditText(final EditText editText) {
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        editText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void afterTextChanged(Editable arg0) {
                String s = arg0.toString();
                if (!s.equals(s.toUpperCase().trim())) {
                    s = s.toUpperCase().trim();
                    editText.setText(s);
                    editText.setSelection(s.length());
                }
            }
        });
    }

    public static int getColor(int index) {
        int[] colors = new int[]{
                R.color.a,
                R.color.b,
                R.color.c,
                R.color.d,
                R.color.e,
                R.color.f,
        };
        return colors[index % colors.length];
    }

    public static void sendInvite(Context context) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Oy! Wanna fraandship?\nSend me an Oy: " + ParseUser.getCurrentUser().getUsername().toUpperCase());
        sendIntent.setType("text/plain");
        context.startActivity(sendIntent);
    }

    public static class MotherlandArrayAdapter extends ArrayAdapter<String> {
        private final Context context;
        private final String[] values;

        public MotherlandArrayAdapter(Context context, String[] values) {
            super(context, R.layout.item_motherland, values);
            this.context = context;
            this.values = values;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        public View getCustomView(int index, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.item_motherland, parent, false);
            TextView textView = (TextView) rowView.findViewById(R.id.label);
            ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
            textView.setText(values[index]);
            // set the flag
            imageView.setImageResource(context.getResources().getIdentifier("flag_" + values[index].toLowerCase(), "drawable", context.getPackageName()));
            return rowView;
        }
    }

    /**
     * * Method for Setting the Height of the ListView dynamically.
     * *** Hack to fix the issue of not showing all the items of the ListView
     * *** when placed inside a ScrollView  ***
     */
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }
}
