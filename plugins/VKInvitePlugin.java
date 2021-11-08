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
import im.getsocial.sdk.ErrorCode;
import im.getsocial.sdk.GetSocial;
import im.getsocial.sdk.GetSocialException;
import im.getsocial.sdk.invites.InviteCallback;
import im.getsocial.sdk.invites.InviteChannel;
import im.getsocial.sdk.invites.InviteChannelPlugin;
import im.getsocial.sdk.invites.InvitePackage;

/**
 * Invite Channel Plugin for GetSocial SDK.
 * Register plugin via {@link GetSocial#registerInviteChannelPlugin(String pluginId, InviteChannelPlugin plugin)}.
 */
public class VKInvitePlugin extends InviteChannelPlugin implements VKCallback<VKAccessToken> {

	private Activity _activity;
	private InviteCallback _inviteCallback;
	private InvitePackage _invitePackage;

	public VKInvitePlugin(Activity activity) {
		_activity = activity;
	}

	@Override
	public boolean isAvailableForDevice(InviteChannel inviteChannel) {
		return true;
	}

	@Override
	public void presentChannelInterface(InviteChannel inviteChannel, InvitePackage invitePackage, final InviteCallback callback) {
		_inviteCallback = callback;
		_invitePackage = invitePackage;
		if (!VKSdk.isLoggedIn()) {
			VKSdk.login(_activity, VKScope.PHOTOS, VKScope.WALL);
		} else {
			openShareDialog();
		}
	}

	private void openShareDialog() {
		VKShareDialogBuilder shareDialogBuilder = new VKShareDialogBuilder();
		shareDialogBuilder.setText(_invitePackage.getText());
		if (_invitePackage.getImage() != null) {
			VKUploadImage uploadImage = new VKUploadImage(_invitePackage.getImage(), VKImageParameters.pngImage());
			VKUploadImage[] uploadImages = new VKUploadImage[] { uploadImage };
			shareDialogBuilder.setAttachmentImages(uploadImages);
		}
		shareDialogBuilder.setShareDialogListener(new VKShareDialog.VKShareDialogListener() {
			@Override
			public void onVkShareComplete(int result) {
				_inviteCallback.onComplete();
			}

			@Override
			public void onVkShareCancel() {
				_inviteCallback.onCancel();
			}

			@Override
			public void onVkShareError(VKError vkError) {
				_inviteCallback.onError(new GetSocialException(ErrorCode.UNKNOWN, vkError.errorMessage));
			}
		});
		shareDialogBuilder.show(_activity.getFragmentManager(), "VK_SHARE");
	}

	// VKCallback
	@Override
	public void onResult(VKAccessToken vkAccessToken) {
		openShareDialog();
	}

	@Override
	public void onError(VKError vkError) {
		_inviteCallback.onError(new GetSocialException(ErrorCode.UNKNOWN, vkError == null ? "Can not sent VK invite" : vkError.errorMessage));
	}
}
