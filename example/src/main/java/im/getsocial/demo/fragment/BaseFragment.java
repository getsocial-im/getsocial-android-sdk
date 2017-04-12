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

package im.getsocial.demo.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.support.v4.app.Fragment;
import im.getsocial.demo.utils.SimpleLogger;

import javax.annotation.Nullable;

public abstract class BaseFragment extends Fragment implements HasTitle, HasFragmentTag {

	protected SimpleLogger _log;

	protected ActivityListener _activityListener;
	private ProgressDialog _currentProgressDialog;

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);

		Activity activity = getActivity();

		_log = new SimpleLogger(activity, getClass().getSimpleName());

		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try {
			_activityListener = (ActivityListener) activity;
		} catch (ClassCastException exception) {
			throw new ClassCastException(activity.toString()
					+ " must implement ActivityListener");
		}
	}

	protected void showLoading(String title, String text) {
		if (_currentProgressDialog == null) {
			_currentProgressDialog = ProgressDialog.show(getContext(), title, text);
		} else {
			hideLoading();
			showLoading(title, text);
		}
	}

	protected void hideLoading() {
		if (_currentProgressDialog != null) {
			_currentProgressDialog.hide();
			_currentProgressDialog = null;
		}
	}

	protected void addContentFragment(Fragment fragment) {
		_activityListener.addContentFragment(fragment);
	}

	public interface ActivityListener {
		void invalidateUi();

		void addContentFragment(Fragment fragment);

		void putSessionValue(String key, @Nullable String value);

		String getSessionValue(String key);

		void onSdkReset();
	}
}
