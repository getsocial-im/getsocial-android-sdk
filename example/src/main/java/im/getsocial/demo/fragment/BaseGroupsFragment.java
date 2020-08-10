package im.getsocial.demo.fragment;

import android.app.AlertDialog;
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
import im.getsocial.sdk.Callback;
import im.getsocial.sdk.Communities;
import im.getsocial.sdk.FailureCallback;
import im.getsocial.sdk.GetSocial;
import im.getsocial.sdk.common.PagingQuery;
import im.getsocial.sdk.common.PagingResult;
import im.getsocial.sdk.communities.ActivitiesQuery;
import im.getsocial.sdk.communities.CommunitiesAction;
import im.getsocial.sdk.communities.FollowQuery;
import im.getsocial.sdk.communities.GetSocialActivity;
import im.getsocial.sdk.communities.Group;
import im.getsocial.sdk.communities.GroupsQuery;
import im.getsocial.sdk.media.MediaAttachment;
import im.getsocial.sdk.ui.UiAction;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public abstract class BaseGroupsFragment extends BaseSearchFragment<GroupsQuery, Group> {

	private static final List<UiAction> FORBIDDEN_FOR_ANONYMOUS = Arrays.asList(UiAction.LIKE_ACTIVITY, UiAction.LIKE_COMMENT, UiAction.POST_ACTIVITY, UiAction.POST_COMMENT);

	@Override
	protected void load(final PagingQuery<GroupsQuery> query, final Callback<PagingResult<Group>> success, final FailureCallback failure) {
//		Communities.getGroups(query, success, failure);
	}

	@Override
	protected BaseSearchAdapter<? extends ViewHolder> createAdapter() {
		return new BaseSearchAdapter<ViewHolder>() {
			@NonNull
			@Override
			public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
				final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_topic, parent, false);
				return new GroupViewHolder(view);
			}
		};
	}

	public class GroupViewHolder extends ViewHolder {
		@BindView(R.id.topic_title)
		TextView _title;

		@BindView(R.id.topic_dates)
		TextView _dates;

		@BindView(R.id.topic_description)
		TextView _description;

		@BindView(R.id.topic_avatar)
		ImageView _avatar;

		boolean _isFollowing;

		GroupViewHolder(final View view) {
			super(view);
		}

		@Override
		protected void bind(final View itemView) {
			ButterKnife.bind(this, itemView);
		}

		@OnClick(R.id.actions)
		void openActions() {
			final ActionDialog dialog = new ActionDialog(getContext());
			dialog.addAction(new ActionDialog.Action(_item.getFollowersCount() + " Followers") {
				@Override
				public void execute() {
					openFollowers();
				}
			});
			dialog.addAction(new ActionDialog.Action("Feed") {
				@Override
				public void execute() {
					openFeed();
				}
			});
			dialog.addAction(new ActionDialog.Action(_isFollowing ? "Unfollow" : "Follow") {
				@Override
				public void execute() {
					follow();
				}
			});
			if (_item.getSettings().isActionAllowed(CommunitiesAction.POST)) {
				dialog.addAction(new ActionDialog.Action("Post to topic") {
					@Override
					public void execute() {
						postToFeed();
					}
				});
			}
			dialog.addAction(new ActionDialog.Action("Update recent posts") {
				@Override
				public void execute() {
					showRecentPosts();
				}
			});
			dialog.show();
		}

		private void showRecentPosts() {
			Communities.getActivities(new PagingQuery<>(ActivitiesQuery.activitiesInTopic(_item.getId())).withLimit(50), result -> {
				final List<GetSocialActivity> getSocialActivities = result.getEntries();
				final String user = GetSocial.getCurrentUser() == null ? null : GetSocial.getCurrentUser().getId();
				for (final Iterator<GetSocialActivity> it = getSocialActivities.iterator(); it.hasNext(); ) {
					if (!it.next().getAuthor().getId().equals(user)) {
						it.remove();
					}
				}
				if (getSocialActivities.isEmpty()) {
					Toast.makeText(getContext(), "You haven't recently posted in " + _item.getTitle(), Toast.LENGTH_SHORT).show();
					return;
				}
				final String[] activityContents = new String[getSocialActivities.size()];
				for (int i = 0; i < activityContents.length; i++) {
					final GetSocialActivity activity = getSocialActivities.get(i);
					if (activity.getText() != null) {
						activityContents[i] = activity.getText();
					} else if (activity.getAttachments().size() > 0) {
						final MediaAttachment attachment = activity.getAttachments().get(0);
						if (attachment.getImageUrl() != null) {
							activityContents[i] = "[IMAGE] " + attachment.getImageUrl();
						} else {
							activityContents[i] = "[VIDEO] " + attachment.getVideoUrl();
						}
					} else {
						activityContents[i] = activity.toString();
					}
				}
				new AlertDialog.Builder(getContext())
								.setItems(activityContents, (dialog, which) -> {
									dialog.dismiss();
									String activityId = getSocialActivities.get(which).getId();

									addContentFragment(PostActivityFragment.updateActivity(activityId));
								}).show();
			}, exception -> _log.logErrorAndToast("Failed to load activities, error: " + exception.getMessage()));
		}

		void openFeed() {
//			ActivityFeedViewBuilder.create(ActivitiesQuery.activitiesInGroup(_item.getId()))
//							.setActionListener(_activityListener.dependencies().actionListener())
//							.setViewStateListener(new ViewStateListener() {
//								@Override
//								public void onOpen() {
//									_log.logInfoAndToast("Feed open for " + _item.getTitle());
//								}
//
//								@Override
//								public void onClose() {
//									_log.logInfoAndToast("Feed closed for " + _item.getTitle());
//								}
//							})
//							.setUiActionListener((action, pendingAction) -> {
//								final String actionDescription = action.name().replace("_", " ").toLowerCase();
//								_log.logInfoAndToast("User is going to " + actionDescription);
//								if (GetSocial.getCurrentUser().isAnonymous() && FORBIDDEN_FOR_ANONYMOUS.contains(action)) {
//									showAuthorizeUserDialogForPendingAction(actionDescription, pendingAction);
//								} else {
//									pendingAction.proceed();
//								}
//							})
//							.setAvatarClickListener(user -> {
//								_log.logInfoAndToast("User avatar clicked:" + user);
//								if (user.isApp()) {
//									return;
//								}
//								showUserActionDialog(user);
//							})
//							.setMentionClickListener(mention -> {
//								_log.logInfoAndToast("User clicked: " + mention);
//								getUserAndShowActionDialog(mention);
//							})
//							.setTagClickListener(tag -> _log.logInfoAndToast("Tag clicked: " + tag))
//							.show();
		}

		void postToFeed() {
			addContentFragment(PostActivityFragment.postToGroup(_item.getId()));
		}

		void follow() {
			final String groupId = _item.getId();

			if (_isFollowing) {
				Communities.unfollow(FollowQuery.groups(groupId), result -> {
					if (_item.getId().equals(groupId)) {
						_isFollowing = false;
					}
				}, error -> Toast.makeText(getContext(), error.toString(), Toast.LENGTH_SHORT).show());
			} else {
				Communities.follow(FollowQuery.groups(groupId), result -> {
					if (_item.getId().equals(groupId)) {
						_isFollowing = true;
					}
				}, error -> Toast.makeText(getContext(), error.toString(), Toast.LENGTH_SHORT).show());
			}
		}

		void openFollowers() {
			final GroupFollowersFragment fragment = GroupFollowersFragment.create(_item.getId(), _item.getTitle());
			addContentFragment(fragment);
		}

		@Override
		protected void invalidate() {
			_isFollowing = _item.isFollower();
			_title.setText(_item.getTitle());
			_description.setText(_item.getDescription());
			final String avatar = _item.getAvatarUrl();
			if (avatar == null || avatar.isEmpty()) {
				_avatar.setImageResource(R.drawable.avatar_topic);
			} else {
				Picasso.with(getContext()).load(avatar).into(_avatar);
			}
			String date = DateFormat.getDateTimeInstance().format(new Date(_item.getCreatedAt() * 1000));
			if (_item.getCreatedAt() != _item.getUpdatedAt() && _item.getUpdatedAt() != 0) {
				date += " (" + DateFormat.getDateTimeInstance().format(new Date(_item.getUpdatedAt() * 1000)) + ")";
			}
			_dates.setText(date);
		}
	}
}
