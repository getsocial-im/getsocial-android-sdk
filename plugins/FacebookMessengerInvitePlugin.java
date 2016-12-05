/*
 * Published under the MIT License (MIT)
 * Copyright: (c) 2015 GetSocial B.V.
 */

package im.getsocial.testapp;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;

import im.getsocial.sdk.core.plugins.InvitePlugin;

public class FacebookMessengerInvitePlugin extends InvitePlugin
{
	public static final String PROVIDER_NAME = "facebookmessenger";

	private static final String PACKAGE_NAME = "com.facebook.orca";

	private static boolean hasMessengerInstalled(Context context)
	{
		try
		{
			context.getPackageManager().getPackageInfo(PACKAGE_NAME, 0);
			return true;
		}
		catch(PackageManager.NameNotFoundException e)
		{
			return false;
		}
	}

	public FacebookMessengerInvitePlugin()
	{
		setProvider(PROVIDER_NAME);
	}

	public boolean isAvailableForDevice(Context context)
	{
		return hasMessengerInstalled(context);
	}

	@Override
	public void inviteFriends(final Context context, final String subject, final String text, final String referralDataUrl, final Bitmap image, final InviteFriendsObserver callback)
	{
		try
		{
			Intent appIntent = new Intent(Intent.ACTION_SEND);
			appIntent.setType("text/plain");
			appIntent.setPackage("com.facebook.orca");
			appIntent.putExtra(Intent.EXTRA_TEXT, referralDataUrl);
			context.startActivity(Intent.createChooser(appIntent, "Share"));

			callback.onComplete(null, null);
		}
		catch(Exception e)
		{
			callback.onError(e);
		}
	}
}