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

package im.getsocial.demo.dialog;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import im.getsocial.demo.R;
import im.getsocial.demo.ui.UserInfoView;
import im.getsocial.sdk.GetSocial;

import java.util.Map;

public class UserInfoDialog extends DialogFragment {
	
	public UserInfoDialog() {
		// Empty constructor required for DialogFragment
	}

	public static void show(FragmentManager fm) {
		new UserInfoDialog().show(fm, "user_info_dialog");
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		
		View view = inflater.inflate(R.layout.dialog_detailed_user_info, container);
		
		UserInfoView userInfoView = (UserInfoView) view.findViewById(R.id.detailedUserInfo_userInfo);
		userInfoView.updateView();
		
		TextView detailsTextView = (TextView) view.findViewById(R.id.detailedUserInfo_detailedInfo);
		detailsTextView.setText(getUserInfo());
		Linkify.addLinks(detailsTextView, Linkify.WEB_URLS);
		
		Button closeButton = (Button) view.findViewById(R.id.detailedUserInfo_closeButton);
		closeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				dismiss();
			}
		});
		
		return view;
	}
	
	private String getUserInfo() {
		if (!GetSocial.isInitialized()) {
			return "";
		}
		StringBuilder sb = new StringBuilder();

		sb.append("Anonymous: ").append(GetSocial.User.isAnonymous())
				.append("\n\n")
				.append("User ID: ").append(GetSocial.User.getId()).append("\n")
				.append("Avatar URL: ").append(GetSocial.User.getAvatarUrl()).append("\n")
				.append("\n\n");

		if (!GetSocial.User.isAnonymous()) {
			sb.append("IDENTITIES:").append("\n\n");
			Map<String, String> authIdentities = GetSocial.User.getAuthIdentities();
			for (Map.Entry<String, String> entry : authIdentities.entrySet()) {
				sb.append(entry.getKey())
						.append(": ")
						.append(entry.getValue())
						.append("\n\n");
			}
		}
		return sb.toString();
	}
}