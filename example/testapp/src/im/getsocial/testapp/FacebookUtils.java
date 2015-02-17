package im.getsocial.testapp;

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