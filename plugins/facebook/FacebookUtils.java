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

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;

import java.util.Map;

import im.getsocial.sdk.GetSocial;
import im.getsocial.sdk.plugins.AuthPlugin;
import im.getsocial.sdk.plugins.utils.AuthPluginUtils;

public class FacebookUtils
{
	private static final FacebookUtils instance;
	
	static
	{
		instance = new FacebookUtils();
	}
	
	public synchronized static FacebookUtils getInstance()
	{
		return instance;
	}
	
	private OnSessionListener onSessionListener;
	
	private FacebookUtils()
	{
		
	}
	
	public void setOnSessionListener(OnSessionListener onSessionListener)
	{
		this.onSessionListener = onSessionListener;
	}
	
	public void updateSessionState()
	{
		final Session session = Session.getActiveSession();
		
		if(session != null)
		{
			if(session.isOpened())
			{
				Request.executeBatchAsync(Request.newMeRequest(session, new Request.GraphUserCallback()
				{
					@Override
					public void onCompleted(GraphUser user, Response response)
					{
						String userId = "";
						
						if(user != null)
						{
							userId = user.getId();
						}
						
						Map<String, String> info = AuthPluginUtils.createMap(userId, session.getAccessToken(), userId, session.getExpirationDate().getTime());
						
						GetSocial.getInstance().verifyUserIdentity("facebook", info, new AuthPlugin.VerifyIdentityObserver()
						{
							@Override
							public void onComplete()
							{
								if(onSessionListener != null)
								{
									onSessionListener.onSessionOpen();
									onSessionListener = null;
								}
							}
							
							@Override
							public void onError(Exception error)
							{
								onSessionListener = null;
							}
						});
					}
				}));
			}
			else if(session.isClosed())
			{
				GetSocial.getInstance().clearUserIdentity("facebook", null, new AuthPlugin.ClearIdentityObserver()
				{
					@Override
					public void onComplete()
					{
						if(onSessionListener != null)
						{
							onSessionListener.onSessionClose();
							onSessionListener = null;
						}
					}
				});
			}
		}
	}
	
	public interface OnSessionListener
	{
		public void onSessionOpen();
		public void onSessionClose();
	}
}