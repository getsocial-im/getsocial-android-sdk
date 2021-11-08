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

import android.os.Bundle;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import im.getsocial.demo.R;
import im.getsocial.sdk.CompletionCallback;
import im.getsocial.sdk.GetSocial;
import im.getsocial.sdk.GetSocialException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SetReferrerFragment extends BaseFragment {

	private ViewContainer _viewContainer;

	public SetReferrerFragment() {
		//
	}


	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_set_referrer, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		_viewContainer = new ViewContainer(view);
	}

	@Override
	public String getTitle() {
		return "Set Referrer";
	}

	@Override
	public String getFragmentTag() {
		return "setreferrer";
	}

	private void invokeSetReferrer() {
		final String referrerId = _viewContainer._referrerIdInput.getText().toString();
		final String event = _viewContainer._referrerEventInput.getText().toString();

		// collect custom data
		final Map<String, String> customData = new HashMap<>();
		for (int i = 0; i < _viewContainer._customDataKeys.size(); i++) {
			String key = _viewContainer._customDataKeys.get(i).getText().toString();
			String value = _viewContainer._customDataValues.get(i).getText().toString();
			if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
				customData.put(key, value);
			}
		}

		// invoke set referrer
		GetSocial.setReferrer(referrerId, event, customData, new CompletionCallback() {
			@Override
			public void onSuccess() {
				showAlert("Set Referrer", "Referrer was set.");
			}

			@Override
			public void onFailure(GetSocialException exception) {
				showAlert("Set Referrer", "Error: " + exception.getMessage());
			}
		});
	}

	class ViewContainer {

		@BindView(R.id.setReferrer_referrerId)
		EditText _referrerIdInput;
		@BindView(R.id.setReferrer_event)
		EditText _referrerEventInput;

		@BindViews({R.id.key1, R.id.key2, R.id.key3})
		List<EditText> _customDataKeys;
		@BindViews({R.id.value1, R.id.value2, R.id.value3})
		List<EditText> _customDataValues;

		@BindView(R.id.buttonSetReferrer)
		Button _buttonSetReferrer;

		ViewContainer(View view) {

			ButterKnife.bind(this, view);

			_buttonSetReferrer.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					invokeSetReferrer();
				}
			});
		}
	}
}