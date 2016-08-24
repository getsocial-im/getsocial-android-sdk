/*
 *    	Copyright 2015-2016 GetSocial B.V.
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

package im.getsocial.testapp.ui;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import im.getsocial.sdk.core.CurrentUser;
import im.getsocial.sdk.core.GetSocial;
import im.getsocial.sdk.core.User;
import im.getsocial.testapp.R;

public class UserInfoDialog extends DialogFragment
{
	
	public UserInfoDialog()
	{
		// Empty constructor required for DialogFragment
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState)
	{
		getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		
		GetSocial getSocial = GetSocial.getInstance();
		CurrentUser user = getSocial.getCurrentUser();
		
		View view = inflater.inflate(R.layout.fragment_detailed_user_info, container);
		
		UserInfoView userInfoView = (UserInfoView) view.findViewById(R.id.detailedUserInfo_userInfo);
		userInfoView.setUser(user);
		
		TextView detailsTextView = (TextView) view.findViewById(R.id.detailedUserInfo_detailedInfo);
		detailsTextView.setText(getUserInfo(user));
		Linkify.addLinks(detailsTextView, Linkify.WEB_URLS);
		
		Button closeButton = (Button) view.findViewById(R.id.detailedUserInfo_closeButton);
		closeButton.setOnClickListener(
				new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						UserInfoDialog.this.dismiss();
					}
				}
		);
		
		return view;
	}
	
	private String getUserInfo(User user)
	{
		StringBuilder sb = new StringBuilder();
		
		if(user != null)
		{
			sb.append("Anonymous: ");
			sb.append(user.isAnonymous());
			sb.append("\n\n");
			sb.append("guid: ").append(user.getGuid()).append("\n");
			sb.append("avatar: ").append(user.getAvatarUrl()).append("\n");
			sb.append("\n\n");
			sb.append("IDENTITIES:").append("\n\n");

			if(user.getAllIdentities() != null)
			{
				for(String key : user.getAllIdentities().keySet())
				{
					sb.append(key).append(": ").append(user.getUserIdForProvider(key)).append("\n\n");
				}
			}
		}
		return sb.toString();
	}
}