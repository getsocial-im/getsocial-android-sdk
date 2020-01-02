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

package im.getsocial.demo.ui;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.squareup.picasso.Picasso;
import im.getsocial.demo.R;
import im.getsocial.demo.utils.CircleTransform;
import im.getsocial.sdk.GetSocial;

import java.util.ArrayList;
import java.util.Set;

public class UserInfoView extends RelativeLayout {

	@BindView(R.id.userInfo_displayName)
	TextView _displayNameTextView;
	@BindView(R.id.userInfo_extra)
	TextView _extraInfoTextView;
	@BindView(R.id.userInfo_avatar)
	ImageView _avatarImageView;
	@BindView(R.id.userInfo_testDevice)
	ImageView _testDevice;

	public UserInfoView(Context context) {
		super(context);
		init(null, 0);
	}

	public UserInfoView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs, 0);
	}

	public UserInfoView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(attrs, defStyle);
	}

	public void updateView() {
		Handler handler = getHandler();
		if (handler != null) {
			getHandler().post(new Runnable() {
				@Override
				public void run() {
					updateContent();
				}
			});
		}
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		updateView();
	}

	private void init(AttributeSet attrs, int defStyle) {
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		inflater.inflate(R.layout.view_user_info, this, true);

		ButterKnife.bind(this);

		updateContent();
	}

	private void updateContent() {
		if (GetSocial.isInitialized()) {
			_testDevice.setVisibility(GetSocial.Device.isTestDevice() ? VISIBLE : INVISIBLE);
			if (GetSocial.User.isAnonymous()) {
				_extraInfoTextView.setText("Anonymous");
			} else {
				_extraInfoTextView.setText(printProviders());
			}

			String displayName = GetSocial.User.getDisplayName();
			if (TextUtils.isEmpty(displayName)) {
				displayName = "id: " + GetSocial.User.getId();
			}
			_displayNameTextView.setText(displayName);
			String avatarUrl = GetSocial.User.getAvatarUrl();
			if (TextUtils.isEmpty(avatarUrl)) {
				Picasso.with(getContext())
						.load(R.drawable.avatar_default)
						.transform(new CircleTransform())
						.into(_avatarImageView);
			} else {
				Picasso.with(getContext())
						.load(avatarUrl)
						.transform(new CircleTransform())
						.placeholder(R.drawable.avatar_default)
						.into(_avatarImageView);
			}
		} else {
			_displayNameTextView.setText(R.string.no_user);
			_extraInfoTextView.setText(R.string.probably_you_are_offline);

			Picasso.with(getContext())
					.load(R.drawable.avatar_default)
					.transform(new CircleTransform())
					.into(_avatarImageView);
		}
	}

	private String printProviders() {
		StringBuilder sb = new StringBuilder();
		Set<String> keySet = GetSocial.User.getAuthIdentities().keySet();
		ArrayList<String> providers = new ArrayList<>(keySet);
		for (int i = 0; i < keySet.size() && i < 3; i++) {
			sb.append(providers.get(i));
			if (i < providers.size() - 1) {
				sb.append(" / ");
			}
		}
		if (providers.size() > 3) {
			sb.append("…");
		}
		return sb.toString();
	}
}
