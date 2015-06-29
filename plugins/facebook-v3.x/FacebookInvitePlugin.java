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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;

import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.widget.WebDialog;

import im.getsocial.sdk.plugins.InvitePlugin;

public abstract class FacebookInvitePlugin extends InvitePlugin
{
	private Activity activity;
	
	public FacebookInvitePlugin(Activity activity)
	{
		this.activity = activity;
	}
	
	public abstract void authenticateUser();
	
	@Override
	public boolean isAvailableForDevice(Context context)
	{
		return isEnabled();
	}
	
	@Override
	public void inviteFriends(Context context, String subject, String text, String referralDataUrl, Bitmap image, InviteFriendsObserver callback)
	{
		showFBRequestDialog("", "Invite your friends", null, callback);
	}
	
	public void showFBRequestDialog(final String title, final String message, final Map<String, String> parameters, final InvitePlugin.InviteFriendsObserver callback)
	{
		Session session = Session.getActiveSession();
		
		if(session != null && session.isOpened())
		{
			Bundle bundle = new Bundle();
			
			if(parameters != null && !parameters.isEmpty())
			{
				for(Map.Entry<String, String> entry : parameters.entrySet())
				{
					bundle.putString(entry.getKey(), entry.getValue());
				}
			}
			
			requestsDialogBuilder(activity, title, message, bundle, session, callback);
		}
		else
		{
			FacebookUtils.getInstance().setOnSessionListener(new FacebookUtils.OnSessionListener()
			{
				@Override
				public void onSessionOpen()
				{
					showFBRequestDialog(title, message, parameters, callback);
				}
				
				@Override
				public void onSessionClose()
				{
					
				}
			});
			
			authenticateUser();
		}
	}
	
	private void requestsDialogBuilder(final Context context, final String title, final String message, final Bundle bundle, final Session session, final InvitePlugin.InviteFriendsObserver callback)
	{
		activity.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				WebDialog.RequestsDialogBuilder requestsDialog = new WebDialog.RequestsDialogBuilder(context, session, bundle);
				requestsDialog.setMessage(message);
				requestsDialog.setTitle(title);
				requestsDialog.setOnCompleteListener(
						new WebDialog.OnCompleteListener()
						{
							@Override
							public void onComplete(Bundle values, FacebookException error)
							{
								if(error != null)
								{
									if(error instanceof FacebookOperationCanceledException)
									{
										callback.onCancel();
									}
									else
									{
										callback.onError(error);
									}
								}
								else
								{
									String requestId = null;
									List<String> to = new ArrayList<String>();
									
									for(String key : values.keySet())
									{
										if(key.equalsIgnoreCase("request"))
										{
											requestId = values.getString(key);
										}
										else if(key.startsWith("to["))
										{
											to.add(values.getString(key));
										}
									}
									
									if(requestId != null)
									{
										callback.onComplete(requestId, to);
									}
									else
									{
										callback.onCancel();
									}
								}
							}
						});
				requestsDialog.build().show();
			}
		});
	}
}