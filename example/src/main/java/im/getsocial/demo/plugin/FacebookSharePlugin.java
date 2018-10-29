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
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.widget.Toast;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import im.getsocial.sdk.GetSocial;
import im.getsocial.sdk.invites.InviteCallback;
import im.getsocial.sdk.invites.InviteChannel;
import im.getsocial.sdk.invites.InviteChannelPlugin;
import im.getsocial.sdk.invites.InvitePackage;

/**
 * Invite Channel Plugin for GetSocial SDK. 
 * Register plugin via {@link GetSocial#registerInviteChannelPlugin(String pluginId, InviteChannelPlugin plugin)}.
 */
public class FacebookSharePlugin extends InviteChannelPlugin {

	private final Activity _activity;
	private final CallbackManager _callbackManager;
	private final ConnectivityManager _connectivityManager;

	public FacebookSharePlugin(Activity activity, CallbackManager callbackManager) {
		_activity = activity;
		_callbackManager = callbackManager;
		_connectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
	}

	@Override
	public boolean isAvailableForDevice(InviteChannel inviteChannel) {
		// always available, if Facebook app not installed, it opens webview.
		return true;
	}

	@Override
	public void presentChannelInterface(InviteChannel inviteChannel, InvitePackage invitePackage, final InviteCallback callback) {
		if (isConnected()) {
			ShareLinkContent.Builder shareLinkBuilder = new ShareLinkContent.Builder();

			shareLinkBuilder.setContentUrl(Uri.parse(invitePackage.getReferralUrl()));

			ShareLinkContent sharedContent = shareLinkBuilder.build();

			ShareDialog shareDialog = new ShareDialog(_activity);
			shareDialog.registerCallback(_callbackManager, new FacebookCallback<ShareDialog.Result>() {
						@Override
						public void onSuccess(ShareDialog.Result result) {
							callback.onComplete();
						}

						@Override
						public void onCancel() {
							callback.onCancel();
						}

						@Override
						public void onError(FacebookException facebookException) {
							callback.onError(facebookException);
						}
					}
			);
			shareDialog.show(sharedContent, ShareDialog.Mode.WEB);
		} else {
			onError("Can't reach Facebook. No internet connection.", callback);
		}
	}

	private boolean isConnected() {
		NetworkInfo networkInfo = _connectivityManager.getActiveNetworkInfo();
		return networkInfo != null && networkInfo.isConnected();
	}

	private void onError(String message, InviteCallback callback) {
		callback.onError(new Exception(message));
		showToastOnMainThread(getContext(), message);
	}

	private void showToastOnMainThread(final Context context, final String message) {
		_activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(context, message, Toast.LENGTH_LONG).show();
			}
		});
	}

}