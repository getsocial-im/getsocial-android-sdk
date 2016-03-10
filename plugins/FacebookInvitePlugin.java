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

package im.getsocial.testapp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.share.model.AppInviteContent;
import com.facebook.share.widget.AppInviteDialog;

import im.getsocial.sdk.core.plugins.InvitePlugin;
import im.getsocial.sdk.core.util.Internet;

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
		if(Internet.isConnected())
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
						}
				);
				appInviteDialog.show(content);
			}
			else
			{
				String message = "Facebook is not available";
				callback.onError(new Exception(message));
				showToast(context, message);
			}
		}
		else
		{
			String message = "Can't reach Facebook. No internet connection.";
			callback.onError(new Exception(message));
			showToast(context, message);
		}
	}
	
	public void showToast(final Context context, final String message)
	{
		Handler mainHandler = new Handler(context.getMainLooper());
		
		Runnable myRunnable = new Runnable()
		{
			@Override
			public void run()
			{
				Toast.makeText(context, message, Toast.LENGTH_LONG).show();
			}
		};
		mainHandler.post(myRunnable);
	}
}