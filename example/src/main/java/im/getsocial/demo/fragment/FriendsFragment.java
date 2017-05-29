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

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.squareup.picasso.Picasso;
import im.getsocial.demo.R;
import im.getsocial.demo.utils.CircleTransform;
import im.getsocial.sdk.Callback;
import im.getsocial.sdk.GetSocial;
import im.getsocial.sdk.GetSocialException;
import im.getsocial.sdk.usermanagement.PublicUser;

import java.util.List;

/**
 * Created by orestsavchak on 2/17/17.
 */
public class FriendsFragment extends BaseFragment {

	public static final String KEY_FRIENDS_COUNT = "GetSocial_FriendsCount_Key";

	private ViewContainer _viewContainer;

	@Nullable
	private String _newFriend;

	@Override
	public String getFragmentTag() {
		return "friends";
	}

	@Override
	public String getTitle() {
		return "Friends";
	}

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_friends, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		_viewContainer = new ViewContainer(view);
	}

	private void addFriend(final String userId) {
		showLoading("Adding friend", "Wait...");
		GetSocial.User.isFriend(userId, new Callback<Boolean>() {
			@Override
			public void onSuccess(Boolean isFriend) {
				if (isFriend) {
					hideLoading();
					Toast.makeText(getContext(), "User " + userId + " is already your friend.", Toast.LENGTH_SHORT).show();
				} else {
					GetSocial.User.addFriend(userId, new Callback<Integer>() {
						@Override
						public void onSuccess(Integer numberOfFriends) {
							_viewContainer._userId.setText("");
							onFriendsUpdated();
						}

						@Override
						public void onFailure(GetSocialException exception) {
							FriendsFragment.this.onFailure(exception);
						}
					});
				}
			}

			@Override
			public void onFailure(GetSocialException exception) {
				FriendsFragment.this.onFailure(exception);
			}
		});
	}

	private void onFriendsUpdated() {
		loadFriends();
	}

	private void removeFriend(final String userId) {
		showLoading("Removing friend", "Wait...");
		GetSocial.User.removeFriend(userId, new Callback<Integer>() {
			@Override
			public void onSuccess(Integer numberOfFriends) {
				Toast.makeText(getContext(), "User " + userId + " was removed from your friends list.", Toast.LENGTH_SHORT).show();
				onFriendsUpdated();
			}

			@Override
			public void onFailure(GetSocialException exception) {
				FriendsFragment.this.onFailure(exception);
			}
		});
	}

	private void loadFriends() {
		showLoading("Loading friends", "Wait...");
		GetSocial.User.getFriends(0, 1000, new Callback<List<PublicUser>>() {
			@Override
			public void onSuccess(List<PublicUser> friends) {
				_viewContainer._friendsList.setAdapter(new FriendsAdapter(getContext(), friends));
				hideLoading();
			}

			@Override
			public void onFailure(GetSocialException exception) {
				FriendsFragment.this.onFailure(exception);
			}
		});
	}

	private void onFailure(GetSocialException exception) {
		Toast.makeText(getContext(), exception.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
		hideLoading();
	}

	public void setNewFriend(String userId) {
		_newFriend = userId;
	}

	class ViewContainer {

		@BindView(R.id.friend_id)
		EditText _userId;

		@BindView(R.id.friends_list)
		ListView _friendsList;

		ViewContainer(View view) {
			ButterKnife.bind(this, view);
			loadFriends();
		}

		@OnClick(R.id.add_friend_btn)
		void addFriend() {
			FriendsFragment.this.addFriend(_userId.getText().toString());
		}
	}

	class FriendsAdapter extends ArrayAdapter<PublicUser> {

		FriendsAdapter(Context context, List<PublicUser> objects) {
			super(context, 0, objects);
		}

		@NonNull
		@Override
		public View getView(int position, @Nullable View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_friend, null);
				convertView.setTag(holder = new ViewHolder(convertView));
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			PublicUser user = getItem(position);
			holder.setUser(user);
			if (user.getId().equals(_newFriend)) {
				convertView.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
			} else {
				convertView.setBackgroundColor(getResources().getColor(android.R.color.transparent));
			}
			return convertView;
		}

		class ViewHolder {

			private PublicUser _user;

			ViewHolder(View view) {
				ButterKnife.bind(this, view);
			}

			void setUser(PublicUser user) {
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

			@BindView(R.id.user_avatar)
			ImageView _avatar;

			@BindView(R.id.user_name)
			TextView _userName;

			@OnClick(R.id.remove_friend_btn)
			void removeFriend() {
				FriendsFragment.this.removeFriend(_user.getId());
			}
		}

	}

}
