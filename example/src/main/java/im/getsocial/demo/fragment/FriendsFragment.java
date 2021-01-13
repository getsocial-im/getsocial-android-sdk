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
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.squareup.picasso.Picasso;
import im.getsocial.demo.R;
import im.getsocial.demo.utils.CircleTransform;
import im.getsocial.sdk.Callback;
import im.getsocial.sdk.Communities;
import im.getsocial.sdk.FailureCallback;
import im.getsocial.sdk.GetSocialError;
import im.getsocial.sdk.common.PagingQuery;
import im.getsocial.sdk.common.PagingResult;
import im.getsocial.sdk.common.SimplePagingQuery;
import im.getsocial.sdk.communities.FriendsQuery;
import im.getsocial.sdk.communities.SuggestedFriend;
import im.getsocial.sdk.communities.User;
import im.getsocial.sdk.communities.UserId;
import im.getsocial.sdk.communities.UserIdList;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by orestsavchak on 2/17/17.
 */
public class FriendsFragment extends BaseUsersListFragment<FriendsQuery> {

	public static final String KEY_FRIENDS_COUNT = "GetSocial_FriendsCount_Key";

	@Override
	public String getFragmentTag() {
		return "friends";
	}

	@Override
	public String getTitle() {
		return "Friends";
	}

	@Override
	protected void load(final PagingQuery<FriendsQuery> query, final Callback<PagingResult<User>> success, final FailureCallback failure) {
		Communities.getFriends(query, success, failure);
	}

	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
		menu.add(Menu.NONE, 0x42, Menu.NONE, "Suggest Friends");
		menu.add(Menu.NONE, 0x43, Menu.NONE, "Add Friend");
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		_query.setVisibility(View.GONE);
	}

	@Override
	public boolean onOptionsItemSelected(final android.view.MenuItem item) {
		if (item.getItemId() == 0x42) {
			suggestFriends();
			return true;
		}
		if (item.getItemId() == 0x43) {
			showAddFriendDialog();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected FriendsQuery createQuery(final String query) {
		return FriendsQuery.ofUser(UserId.currentUser());
	}

	private void showAddFriendDialog() {
		final EditText input = new EditText(getContext());
		new AlertDialog.Builder(getContext())
						.setTitle("Add friend")
						.setView(input)
						.setPositiveButton("Add", (dialog, which) -> {
							String userId = input.getText().toString();
							Communities.addFriends(
											UserIdList.create(userId),
											integer -> Toast.makeText(getContext(), userId + " is now your friend!", Toast.LENGTH_SHORT).show(),
											error -> _log.logErrorAndToast("Failed to add friend: " + error)
							);
						})
						.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
						.show();
	}

	private void suggestFriends() {
		showLoading("Loading suggested friends", "Wait please...");
		Communities.getSuggestedFriends(SimplePagingQuery.simple(10), result -> {
			hideLoading();
			if (result.getEntries().isEmpty()) {
				Toast.makeText(getContext(), "No more suggested friends available", Toast.LENGTH_SHORT).show();
			} else {
				showSuggestedFriends(result.getEntries());
			}
		}, error -> {
			hideLoading();
			_log.logErrorAndToast("Failed to get suggested friends: " + error);
		});
	}

	private void showSuggestedFriends(final List<SuggestedFriend> suggestedFriends) {
		final ListView friendsView = new ListView(getContext());
		final ArrayAdapter arrayAdapter = new SuggestedFriendsAdapter(friendsView, getContext(), suggestedFriends);
		friendsView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		friendsView.setAdapter(arrayAdapter);
		friendsView.setOnItemClickListener((parent, view, position, id) -> arrayAdapter.notifyDataSetChanged());
		new AlertDialog.Builder(getContext())
						.setTitle("Suggested friends")
						.setView(friendsView)
						.setPositiveButton("Add Friends", (dialog, which) -> {
							final SparseBooleanArray checked = friendsView.getCheckedItemPositions();
							final List<SuggestedFriend> friendsToAdd = new ArrayList<>(checked.size());
							for (int i = 0; i < arrayAdapter.getCount(); i++) {
								if (checked.get(i)) {
									friendsToAdd.add(suggestedFriends.get(i));
								}
							}
							addFriends(friendsToAdd);
							dialog.dismiss();
						})
						.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
						.show();
	}

	private void addFriends(final List<SuggestedFriend> friendsToAdd) {
		if (friendsToAdd.isEmpty()) {
			return;
		}
		showLoading("Adding friends", "Wait please...");
		final List<String> userIds = new ArrayList<>();
		for (final SuggestedFriend friend : friendsToAdd) {
			userIds.add(friend.getId());
		}
		Communities.addFriends(UserIdList.create(userIds),
						integer -> {
							hideLoading();
							onFriendsUpdated();
						},
						error -> {
							hideLoading();
							Toast.makeText(getContext(), "Failed to add friends: " + error, Toast.LENGTH_SHORT).show();
						});
	}

	private void onFriendsUpdated() {
		loadItems();
	}

	private void removeFriend(final User user) {
		showLoading("Removing friend", "Wait...");
		Communities.removeFriends(UserIdList.create(user.getId()), numberOfFriends -> {
			Toast.makeText(getContext(), "User " + user.getDisplayName() + " was removed from your friends list.", Toast.LENGTH_SHORT).show();
			onFriendsUpdated();
		}, this::onFailure);
	}

	private void onFailure(final GetSocialError error) {
		Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
		hideLoading();
	}

	class FriendsAdapter extends ArrayAdapter<User> {

		FriendsAdapter(final Context context, final List<User> objects) {
			super(context, 0, objects);
		}

		@NonNull
		@Override
		public View getView(final int position, @Nullable View convertView, final ViewGroup parent) {
			final ViewHolder holder;
			if (convertView == null) {
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_friend, null);
				convertView.setTag(holder = new ViewHolder(convertView));
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.setUser(getItem(position));

			return convertView;
		}

		class ViewHolder {

			@BindView(R.id.user_avatar)
			ImageView _avatar;
			@BindView(R.id.user_name)
			TextView _userName;
			private User _user;

			ViewHolder(final View view) {
				ButterKnife.bind(this, view);
			}

			void setUser(final User user) {
				_user = user;
				populate();
			}

			private void populate() {
				_userName.setText(_user.getDisplayName());
				if (TextUtils.isEmpty(_user.getAvatarUrl())) {
					Picasso.with(getContext())
									.load(R.drawable.avatar_default)
									.transform(new CircleTransform())
									.into(_avatar);
					return;
				}
				Picasso.with(getContext())
								.load(_user.getAvatarUrl())
								.placeholder(R.drawable.avatar_default)
								.transform(new CircleTransform())
								.into(_avatar);
			}

			@OnClick(R.id.remove_friend_btn)
			void removeFriend() {
				FriendsFragment.this.removeFriend(_user);
			}

//			@OnClick(R.id.message_friend_btn)
//			void openChatDialog() {
//				final Bundle bundle = new Bundle();
//				bundle.putString(ChatMessagesFragment.KEY_RECIPIENT_ID, _user.getId());
//				bundle.putString(ChatMessagesFragment.KEY_RECIPIENT_NAME, _userName.getText().toString());
//
//				final ChatMessagesFragment chatFragment = new ChatMessagesFragment();
//				chatFragment.setArguments(bundle);
//
//				addContentFragment(chatFragment);
//			}
		}

	}

	class SuggestedFriendsAdapter extends ArrayAdapter<SuggestedFriend> {

		private final ListView _listView;

		SuggestedFriendsAdapter(final ListView listView, @NonNull final Context context, @NonNull final List<SuggestedFriend> objects) {
			super(context, 0, objects);
			_listView = listView;
		}

		@NonNull
		@Override
		public View getView(final int position, @Nullable View convertView, @NonNull final ViewGroup parent) {
			final ViewHolder holder;
			if (convertView == null) {
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_suggested_friend, null);
				convertView.setTag(holder = new ViewHolder(convertView));
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			final SuggestedFriend user = getItem(position);
			holder.setUser(user);
			if (_listView.getCheckedItemPositions().get(position)) {
				convertView.setBackgroundColor(getResources().getColor(R.color.grey));
			} else {
				convertView.setBackgroundColor(getResources().getColor(R.color.transparent));
			}

			return convertView;
		}

		class ViewHolder {

			@BindView(R.id.user_avatar)
			ImageView _avatar;
			@BindView(R.id.user_name)
			TextView _userName;
			@BindView(R.id.mutual_friends)
			TextView _mutualFriends;
			private SuggestedFriend _user;

			ViewHolder(final View view) {
				ButterKnife.bind(this, view);
			}

			void setUser(final SuggestedFriend user) {
				_user = user;
				populate();
			}

			private void populate() {
				_userName.setText(_user.getDisplayName());
				_mutualFriends.setText(String.valueOf(_user.getMutualFriendsCount()));
				if (TextUtils.isEmpty(_user.getAvatarUrl())) {
					Picasso.with(getContext())
									.load(R.drawable.avatar_default)
									.transform(new CircleTransform())
									.into(_avatar);
					return;
				}
				Picasso.with(getContext())
								.load(_user.getAvatarUrl())
								.placeholder(R.drawable.avatar_default)
								.transform(new CircleTransform())
								.into(_avatar);
			}
		}
	}

}
