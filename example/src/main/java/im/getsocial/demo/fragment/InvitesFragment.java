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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import im.getsocial.demo.adapter.MenuItem;
import im.getsocial.sdk.GetSocial;
import im.getsocial.sdk.GetSocialException;
import im.getsocial.sdk.invites.FetchReferralDataCallback;
import im.getsocial.sdk.invites.ReferralData;
import im.getsocial.sdk.ui.GetSocialUi;
import im.getsocial.sdk.ui.invites.InviteUiCallback;

import java.util.ArrayList;
import java.util.List;

public class InvitesFragment extends BaseListFragment {

	public InvitesFragment() {
	}

	@NonNull
	protected List<MenuItem> createListData() {
		List<MenuItem> listData = new ArrayList<>();

		listData.add(new MenuItem.Builder("Open Invites UI")
				.withAction(new MenuItem.Action() {
					@Override
					public void execute() {
						openInvitesUi();
					}
				})
				.build());

		listData.add(new MenuItem.Builder("Send Customized Invite")
				.withAction(new MenuItem.Action() {
					@Override
					public void execute() {
						sendCustomizedInvite();
					}
				})
				.build());

		listData.add(new MenuItem.Builder("Check Referral Data")
				.withAction(new MenuItem.Action() {
					@Override
					public void execute() {
						checkReferralData();
					}
				})
				.build());

		return listData;
	}

	//region Presenter
	private void openInvitesUi() {
		GetSocialUi.createInvitesView().setInviteCallback(new InviteUiCallback() {
			@Override
			public void onComplete(final String channelId) {
				_log.logInfoAndToast("Invite channel launched successfully.");
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

	private void sendCustomizedInvite() {
		addContentFragment(new CustomInviteFragment());
	}

	private void checkReferralData() {
		GetSocial.getReferralData(new FetchReferralDataCallback() {
			@Override
			public void onSuccess(@Nullable ReferralData referralData) {
				if (referralData == null) {
					_log.logInfoAndToast("No referral data.");
				} else {
					_log.logInfoAndToast("Referral data received: [" + referralData + "]");
				}
			}

			@Override
			public void onFailure(GetSocialException e) {
				_log.logInfoAndToast("Could not get referral data: "+e.getMessage());
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

}
