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

package im.getsocial.demo.fragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import im.getsocial.demo.adapter.MenuItem;
import im.getsocial.demo.dialog.ReferralDataDialog;
import im.getsocial.sdk.Callback;
import im.getsocial.sdk.GetSocial;
import im.getsocial.sdk.GetSocialException;
import im.getsocial.sdk.invites.FetchReferralDataCallback;
import im.getsocial.sdk.invites.ReferralData;
import im.getsocial.sdk.invites.ReferredUser;
import im.getsocial.sdk.ui.GetSocialUi;
import im.getsocial.sdk.ui.invites.InviteUiCallback;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class InvitesFragment extends BaseListFragment {

	public InvitesFragment() {
	}

	@NonNull
	protected List<MenuItem> createListData() {
		return Arrays.asList(
				new MenuItem.Builder("Open Invites UI")
						.withAction(new MenuItem.Action() {
							@Override
							public void execute() {
								openInvitesUi();
							}
						})
						.build(),
				navigationListItem("Send Customized Invite", CustomInviteFragment.class),
				new MenuItem.Builder("Create Invite Url")
						.withAction(new MenuItem.Action() {
							@Override
							public void execute() {
								createInviteUrl();
							}
						})
						.build(),
				new MenuItem.Builder("Check Referral Data")
						.withAction(new MenuItem.Action() {
							@Override
							public void execute() {
								checkReferralData();
							}
						})
						.build(),
				new MenuItem.Builder("Check Referred Users")
						.withAction(new MenuItem.Action() {
							@Override
							public void execute() {
								checkReferredUsers();
							}
						})
						.build()
		);
	}

	//region Presenter
	private void openInvitesUi() {
		GetSocialUi.createInvitesView().setInviteCallback(new InviteUiCallback() {
			@Override
			public void onComplete(final String channelId) {
				_log.logInfoAndToast("Invite was successfully sent.");
			}

			@Override
			public void onCancel(final String channelId) {
				_log.logInfoAndToast("Invite canceled.");
			}

			@Override
			public void onError(final String channelId, Throwable error) {
				_log.logErrorAndToast("Error sending invite.");
			}
		}).show();
	}

	private void checkReferralData() {
		GetSocial.getReferralData(new FetchReferralDataCallback() {
			@Override
			public void onSuccess(@Nullable ReferralData referralData) {
				ReferralDataDialog.showReferralData(getActivity().getSupportFragmentManager(), referralData);
			}

			@Override
			public void onFailure(GetSocialException e) {
				_log.logInfoAndToast("Could not get referral data: " + e.getMessage());
			}
		});
	}

	private void checkReferredUsers() {
		GetSocial.getReferredUsers(new Callback<List<ReferredUser>>() {
			@Override
			public void onSuccess(List<ReferredUser> result) {
				if (result.size() > 0) {
					String message = "";
					for (ReferredUser referredUser : result) {
						message += formatReferredUserInfo(referredUser) + ", ";
					}
					message = message.substring(0, message.length() - 2);
					_log.logInfoAndToast("Referred users: " + message);
				} else {
					_log.logInfoAndToast("No referred users.");
				}
			}

			@Override
			public void onFailure(GetSocialException exception) {
				_log.logInfoAndToast("Could not get referred users: " + exception.getMessage());
			}
		});
	}

	private void createInviteUrl() {
		GetSocial.createInviteLink(null, new Callback<String>() {
			@Override
			public void onSuccess(String result) {
				_log.logInfoAndToast("Invite link: " + result);
			}

			@Override
			public void onFailure(GetSocialException exception) {
				_log.logErrorAndToast("Failed to create invite link: " + exception);
			}
		});
	}

	@Override
	public String getTitle() {
		return "Invites";
	}

	@Override
	public String getFragmentTag() {
		return "invites";
	}
	//endregion

	//region private
	private static String formatReferredUserInfo(ReferredUser referredUser) {
		return String.format("%s(date=%s; channel=%s, reinstall=%b, suspicious=%b, platform=%s)",
				referredUser.getDisplayName(),
				DateFormat.getDateTimeInstance().format(new Date(referredUser.getInstallationDate() * 1000)),
				referredUser.getInstallationChannel(),
				referredUser.isReinstall(),
				referredUser.isInstallSuspicious(),
				referredUser.getInstallPlatform()
		);
	}
	//endregion

}
