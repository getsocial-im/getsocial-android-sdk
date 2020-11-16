package im.getsocial.demo.fragment;

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

import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import im.getsocial.demo.R;
import im.getsocial.demo.dialog.action_dialog.ActionDialog;
import im.getsocial.sdk.Callback;
import im.getsocial.sdk.Communities;
import im.getsocial.sdk.FailureCallback;
import im.getsocial.sdk.GetSocial;
import im.getsocial.sdk.common.PagingQuery;
import im.getsocial.sdk.common.PagingResult;
import im.getsocial.sdk.communities.GroupMember;
import im.getsocial.sdk.communities.MemberStatus;
import im.getsocial.sdk.communities.MembersQuery;
import im.getsocial.sdk.communities.RemoveGroupMembersQuery;
import im.getsocial.sdk.communities.Role;
import im.getsocial.sdk.communities.UpdateGroupMembersQuery;
import im.getsocial.sdk.communities.UserIdList;

public class GroupMembersFragment extends BaseSearchFragment<MembersQuery, GroupMember> {

	private String _groupId;
	private Role _role;

	@Override
	protected BaseSearchAdapter<? extends ViewHolder> createAdapter() {
		return new BaseSearchAdapter<ViewHolder>() {
			@NonNull
			@Override
			public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
				final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_group_member, parent, false);
				return new GroupMemberViewHolder(view);
			}
		};
	}

	@Nullable
	@Override
	public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
		final Bundle bundle = getArguments();
		_groupId = bundle.getString("groupId");
		Integer intRole = bundle.getInt("role");
		_role = Role.from(intRole);
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
		if (_role == Role.ADMIN || _role == Role.OWNER) {
			menu.add(Menu.NONE, 0x42, Menu.NONE, "Add");
		}
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(final android.view.MenuItem item) {
		if (item.getItemId() == 0x42) {
			addMember();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void load(PagingQuery<MembersQuery> query, Callback<PagingResult<GroupMember>> success, FailureCallback failure) {
		Communities.getGroupMembers(query, success, failure);
	}

	@Override
	protected MembersQuery createQuery(String groupId) {
		return MembersQuery.ofGroup(_groupId);
	}

	@Override
	public String getFragmentTag() {
		return "groupmembers_fragment";
	}

	@Override
	public String getTitle() {
		return "Group members";
	}

	private void addMember() {
		addContentFragment(AddGroupMemberFragment.create(_groupId));
	}

	public static Fragment create(final String groupId, final Role role) {
		final GroupMembersFragment fragment = new GroupMembersFragment();
		final Bundle args = new Bundle();
		args.putString("groupId", groupId);
		args.putInt("role", role.ordinal());
		fragment.setArguments(args);
		return fragment;
	}

	public class GroupMemberViewHolder extends ViewHolder {

		@BindView(R.id.member_id)
		TextView _userId;

		@BindView(R.id.member_name)
		TextView _memberName;

		@BindView(R.id.user_avatar)
		ImageView _avatar;

		@BindView(R.id.group_member_role)
		TextView _member_role;

		@BindView(R.id.group_member_status)
		TextView _member_status;

		GroupMemberViewHolder(final View view) {
			super(view);
		}

		@Override
		protected void bind(final View itemView) {
			ButterKnife.bind(this, itemView);
		}

		@OnClick(R.id.actions)
		public void openActions() {
			final ActionDialog dialog = new ActionDialog(getContext());
			dialog.addAction(new ActionDialog.Action("Details") {
				@Override
				public void execute() {
					showAlert("Details", _item.toString());
				}
			});
			if ((_role == Role.ADMIN || _role == Role.OWNER) && (!_item.getId().equals(GetSocial.getCurrentUser().getId()) && _item.getMembership().getRole() != Role.OWNER)) {
				dialog.addAction(new ActionDialog.Action("Remove") {
					@Override
					public void execute() {
						removeMember();
					}
				});
				if (_item.getMembership().getStatus() == MemberStatus.APPROVAL_PENDING) {
					dialog.addAction(new ActionDialog.Action("Approve") {
						@Override
						public void execute() {
							approveMember();
						}
					});
				}
			}
			if (_item.getId().equals(GetSocial.getCurrentUser().getId()) && _item.getMembership().getRole() != Role.OWNER) {
				dialog.addAction(new ActionDialog.Action("Leave") {
					@Override
					public void execute() {
						leaveGroup();
					}
				});
			}

			dialog.show();
		}

		private void approveMember() {
			UpdateGroupMembersQuery query = UpdateGroupMembersQuery.create(_groupId, UserIdList.create(_item.getId()))
					.withMemberStatus(MemberStatus.MEMBER);
			Communities.updateGroupMembers(query, result -> {
				updateItem(_item, result.get(0));
			}, error -> Toast.makeText(getContext(), error.toString(), Toast.LENGTH_SHORT).show());
		}

		private void removeMember() {
			final String userId = _item.getId();
			RemoveGroupMembersQuery query = RemoveGroupMembersQuery.create(_groupId, UserIdList.create(userId));
			Communities.removeGroupMembers(query, () -> {
				removeItem(_item);
				Toast.makeText(getContext(), "Member removed", Toast.LENGTH_SHORT).show();
			}, error -> Toast.makeText(getContext(), error.toString(), Toast.LENGTH_SHORT).show());
		}

		private void leaveGroup() {
			RemoveGroupMembersQuery query = RemoveGroupMembersQuery.create(_groupId, UserIdList.create(GetSocial.getCurrentUser().getId()));
			Communities.removeGroupMembers(query, () -> {
				removeItem(_item);
				Toast.makeText(getContext(), "Group left", Toast.LENGTH_SHORT).show();
			}, error -> Toast.makeText(getContext(), error.toString(), Toast.LENGTH_SHORT).show());
		}

		@Override
		protected void invalidate() {
			_userId.setText(_item.getId());
			_memberName.setText(_item.getDisplayName());
			final String avatar = _item.getAvatarUrl();
			if (avatar == null || avatar.isEmpty()) {
				_avatar.setImageResource(R.drawable.avatar_default);
			} else {
				Picasso.with(getContext()).load(avatar).into(_avatar);
			}
			String memberRole = "UNKNOWN";
			String memberStatus = "UNKNOWN";
			if (_item.getMembership() != null) {
				memberRole = _item.getMembership().getRole().name();
				memberStatus = _item.getMembership().getStatus().name();
				if (_item.getMembership().getStatus() == MemberStatus.MEMBER) {
					memberStatus = "APPROVED";
				}
			}
			_member_status.setText("Member status: " + memberStatus);
			_member_role.setText("Member role: " + memberRole);
		}
	}
}
