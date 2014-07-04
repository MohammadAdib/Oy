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

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

public class AboutActivity extends OyUtils.TransitionActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        //Take user to Yo website
        bindViewWithURL(R.id.yo, "http://www.justyo.co");
        //Take user to my website
        bindViewWithURL(R.id.developer, "http://www.mohammadadib.com");
    }

    /**
     * Bind a URL to the view such that clicking the view will open the URL
     * @param view_id the view resource id
     * @param url the URL
     */
    public void bindViewWithURL(int view_id, final String url) {
        findViewById(view_id).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
                overridePendingTransition(OyUtils.ANIM_ENTER, OyUtils.ANIM_EXIT);
            }
        });
    }
}
