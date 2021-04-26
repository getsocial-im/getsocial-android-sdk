package im.getsocial.demo.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.squareup.picasso.Picasso;
import im.getsocial.demo.R;
import im.getsocial.demo.Utils;
import im.getsocial.demo.dialog.action_dialog.ActionDialog;
import im.getsocial.sdk.Callback;
import im.getsocial.sdk.Communities;
import im.getsocial.sdk.ErrorCode;
import im.getsocial.sdk.FailureCallback;
import im.getsocial.sdk.GetSocial;
import im.getsocial.sdk.common.PagingQuery;
import im.getsocial.sdk.common.PagingResult;
import im.getsocial.sdk.communities.ActivitiesQuery;
import im.getsocial.sdk.communities.CommunitiesAction;
import im.getsocial.sdk.communities.FollowQuery;
import im.getsocial.sdk.communities.GetSocialActivity;
import im.getsocial.sdk.communities.Topic;
import im.getsocial.sdk.communities.TopicsQuery;
import im.getsocial.sdk.communities.UserId;
import im.getsocial.sdk.media.MediaAttachment;
import im.getsocial.sdk.ui.CustomErrorMessageProvider;
import im.getsocial.sdk.ui.UiAction;
import im.getsocial.sdk.ui.ViewStateListener;
import im.getsocial.sdk.ui.communities.ActivityFeedViewBuilder;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class TopicsSearchFragment extends BaseSearchFragment<TopicsQuery, Topic> {

	private static final List<UiAction> FORBIDDEN_FOR_ANONYMOUS = Arrays.asList(UiAction.LIKE_ACTIVITY, UiAction.LIKE_COMMENT, UiAction.POST_ACTIVITY, UiAction.POST_COMMENT);

	private boolean _myTopicsOnly;
	private String _userId;
	private String _userName;

	static Fragment followedBy(final String userId, final String userName) {
		final TopicsSearchFragment fragment = new TopicsSearchFragment();
		final Bundle args = new Bundle();
		args.putString("user", userId);
		args.putString("name", userName);
		fragment.setArguments(args);
		return fragment;
	}

	@Nullable
	@Override
	public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
		_userId = getArguments() == null ? null : getArguments().getString("user");
		_userName = getArguments() == null ? null : getArguments().getString("name");
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
		if (_userId == null) {
			menu.add(Menu.NONE, 0x42, Menu.NONE, _myTopicsOnly ? "Show all" : "Followed by me");
		}
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(final android.view.MenuItem item) {
		if (item.getItemId() == 0x42) {
			filter();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		_query.setVisibility(_userId == null ? View.VISIBLE : View.GONE);
	}

	private void filter() {
		_myTopicsOnly = !_myTopicsOnly;
		getActivity().invalidateOptionsMenu();
		_query.setVisibility(_myTopicsOnly ? View.GONE : View.VISIBLE);
		loadItems();
	}

	@Override
	protected BaseSearchAdapter<? extends ViewHolder> createAdapter() {
		return new BaseSearchAdapter<ViewHolder>() {
			@NonNull
			@Override
			public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
				final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_topic, parent, false);
				return new TopicViewHolder(view);
			}
		};
	}

	@Override
	protected void load(final PagingQuery<TopicsQuery> query, final Callback<PagingResult<Topic>> success, final FailureCallback failure) {
		Communities.getTopics(query, success, failure);
	}

	@Override
	protected TopicsQuery createQuery(final String query) {
		if (_userId != null) {
			return TopicsQuery.followedByUser(UserId.create(_userId));
		}
		if (_myTopicsOnly) {
			return TopicsQuery.followedByUser(UserId.currentUser());
		}
		return TopicsQuery.find(query);
	}

	@Override
	public String getFragmentTag() {
		if (_userId != null) {
			return "topics_" + _userId;
		}
		if (_myTopicsOnly) {
			return "my_topics";
		}
		return "topics";
	}

	@Override
	public String getTitle() {
		if (_userId != null) {
			return "Topics of " + _userName;
		}
		if (_myTopicsOnly) {
			return "My Topics";
		}

		return "Topics";
	}

	public class TopicViewHolder extends ViewHolder {
		@BindView(R.id.topic_title)
		TextView _title;

		@BindView(R.id.topic_dates)
		TextView _dates;

		@BindView(R.id.topic_description)
		TextView _description;

		@BindView(R.id.topic_avatar)
		ImageView _avatar;

		boolean _isFollowing;

		TopicViewHolder(final View view) {
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
					openFeed(false);
				}
			});
			dialog.addAction(new ActionDialog.Action("Activities created by Me") {
				@Override
				public void execute() {
					openFeed(true);
				}
			});
			dialog.addAction(new ActionDialog.Action(_isFollowing ? "Unfollow" : "Follow") {
				@Override
				public void execute() {
					followTopic();
				}
			});
			if (_item.getSettings().isActionAllowed(CommunitiesAction.POST)) {
				dialog.addAction(new ActionDialog.Action("Post to topic") {
					@Override
					public void execute() {
						postToTopic();
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
			final ActivitiesQuery query = ActivitiesQuery.activitiesInTopic(_item.getId()).byUser(UserId.currentUser());
			final PagingQuery<ActivitiesQuery> pagingQuery = new PagingQuery<>(query).withLimit(5);
			Communities.getActivities(pagingQuery, result -> {
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
			}, exception -> {
				_log.logErrorAndToast("Failed to load activities, error: " + exception.getMessage());
			});
		}

		void openFeed(boolean currentUser) {
			ActivitiesQuery query = ActivitiesQuery.activitiesInTopic(_item.getId());
			if (currentUser) {
				query = query.byUser(UserId.currentUser());
			}
			ActivityFeedViewBuilder builder = ActivityFeedViewBuilder.create(query)
							.setActionListener(_activityListener.dependencies().actionListener())
							.setViewStateListener(new ViewStateListener() {
								@Override
								public void onOpen() {
									_log.logInfoAndToast("Feed open for " + _item.getTitle());
								}

								@Override
								public void onClose() {
									_log.logInfoAndToast("Feed closed for " + _item.getTitle());
								}
							})
							.setUiActionListener((action, pendingAction) -> {
								final String actionDescription = action.name().replace("_", " ").toLowerCase();
								_log.logInfoAndToast("User is going to " + actionDescription);
								if (GetSocial.getCurrentUser().isAnonymous() && FORBIDDEN_FOR_ANONYMOUS.contains(action)) {
									showAuthorizeUserDialogForPendingAction(actionDescription, pendingAction);
								} else {
									pendingAction.proceed();
								}
							})
							.setAvatarClickListener(user -> {
								_log.logInfoAndToast("User avatar clicked:" + user);
								if (user.isApp()) {
									return;
								}
								showUserActionDialog(user);
							})
							.setMentionClickListener(mention -> {
								_log.logInfoAndToast("User clicked: " + mention);
								getUserAndShowActionDialog(mention);
							})
							.setTagClickListener(tag -> _log.logInfoAndToast("Tag clicked: " + tag));
							if (Utils.isCustomErrorMessageEnabled(getContext())) {
								builder.setCustomErrorMessageProvider(new CustomErrorMessageProvider() {
									@Override
									public String onError(int errorCode, String errorMessage) {
										if (errorCode == ErrorCode.ACTIVITY_REJECTED) {
											return "Be careful what you say :)";
										}
										return errorMessage;
									}
								});
							}
							builder.show();
		}

		void postToTopic() {
			addContentFragment(PostActivityFragment.postToTopic(_item.getId()));
		}

		void followTopic() {
			final String topicId = _item.getId();

			if (_isFollowing) {
				Communities.unfollow(FollowQuery.topics(topicId), result -> {
					if (_item.getId().equals(topicId)) {
						_isFollowing = false;
					}
				}, error -> Toast.makeText(getContext(), error.toString(), Toast.LENGTH_SHORT).show());
			} else {
				Communities.follow(FollowQuery.topics(topicId), result -> {
					if (_item.getId().equals(topicId)) {
						_isFollowing = true;
					}
				}, error -> Toast.makeText(getContext(), error.toString(), Toast.LENGTH_SHORT).show());
			}
		}

		void openFollowers() {
			final TopicFollowersFragment fragment = TopicFollowersFragment.create(_item.getId(), _item.getTitle());
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
