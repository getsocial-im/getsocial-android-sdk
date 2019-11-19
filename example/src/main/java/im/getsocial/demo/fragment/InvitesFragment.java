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
import android.content.DialogInterface;
import android.widget.FrameLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import im.getsocial.demo.adapter.MenuItem;
import im.getsocial.demo.dialog.ReferralDataDialog;
import im.getsocial.demo.utils.EditTextWOCopyPaste;
import im.getsocial.demo.utils.PixelUtils;
import im.getsocial.demo.utils.UserIdentityUtils;
import im.getsocial.sdk.Callback;
import im.getsocial.sdk.GetSocial;
import im.getsocial.sdk.GetSocialException;
import im.getsocial.sdk.invites.FetchReferralDataCallback;
import im.getsocial.sdk.invites.ReferralData;
import im.getsocial.sdk.invites.ReferralUser;
import im.getsocial.sdk.invites.ReferralUsersQuery;
import im.getsocial.sdk.invites.ReferredUser;
import im.getsocial.sdk.ui.GetSocialUi;
import im.getsocial.sdk.ui.invites.InviteUiCallback;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

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
				new MenuItem.Builder("Check Referred Users (OLD)")
						.withAction(new MenuItem.Action() {
							@Override
							public void execute() {
								checkReferredUsersOld();
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
						.build(),
				new MenuItem.Builder("Check Referrer Users")
						.withAction(new MenuItem.Action() {
							@Override
							public void execute() {
								checkReferrerUsers();
							}
						})
						.build(),
				navigationListItem("Set referrer", SetReferrerFragment.class)
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
			public void onFailure(GetSocialException exception) {
				showAlert("Referral Data", "Error: " + exception.getMessage());
			}
		});
	}

	private void checkReferredUsersOld() {
		GetSocial.getReferredUsers(new Callback<List<ReferredUser>>() {
			@Override
			public void onSuccess(List<ReferredUser> result) {
				if (result.size() > 0) {
					String message = "";
					for (ReferredUser referredUser : result) {
						message += formatReferredUserInfo(referredUser) + ", ";
					}
					message = message.substring(0, message.length() - 2);
					showAlert("Referred Users", message);
				} else {
					showAlert("Referred Users", "No referred users.");
				}
			}

			@Override
			public void onFailure(GetSocialException exception) {
				showAlert("Referred Users", "Error: " + exception.getMessage());
			}
		});
	}

	private void checkReferredUsers() {
		showReferrerEvenDialog(new ReferrerEventDialogCallback() {
			@Override
			public void onSelect(String event) {
				final ReferralUsersQuery query = event.isEmpty() ? ReferralUsersQuery.allUsers() : ReferralUsersQuery.usersForEvent(event);

				GetSocial.getReferredUsers(query, new Callback<List<ReferralUser>>() {
					@Override
					public void onSuccess(List<ReferralUser> result) {
						if (result.size() > 0) {
							final StringBuilder messageBuilder = new StringBuilder();
							for (ReferralUser referredUser : result) {
								messageBuilder.append(formatReferralUserInfo(referredUser)).append(", ");
							}
							String message = messageBuilder.toString();
							message = message.substring(0, message.length() - 2);
							showAlert("Referred Users", message);
						} else {
							showAlert("Referred Users", "No referred users.");
						}
					}

					@Override
					public void onFailure(GetSocialException exception) {
						showAlert("Referred Users", "Error: " + exception.getMessage());
					}
				});
			}
		});
	}

	private void checkReferrerUsers() {
		showReferrerEvenDialog(new ReferrerEventDialogCallback() {
			@Override
			public void onSelect(String event) {
				final ReferralUsersQuery query = event.isEmpty() ? ReferralUsersQuery.allUsers() : ReferralUsersQuery.usersForEvent(event);

				GetSocial.getReferrerUsers(query, new Callback<List<ReferralUser>>() {
					@Override
					public void onSuccess(List<ReferralUser> result) {
						if (result.size() > 0) {
							final StringBuilder messageBuilder = new StringBuilder();
							for (ReferralUser referredUser : result) {
								messageBuilder.append(formatReferralUserInfo(referredUser)).append(", ");
							}
							String message = messageBuilder.toString();
							message = message.substring(0, message.length() - 2);
							showAlert("Referrer Users", message);
						} else {
							showAlert("Referrer Users", "No referrer users.");
						}
					}

					@Override
					public void onFailure(GetSocialException exception) {
						showAlert("Referrer Users", "Error: " + exception.getMessage());
					}
				});
			}
		});
	}

	private void showReferrerEvenDialog(ReferrerEventDialogCallback onSelect) {
		final EditTextWOCopyPaste eventName = new EditTextWOCopyPaste(getContext());
		eventName.setLongClickable(false);

		final int _8dp = PixelUtils.dp2px(getContext(), 8);
		FrameLayout frameLayout = new FrameLayout(getContext());
		frameLayout.setPadding(_8dp, _8dp, _8dp, _8dp);
		frameLayout.addView(eventName);

		eventName.setSelection(eventName.getText().length());
		eventName.setContentDescription("event_name");

		new AlertDialog.Builder(getContext()).setView(frameLayout).setTitle("Referrer Event")
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(final DialogInterface dialogInterface, int which) {
						onSelect.onSelect(eventName.getText().toString());
					}
				}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int which) {
						dialogInterface.cancel();
					}
				}).create().show();
	}

	private void createInviteUrl() {
		GetSocial.createInviteLink(null, new Callback<String>() {
			@Override
			public void onSuccess(String result) {
				showAlert("Invite link", result);
			}

			@Override
			public void onFailure(GetSocialException exception) {
				showAlert("Failed to create invite link", "Error: " + exception.getMessage());
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

	private static String formatReferralUserInfo(ReferralUser referralUser) {
		return String.format("%s(date=%s; event=%s, data=%s)",
				referralUser.getDisplayName(),
				DateFormat.getDateTimeInstance().format(new Date(referralUser.getEventDate() * 1000)),
				referralUser.getEvent(),
				referralUser.getEventData()
		);
	}

	private interface ReferrerEventDialogCallback {
		void onSelect(String event);
	}
	//endregion

}
