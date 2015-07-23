package im.getsocial.testapp;

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