package im.getsocial.demo.fragment;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.Nullable;
import im.getsocial.sdk.Callback;
import im.getsocial.sdk.Communities;
import im.getsocial.sdk.FailureCallback;
import im.getsocial.sdk.common.PagingQuery;
import im.getsocial.sdk.common.PagingResult;
import im.getsocial.sdk.communities.FollowersQuery;
import im.getsocial.sdk.communities.User;

public abstract class FollowersFragment extends BaseUsersListFragment<FollowersQuery> {

	protected int _count = -1;

	@Override
	public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		_query.setVisibility(View.GONE);
		loadCount(createQuery(""), result -> {
			_count = result;
			_activityListener.invalidateUi();
		}, this::onError);
	}


	private void loadCount(final FollowersQuery query, final Callback<Integer> callback, final FailureCallback failure) {
		Communities.getFollowersCount(query, callback, failure);
	}

	@Override
	protected void load(final PagingQuery<FollowersQuery> query, final Callback<PagingResult<User>> success, final FailureCallback failure) {
		Communities.getFollowers(query, success, failure);
	}

}
