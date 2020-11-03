/*
 *	Copyright 2015-2017 GetSocial B.V.
 *
 *	Licensed under the Apache License, Version 2.0 (the "License");
 *	you may not use this file except in compliance with the License.
 *	You may obtain a copy of the License at
 *
 *	http://www.apache.org/licenses/LICENSE-2.0
 *
 *	Unless required by applicable law or agreed to in writing, software
 *	distributed under the License is distributed on an "AS IS" BASIS,
 *	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *	See the License for the specific language governing permissions and
 *	limitations under the License.
 */

package im.getsocial.demo.plugin;

import android.app.Activity;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.photo.VKImageParameters;
import com.vk.sdk.api.photo.VKUploadImage;
import com.vk.sdk.dialogs.VKShareDialog;
import com.vk.sdk.dialogs.VKShareDialogBuilder;
import im.getsocial.sdk.Invites;
import im.getsocial.sdk.invites.Invite;
import im.getsocial.sdk.invites.InviteCallback;
import im.getsocial.sdk.invites.InviteChannel;
import im.getsocial.sdk.invites.InviteChannelPlugin;

/**
 * Invite Channel Plugin for GetSocial SDK.
 * Register plugin via {@link Invites#registerPlugin(InviteChannelPlugin, String)}.
 */
public class VKInvitePlugin extends InviteChannelPlugin implements VKCallback<VKAccessToken> {

	private final Activity _activity;
	private InviteCallback _inviteCallback;
	private Invite _invite;

	public VKInvitePlugin(final Activity activity) {
		_activity = activity;
	}

	@Override
	public boolean isAvailableForDevice(final InviteChannel inviteChannel) {
		return true;
	}

	@Override
	public void presentChannelInterface(final InviteChannel inviteChannel, final Invite invite, final InviteCallback callback) {
		_inviteCallback = callback;
		_invite = invite;
		if (!VKSdk.isLoggedIn()) {
			VKSdk.login(_activity, VKScope.PHOTOS, VKScope.WALL);
		} else {
			openShareDialog();
		}
	}

	private void openShareDialog() {
		final VKShareDialogBuilder shareDialogBuilder = new VKShareDialogBuilder();
		shareDialogBuilder.setText(_invite.getText());
		if (_invite.getImage() != null) {
			final VKUploadImage uploadImage = new VKUploadImage(_invite.getImage(), VKImageParameters.pngImage());
			final VKUploadImage[] uploadImages = new VKUploadImage[] {uploadImage};
			shareDialogBuilder.setAttachmentImages(uploadImages);
		}
		shareDialogBuilder.setShareDialogListener(new VKShareDialog.VKShareDialogListener() {
			@Override
			public void onVkShareComplete(final int result) {
				_inviteCallback.onComplete();
			}

			@Override
			public void onVkShareCancel() {
				_inviteCallback.onCancel();
			}

			@Override
			public void onVkShareError(final VKError vkError) {
				_inviteCallback.onError(new Exception(vkError.errorMessage));
			}
		});
		shareDialogBuilder.show(_activity.getFragmentManager(), "VK_SHARE");
	}

	// VKCallback
	@Override
	public void onResult(final VKAccessToken vkAccessToken) {
		openShareDialog();
	}

	@Override
	public void onError(final VKError vkError) {
		_inviteCallback.onError(new Exception(vkError == null ? "Can not sent VK invite" : vkError.errorMessage));
	}
}
