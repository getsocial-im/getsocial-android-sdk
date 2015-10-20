/*
 * Published under the MIT License (MIT)
 * Copyright: (c) 2015 GetSocial B.V.
 */

package im.getsocial.testapp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.share.model.AppInviteContent;
import com.facebook.share.widget.AppInviteDialog;

import im.getsocial.sdk.core.plugins.InvitePlugin;

public class FacebookInvitePlugin extends InvitePlugin
{
	private Activity activity;
	private CallbackManager callbackManager;
	
	public FacebookInvitePlugin(Activity activity, CallbackManager callbackManager)
	{
		this.activity = activity;
		this.callbackManager = callbackManager;
	}
	
	@Override
	public boolean isAvailableForDevice(Context context)
	{
		return isEnabled();
	}
	
	@Override
	public void inviteFriends(final Context context, final String subject, final String text, final String referralDataUrl, final Bitmap image, final InviteFriendsObserver callback)
	{
		if(AppInviteDialog.canShow())
		{
			AppInviteContent content = new AppInviteContent.Builder()
					.setApplinkUrl(referralDataUrl)
					.build();
			
			AppInviteDialog appInviteDialog = new AppInviteDialog(activity);
			appInviteDialog.registerCallback(callbackManager, new FacebookCallback<AppInviteDialog.Result>()
			{
				@Override
				public void onSuccess(AppInviteDialog.Result result)
				{
					callback.onComplete(null, null);
				}
				
				@Override
				public void onCancel()
				{
					callback.onCancel();
				}
				
				@Override
				public void onError(FacebookException e)
				{
					callback.onError(e);
				}
			});
			appInviteDialog.show(content);
		}
	}
}