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

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;

import java.util.Map;

import im.getsocial.sdk.GetSocial;
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
	
	private AccessTokenTracker accessTokenTracker;
	
	private FacebookUtils()
	{
		AccessToken accessToken = AccessToken.getCurrentAccessToken();
		
		if(accessToken != null && !accessToken.isExpired())
		{
			login(accessToken);
		}
		
		accessTokenTracker = new AccessTokenTracker()
		{
			@Override
			protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken newAccessToken)
			{
				if(newAccessToken != null)
				{
					login(newAccessToken);
				}
				else if(oldAccessToken != null)
				{
					logout();
				}
			}
		};
	}
	
	public void startTracking()
	{
		accessTokenTracker.startTracking();
	}
	
	public void stopTracking()
	{
		accessTokenTracker.stopTracking();
	}
	
	private void login(AccessToken accessToken)
	{
		Map<String, String> info = AuthPluginUtils.createMap(accessToken.getUserId(), accessToken.getToken(), accessToken.getUserId(), accessToken.getExpires().getTime());
		
		GetSocial.getInstance().verifyUserIdentity("facebook", info, null);
	}
	
	private void logout()
	{
		GetSocial.getInstance().clearUserIdentity("facebook", null, null);
	}
}