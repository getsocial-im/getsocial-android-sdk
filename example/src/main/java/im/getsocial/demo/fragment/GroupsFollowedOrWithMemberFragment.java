package im.getsocial.demo.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.Nullable;
import im.getsocial.sdk.communities.GroupsQuery;
import im.getsocial.sdk.communities.UserId;

public class GroupsFollowedOrWithMemberFragment extends BaseGroupsFragment {

	private String _id;
	private String _name;
	private boolean _member;

	public static GroupsFollowedOrWithMemberFragment create(final String userId, final String userName, final boolean member) {
		final Bundle args = new Bundle();
		args.putString("id", userId);
		args.putString("name", userName);
		args.putBoolean("member", member);
		final GroupsFollowedOrWithMemberFragment fragment = new GroupsFollowedOrWithMemberFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		_query.setVisibility(View.GONE);
	}

	@Nullable
	@Override
	public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
		final Bundle args = getArguments();
		if (args != null) {
			_id = getArguments().getString("user");
			_name = getArguments().getString("name");
			_member = getArguments().getBoolean("member");
		}
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	protected GroupsQuery createQuery(final String query) {
		final UserId user = UserId.create(_id);
		return _member ? GroupsQuery.withMember(user) : GroupsQuery.followedByUser(user);
	}

	@Override
	public String getFragmentTag() {
		return (_member ? "groups_member_" : "groups_follower_") + _id;
	}

	@Override
	public String getTitle() {
		return (_member ? "Groups of " : "Followed by ") + _name;
	}
}
