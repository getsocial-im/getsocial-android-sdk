package im.getsocial.demo.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.Nullable;
import im.getsocial.sdk.Callback;
import im.getsocial.sdk.Communities;
import im.getsocial.sdk.FailureCallback;
import im.getsocial.sdk.common.PagingQuery;
import im.getsocial.sdk.common.PagingResult;
import im.getsocial.sdk.communities.FriendsQuery;
import im.getsocial.sdk.communities.User;
import im.getsocial.sdk.communities.UserId;

public class GenericFriendsFragment extends BaseUsersListFragment<FriendsQuery> {

	private String _userId;
	private String _userName;
	private int _count = -1;

	public static GenericFriendsFragment create(final String id, final String name) {
		final Bundle bundle = new Bundle();
		bundle.putString("id", id);
		bundle.putString("name", name);
		final GenericFriendsFragment friendsFragment = new GenericFriendsFragment();
		friendsFragment.setArguments(bundle);
		return friendsFragment;
	}

	@Override
	public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		_query.setVisibility(View.GONE);
		Communities.getFriendsCount(createQuery(SearchObject.empty()), result -> {
			_count = result;
			_activityListener.invalidateUi();
		}, this::onError);
	}


	@Override
	public void onCreate(@Nullable final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			setArguments(savedInstanceState);
		}
		_userId = getArguments().getString("id");
		_userName = getArguments().getString("name");
	}

	@Override
	protected void load(final PagingQuery<FriendsQuery> query, final Callback<PagingResult<User>> success, final FailureCallback failure) {
		Communities.getFriends(query, success, failure);
	}

	@Override
	protected FriendsQuery createQuery(final SearchObject searchObject) {
		return FriendsQuery.ofUser(UserId.create(_userId));
	}

	@Override
	public String getFragmentTag() {
		return "user_friends";
	}

	@SuppressLint("DefaultLocale")
	@Override
	public String getTitle() {
		if (_count == -1) {
			return String.format("Friends of %s", _userName);
		}
		return String.format("(%d) Friends of %s", _count, _userName);
	}
}
