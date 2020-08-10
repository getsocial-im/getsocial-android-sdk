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

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import com.kakao.kakaolink.v2.KakaoLinkResponse;
import com.kakao.kakaolink.v2.KakaoLinkService;
import com.kakao.message.template.ContentObject;
import com.kakao.message.template.FeedTemplate;
import com.kakao.message.template.LinkObject;
import com.kakao.network.ErrorResult;
import com.kakao.network.callback.ResponseCallback;
import im.getsocial.sdk.invites.Invite;
import im.getsocial.sdk.invites.InviteCallback;
import im.getsocial.sdk.invites.InviteChannel;
import im.getsocial.sdk.invites.InviteChannelPlugin;

public class KakaoInvitePlugin extends InviteChannelPlugin {

	private static final int SHARED_IMAGE_WIDTH = 300;

	private static final String PACKAGE_NAME = "com.kakao.talk";

	private static boolean hasKakaoInstalled(final Context context) {
		try {
			context.getPackageManager().getPackageInfo(PACKAGE_NAME, 0);
			return true;
		} catch (final PackageManager.NameNotFoundException e) {
			return false;
		}
	}

	@Override
	public boolean isAvailableForDevice(final InviteChannel inviteChannel) {
		return hasKakaoInstalled(getContext());
	}

	@Override
	public void presentChannelInterface(final InviteChannel inviteChannel, final Invite invite, final InviteCallback callback) {
		final LinkObject linkObject = LinkObject.newBuilder().setMobileWebUrl(invite.getReferralUrl()).build();
		final ContentObject.Builder contentObject = ContentObject.newBuilder(invite.getText(), invite.getImageUrl(), linkObject);

		if (invite.getImageUrl() != null && invite.getImage() != null) {
			final Bitmap image = invite.getImage();
			final double ratio = (double) image.getHeight() / (double) image.getWidth();
			final int height = (int) (ratio * SHARED_IMAGE_WIDTH);
			contentObject.setImageHeight(height);
			contentObject.setImageWidth(SHARED_IMAGE_WIDTH);
		}

		final FeedTemplate.Builder templateBuilder = FeedTemplate.newBuilder(contentObject.build());
		KakaoLinkService.getInstance().sendDefault(getContext(), templateBuilder.build(), new ResponseCallback<KakaoLinkResponse>() {
			@Override
			public void onFailure(final ErrorResult errorResult) {
				callback.onError(new Exception(errorResult.getErrorMessage()));
			}

			@Override
			public void onSuccess(final KakaoLinkResponse kakaoLinkResponse) {
				callback.onComplete();
			}
		});
	}
}