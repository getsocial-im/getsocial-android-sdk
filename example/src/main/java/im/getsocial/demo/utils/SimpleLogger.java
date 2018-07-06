/*
*    	Copyright 2015-2017 GetSocial B.V.
*
*	Licensed under the Apache License, Version 2.0 (the "License");
*	you may not use this file except in compliance with the License.
*	You may obtain a copy of the License at
*
*    	http://www.apache.org/licenses/LICENSE-2.0
*
*	Unless required by applicable law or agreed to in writing, software
*	distributed under the License is distributed on an "AS IS" BASIS,
*	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*	See the License for the specific language governing permissions and
*	limitations under the License.
*/

package im.getsocial.demo.utils;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

public class SimpleLogger {

	private final Activity _activity;
	private final String _tag;

	public SimpleLogger(Activity activity, String tag) {
		_activity = activity;
		_tag = tag;
	}

	public void logInfo(Object message) {
		Log.i(_tag, message.toString());
		Console.logInfo(message.toString());
	}

	public void logInfoAndToast(Object message) {
		Log.i(_tag, message.toString());
		Console.logInfo(message.toString());
		toastOnUiThread(message.toString());
	}

	private void logWarningAndToast(Object message) {
		Log.w(_tag, message.toString());
		Console.logWarning(message.toString());
		toastOnUiThread(message.toString());
	}

	public void logErrorAndToast(Object message) {
		Log.e(_tag, message.toString());
		Console.logError(message.toString());

		if (message instanceof Throwable) {
			Throwable throwable = (Throwable) message;
			toastOnUiThread(throwable.getMessage());
		} else {
			toastOnUiThread(message.toString());
		}
	}

	private void toastOnUiThread(final String message) {
		_activity.runOnUiThread(
				new Runnable() {
					@Override
					public void run() {
						Toast.makeText(_activity, message, Toast.LENGTH_SHORT).show();
					}
				}
		);
	}
}
