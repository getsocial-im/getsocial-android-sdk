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
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.facebook.login.LoginManager;

import im.getsocial.demo.MainActivity;
import im.getsocial.demo.R;
import im.getsocial.demo.dependencies.DependenciesContainer;
import im.getsocial.demo.dialog.action_dialog.ActionDialog;
import im.getsocial.demo.utils.EditTextWOCopyPaste;
import im.getsocial.demo.utils.ImagePicker;
import im.getsocial.demo.utils.SimpleLogger;
import im.getsocial.sdk.Communities;
import im.getsocial.sdk.CompletionCallback;
import im.getsocial.sdk.ErrorCode;
import im.getsocial.sdk.GetSocial;
import im.getsocial.sdk.GetSocialError;
import im.getsocial.sdk.communities.ActivitiesQuery;
import im.getsocial.sdk.communities.ConflictUser;
import im.getsocial.sdk.communities.FollowQuery;
import im.getsocial.sdk.communities.Identity;
import im.getsocial.sdk.communities.User;
import im.getsocial.sdk.communities.UserId;
import im.getsocial.sdk.communities.UserIdList;
import im.getsocial.sdk.communities.UserUpdate;
import im.getsocial.sdk.ui.GetSocialUi;
import im.getsocial.sdk.ui.MentionClickListener;
import im.getsocial.sdk.ui.UiAction;
import im.getsocial.sdk.ui.communities.ActivityFeedViewBuilder;

import javax.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public abstract class BaseFragment extends Fragment implements HasTitle, HasFragmentTag {

	protected static final String CUSTOM_PROVIDER = "custom";
	protected static final List<String> FACEBOOK_PERMISSIONS = Arrays.asList("email", "user_friends", "public_profile");
	protected SimpleLogger _log;

	protected ActivityListener _activityListener;
	private ProgressDialog _currentProgressDialog;
	private ImagePicker _imagePicker;

	protected void inject(final DependenciesContainer dependencies) {
		//
	}

	@Override
	public void onActivityResult(final int requestCode, final int resultCode, @Nullable final Intent data) {
		if (!(_imagePicker != null && _imagePicker.onActivityResult(requestCode, resultCode, data))) {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	@Override
	public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
		if (!(_imagePicker != null && _imagePicker.onRequestPermissionResult(requestCode, permissions, grantResults))) {
			super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		}
	}

	@Override
	public void onAttach(final Context context) {
		super.onAttach(context);

		final Activity activity = getActivity();

		_log = new SimpleLogger(activity, getClass().getSimpleName());

		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try {
			_activityListener = (ActivityListener) activity;
		} catch (final ClassCastException exception) {
			throw new ClassCastException(activity.toString()
							+ " must implement ActivityListener");
		}

		inject(_activityListener.dependencies());
	}

	@Override
	public void onViewStateRestored(@Nullable final Bundle savedInstanceState) {
		if (_imagePicker != null) {
			_imagePicker.onViewStateRestored(savedInstanceState);
		}
		super.onViewStateRestored(savedInstanceState);
	}

	@Override
	public void onSaveInstanceState(final Bundle outState) {
		if (_imagePicker != null) {
			_imagePicker.onSaveInstanceState(outState);
		}
		super.onSaveInstanceState(outState);
	}

	protected void pickImageFromDevice(final int requestCode) {
		_imagePicker = new ImagePicker(this, requestCode);
		_imagePicker.pickImageFromDevice(createImagePickerCallback());
	}

	protected void pickVideoFromDevice(final int requestCode) {
		_imagePicker = new ImagePicker(this, requestCode);
		_imagePicker.pickVideoFromDevice(createImagePickerCallback());
	}

	private ImagePicker.Callback createImagePickerCallback() {
		return new ImagePicker.Callback() {
			@Override
			public void onImageChosen(final Uri imageUri, final int requestCode) {
				onImagePickedFromDevice(imageUri, requestCode);
				_imagePicker = null;
			}

			@Override
			public void onVideoChosen(final Uri videoUri, final int requestCode) {
				onVideoPickedFromDevice(videoUri, requestCode);
			}

			@Override
			public void onCancel() {
				_imagePicker = null;
			}
		};
	}

	protected void onImagePickedFromDevice(final Uri imageUri, final int requestCode) {
		// Override in children, if you need to handle image picking
	}

	protected void onVideoPickedFromDevice(final Uri videoUri, final int requestCode) {
		// Override in children, if you need to handle video picking
	}

	protected void showLoading(final String title, final String text) {
		if (_currentProgressDialog == null) {
			_currentProgressDialog = ProgressDialog.show(getContext(), title, text);
		} else {
			hideLoading();
			showLoading(title, text);
		}
	}

	protected void hideLoading() {
		if (_currentProgressDialog != null) {
			_currentProgressDialog.dismiss();
			_currentProgressDialog = null;
		}
	}

	protected void showAuthorizeUserDialogForPendingAction(final String actionDescription, final UiAction.Pending pendingAction) {
		new AlertDialog.Builder(getContext()).setTitle("Authorize to " + actionDescription)
						.setNegativeButton("Cancel", (dialog, which) ->
										_log.logInfoAndToast("Can not " + actionDescription + " without authorization.")
						)
						.setItems(new CharSequence[] {"Facebook", "Custom"}, (dialog, which) -> {
							switch (which) {
								case 0:
									addFacebookUserIdentity(pendingAction::proceed);
									break;
								case 1:
									addCustomUserIdentity(pendingAction::proceed);
									break;
								default:
									break;
							}
							dialog.dismiss();
						})
						.show();
	}


	protected void showAlert(final String title, final String message) {
		new AlertDialog.Builder(getContext())
						.setTitle(title)
						.setMessage(message)
						.setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.dismiss())
						.show();
	}

	protected void addContentFragment(final Fragment fragment) {
		_activityListener.addContentFragment(fragment);
	}

	protected void addFacebookUserIdentity(final CompletionCallback completionCallback) {
		final CompletionCallback wrapped = () -> {
			completionCallback.onSuccess();
			setFacebookInfo();
		};
		final AddUserIdentityOutcomeCallback addUserIdentityOutcomeCallback = new AddUserIdentityOutcomeCallback() {
			@Override
			public void onSuccess() {
				_log.logInfoAndToast("Added Facebook identity successfully");
				wrapped.onSuccess();
			}

			@Override
			public void onFailure(final GetSocialError exception) {
				_log.logInfoAndToast("Adding Facebook identity failed : " + exception);
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
				wrapped.onSuccess();
			}
		};

		final AccessTokenTracker accessTokenTracker = new AccessTokenTracker() {
			@Override
			protected void onCurrentAccessTokenChanged(final AccessToken oldAccessToken, @Nullable final AccessToken newAccessToken) {

				if (newAccessToken == null) {
					addUserIdentityOutcomeCallback.onFailure(new GetSocialError(ErrorCode.UNKNOWN, "Facebook SDK did not provide an AccessToken"));
					return;
				}

				stopTracking();

				final String tokenString = newAccessToken.getToken();

				addIdentity(Identity.facebook(tokenString), addUserIdentityOutcomeCallback);
			}
		};
		accessTokenTracker.startTracking();
		LoginManager.getInstance().logInWithReadPermissions(getActivity(), FACEBOOK_PERMISSIONS);
	}

	protected void setFacebookInfo() {
		final Profile fbProfile = Profile.getCurrentProfile();

		final String userDisplayName = fbProfile.getFirstName() + " " + fbProfile.getLastName();
		final String profileImageUri = fbProfile.getProfilePictureUri(250, 250).toString();

		final UserUpdate userDetails = new UserUpdate();
		userDetails.updateAvatarUrl(profileImageUri);
		userDetails.updateDisplayName(userDisplayName);

		GetSocial.getCurrentUser().updateDetails(userDetails, () -> _activityListener.invalidateUi(), error -> {
			_log.logErrorAndToast(error.getMessage());
		});
	}

	protected void addCustomUserIdentity(final CompletionCallback completionCallback) {
		final String providerId = CUSTOM_PROVIDER;

		final LayoutInflater layoutInflater = LayoutInflater.from(getContext());
		final View view = layoutInflater.inflate(R.layout.dialog_custom_identity, null, false);

		final EditTextWOCopyPaste userIdEditText = view.findViewById(R.id.user_id);
		final EditTextWOCopyPaste tokenEditText = view.findViewById(R.id.user_token);

		final UserManagementFragment.AddUserIdentityOutcomeCallback addUserIdentityOutcomeCallback = new UserManagementFragment.AddUserIdentityOutcomeCallback() {
			@Override
			public void onSuccess() {
				_log.logInfoAndToast("Added custom user identity successfully");
				completionCallback.onSuccess();
			}

			@Override
			public void onFailure(final GetSocialError exception) {
				_log.logInfoAndToast("Adding custom user identity failed : " + exception);
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

		final AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
						.setView(view)
						.setPositiveButton("Add", (dialog, which) -> {
											String userId = userIdEditText.getText().toString().trim();
											String token = tokenEditText.getText().toString().trim();
											addIdentity(Identity.custom(providerId, userId, token), addUserIdentityOutcomeCallback);
										}
						)
						.setNegativeButton("Cancel", (dialog, which) -> {
						});
		builder.show();
	}

	protected void addIdentity(final Identity identity, final UserManagementFragment.AddUserIdentityOutcomeCallback addUserIdentityOutcomeCallback) {
		GetSocial.getCurrentUser().addIdentity(
						identity,
						addUserIdentityOutcomeCallback::onSuccess,
						conflictUser -> resolveConflictIdentities(conflictUser, addUserIdentityOutcomeCallback, identity),
						addUserIdentityOutcomeCallback::onFailure
		);
	}

	protected void resolveConflictIdentities(final ConflictUser conflictUser, final AddUserIdentityOutcomeCallback addUserIdentityOutcomeCallback, final Identity identity) {
		showDialogToSolveIdentityConflict(conflictUser, new ConflictResolution() {
			@Override
			public void resolveWithCurrentUser() {
				addUserIdentityOutcomeCallback.onConflictResolvedWithCurrent();
			}

			@Override
			public void resolveWithConflictUser() {
				GetSocial.switchUser(identity, new SafeCompletionCallback() {
					@Override
					public void onSafeSuccess() {
						addUserIdentityOutcomeCallback.onConflictResolvedWithRemote();
					}
				}, addUserIdentityOutcomeCallback::onFailure);
			}
		});
	}

	protected void showDialogToSolveIdentityConflict(final ConflictUser conflictUser, final ConflictResolution conflictResolution) {
		new AlertDialog.Builder(getContext())
						.setTitle("Conflict")
						.setMessage(String.format("The new identity is already linked to another user(%s). Which one do you want to continue using?", conflictUser.getDisplayName()))
						.setPositiveButton("Remote", (dialog, whichButton) ->
										conflictResolution.resolveWithConflictUser()
						)
						.setNegativeButton("Current", (dialog, whichButton) ->
										conflictResolution.resolveWithCurrentUser()
						)
						.show();
	}

	protected void getUserAndShowActionDialog(final String mention) {
		if (mention.equals(MentionClickListener.APP_SHORTCUT)) {
			Toast.makeText(getContext(), "Application mention clicked.", Toast.LENGTH_SHORT).show();
			return;
		}
		Communities.getUser(
						UserId.create(mention),
						this::showUserActionDialog,
						error -> _log.logErrorAndToast("Failed to get user: " + error.getMessage())
		);
	}

	protected void showUserActionDialog(final User user) {
		final ActionDialog actionDialog = new ActionDialog(getContext())
						.addAction(new ActionDialog.Action("Show User Feed") {
							@Override
							public void execute() {
								ActivityFeedViewBuilder
												.create(ActivitiesQuery.feedOf(UserId.create(user.getId())))
												.show();
							}
						});
		if (user.isApp()) {
			Toast.makeText(getContext(), "App user avatar clicked", Toast.LENGTH_SHORT).show();
		} else if (isCurrentUser(user)) {
			actionDialog.show();
		} else {
			checkIfFriendsAndShowDialog(user, actionDialog.setTitle("User " + user.getDisplayName()));
		}
	}

	private boolean isCurrentUser(final User user) {
		return user.getId().equals(GetSocial.getCurrentUser().getId());
	}

	private void checkIfFriendsAndShowDialog(final User user, final ActionDialog actionDialog) {
		final boolean[] sync = new boolean[1];
		final Runnable showOnLoad = () -> {
			if (sync[0]) {
				actionDialog.show();
			} else {
				sync[0] = true;
			}
		};
		actionDialog.addAction(new ActionDialog.Action("Open Chat") {
			@Override
			public void execute() {
				GetSocialUi.closeView();
				ChatMessagesFragment fragment = ChatMessagesFragment.chatWith(user.getId());
				addContentFragment(fragment);
			}
		});

		Communities.isFollowing(UserId.currentUser(), FollowQuery.users(UserIdList.create(user.getId())), followingMap -> {
			final boolean isFollowing = Boolean.TRUE.equals(followingMap.get(user.getId()));
			actionDialog.addAction(new ActionDialog.Action(isFollowing ? "Unfollow" : "Follow") {
				@Override
				public void execute() {
					if (isFollowing) {
						unfollow(user);
					} else {
						follow(user);
					}
				}
			});
			showOnLoad.run();
		}, error -> _log.logErrorAndToast("Failed to check if following: " + error));
		Communities.isFriend(UserId.create(user.getId()), isFriend -> {
			actionDialog.addAction(new ActionDialog.Action(isFriend ? "Remove from Friends" : "Add to Friends") {
				@Override
				public void execute() {
					if (isFriend) {
						removeFriend(user);
					} else {
						addFriend(user);
					}
				}
			});
			showOnLoad.run();
		}, error -> _log.logErrorAndToast("Failed to check if friend: " + error));
	}

	private void addFriend(final User user) {
		Communities.addFriends(
						UserIdList.create(user.getId()),
						integer -> Toast.makeText(getContext(), user.getDisplayName() + " is now your friend!", Toast.LENGTH_SHORT).show(),
						error -> _log.logErrorAndToast("Failed to add friend: " + error)
		);
	}

	private void removeFriend(final User user) {
		Communities.removeFriends(
						UserIdList.create(user.getId()),
						integer -> Toast.makeText(getContext(), user.getDisplayName() + " is not your friend anymore!", Toast.LENGTH_SHORT).show(),
						error -> _log.logErrorAndToast("Failed to remove friend: " + error)
		);
	}

	private void follow(final User user) {
		Communities.follow(
						FollowQuery.users(UserIdList.create(user.getId())),
						integer -> Toast.makeText(getContext(), "Following " + user.getDisplayName(), Toast.LENGTH_SHORT).show(),
						error -> _log.logErrorAndToast("Failed to add friend: " + error)
		);
	}

	private void unfollow(final User user) {
		Communities.unfollow(
						FollowQuery.users(UserIdList.create(user.getId())),
						integer -> Toast.makeText(getContext(), "Unfollowed " + user.getDisplayName(), Toast.LENGTH_SHORT).show(),
						error -> _log.logErrorAndToast("Failed to add friend: " + error)
		);
	}

	protected byte[] getBytesFromInputStream(InputStream is) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		byte[] buffer = new byte[0xFFFF];
		for (int len = is.read(buffer); len != -1; len = is.read(buffer)) {
			os.write(buffer, 0, len);
		}
		return os.toByteArray();
	}

	protected void disconnectFromFacebook() {

		if (AccessToken.getCurrentAccessToken() == null) {
			return; // already logged out
		}

		new GraphRequest(AccessToken.getCurrentAccessToken(), "/me/permissions/", null, HttpMethod.DELETE, new GraphRequest.Callback() {
			@Override
			public void onCompleted(final GraphResponse graphResponse) {
				LoginManager.getInstance().logOut();
			}
		}).executeAsync();
	}


	public interface ActivityListener {
		void invalidateUi();

		void addContentFragment(Fragment fragment);

		void putSessionValue(String key, @Nullable String value);

		String getSessionValue(String key);

		DependenciesContainer dependencies();

		void onBackPressed();
	}

	protected interface ConflictResolution {
		void resolveWithCurrentUser();

		void resolveWithConflictUser();
	}

	protected interface AddUserIdentityOutcomeCallback {
		void onSuccess();

		void onFailure(GetSocialError exception);

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
	}
}
