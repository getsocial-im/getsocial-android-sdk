/*
 *    	Copyright 2015-2017 GetSocial B.V.
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

package im.getsocial.demo.plugin;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import com.kakao.KakaoLink;
import com.kakao.KakaoParameterException;
import im.getsocial.sdk.invites.InviteCallback;
import im.getsocial.sdk.invites.InviteChannel;
import im.getsocial.sdk.invites.InviteChannelPlugin;
import im.getsocial.sdk.invites.InvitePackage;

public class KakaoInvitePlugin extends InviteChannelPlugin {
	public static final String PROVIDER_NAME = "kakao";

	private static final String PACKAGE_NAME = "com.kakao.talk";
	private final Activity _activity;

	public KakaoInvitePlugin(Activity activity) {
		super(activity);
		_activity = activity;
	}

	private static boolean hasKakaoInstalled(Context context) {
		try {
			context.getPackageManager().getPackageInfo(PACKAGE_NAME, 0);
			return true;
		} catch (PackageManager.NameNotFoundException e) {
			return false;
		}
	}

	@Override
	public boolean isAvailableForDevice(InviteChannel inviteChannel) {
		return hasKakaoInstalled(getContext());
	}

	@Override
	public void presentChannelInterface(InviteChannel inviteChannel, InvitePackage invitePackage, InviteCallback callback) {
		try {
			final KakaoLink kakaoLink = KakaoLink.getKakaoLink(getContext());
			String linkContents = kakaoLink.createKakaoTalkLinkMessageBuilder()
					.addText(invitePackage.getText())
					.addWebLink(invitePackage.getReferralUrl(), invitePackage.getReferralUrl())
					.build();
			kakaoLink.sendMessage(linkContents, _activity);

			callback.onComplete();
		} catch (KakaoParameterException e) {
			callback.onError(e);
		}
	}
}