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
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.squareup.picasso.Picasso;
import im.getsocial.demo.R;
import im.getsocial.demo.utils.CircleTransform;
import im.getsocial.sdk.usermanagement.PublicUser;

public class UserInfoDialog extends DialogFragment {

	private PublicUser _publicUser;

	@BindView(R.id.display_name)
	TextView _displayName;

	@BindView(R.id.user_avatar)
	ImageView _avatar;

	public static void show(FragmentManager fm, PublicUser publicUser) {
		UserInfoDialog dialog = new UserInfoDialog();
		dialog._publicUser = publicUser;
		dialog.show(fm, "user_info_dialog");
	}

	@OnClick(R.id.buttonClose)
	public void closeView() {
		dismiss();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

		View view = inflater.inflate(R.layout.dialog_userinfo, container);
		ButterKnife.bind(this, view);
		_displayName.setText(_publicUser.getDisplayName());

		if (TextUtils.isEmpty(_publicUser.getAvatarUrl())) {
			Picasso.with(getContext())
					.load(R.drawable.avatar_default)
					.transform(new CircleTransform())
					.into(_avatar);
		} else {
			Picasso.with(getContext())
					.load(_publicUser.getAvatarUrl())
					.placeholder(R.drawable.avatar_default)
					.transform(new CircleTransform())
					.into(_avatar);
		}

		return view;
	}
}
