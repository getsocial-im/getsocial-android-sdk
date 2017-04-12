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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import im.getsocial.demo.R;
import im.getsocial.demo.adapter.EnabledCheck;
import im.getsocial.demo.adapter.MenuItem;
import im.getsocial.demo.utils.PixelUtils;
import im.getsocial.demo.utils.UserIdentityUtils;
import im.getsocial.sdk.CompletionCallback;
import im.getsocial.sdk.GetSocial;
import im.getsocial.sdk.GetSocialException;
import im.getsocial.sdk.usermanagement.AddAuthIdentityCallback;
import im.getsocial.sdk.usermanagement.AuthIdentity;
import im.getsocial.sdk.usermanagement.AuthIdentityProviderIds;
import im.getsocial.sdk.usermanagement.ConflictUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UserManagementFragment extends BaseListFragment {

	private static final List<String> FACEBOOK_PERMISSIONS = Arrays.asList("email", "user_friends");
	private static final String CUSTOM_PROVIDER = "custom";

	public UserManagementFragment() {
	}

	protected void invalidateUi() {
		invalidateList();
		_activityListener.invalidateUi();
	}

	@NonNull
	protected List<MenuItem> createListData() {
		List<MenuItem> listData = new ArrayList<>();

		listData.add(new MenuItem.Builder("Change Display Name")
				.withAction(new MenuItem.Action() {
					@Override
					public void execute() {
						changeDisplayName();
					}
				})
				.build());

		listData.add(new MenuItem.Builder("Change User Avatar")
				.withAction(new MenuItem.Action() {
					@Override
					public void execute() {
						changeUserAvatar();
					}
				})
				.build());

		listData.add(new MenuItem.Builder("Add Facebook user identity")
				.withAction(new MenuItem.Action() {
					@Override
					public void execute() {
						addFacebookUserIdentity();
					}
				})
				.withEnabledCheck(new EnabledCheck() {
					@Override
					public boolean isOptionEnabled() {
						return !GetSocial.User.getAuthIdentities().containsKey(AuthIdentityProviderIds.FACEBOOK);
					}
				})
				.build());

		listData.add(new MenuItem.Builder("Add Custom user identity")
				.withAction(new MenuItem.Action() {
					@Override
					public void execute() {
						addCustomUserIdentity();
					}
				})
				.withEnabledCheck(new EnabledCheck() {
					@Override
					public boolean isOptionEnabled() {
						return !GetSocial.User.getAuthIdentities().containsKey(CUSTOM_PROVIDER);
					}
				})
				.build());

		listData.add(new MenuItem.Builder("Remove Facebook user identity")
				.withSubtitle("Log out from Facebook")
				.withAction(new MenuItem.Action() {
					@Override
					public void execute() {
						removeFacebookUserIdentity();
					}
				})
				.withEnabledCheck(new EnabledCheck() {
					@Override
					public boolean isOptionEnabled() {
						return GetSocial.User.getAuthIdentities().containsKey(AuthIdentityProviderIds.FACEBOOK);
					}
				})
				.build());

		listData.add(new MenuItem.Builder("Remove Custom user identity")
				.withAction(new MenuItem.Action() {
					@Override
					public void execute() {
						removeCustomUserIdentity();
					}
				})
				.withEnabledCheck(new EnabledCheck() {
					@Override
					public boolean isOptionEnabled() {
						return GetSocial.User.getAuthIdentities().containsKey(CUSTOM_PROVIDER);
					}
				})
				.build());
		return listData;
	}


	//region Presenter

	private void changeUserAvatar() {
		GetSocial.User.setAvatarUrl(UserIdentityUtils.getRandomAvatar(),
				new SafeCompletionCallback() {
					@Override
					public void onSafeSuccess() {
						_activityListener.invalidateUi();
						Toast.makeText(getContext(), "Avatar has been changed successfully!", Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onSafeFailure(GetSocialException exception) {
						Toast.makeText(getContext(), "Error changing avatar: \n" + exception.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
					}
				});
	}

	private void changeDisplayName() {
		final EditText displayNameInput = new EditText(getContext());
		final int _8dp = PixelUtils.dp2px(getContext(), 8);
		FrameLayout frameLayout = new FrameLayout(getContext());
		frameLayout.setPadding(_8dp, _8dp, _8dp, _8dp);
		frameLayout.addView(displayNameInput);

		displayNameInput.setText(UserIdentityUtils.getDisplayName());
		displayNameInput.setSelection(displayNameInput.getText().length());

		new AlertDialog.Builder(getContext())
				.setView(frameLayout)
				.setTitle("User Display Name")
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(final DialogInterface dialogInterface, int which) {
						GetSocial.User.setDisplayName(displayNameInput.getText().toString(),
								new SafeCompletionCallback() {
									@Override
									public void onSafeSuccess() {
										dialogInterface.dismiss();
										_activityListener.invalidateUi();
										Toast.makeText(getContext(), "Display name has been changed successfully!", Toast.LENGTH_SHORT).show();
									}

									@Override
									public void onSafeFailure(GetSocialException exception) {
										Toast.makeText(getContext(), "Error changing display name: \n" + exception.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
									}
								}
						);
					}
				})
				.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int which) {
						dialogInterface.cancel();
					}
				})
				.create()
				.show();
	}

	private void addFacebookUserIdentity() {
		final AddUserIdentityOutcomeCallback addUserIdentityOutcomeCallback = new AddUserIdentityOutcomeCallback() {
			@Override
			public void onSuccess() {
				_log.logInfoAndToast("Added Facebook identity successfully");
				invalidateUi();
			}

			@Override
			public void onFailure(Throwable throwable) {
				_log.logInfoAndToast("Adding Facebook identity failed : " + throwable);
				disconnectFromFacebook();
			}

			@Override
			public void onConflictResolvedWithCurrent() {
				_log.logInfoAndToast("Conflict adding Facebook user identity resolved with current");
				disconnectFromFacebook();
			}

			@Override
			public void onConflictResolvedWithRemote() {
				_log.logInfoAndToast("Conflict adding Facebook user identity resolved with remote");
				invalidateUi();
			}
		};

		final AccessTokenTracker accessTokenTracker = new AccessTokenTracker() {
			@Override
			protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, @Nullable AccessToken newAccessToken) {

				if (newAccessToken == null) {
					addUserIdentityOutcomeCallback.onFailure(new IllegalStateException("Facebook SDK did not provide an AccessToken"));
					return;
				}

				stopTracking();

				final String tokenString = newAccessToken.getToken();

				addIdentity(AuthIdentity.createFacebookIdentity(tokenString), addUserIdentityOutcomeCallback);
			}
		};
		accessTokenTracker.startTracking();
		LoginManager.getInstance().logInWithReadPermissions(getActivity(), FACEBOOK_PERMISSIONS);
	}

	private void addCustomUserIdentity() {

		final String providerId = CUSTOM_PROVIDER;

		LayoutInflater layoutInflater = LayoutInflater.from(getContext());
		final View view = layoutInflater.inflate(R.layout.dialog_custom_identity, null, false);

		final EditText userdIdEditText = (EditText) view.findViewById(R.id.user_id);
		final EditText tokenEditText = (EditText) view.findViewById(R.id.user_token);

		final AddUserIdentityOutcomeCallback addUserIdentityOutcomeCallback = new AddUserIdentityOutcomeCallback() {
			@Override
			public void onSuccess() {
				_log.logInfoAndToast("Added custom user identity successfully");
				invalidateUi();
			}

			@Override
			public void onFailure(Throwable throwable) {
				_log.logInfoAndToast("Adding custom user identity failed : " + throwable);
			}

			@Override
			public void onConflictResolvedWithCurrent() {
				_log.logInfoAndToast("Conflict adding custom user identity resolved with current");
			}

			@Override
			public void onConflictResolvedWithRemote() {
				_log.logInfoAndToast("Conflict adding custom user identity resolved with remote");
				invalidateUi();
			}
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
				.setView(view)
				.setPositiveButton("Add", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								String userId = userdIdEditText.getText().toString().trim();
								String token = tokenEditText.getText().toString().trim();
								addIdentity(AuthIdentity.createCustomIdentity(providerId, userId, token), addUserIdentityOutcomeCallback);
							}
						}
				)
				.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {

							}
						}
				);
		builder.show();
	}

	private void addIdentity(final AuthIdentity authIdentity, final AddUserIdentityOutcomeCallback addUserIdentityOutcomeCallback) {
		GetSocial.User.addAuthIdentity(authIdentity,
				new AddAuthIdentityCallback() {
					@Override
					public void onComplete() {
						addUserIdentityOutcomeCallback.onSuccess();
					}

					@Override
					public void onFailure(GetSocialException exception) {
						addUserIdentityOutcomeCallback.onFailure(exception);
					}

					@Override
					public void onConflict(ConflictUser conflictUser) {
						resolveConflictIdentities(conflictUser, addUserIdentityOutcomeCallback, authIdentity);
					}
				}
		);
	}

	private void resolveConflictIdentities(ConflictUser conflictUser, final AddUserIdentityOutcomeCallback addUserIdentityOutcomeCallback, final AuthIdentity authIdentity) {
		showDialogToSolveIdentityConflict(conflictUser, new ConflictResolution() {
			@Override
			public void resolveWithCurrentUser() {
				addUserIdentityOutcomeCallback.onConflictResolvedWithCurrent();
			}

			@Override
			public void resolveWithConflictUser() {
				GetSocial.User.switchUser(authIdentity, new SafeCompletionCallback() {
					@Override
					public void onSafeSuccess() {
						addUserIdentityOutcomeCallback.onConflictResolvedWithRemote();
					}

					@Override
					public void onSafeFailure(GetSocialException exception) {
						addUserIdentityOutcomeCallback.onFailure(exception);
					}
				});
			}
		});
	}

	private void removeFacebookUserIdentity() {
		removeUserIdentity(AuthIdentityProviderIds.FACEBOOK);

		disconnectFromFacebook();
	}

	private void removeCustomUserIdentity() {
		removeUserIdentity(CUSTOM_PROVIDER);
	}

	//endregion

	//region helpers

	private void showDialogToSolveIdentityConflict(final ConflictUser conflictUser, final ConflictResolution conflictResolution) {
		new AlertDialog.Builder(getContext())
				.setTitle("Conflict")
				.setMessage(String.format("The new identity is already linked to another user(%s). Which one do you want to continue using?", conflictUser.getDisplayName()))
				.setPositiveButton("Remote", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								conflictResolution.resolveWithConflictUser();
							}
						}
				)
				.setNegativeButton("Current", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								conflictResolution.resolveWithCurrentUser();
							}
						}
				)
				.show();
	}

	private void removeUserIdentity(final String providerId) {
		GetSocial.User.removeAuthIdentity(
				providerId,
				new SafeCompletionCallback() {
					@Override
					public void onSafeSuccess() {
						invalidateUi();
						_log.logInfoAndToast(String.format("Successfully removed user identity '%s'", providerId));
					}

					@Override
					public void onSafeFailure(GetSocialException exception) {
						_log.logErrorAndToast(String.format("Failed to remove user identity '%s', error: %s", providerId, exception.getMessage()));
					}

				}
		);
	}

	protected void disconnectFromFacebook() {

		if (AccessToken.getCurrentAccessToken() == null) {
			return; // already logged out
		}

		new GraphRequest(AccessToken.getCurrentAccessToken(), "/me/permissions/", null, HttpMethod.DELETE, new GraphRequest.Callback() {
			@Override
			public void onCompleted(GraphResponse graphResponse) {
				LoginManager.getInstance().logOut();
			}
		}).executeAsync();
	}

	@Override
	public String getTitle() {
		return "User Management";
	}

	@Override
	public String getFragmentTag() {
		return "usermanagement";
	}

	private interface ConflictResolution {
		void resolveWithCurrentUser();

		void resolveWithConflictUser();
	}

	private interface AddUserIdentityOutcomeCallback {
		void onSuccess();

		void onFailure(Throwable throwable);

		void onConflictResolvedWithCurrent();

		void onConflictResolvedWithRemote();
	}


	private abstract class SafeCompletionCallback implements CompletionCallback {

		@Override
		public void onSuccess() {
			if (getContext() != null) {
				onSafeSuccess();
			}

		}

		protected abstract void onSafeSuccess();

		@Override
		public void onFailure(GetSocialException exception) {
			if (getContext() != null) {
				onSafeFailure(exception);
			}
		}

		protected abstract void onSafeFailure(GetSocialException exception);
	}
	//endregion
}
