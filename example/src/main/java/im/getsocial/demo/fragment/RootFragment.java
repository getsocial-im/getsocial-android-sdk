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

import android.text.TextUtils;
import im.getsocial.demo.adapter.MenuItem;
import im.getsocial.demo.adapter.TextGenerator;
import im.getsocial.demo.utils.Console;
import im.getsocial.sdk.Callback;
import im.getsocial.sdk.GetSocial;
import im.getsocial.sdk.GetSocialException;

import java.util.ArrayList;
import java.util.List;

public class RootFragment extends BaseListFragment {

	public RootFragment() {
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!GetSocial.isInitialized()) {
			return;
		}
		GetSocial.User.getFriendsCount(new Callback<Integer>() {
			@Override
			public void onSuccess(Integer friendsCount) {
				_activityListener.putSessionValue(FriendsFragment.KEY_FRIENDS_COUNT, String.valueOf(friendsCount));
				invalidateList();
			}

			@Override
			public void onFailure(GetSocialException exception) {
				Console.logError(exception.getLocalizedMessage());
			}
		});
	}

	@Override
	protected List<MenuItem> createListData() {
		List<MenuItem> listData = new ArrayList<>();

		listData.add(new MenuItem.Builder("User Management")
				.withAction(new MenuItem.Action() {
					@Override
					public void execute() {
						openUserManagement();
					}
				})
				.build());

		listData.add(new MenuItem.Builder("Friends")
				.withSubtitle(new TextGenerator() {
					@Override
					public String generateText() {
						String friendsCount = _activityListener.getSessionValue(FriendsFragment.KEY_FRIENDS_COUNT);
						if (TextUtils.isEmpty(friendsCount)) {
							return "You have 0 friends";
						} else {
							return "You have " + friendsCount + " friends";
						}
					}
				})
				.withAction(new MenuItem.Action() {
					@Override
					public void execute() {
						openFriendsFragment();
					}
				})
				.build());

		listData.add(new MenuItem.Builder("Invites")
				.withAction(new MenuItem.Action() {
					@Override
					public void execute() {
						openInvites();
					}
				})
				.build());

		listData.add(new MenuItem.Builder("Activities")
				.withAction(new MenuItem.Action() {
					@Override
					public void execute() {
						openActivities();
					}
				})
				.build());

		listData.add(new MenuItem.Builder("UI Customization")
				.withSubtitle(new TextGenerator() {
					@Override
					public String generateText() {
						String savedName = _activityListener.getSessionValue(UiCustomizationFragment.UI_CONFIGURATION_NAME_KEY);
						if (TextUtils.isEmpty(savedName)) {
							return "Current UI: default";
						} else {
							return "Current UI: " + savedName;
						}
					}
				})
				.withAction(new MenuItem.Action() {
					@Override
					public void execute() {
						openUiCustomization();
					}
				})
				.build());

		listData.add(new MenuItem.Builder("Settings")
				.withAction(new MenuItem.Action() {
					@Override
					public void execute() {
						openSettings();
					}
				})
				.build());

		return listData;
	}

	//region Presenter
	protected void openUserManagement() {
		addContentFragment(new UserManagementFragment());
	}

	protected void openFriendsFragment() {
		addContentFragment(new FriendsFragment());
	}

	protected void openInvites() {
		addContentFragment(new InvitesFragment());
	}

	protected void openUiCustomization() {
		addContentFragment(new UiCustomizationFragment());
	}

	protected void openSettings() {
		addContentFragment(new SettingsFragment());
	}

	protected void openActivities() {
		addContentFragment(new ActivitiesFragment());
	}

	@Override
	public String getTitle() {
		return "";
	}

	@Override
	public String getFragmentTag() {
		return "root";
	}

	//endregion
}
