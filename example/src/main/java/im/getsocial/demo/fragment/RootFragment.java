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

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import androidx.annotation.Nullable;
import im.getsocial.demo.adapter.MenuItem;
import im.getsocial.demo.dependencies.DependenciesContainer;
import im.getsocial.demo.dependencies.components.NotificationsManager;
import im.getsocial.demo.utils.Console;
import im.getsocial.sdk.Communities;
import im.getsocial.sdk.GetSocial;
import im.getsocial.sdk.communities.FriendsQuery;
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
						navigationListItem("Activities", ActivitiesFragment.class),
						navigationListItem("Topics", TopicsSearchFragment.class),
						navigationListItem("Tags", TagsListFragment.class),
//						navigationListItem("Groups", GroupsFragment.class),
						navigationListItem("Users", UsersSearchFragment.class),
						navigationListItem("Users by IDs", TestUsersByIdFragment.class),
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
						navigationListItem("Custom Analytics Events", CustomAnalyticsEventsFragment.class)
		);
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

	//endregion
}
