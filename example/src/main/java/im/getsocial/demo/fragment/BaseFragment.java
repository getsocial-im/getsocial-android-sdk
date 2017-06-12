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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import im.getsocial.demo.R;
import im.getsocial.demo.utils.SimpleLogger;
import im.getsocial.sdk.CompletionCallback;
import im.getsocial.sdk.GetSocial;
import im.getsocial.sdk.GetSocialException;
import im.getsocial.sdk.ui.UiAction;
import im.getsocial.sdk.usermanagement.AddAuthIdentityCallback;
import im.getsocial.sdk.usermanagement.AuthIdentity;
import im.getsocial.sdk.usermanagement.ConflictUser;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

import static im.getsocial.sdk.core.exception.GetSocialExceptionAdapter.upgradeToGetSocialException;

public abstract class BaseFragment extends Fragment implements HasTitle, HasFragmentTag {

	private static final List<String> FACEBOOK_PERMISSIONS = Arrays.asList("email", "user_friends", "public_profile");

	protected static final String CUSTOM_PROVIDER = "custom";
	protected SimpleLogger _log;

	protected ActivityListener _activityListener;
	private ProgressDialog _currentProgressDialog;

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);

		Activity activity = getActivity();

		_log = new SimpleLogger(activity, getClass().getSimpleName());

		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try {
			_activityListener = (ActivityListener) activity;
		} catch (ClassCastException exception) {
			throw new ClassCastException(activity.toString()
					+ " must implement ActivityListener");
		}
	}

	protected void showLoading(String title, String text) {
		if (_currentProgressDialog == null) {
			_currentProgressDialog = ProgressDialog.show(getContext(), title, text);
		} else {
			hideLoading();
			showLoading(title, text);
		}
	}

	protected void hideLoading() {
		if (_currentProgressDialog != null) {
			_currentProgressDialog.hide();
			_currentProgressDialog = null;
		}
	}

	protected void showAuthorizeUserDialogForPendingAction(final String actionDescription, final UiAction.Pending pendingAction) {
		final CompletionCallback completionCallback = new CompletionCallback() {
			@Override
			public void onSuccess() {
				pendingAction.proceed();
			}

			@Override
			public void onFailure(GetSocialException exception) {
				_log.logErrorAndToast("You can not " + actionDescription + " because of exception during authorization: " + exception.getMessage());
			}
		};
		new AlertDialog.Builder(getContext()).setTitle("Authorize to " + actionDescription)
				.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						_log.logInfoAndToast("Can not " + actionDescription + " without authorization.");
					}
				})
				.setItems(new CharSequence[]{"Facebook", "Custom"}, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
							case 0:
								addFacebookUserIdentity(completionCallback);
								break;
							case 1:
								addCustomUserIdentity(completionCallback);
								break;
							default:
								break;
						}
						dialog.dismiss();
					}
				})
				.show();
	}


	protected void showAlert(String title, String message) {
		new AlertDialog.Builder(getContext())
				.setTitle(title)
				.setMessage(message)
				.show();
	}

	protected void addContentFragment(Fragment fragment) {
		_activityListener.addContentFragment(fragment);
	}

	protected void addFacebookUserIdentity(final CompletionCallback completionCallback) {
		final CompletionCallback wrapped = new CompletionCallback() {
			@Override
			public void onSuccess() {
				completionCallback.onSuccess();
				setFacebookDisplayName();
				setFacebookAvatar();
			}

			@Override
			public void onFailure(GetSocialException e) {
				completionCallback.onFailure(e);
			}
		};
		final AddUserIdentityOutcomeCallback addUserIdentityOutcomeCallback = new AddUserIdentityOutcomeCallback() {
			@Override
			public void onSuccess() {
				_log.logInfoAndToast("Added Facebook identity successfully");
				wrapped.onSuccess();
			}

			@Override
			public void onFailure(Throwable throwable) {
				_log.logInfoAndToast("Adding Facebook identity failed : " + throwable);
				disconnectFromFacebook();
				wrapped.onFailure(upgradeToGetSocialException(throwable));
			}

			@Override
			public void onConflictResolvedWithCurrent() {
				_log.logInfoAndToast("Conflict adding Facebook user identity resolved with current");
				disconnectFromFacebook();
			}

			@Override
			public void onConflictResolvedWithRemote() {
				_log.logInfoAndToast("Conflict adding Facebook user identity resolved with remote");
				wrapped.onSuccess();
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

	private void setFacebookDisplayName() {
		Profile fbProfile = Profile.getCurrentProfile();

		GetSocial.User.setDisplayName(fbProfile.getFirstName() + " " + fbProfile.getLastName(), new CompletionCallback() {
			@Override
			public void onSuccess() {
				_activityListener.invalidateUi();
			}

			@Override
			public void onFailure(GetSocialException exception) {
				//failed to set facebook name
			}
		});
	}

	private void setFacebookAvatar() {
		Profile fbProfile = Profile.getCurrentProfile();
		String profileImageUri = fbProfile.getProfilePictureUri(250, 250).toString();

		GetSocial.User.setAvatarUrl(profileImageUri, new CompletionCallback() {
			@Override
			public void onSuccess() {
				_activityListener.invalidateUi();
			}

			@Override
			public void onFailure(GetSocialException e) {
				// failed to set avatar url
			}
		});
	}

	protected void addCustomUserIdentity(final CompletionCallback completionCallback) {
		final String providerId = CUSTOM_PROVIDER;

		LayoutInflater layoutInflater = LayoutInflater.from(getContext());
		final View view = layoutInflater.inflate(R.layout.dialog_custom_identity, null, false);

		final EditText userdIdEditText = (EditText) view.findViewById(R.id.user_id);
		final EditText tokenEditText = (EditText) view.findViewById(R.id.user_token);

		final UserManagementFragment.AddUserIdentityOutcomeCallback addUserIdentityOutcomeCallback = new UserManagementFragment.AddUserIdentityOutcomeCallback() {
			@Override
			public void onSuccess() {
				_log.logInfoAndToast("Added custom user identity successfully");
				completionCallback.onSuccess();
			}

			@Override
			public void onFailure(Throwable throwable) {
				_log.logInfoAndToast("Adding custom user identity failed : " + throwable);
				completionCallback.onFailure(upgradeToGetSocialException(throwable));
			}

			@Override
			public void onConflictResolvedWithCurrent() {
				_log.logInfoAndToast("Conflict adding custom user identity resolved with current");
			}

			@Override
			public void onConflictResolvedWithRemote() {
				_log.logInfoAndToast("Conflict adding custom user identity resolved with remote");
				completionCallback.onSuccess();
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

	protected void addIdentity(final AuthIdentity authIdentity, final UserManagementFragment.AddUserIdentityOutcomeCallback addUserIdentityOutcomeCallback) {
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

	protected void resolveConflictIdentities(ConflictUser conflictUser, final AddUserIdentityOutcomeCallback addUserIdentityOutcomeCallback, final AuthIdentity authIdentity) {
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

	protected void showDialogToSolveIdentityConflict(final ConflictUser conflictUser, final ConflictResolution conflictResolution) {
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


	public interface ActivityListener {
		void invalidateUi();

		void addContentFragment(Fragment fragment);

		void putSessionValue(String key, @Nullable String value);

		String getSessionValue(String key);

		void onSdkReset();
	}

	protected interface ConflictResolution {
		void resolveWithCurrentUser();

		void resolveWithConflictUser();
	}

	protected interface AddUserIdentityOutcomeCallback {
		void onSuccess();

		void onFailure(Throwable throwable);

		void onConflictResolvedWithCurrent();

		void onConflictResolvedWithRemote();
	}

	protected abstract class SafeCompletionCallback implements CompletionCallback {

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
}
