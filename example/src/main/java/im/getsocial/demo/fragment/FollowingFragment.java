package im.getsocial.demo.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.Nullable;
import im.getsocial.sdk.Callback;
import im.getsocial.sdk.Communities;
import im.getsocial.sdk.FailureCallback;
import im.getsocial.sdk.common.PagingQuery;
import im.getsocial.sdk.common.PagingResult;
import im.getsocial.sdk.communities.User;
import im.getsocial.sdk.communities.UserId;
import im.getsocial.sdk.communities.UsersQuery;

public class FollowingFragment extends BaseUsersListFragment<UsersQuery> {

	private String _userId;
	private String _displayName;

	public static FollowingFragment followedBy(final String userId, final String displayName) {
		final Bundle args = new Bundle();
		args.putString("id", userId);
		args.putString("name", displayName);
		final FollowingFragment fragment = new FollowingFragment();

		fragment.setArguments(args);

		return fragment;
	}

	@Nullable
	@Override
	public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
		_userId = getArguments().getString("id");
		_displayName = getArguments().getString("name");
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		_query.setVisibility(View.GONE);
	}

	@Override
	protected void load(final PagingQuery<UsersQuery> query, final Callback<PagingResult<User>> success, final FailureCallback failure) {
		Communities.getUsers(query, success, failure);
	}

	@Override
	protected UsersQuery createQuery(final String query) {
		return UsersQuery.followedByUser(UserId.create(_userId));
	}

	@Override
	public String getFragmentTag() {
		return "followed_by_" + _userId;
	}

	@Override
	public String getTitle() {
		return "Followed by " + _displayName;
	}
}
