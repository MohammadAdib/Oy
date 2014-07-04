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

import android.app.Application;

import com.parse.Parse;
import com.parse.PushService;

public class OyApplication extends Application {

    public static boolean running = false;

	@Override
	public void onCreate() {
        Parse.initialize(this, "Mo7icKI3Sluuo1LvYJDvp7lcZBQmJbcIaS5dosTK", "ZHcw9IWmSBRkirpkfmLBBAPKvNrog4h0qb63qo8G");
        PushService.setDefaultPushCallback(this, MainActivity.class);
        running = true;
	}

}
