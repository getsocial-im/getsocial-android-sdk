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
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.Nullable;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.login.LoginManager;
import im.getsocial.demo.R;
import im.getsocial.demo.adapter.MenuItem;
import im.getsocial.demo.dependencies.DependenciesContainer;
import im.getsocial.demo.dependencies.components.NotificationsManager;
import im.getsocial.demo.dialog.action_dialog.ActionDialog;
import im.getsocial.demo.utils.Console;
import im.getsocial.sdk.Communities;
import im.getsocial.sdk.CompletionCallback;
import im.getsocial.sdk.ErrorCode;
import im.getsocial.sdk.FailureCallback;
import im.getsocial.sdk.GetSocial;
import im.getsocial.sdk.GetSocialError;
import im.getsocial.sdk.communities.FriendsQuery;
import im.getsocial.sdk.communities.Identity;
import im.getsocial.sdk.communities.UserId;

import java.util.Arrays;
import java.util.List;

public class RootFragment extends BaseListFragment implements NotificationsManager.Listener {

	private NotificationsManager _notificationsManager;

	public RootFragment() {
	}

	@Override
	protected void inject(final DependenciesContainer dependencies) {
		_notificationsManager = dependencies.notificationsManager();
	}

	@Override
	public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		_notificationsManager.addListener(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		GetSocial.addOnInitializeListener(_notificationsManager::sync);
		if (!GetSocial.isInitialized()) {
			return;
		}
		Communities.getFriendsCount(FriendsQuery.ofUser(UserId.currentUser()), friendsCount -> {
			_activityListener.putSessionValue(FriendsFragment.KEY_FRIENDS_COUNT, String.valueOf(friendsCount));
			invalidateList();
		}, error -> Console.logError(error.getMessage()));
	}

	@Override
	protected List<MenuItem> createListData() {
		return Arrays.asList(
						navigationListItem("User Management", UserManagementFragment.class, builder -> builder.withEnabledCheck(GetSocial::isInitialized)),
						navigationListItem("Friends", FriendsFragment.class, builder -> builder.withSubtitle(() -> {
							final String friendsCount = _activityListener.getSessionValue(FriendsFragment.KEY_FRIENDS_COUNT);
							final String count = TextUtils.isEmpty(friendsCount) ? "0" : friendsCount;
							return "You have " + count + " friends";
						})),
						navigationListItem("Invites", InvitesFragment.class),
						navigationListItem("Search", SearchFragment.class),
						navigationListItem("Activities", ActivitiesFragment.class),
						navigationListItem("Topics", TopicsSearchFragment.class),
						navigationListItem("Tags", TagsListFragment.class),
						navigationListItem("Labels", LabelsListFragment.class),
						navigationListItem("Groups", GroupsFragment.class),
						navigationListItem("Chats", ChatsFragment.class),
						navigationListItem("Users", UsersSearchFragment.class),
						navigationListItem("Users by IDs", TestUsersByIdFragment.class),
						navigationListItem("Blocked Users", BlockedUsersFragment.class),
						navigationListItem("Notifications", NotificationsFragment.class, builder -> builder.withSubtitle(() -> "You have " + _notificationsManager.getNewNotificationsCount() + " new notifications")),
						navigationListItem("Promo Codes", PromoCodesFragment.class),
						navigationListItem("UI Customization", UiCustomizationFragment.class, builder -> builder.withSubtitle(() -> {
							final String savedName = _activityListener.getSessionValue(UiCustomizationFragment.UI_CONFIGURATION_NAME_KEY);
							if (TextUtils.isEmpty(savedName)) {
								return "Current UI: default";
							} else {
								return "Current UI: " + savedName;
							}
						})),
						navigationListItem("Settings", SettingsFragment.class),
						navigationListItem("IAP", PurchaseFragment.class),
						navigationListItem("Custom Analytics Events", CustomAnalyticsEventsFragment.class),
						MenuItem.builder("Init with new anonymous user").withAction(() -> {
							if (GetSocial.isInitialized()) {
								_log.logInfoAndToast("Already initialized, call resetWithoutInit first");
								return;
							}
							GetSocial.init();
							GetSocial.addOnInitializeListener(() -> {
								_log.logInfoAndToast("Anonymous User logged in");
								invalidateUi();
							});
						}).withEnabledCheck(() -> !GetSocial.isInitialized()).build(),
						MenuItem.builder("Init with...").withAction(() -> loginWith())
										.withEnabledCheck(() -> !GetSocial.isInitialized()).build());

	}

	//region Presenter
	@Override
	public String getTitle() {
		return "";
	}

	@Override
	public String getFragmentTag() {
		return "root";
	}

	@Override
	public void onSync() {
		invalidateList();
	}


	private void loginWith() {
		final CompletionCallback callback = () -> {
			_log.logInfoAndToast("Successfully logged in");
			invalidateUi();
		};
		final FailureCallback failureCallback = exception -> {
			_log.logErrorAndToast(exception);
			invalidateUi();
		};
		new ActionDialog(getContext())
				.addAction(new ActionDialog.Action("Facebook") {
					@Override
					public void execute() {
						loginWithFacebook(callback, failureCallback);
					}
				})
				.addAction(new ActionDialog.Action("Custom Identity") {
					@Override
					public void execute() {
						loginWithCustom(callback, failureCallback);
					}
				})
				.addAction(new ActionDialog.Action("Trusted Identity") {
					@Override
					public void execute() {
						loginWithTrusted(callback, failureCallback);
					}
				})
				.setTitle("Login with...").show();
	}

	private void invalidateUi() {
		_activityListener.invalidateUi();
		invalidateList();
	}

	protected void loginWithFacebook(final CompletionCallback callback, final FailureCallback failureCallback) {
		final CompletionCallback wrapped = () -> {
			callback.onSuccess();
			setFacebookInfo();
		};
		final AccessTokenTracker accessTokenTracker = new AccessTokenTracker() {
			@Override
			protected void onCurrentAccessTokenChanged(final AccessToken oldAccessToken, @javax.annotation.Nullable final AccessToken newAccessToken) {

				if (newAccessToken == null) {
					failureCallback.onFailure(new GetSocialError(ErrorCode.UNKNOWN, "Facebook SDK did not provide an AccessToken"));
					return;
				}

				stopTracking();

				final String tokenString = newAccessToken.getToken();

				GetSocial.init(Identity.facebook(tokenString), wrapped, failureCallback);
			}
		};
		accessTokenTracker.startTracking();
		LoginManager.getInstance().logInWithReadPermissions(getActivity(), FACEBOOK_PERMISSIONS);
	}

	protected void loginWithCustom(final CompletionCallback callback, final FailureCallback failureCallback) {
		final String providerId = CUSTOM_PROVIDER;

		final LayoutInflater layoutInflater = LayoutInflater.from(getContext());
		final View view = layoutInflater.inflate(R.layout.dialog_custom_identity, null, false);

		final EditText userIdEditText = view.findViewById(R.id.user_id);
		final EditText tokenEditText = view.findViewById(R.id.user_token);

		final AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
						.setView(view)
						.setPositiveButton("Add", new DialogInterface.OnClickListener() {
											@Override
											public void onClick(final DialogInterface dialog, final int which) {
												final String userId = userIdEditText.getText().toString().trim();
												final String token = tokenEditText.getText().toString().trim();
												GetSocial.init(Identity.custom(providerId, userId, token), callback, failureCallback);
											}
										}
						)
						.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
											@Override
											public void onClick(final DialogInterface dialog, final int which) {

											}
										}
						);
		builder.show();
	}

	protected void loginWithTrusted(final CompletionCallback callback, final FailureCallback failureCallback) {
		final LayoutInflater layoutInflater = LayoutInflater.from(getContext());
		final View view = layoutInflater.inflate(R.layout.dialog_custom_identity, null, false);

		final EditText providerIdEditText = view.findViewById(R.id.user_id);
		final EditText tokenEditText = view.findViewById(R.id.user_token);

		final AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
				.setView(view)
				.setPositiveButton("Add", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(final DialogInterface dialog, final int which) {
								final String providerId = providerIdEditText.getText().toString().trim();
								final String token = tokenEditText.getText().toString().trim();
								GetSocial.init(Identity.custom(providerId, null, token), callback, failureCallback);
							}
						}
				)
				.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(final DialogInterface dialog, final int which) {

							}
						}
				);
		builder.show();
	}
	//endregion
}
