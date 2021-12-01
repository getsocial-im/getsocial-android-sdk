package im.getsocial.demo.fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.squareup.picasso.Picasso;
import im.getsocial.demo.R;
import im.getsocial.demo.dialog.action_dialog.ActionDialog;
import im.getsocial.sdk.Communities;
import im.getsocial.sdk.GetSocial;
import im.getsocial.sdk.communities.ActivitiesQuery;
import im.getsocial.sdk.communities.FollowQuery;
import im.getsocial.sdk.communities.User;
import im.getsocial.sdk.communities.UserId;
import im.getsocial.sdk.communities.UserIdList;
import im.getsocial.sdk.ui.communities.ActivityFeedViewBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseUsersListFragment<Query> extends BaseSearchFragment<Query, User> {

	private Map<String, Boolean> _friends = new HashMap<>();
	private Map<String, Boolean> _followers = new HashMap<>();

	@Override
	protected BaseSearchAdapter<? extends ViewHolder> createAdapter() {
		return new BaseSearchAdapter<ViewHolder>() {
			@NonNull
			@Override
			public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
				final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_user, parent, false);
				return new UserViewHolder(view);
			}
		};
	}

	@Override
	protected void onDataChanged(final List<User> users) {
		final UserIdList list = UserIdList.create(toListString(users));
		Communities.areFriends(list, result -> _friends = new HashMap<>(result), this::onError);
		Communities.isFollowing(UserId.currentUser(), FollowQuery.users(list), result -> _followers = new HashMap<>(result), this::onError);
	}

	private List<String> toListString(final List<User> users) {
		final List<String> list = new ArrayList<>();
		for (final User user : users) {
			list.add(user.getId());
		}
		return list;
	}

	private boolean isFriend(final String userId) {
		return _friends.containsKey(userId) && _friends.get(userId);
	}

	private boolean isFollowing(final String userId) {
		return _followers.containsKey(userId) && _followers.get(userId);
	}

	public class UserViewHolder extends ViewHolder {

		@BindView(R.id.user_title)
		TextView _title;

		@BindView(R.id.user_id)
		TextView _dates;

		@BindView(R.id.user_description)
		TextView _description;

		@BindView(R.id.user_avatar)
		ImageView _avatar;

		UserViewHolder(final View view) {
			super(view);
		}

		@Override
		protected void bind(final View itemView) {
			ButterKnife.bind(this, itemView);
		}

		@OnClick(R.id.actions)
		public void openActinos() {
			final String userId = _item.getId();
			final ActionDialog dialog = new ActionDialog(getContext());
			dialog.addAction(new ActionDialog.Action("Followers") {
				@Override
				public void execute() {
					final UsersFollowersFragment fragment = UsersFollowersFragment.create(_item.getId(), _item.getDisplayName());
					addContentFragment(fragment);
				}
			});
			// if not current user
			if (!GetSocial.getCurrentUser().getId().equals(userId)) {
				dialog.addAction(new ActionDialog.Action(isFriend(userId) ? "Remove Friend" : "Add Friend") {
					@Override
					public void execute() {
						if (isFriend(userId)) {
							Communities.removeFriends(UserIdList.create(userId),
											result -> _friends.put(userId, false),
											BaseUsersListFragment.this::onError);
						} else {
							Communities.addFriends(UserIdList.create(userId),
											result -> _friends.put(userId, true),
											BaseUsersListFragment.this::onError);
						}
					}
				}).addAction(new ActionDialog.Action(isFollowing(userId) ? "Unfollow" : "Follow") {
					@Override
					public void execute() {
						if (isFollowing(userId)) {
							Communities.unfollow(FollowQuery.users(UserIdList.create(userId)),
											result -> {
												Toast.makeText(getContext(), "Not following " + _item.getDisplayName() + ", total following: " + result, Toast.LENGTH_SHORT).show();
												_followers.put(userId, false);
											},
											BaseUsersListFragment.this::onError);
						} else {
							Communities.follow(FollowQuery.users(UserIdList.create(userId)),
											result -> {
												Toast.makeText(getContext(), "Following " + _item.getDisplayName() + ", total following: " + result, Toast.LENGTH_SHORT).show();
												_followers.put(userId, true);
											},
											BaseUsersListFragment.this::onError);
						}
					}
				});
			}
			dialog.addAction(new ActionDialog.Action("User Feed") {
				@Override
				public void execute() {
					ActivityFeedViewBuilder.create(
									ActivitiesQuery.feedOf(UserId.create(_item.getId()))
					).show();
				}
			});
			dialog.addAction(new ActionDialog.Action("All By Author") {
				@Override
				public void execute() {
					ActivityFeedViewBuilder.create(
									ActivitiesQuery.everywhere().byUser(
													UserId.create(_item.getId())
									)
					).show();
				}
			});
			dialog.addAction(new ActionDialog.Action("Followed Topics") {
				@Override
				public void execute() {
					addContentFragment(TopicsSearchFragment.followedBy(_item.getId(), _item.getDisplayName()));
				}
			});
			dialog.addAction(new ActionDialog.Action("Followed Users") {
				@Override
				public void execute() {
					addContentFragment(FollowingFragment.followedBy(_item.getId(), _item.getDisplayName()));
				}
			});
			dialog.addAction(new ActionDialog.Action("Friends") {
				@Override
				public void execute() {
					addContentFragment(GenericFriendsFragment.create(_item.getId(), _item.getDisplayName()));
				}
			});
			dialog.show();
		}

		@Override
		protected void invalidate() {
			_title.setText(_item.getDisplayName());
			_description.setText(formatUserDescription(_item));
			final String avatar = _item.getAvatarUrl();
			if (avatar == null || avatar.isEmpty()) {
				_avatar.setImageResource(R.drawable.avatar_default);
			} else {
				Picasso.with(getContext()).load(avatar).into(_avatar);
			}
			_dates.setText(_item.getDisplayName());
		}

		private String formatUserDescription(final User user) {
			final StringBuilder sb = new StringBuilder();
			sb.append("Identities: ");
			for (final Map.Entry<String, String> identity : user.getIdentities().entrySet()) {
				sb.append(identity.getKey()).append("=").append(identity.getValue()).append(";");
			}
			sb.append("\nProperties: ");
			for (final Map.Entry<String, String> property : user.getPublicProperties().entrySet()) {
				sb.append(property.getKey()).append("=").append(property.getValue()).append(";");
			}
			return sb.toString();
		}

	}
}
