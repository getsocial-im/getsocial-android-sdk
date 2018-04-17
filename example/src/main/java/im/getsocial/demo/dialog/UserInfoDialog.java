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

import java.util.HashMap;
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
		UserInfoBuilder sb = new UserInfoBuilder();

		Map<String, Object> userDetails = new HashMap<String, Object>();
		userDetails.put("Anonymous", GetSocial.User.isAnonymous());
		userDetails.put("User ID", GetSocial.User.getId());
		userDetails.put("Display Name", GetSocial.User.getDisplayName());
		userDetails.put("Avatar URL", GetSocial.User.getAvatarUrl());
		userDetails.put("Identities", GetSocial.User.getAuthIdentities());
		userDetails.put("Public Properties", GetSocial.User.getAllPublicProperties());
		userDetails.put("Private Properties", GetSocial.User.getAllPrivateProperties());

		sb.append("Anonymous: ").append(GetSocial.User.isAnonymous())
				.endLine()
				.append("User ID: ").append(GetSocial.User.getId()).endLine()
				.append("Avatar URL: ").append(GetSocial.User.getAvatarUrl()).endLine()
				.endLine();

		if (!GetSocial.User.isAnonymous()) {
			sb.append("IDENTITIES:").endLine()
					.append(GetSocial.User.getAuthIdentities());
		}
		sb.append("PUBLIC PROPERTIES:").endLine()
				.append(GetSocial.User.getAllPublicProperties());

		sb.append("PRIVATE PROPERTIES:").endLine()
				.append(GetSocial.User.getAllPrivateProperties());

		sb.append("JSON:").endLine()
				.append(toJson(userDetails));

		return sb.toString();
	}

	private String toJson(Object object) {
		if (object instanceof Map) {
			return mapToJson((Map<String, Object>) object);
		}
		if (object instanceof Iterable) {
			return iterableToJson((Iterable) object);
		}
		if (object instanceof Boolean) {
			return object.toString();
		}
		if (object instanceof Number) {
			return object.toString();
		}
		return "\"" + object.toString() + "\"";
	}

	private String iterableToJson(Iterable iterable) {
		StringBuilder sb = new StringBuilder();
		sb.append("{");

		for (final Object entry : iterable) {
			sb.append(toJson(entry)).append(",");
		}
		if (sb.charAt(sb.length() - 1) == ',') {
			sb.deleteCharAt(sb.length() - 1);
		}
		sb.append("}");
		return sb.toString();
	}

	private String mapToJson(Map<String, Object> map) {
		StringBuilder sb = new StringBuilder();
		sb.append("{");

		for (final Map.Entry<String, Object> entry : map.entrySet()) {
			sb.append("\"").append(entry.getKey()).append("\"").append(":")
					.append(toJson(entry.getValue())).append(",");
		}
		if (sb.charAt(sb.length() - 1) == ',') {
			sb.deleteCharAt(sb.length() - 1);
		}
		sb.append("}");
		return sb.toString();
	}

	private static class UserInfoBuilder {
		private final StringBuilder _stringBuilder;

		UserInfoBuilder() {
			_stringBuilder = new StringBuilder();
		}

		public UserInfoBuilder append(String string) {
			_stringBuilder.append(string);
			return this;
		}

		public UserInfoBuilder append(boolean bool) {
			_stringBuilder.append(bool);
			return this;
		}

		public UserInfoBuilder append(Map<String, String> map) {
			for (Map.Entry<String, String> entry : map.entrySet()) {
				append(entry.getKey())
						.append(": ")
						.append(entry.getValue())
						.endLine();
			}
			return this;
		}

		public UserInfoBuilder endLine() {
			_stringBuilder.append("\n\n");
			return this;
		}

		@Override
		public String toString() {
			return _stringBuilder.toString();
		}
	}
}