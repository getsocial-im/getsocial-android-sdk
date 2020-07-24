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

import android.app.AlertDialog;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import im.getsocial.demo.adapter.MenuItem;
import im.getsocial.demo.utils.EditTextWOCopyPaste;
import im.getsocial.demo.utils.PixelUtils;
import im.getsocial.sdk.GetSocialError;
import im.getsocial.sdk.Invites;
import im.getsocial.sdk.common.PagingQuery;
import im.getsocial.sdk.invites.ReferralUser;
import im.getsocial.sdk.invites.ReferralUsersQuery;
import im.getsocial.sdk.ui.invites.InviteUiCallback;
import im.getsocial.sdk.ui.invites.InvitesViewBuilder;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class InvitesFragment extends BaseListFragment {

	public InvitesFragment() {
	}

	//region private
	private static String formatReferralUserInfo(final ReferralUser referralUser) {
		return String.format("%s(date=%s; event=%s, data=%s)",
						referralUser.getDisplayName(),
						DateFormat.getDateTimeInstance().format(new Date(referralUser.getEventDate() * 1000)),
						referralUser.getEvent(),
						referralUser.getEventData()
		);
	}

	@NonNull
	protected List<MenuItem> createListData() {
		return Arrays.asList(
						new MenuItem.Builder("Open Invites UI")
										.withAction(this::openInvitesUi)
										.build(),
						navigationListItem("Send Customized Invite", CustomInviteFragment.class),
						new MenuItem.Builder("Create Invite Url")
										.withAction(this::createInviteUrl)
										.build(),
						new MenuItem.Builder("Check Referred Users")
										.withAction(this::checkReferredUsers)
										.build(),
						new MenuItem.Builder("Check Referrer Users")
										.withAction(this::checkReferrerUsers)
										.build(),
						navigationListItem("Set referrer", SetReferrerFragment.class)
		);
	}

	//region Presenter
	private void openInvitesUi() {
		InvitesViewBuilder.create().setInviteCallback(new InviteUiCallback() {
			@Override
			public void onComplete(final String channelId) {
				_log.logInfoAndToast("Invite was successfully sent.");
			}

			@Override
			public void onCancel(final String channelId) {
				_log.logInfoAndToast("Invite canceled.");
			}

			@Override
			public void onError(final String channelId, final GetSocialError error) {
				_log.logErrorAndToast("Error sending invite.");
			}
		}).show();
	}

	private void checkReferredUsers() {
		showReferrerEvenDialog(event -> {
			final ReferralUsersQuery query = event.isEmpty() ? ReferralUsersQuery.allUsers() : ReferralUsersQuery.usersForEvent(event);

			Invites.getReferredUsers(new PagingQuery<>(query), result -> {
				if (result.getEntries().size() > 0) {
					final StringBuilder messageBuilder = new StringBuilder();
					for (final ReferralUser referredUser : result.getEntries()) {
						messageBuilder.append(formatReferralUserInfo(referredUser)).append(", ");
					}
					String message = messageBuilder.toString();
					message = message.substring(0, message.length() - 2);
					showAlert("Referred Users", message);
				} else {
					showAlert("Referred Users", "No referred users.");
				}
			}, error -> {
				showAlert("Referred Users", "Error: " + error.getMessage());
			});
		});
	}

	private void checkReferrerUsers() {
		showReferrerEvenDialog(new ReferrerEventDialogCallback() {
			@Override
			public void onSelect(final String event) {
				final ReferralUsersQuery query = event.isEmpty() ? ReferralUsersQuery.allUsers() : ReferralUsersQuery.usersForEvent(event);

				Invites.getReferrerUsers(new PagingQuery<>(query), result -> {
					if (result.getEntries().size() > 0) {
						final StringBuilder messageBuilder = new StringBuilder();
						for (final ReferralUser referredUser : result.getEntries()) {
							messageBuilder.append(formatReferralUserInfo(referredUser)).append(", ");
						}
						String message = messageBuilder.toString();
						message = message.substring(0, message.length() - 2);
						showAlert("Referrer Users", message);
					} else {
						showAlert("Referrer Users", "No referrer users.");
					}
				}, error -> {
					showAlert("Referrer Users", "Error: " + error.getMessage());
				});
			}
		});
	}

	private void showReferrerEvenDialog(final ReferrerEventDialogCallback onSelect) {
		final EditTextWOCopyPaste eventName = new EditTextWOCopyPaste(getContext());
		eventName.setLongClickable(false);

		final int _8dp = PixelUtils.dp2px(getContext(), 8);
		final FrameLayout frameLayout = new FrameLayout(getContext());
		frameLayout.setPadding(_8dp, _8dp, _8dp, _8dp);
		frameLayout.addView(eventName);

		eventName.setSelection(eventName.getText().length());
		eventName.setContentDescription("event_name");

		new AlertDialog.Builder(getContext()).setView(frameLayout).setTitle("Referrer Event")
						.setPositiveButton("OK", (dialogInterface, which) -> onSelect.onSelect(eventName.getText().toString()))
						.setNegativeButton("Cancel", (dialogInterface, which) -> dialogInterface.cancel()).create().show();
	}

	private void createInviteUrl() {
		Invites.createLink(null, result -> showAlert("Invite link", result), error -> {
			showAlert("Failed to create invite link", "Error: " + error.getMessage());
		});
	}

	@Override
	public String getTitle() {
		return "Invites";
	}
	//endregion

	@Override
	public String getFragmentTag() {
		return "invites";
	}

	private interface ReferrerEventDialogCallback {
		void onSelect(String event);
	}
	//endregion

}
