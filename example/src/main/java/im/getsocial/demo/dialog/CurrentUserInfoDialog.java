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
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import im.getsocial.demo.R;
import im.getsocial.demo.ui.UserInfoView;
import im.getsocial.sdk.GetSocial;

import java.util.HashMap;
import java.util.Map;

public class CurrentUserInfoDialog extends DialogFragment {

	@BindView(R.id.detailedUserInfo_userInfo)
	UserInfoView _userInfoView;

	@BindView(R.id.detailedUserInfo_detailedInfo)
	TextView _detailedTextView;

	public CurrentUserInfoDialog() {
		// Empty constructor required for DialogFragment
	}

	public static void show(final FragmentManager fm) {
		new CurrentUserInfoDialog().show(fm, "current_user_info_dialog");
	}

	private static String getUserInfo() {
		if (!GetSocial.isInitialized()) {
			return "";
		}

		final Map<String, Object> userDetails = new HashMap<>();
		userDetails.put("Anonymous", GetSocial.getCurrentUser().isAnonymous());
		userDetails.put("User ID", GetSocial.getCurrentUser().getId());
		userDetails.put("Display Name", GetSocial.getCurrentUser().getDisplayName());
		userDetails.put("Avatar URL", GetSocial.getCurrentUser().getAvatarUrl());
		userDetails.put("Identities", GetSocial.getCurrentUser().getIdentities());
		userDetails.put("Public Properties", GetSocial.getCurrentUser().getPublicProperties());
		userDetails.put("Private Properties", GetSocial.getCurrentUser().getPrivateProperties());

		final UserInfoBuilder sb = new UserInfoBuilder();
		sb.append("Anonymous: ").append(GetSocial.getCurrentUser().isAnonymous())
						.endLine()
						.append("User ID: ").append(GetSocial.getCurrentUser().getId()).endLine()
						.append("Avatar URL: ").append(GetSocial.getCurrentUser().getAvatarUrl()).endLine()
						.endLine();

		if (!GetSocial.getCurrentUser().isAnonymous()) {
			sb.append("IDENTITIES:").endLine()
							.append(GetSocial.getCurrentUser().getIdentities());
		}
		sb.append("PUBLIC PROPERTIES:").endLine()
						.append(GetSocial.getCurrentUser().getPublicProperties());

		sb.append("PRIVATE PROPERTIES:").endLine()
						.append(GetSocial.getCurrentUser().getPrivateProperties());

		sb.append("BAN INFO").endLine();
		sb.append("Is banned: ").append(GetSocial.getCurrentUser().isBanned()).endLine();
		if (GetSocial.getCurrentUser().isBanned()) {
			sb.append("Expiry: ").append("" + GetSocial.getCurrentUser().getBanInfo().getExpiry()).endLine();
			sb.append("Reason: ").append(GetSocial.getCurrentUser().getBanInfo().getReason()).endLine();
		}

		sb.append("JSON:").endLine()
						.append(toJson(userDetails));

		return sb.toString();
	}

	private static String toJson(final Object object) {
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

	private static String iterableToJson(final Iterable iterable) {
		final StringBuilder sb = new StringBuilder();
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

	private static String mapToJson(final Map<String, Object> map) {
		final StringBuilder sb = new StringBuilder();
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

	@OnClick(R.id.detailedUserInfo_closeButton)
	public void closeView() {
		dismiss();
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
													 final Bundle savedInstanceState) {
		getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

		final View view = inflater.inflate(R.layout.dialog_detailed_user_info, container);

		ButterKnife.bind(this, view);

		_userInfoView.updateView(GetSocial.getCurrentUser());
		_detailedTextView.setText(getUserInfo());
		Linkify.addLinks(_detailedTextView, Linkify.WEB_URLS);

		return view;
	}

	private static class UserInfoBuilder {
		private final StringBuilder _stringBuilder;

		UserInfoBuilder() {
			_stringBuilder = new StringBuilder();
		}

		UserInfoBuilder append(final String string) {
			_stringBuilder.append(string);
			return this;
		}

		UserInfoBuilder append(final boolean bool) {
			_stringBuilder.append(bool);
			return this;
		}

		UserInfoBuilder append(final Map<String, String> map) {
			for (final Map.Entry<String, String> entry : map.entrySet()) {
				append(entry.getKey())
								.append(": ")
								.append(entry.getValue())
								.endLine();
			}
			return this;
		}

		UserInfoBuilder endLine() {
			_stringBuilder.append("\n\n");
			return this;
		}

		@Override
		public String toString() {
			return _stringBuilder.toString();
		}
	}
}