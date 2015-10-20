/*
 * Published under the MIT License (MIT)
 * Copyright: (c) 2015 GetSocial B.V.
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

import java.util.Collection;

import im.getsocial.sdk.core.GetSocial;
import im.getsocial.sdk.core.IdentityInfo;
import im.getsocial.sdk.core.UserIdentity;
import im.getsocial.testapp.R;

public class UserInfoDialog extends DialogFragment
{
	private UserInfoView userInfoView;
	private TextView detailsTextView;
	//	private Button loginLogoutButton;
	private Button closeButton;
	
	private GetSocial getSocial;
	
	public UserInfoDialog()
	{
		// Empty constructor required for DialogFragment
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		
		getSocial = GetSocial.getInstance();
		UserIdentity user = getSocial.getLoggedInUser();
		
		View view = inflater.inflate(R.layout.fragment_detailed_user_info, container);
		
		userInfoView = (UserInfoView) view.findViewById(R.id.detailedUserInfo_userInfo);
		userInfoView.setUser(user);
		
		detailsTextView = (TextView) view.findViewById(R.id.detailedUserInfo_detailedInfo);
		detailsTextView.setText(printUserInfo(user));
		Linkify.addLinks(detailsTextView, Linkify.WEB_URLS);

//		loginLogoutButton = (Button) view.findViewById(R.id.detailedUserInfo_loginLogoutButton);
//		loginLogoutButton.setText(getSocial.isUserLoggedIn() ? "LOGOUT", "LOGIN");
//		loginLogoutButton.setOnClickListener(
//				new View.OnClickListener()
//				{
//					@Override
//					public void onClick(View v)
//					{
//						if(getSocial.isUserLoggedIn())
//						{
//							getSocial.logout(null);
//						}
//					}
//				}
//		);
		
		closeButton = (Button) view.findViewById(R.id.detailedUserInfo_closeButton);
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
	
	private String printUserInfo(UserIdentity user)
	{
		StringBuilder sb = new StringBuilder();
		
		if(user != null)
		{
			sb.append("guid: ").append(user.getGuid()).append("\n");
			sb.append("avatar: ").append(user.getAvatarUrl()).append("\n");
			sb.append("\n\n");
			sb.append("IDENTITIES:").append("\n\n");

			for(String key : user.getIdentities().keySet())
			{
				sb.append(key).append(": ").append(user.getIdentities().get(key)).append("\n\n");
			}
		}
		return sb.toString();
	}
}