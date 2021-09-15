package im.getsocial.demo.fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
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
import im.getsocial.sdk.communities.AddGroupMembersQuery;
import im.getsocial.sdk.communities.CommunitiesAction;
import im.getsocial.sdk.communities.Group;
import im.getsocial.sdk.communities.GroupsQuery;
import im.getsocial.sdk.communities.JoinGroupQuery;
import im.getsocial.sdk.communities.MemberStatus;
import im.getsocial.sdk.communities.RemoveGroupMembersQuery;
import im.getsocial.sdk.communities.RemoveGroupsQuery;
import im.getsocial.sdk.communities.Role;
import im.getsocial.sdk.communities.UserId;
import im.getsocial.sdk.communities.UserIdList;
import im.getsocial.sdk.ui.CustomErrorMessageProvider;
import im.getsocial.sdk.ui.UiAction;
import im.getsocial.sdk.ui.ViewStateListener;
import im.getsocial.sdk.ui.communities.ActivityFeedViewBuilder;

public abstract class BaseGroupsFragment extends BaseSearchFragment<GroupsQuery, Group> {

	private static final List<UiAction> FORBIDDEN_FOR_ANONYMOUS = Arrays.asList(UiAction.LIKE_ACTIVITY, UiAction.LIKE_COMMENT, UiAction.POST_ACTIVITY, UiAction.POST_COMMENT);

	@Override
	protected void load(final PagingQuery<GroupsQuery> query, final Callback<PagingResult<Group>> success, final FailureCallback failure) {
		Communities.getGroups(query, success, failure);
	}

	@Override
	protected BaseSearchAdapter<? extends ViewHolder> createAdapter() {
		return new BaseSearchAdapter<ViewHolder>() {
			@NonNull
			@Override
			public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
				final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_group, parent, false);
				return new GroupViewHolder(view);
			}
		};
	}

	public class GroupViewHolder extends ViewHolder {
		@BindView(R.id.group_title)
		TextView _title;

		@BindView(R.id.group_dates)
		TextView _dates;

		@BindView(R.id.group_description)
		TextView _description;

		@BindView(R.id.group_avatar)
		ImageView _avatar;

		@BindView(R.id.group_score)
		TextView _score;

		@BindView(R.id.group_member_role)
		TextView _member_role;

		@BindView(R.id.group_member_status)
		TextView _member_status;

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
			MemberStatus status = null;
			Role role = null;
			if (_item.getMembership() != null) {
				status = _item.getMembership().getStatus();
				role = _item.getMembership().getRole();
			}
			dialog.addAction(new ActionDialog.Action("Details") {
				@Override
				public void execute() {
					showAlert("Details", _item.toString());
				}
			});
			if (!_item.getSettings().isPrivate() || status == MemberStatus.MEMBER) {
				dialog.addAction(new ActionDialog.Action("Feed UI") {
					@Override
					public void execute() {
						openFeed(false);
					}
				});
				dialog.addAction(new ActionDialog.Action("Activities") {
					@Override
					public void execute() {
						openActivities();
					}
				});
				dialog.addAction(new ActionDialog.Action("Activities created by Me") {
					@Override
					public void execute() {
						openFeed(true);
					}
				});
				dialog.addAction(new ActionDialog.Action("Activities with Polls") {
					@Override
					public void execute() {
						openPolls();
					}
				});
				dialog.addAction(new ActionDialog.Action("Announcements with Polls") {
					@Override
					public void execute() {
						openAnnouncementsPolls();
					}
				});
			}
			if (status == MemberStatus.MEMBER) {
				dialog.addAction(new ActionDialog.Action("Show Members") {
					@Override
					public void execute() {
						showGroupMembers();
					}
				});
				if (_item.getSettings().isActionAllowed(CommunitiesAction.POST)) {
					dialog.addAction(new ActionDialog.Action("Post to group") {
						@Override
						public void execute() {
							postToFeed();
						}
					});
					dialog.addAction(new ActionDialog.Action("Create poll") {
						@Override
						public void execute() {
							createPoll();
						}
					});
				}
			}
			if (role == Role.ADMIN || role == Role.OWNER) {
				dialog.addAction(new ActionDialog.Action("Edit") {
					@Override
					public void execute() {
						addContentFragment(CreateGroupFragment.updateGroup(_item.getId()));
					}
				});
				dialog.addAction(new ActionDialog.Action("Delete") {
					@Override
					public void execute() {
						deleteGroup();
					}
				});
			}
			if (_item.getMembership() == null) {
				dialog.addAction(new ActionDialog.Action("Join") {
					@Override
					public void execute() {
						join();
					}
				});
			}
			if (status == MemberStatus.INVITATION_PENDING) {
				dialog.addAction(new ActionDialog.Action("Approve invitation") {
					@Override
					public void execute() {
						approveInvitation();
					}
				});
			}
			if (_item.getMembership() != null && _item.getMembership().getRole() != Role.OWNER) {
				dialog.addAction(new ActionDialog.Action("Leave") {
					@Override
					public void execute() {
						leaveGroup();
					}
				});
			}
			dialog.show();
		}

		private void leaveGroup() {
			RemoveGroupMembersQuery query = RemoveGroupMembersQuery.create(_item.getId(), UserIdList.create(GetSocial.getCurrentUser().getId()));
			Communities.removeGroupMembers(query, () -> {
				if (getFragmentTag().equals("groups")) {
					reloadGroup();
				}
				if (getFragmentTag().equals("my_groups")) {
					removeItem(_item);
				}
				Toast.makeText(getContext(), "Group left", Toast.LENGTH_SHORT).show();
			}, error -> Toast.makeText(getContext(), error.toString(), Toast.LENGTH_SHORT).show());
		}

		void openPolls() {
			addContentFragment(PollsListFragment.inGroup(_item.getId()));
		}

		void openActivities() {
			addContentFragment(ActivitiesListFragment.inGroup(_item.getId()));
		}

		void openAnnouncementsPolls() {
			addContentFragment(PollsListFragment.inGroupAnnouncements(_item.getId()));
		}

		void openFeed(boolean currentUser) {
			ActivitiesQuery query = ActivitiesQuery.activitiesInGroup(_item.getId());
			if (currentUser) {
				query = query.byUser(UserId.currentUser());
			}
			ActivityFeedViewBuilder builder = ActivityFeedViewBuilder.create(query)
							.setActionListener(_activityListener.dependencies().actionListener())
							.setViewStateListener(new ViewStateListener() {
								@Override
								public void onOpen() {
									_log.logInfoAndToast("Feed open for " + _item.getId());
								}

								@Override
								public void onClose() {
									_log.logInfoAndToast("Feed closed for " + _item.getId());
									loadItems();
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

		void postToFeed() {
			addContentFragment(PostActivityFragment.postToGroup(_item.getId()));
		}

		void createPoll() {
			addContentFragment(CreatePollFragment.postToGroup(_item.getId()));
		}

		void showGroupMembers() {
			final String groupId = _item.getId();
			final Role role = _item.getMembership().getRole();
			addContentFragment(GroupMembersFragment.create(groupId, role));
		}

		void deleteGroup() {
			final String groupId = _item.getId();
			RemoveGroupsQuery query = RemoveGroupsQuery.groups(groupId);
			Communities.removeGroups(query,
					() -> {
						removeItem(_item);
						Toast.makeText(getContext(), "Group removed", Toast.LENGTH_SHORT).show();
					}, error -> Toast.makeText(getContext(), error.toString(), Toast.LENGTH_SHORT).show());
		}

		void approveInvitation() {
			final String groupId = _item.getId();
			JoinGroupQuery query = new JoinGroupQuery(groupId).withInvitationToken(_item.getMembership().getInvitationToken());

			Communities.joinGroup(query, result -> {
				reloadGroup();
				Toast.makeText(getContext(), "Joined to group", Toast.LENGTH_SHORT).show();
			}, error -> Toast.makeText(getContext(), error.toString(), Toast.LENGTH_SHORT).show());
		}

		void join() {
			final String groupId = _item.getId();
			JoinGroupQuery query = new JoinGroupQuery(groupId);
			Communities.joinGroup(query, result -> {
				reloadGroup();
				Toast.makeText(getContext(), "Joined to group", Toast.LENGTH_SHORT).show();
			}, error -> Toast.makeText(getContext(), error.toString(), Toast.LENGTH_SHORT).show());
		}

		private void reloadGroup() {
			Communities.getGroup(_item.getId(), result -> {
				updateItem(_item, result);
			}, error -> Toast.makeText(getContext(), error.toString(), Toast.LENGTH_SHORT).show());
		}

		@Override
		protected void invalidate() {
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
			String memberRole = "UNKNOWN";
			String memberStatus = "UNKNOWN";
			if (_item.getMembership() != null) {
				memberRole = _item.getMembership().getRole().name();
				memberStatus = _item.getMembership().getStatus().name();
				if (_item.getMembership().getStatus() == MemberStatus.MEMBER) {
					memberStatus = "APPROVED";
				}
			}
			_score.setText("Popularity: " + _item.getPopularity());
			_member_role.setText("Member role: " + memberRole);
			_member_status.setText("Member status: " + memberStatus);
		}
	}
}
