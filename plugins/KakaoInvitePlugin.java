/*
 * Published under the MIT License (MIT)
 * Copyright: (c) 2015 GetSocial B.V.
 */

package im.getsocial.testapp;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;

import com.kakao.KakaoLink;
import com.kakao.KakaoParameterException;

import im.getsocial.sdk.core.plugins.InvitePlugin;

public class KakaoInvitePlugin extends InvitePlugin
{
	public static final String PROVIDER_NAME = "kakao";

	private static final String PACKAGE_NAME = "com.kakao.talk";

	public KakaoInvitePlugin()
	{
		setProvider(PROVIDER_NAME);
	}

	public boolean isAvailableForDevice(Context context)
	{
		return hasKakaoInstalled(context);
	}

	@Override
	public void inviteFriends(final Context context, final String subject, final String text, final String referralDataUrl, final Bitmap image, final InviteFriendsObserver callback)
	{
		try
		{
			final KakaoLink kakaoLink = KakaoLink.getKakaoLink(context);
			String linkContents =  kakaoLink.createKakaoTalkLinkMessageBuilder()
										    .addText(text)
										    .addWebLink(referralDataUrl,referralDataUrl)
										    .build();
			kakaoLink.sendMessage(linkContents, context);

			callback.onComplete(null, null);
		}
		catch(KakaoParameterException e)
		{
			callback.onError(e);
		}
	}

	private static boolean hasKakaoInstalled(Context context)
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
}