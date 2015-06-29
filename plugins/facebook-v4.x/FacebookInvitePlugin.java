/**
 * The MIT License (MIT)
 * 
 * Copyright (c) 2015 GetSocial B.V.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.share.model.AppInviteContent;
import com.facebook.share.widget.AppInviteDialog;

import im.getsocial.sdk.plugins.InvitePlugin;

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