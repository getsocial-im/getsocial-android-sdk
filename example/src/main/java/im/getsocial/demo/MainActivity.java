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

package im.getsocial.demo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import com.facebook.CallbackManager;
import com.vk.sdk.VKSdk;
import im.getsocial.demo.dependencies.DependenciesContainer;
import im.getsocial.demo.dependencies.components.NotificationsManager;
import im.getsocial.demo.dialog.CurrentUserInfoDialog;
import im.getsocial.demo.dialog.ReferralDataDialog;
import im.getsocial.demo.dialog.UserInfoDialog;
import im.getsocial.demo.fragment.BaseFragment;
import im.getsocial.demo.fragment.ChatMessagesFragment;
import im.getsocial.demo.fragment.ConsoleFragment;
import im.getsocial.demo.fragment.FriendsFragment;
import im.getsocial.demo.fragment.HasFragmentTag;
import im.getsocial.demo.fragment.HasTitle;
import im.getsocial.demo.fragment.RootFragment;
import im.getsocial.demo.plugin.FacebookSharePlugin;
import im.getsocial.demo.plugin.InstagramStoriesPlugin;
import im.getsocial.demo.plugin.KakaoInvitePlugin;
import im.getsocial.demo.plugin.VKInvitePlugin;
import im.getsocial.demo.ui.PickActionView;
import im.getsocial.demo.ui.UserInfoView;
import im.getsocial.demo.utils.CompatibilityUtils;
import im.getsocial.demo.utils.Console;
import im.getsocial.demo.utils.SimpleLogger;
import im.getsocial.sdk.Communities;
import im.getsocial.sdk.GetSocial;
import im.getsocial.sdk.Invites;
import im.getsocial.sdk.Notifications;
import im.getsocial.sdk.actions.Action;
import im.getsocial.sdk.actions.ActionDataKeys;
import im.getsocial.sdk.actions.ActionListener;
import im.getsocial.sdk.actions.ActionTypes;
import im.getsocial.sdk.communities.CurrentUser;
import im.getsocial.sdk.communities.FriendsQuery;
import im.getsocial.sdk.communities.OnCurrentUserChangedListener;
import im.getsocial.sdk.communities.UserId;
import im.getsocial.sdk.communities.UserIdList;
import im.getsocial.sdk.communities.UsersQuery;
import im.getsocial.sdk.invites.InviteChannelIds;
import im.getsocial.sdk.json.serializer.Getson;
import im.getsocial.sdk.notifications.Notification;
import im.getsocial.sdk.notifications.NotificationButton;
import im.getsocial.sdk.notifications.NotificationContext;
import im.getsocial.sdk.notifications.NotificationStatus;
import im.getsocial.sdk.notifications.OnNotificationClickedListener;
import im.getsocial.sdk.notifications.OnNotificationReceivedListener;
import im.getsocial.sdk.ui.GetSocialUi;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements BaseFragment.ActivityListener, OnCurrentUserChangedListener, ActionListener, OnNotificationClickedListener, OnNotificationReceivedListener {

	private static final String KEY_APP_SESSION = "GetSocial_AppSession_Key";
	protected final CallbackManager _facebookCallbackManager = CallbackManager.Factory.create();
	private final Map<String, String> _demoAppSessionData = new HashMap<>();
	protected SimpleLogger _log;
	protected VKInvitePlugin _vkInvitePlugin;
	private ViewContainer _viewContainer;
	private DependenciesContainer _dependenciesContainer;
	private String _chatId = null;

	@Override
	protected void onCreate(@Nullable final Bundle savedInstanceState) {
		_dependenciesContainer = new DependenciesContainer() {

			private final NotificationsManager _notificationsManager = new NotificationsManager(getApplicationContext());

			@Override
			public NotificationsManager notificationsManager() {
				return _notificationsManager;
			}

			@Override
			public ActionListener actionListener() {
				return MainActivity.this;
			}

			@Override
			public OnNotificationClickedListener notificationHandler() {
				return MainActivity.this;
			}
		};
		super.onCreate(savedInstanceState);
		_log = new SimpleLogger(this, getClass().getSimpleName());

		setContentView(R.layout.activity_main);
		_viewContainer = new ViewContainer(this);

		if (savedInstanceState == null) {
			addRootFragment();
		}

		_vkInvitePlugin = new VKInvitePlugin(this);

		setupGetSocial();
	}

	protected void addRootFragment() {
		addContentFragment(new RootFragment());
	}

	@Override
	protected void onRestoreInstanceState(final Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		if (savedInstanceState.containsKey(KEY_APP_SESSION)) {
			final Map<String, String> map = (Map<String, String>) savedInstanceState.getSerializable(KEY_APP_SESSION);
			_demoAppSessionData.putAll(map);
		}
	}

	@Override
	public void onSaveInstanceState(final Bundle outState, final PersistableBundle outPersistentState) {
		outState.putSerializable(KEY_APP_SESSION, new HashMap<>(_demoAppSessionData));

		super.onSaveInstanceState(outState, outPersistentState);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		if (item.getItemId() == R.id.action_console) {
			openConsole();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		_facebookCallbackManager.onActivityResult(requestCode, resultCode, data);
		VKSdk.onActivityResult(requestCode, resultCode, data, _vkInvitePlugin);
	}

	@Override
	public void onBackPressed() {
		if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
			finish();
		} else {
			super.onBackPressed();
		}
	}

	@Override
	protected void onNewIntent(final Intent intent) {
		super.onNewIntent(intent);

		// setIntent is needed to call in case your Activity launch mode is set to
		// singleTop or singleTask, so in GetSocial SDK gets the
		// last instance of Intent that brought Activity to foreground
		setIntent(intent);
	}

	@Override
	protected void onResume() {
		super.onResume();
		_viewContainer.updateView();
	}

	@Nullable
	private Fragment getActiveFragment() {
		if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
			return null;
		}
		final String tag = getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 1).getName();

		return getSupportFragmentManager().findFragmentByTag(tag);
	}

	private void openConsole() {
		addContentFragment(new ConsoleFragment());
	}

	protected void setupGetSocial() {
		Console.logInfo(getDemoAppInfo());
		Notifications.setOnNotificationClickedListener(this);
		Notifications.setOnNotificationReceivedListener(this);
		GetSocial.addOnCurrentUserChangedListener(this);
		GetSocial.addOnInitializeListener(() -> {
			invalidateUi();
			final RootFragment rootFragment = findRootFragment();
			if (rootFragment != null) {
				rootFragment.invalidateList();
			}
			if (this._chatId != null) {
				openChat(this._chatId);
				this._chatId = null;
			}
		});
		Invites.setReferralDataListener(referralData -> ReferralDataDialog.showReferralData(getSupportFragmentManager(), referralData));
		Notifications.setOnTokenReceivedListener(deviceToken ->
						Console.logInfo(String.format("Push token: %s", deviceToken))
		);
		registerCustomInvitesChannelPlugins();
	}

	@Override
	public void onNotificationClicked(final Notification notification, final NotificationContext context) {
		if (context.getActionButtonId() == null) {
			if (ActionTypes.ADD_FRIEND.equals(notification.getAction().getType())) {
				showAddFriendDialog(notification);
				return;
			}
			if (ActionTypes.OPEN_CHAT.equals(notification.getAction().getType())) {
				GetSocialUi.closeView();
				String chatId = notification.getAction().getData().get(ActionDataKeys.OpenChat.CHAT_ID);
				if (GetSocial.isInitialized()) {
					openChat(chatId);
				} else {
					this._chatId = chatId;
				}
				return;
			}

			handleAction(notification.getAction());
		} else {
			handleCustomAction(notification, context.getActionButtonId());
		}
	}

	@Override
	public void onNotificationReceived(final Notification notification) {
		if (ActionTypes.ADD_FRIEND.equals(notification.getAction().getType())) {
			showAddFriendDialog(notification);
		}
		if (ActionTypes.OPEN_CHAT.equals(notification.getAction().getType())) {
			GetSocialUi.closeView();
			String chatId = notification.getAction().getData().get(ActionDataKeys.OpenChat.CHAT_ID);
			if (GetSocial.isInitialized()) {
				openChat(chatId);
			} else {
				this._chatId = chatId;
			}
		}
		Toast.makeText(MainActivity.this, notification.getText(), Toast.LENGTH_SHORT).show();
	}

	private void openChat(final String chatId) {
		ChatMessagesFragment fragment = ChatMessagesFragment.openChat(chatId);
		addContentFragment(fragment);
	}

	private void showAddFriendDialog(final Notification notification) {
		new AlertDialog.Builder(this)
						.setTitle(notification.getTitle())
						.setMessage(notification.getText())
						.setPositiveButton("Confirm", (dialog, which) -> {
							dialog.dismiss();
							handleCustomAction(notification, NotificationButton.CONSUME_ACTION);
						})
						.setNegativeButton("Delete", (dialog, which) -> {
							dialog.dismiss();
							handleCustomAction(notification, NotificationButton.IGNORE_ACTION);
						})
						.setNeutralButton("Dismiss", (dialog, which) -> dialog.dismiss())
						.create()
						.show();
	}

	private boolean handleCustomAction(final Notification notification, final String actionId) {
		final Action action = notification.getAction();
		if (actionId.equals(NotificationButton.CONSUME_ACTION)) {
			if (ActionTypes.ADD_FRIEND.equals(action.getType())) {
				final String userId = action.getData().get(ActionDataKeys.AddFriend.USER_ID);
				final String userName = action.getData().get(PickActionView.KEY_USER_NAME);
				addFriend(userId, userName);
			} else {
				GetSocial.handle(action);
			}
			_dependenciesContainer.notificationsManager().setStatus(notification.getId(), NotificationStatus.CONSUMED);
		} else {
			_dependenciesContainer.notificationsManager().setStatus(notification.getId(), NotificationStatus.IGNORED);
		}
		return false;
	}

	private void addFriend(final String userId, final String userName) {
		Communities.addFriends(
						UserIdList.create(userId),
						result -> Toast.makeText(MainActivity.this, userName + " is now your friend!", Toast.LENGTH_SHORT).show(),
						error -> Toast.makeText(MainActivity.this, "Failed to add " + userName + ", error: " + error, Toast.LENGTH_SHORT).show()
		);
	}

	protected void registerCustomInvitesChannelPlugins() {
		Invites.registerPlugin(new KakaoInvitePlugin(), InviteChannelIds.KAKAO);
		Invites.registerPlugin(new FacebookSharePlugin(this, _facebookCallbackManager), InviteChannelIds.FACEBOOK);
		Invites.registerPlugin(_vkInvitePlugin, InviteChannelIds.VK);
		Invites.registerPlugin(new InstagramStoriesPlugin(), InviteChannelIds.INSTAGRAM_STORIES);
	}

	private void showUserInfoDialog(final String userId) {
		Communities.getUser(
						UserId.create(userId),
						publicUser -> UserInfoDialog.show(getSupportFragmentManager(), publicUser),
						error -> _log.logErrorAndToast("Failed to get user: " + error.getMessage())
		);
	}

	protected String getDemoAppInfo() {
		return String.format(Locale.getDefault(), "GetSocial Android Demo\nSDK v%s. Build v%d", GetSocial.getSdkVersion(), BuildConfig.VERSION_CODE);
	}

	private void showUserDetails() {
		if (GetSocial.isInitialized()) {
			CurrentUserInfoDialog.show(getSupportFragmentManager());
		}
	}

	private void copyUserIdToClipboard() {
		final String userId = GetSocial.getCurrentUser().getId();

		final ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
		final ClipData clip = ClipData.newPlainText("GetSocial User ID", userId);
		clipboard.setPrimaryClip(clip);
		vibrate();
		Toast.makeText(this, "Copied " + userId + " to clipboard.", Toast.LENGTH_LONG).show();
	}

	private void vibrate() {
		final Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

		if (vibrator == null) {
			return;
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
		} else {
			vibrator.vibrate(500);
		}
	}

	private RootFragment findRootFragment() {
		return findFragment("root");
	}

	private <T extends Fragment> T findFragment(final String tag) {
		return (T) getSupportFragmentManager().findFragmentByTag(tag);
	}

	//region ActivityListener

	@Override
	public void invalidateUi() {
		_viewContainer.updateView();
	}

	@Override
	public void addContentFragment(final Fragment fragment) {
		String tag = "";
		if (fragment instanceof HasFragmentTag) {
			tag = ((HasFragmentTag) fragment).getFragmentTag();
		}
		getSupportFragmentManager().beginTransaction()
						.replace(R.id.content, fragment, tag)
						.addToBackStack(tag)
						.commitAllowingStateLoss();
	}

	@Override
	public void putSessionValue(final String key, @Nullable final String value) {
		_demoAppSessionData.put(key, value);
	}

	@Override
	@Nullable
	public String getSessionValue(final String key) {
		return _demoAppSessionData.get(key);
	}

	@Override
	public DependenciesContainer dependencies() {
		return _dependenciesContainer;
	}
	//endregion

	//region GetSocial listeners

	@Override
	public void onUserChanged(final CurrentUser newUser) {
		_viewContainer._userInfoView.updateView(newUser);
		Communities.getFriendsCount(FriendsQuery.ofUser(UserId.currentUser()), friendsCount -> {
			putSessionValue(FriendsFragment.KEY_FRIENDS_COUNT, String.valueOf(friendsCount));
			final RootFragment rootFragment = findRootFragment();
			if (rootFragment != null) {
				rootFragment.invalidateList();
			}
		}, error -> Console.logError(error.getMessage()));
		dependencies().notificationsManager().sync();
	}

	@Override
	public void handleAction(final Action action) {
		_log.logInfoAndToast("Action invoked: " + action);
		switch (action.getType()) {
			case ActionTypes.OPEN_PROFILE:
				final String userId = action.getData().get(ActionDataKeys.OpenProfile.USER_ID);
				showUserInfoDialog(userId);
				return;
			case "custom":
				_log.logInfo("Received custom action:" + action.getData());
				return;
		}
		GetSocial.handle(action);
	}
	//endregion

	class ViewContainer {

		@BindView(R.id.toolbar)
		Toolbar _toolbar;
		@BindView(R.id.textViewVersion)
		TextView _versionTextView;
		@BindView(R.id.userInfoView)
		UserInfoView _userInfoView;

		private final Context _context;

		public ViewContainer(final Activity activity) {
			_context = activity;
			ButterKnife.bind(this, activity);

			setSupportActionBar(_toolbar);

			getSupportFragmentManager().addOnBackStackChangedListener(this::updateView);
		}

		@OnClick(R.id.userInfoView)
		void onUserInfoClicked() {
			showUserDetails();
		}

		@OnLongClick(R.id.userInfoView)
		boolean onUserInfoLongClicked() {
			copyUserIdToClipboard();
			return true;
		}

		@SuppressLint("ResourceAsColor")
		private void updateView() {
			_versionTextView.setText(getDemoAppInfo());

			if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
				_toolbar.setNavigationIcon(null);
				_userInfoView.setVisibility(View.VISIBLE);
				_userInfoView.updateView(GetSocial.getCurrentUser());
			} else {
				_userInfoView.setVisibility(View.GONE);

				final Fragment fragment = getActiveFragment();
				if (fragment instanceof HasTitle) {
					final HasTitle hasTitle = (HasTitle) fragment;
					_toolbar.setTitle(hasTitle.getTitle());
				}

				final Drawable navigationIcon = CompatibilityUtils.getDrawable(_context, R.drawable.ic_menu_back);
				final Drawable wrappedNavigationIcon = DrawableCompat.wrap(navigationIcon);
				DrawableCompat.setTint(wrappedNavigationIcon, getResources().getColor(R.color.primary_text));
				_toolbar.setNavigationIcon(navigationIcon);

				_toolbar.setNavigationOnClickListener(
								view -> onBackPressed()
				);
			}
		}
	}
}
