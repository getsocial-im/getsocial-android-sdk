package im.getsocial.demo.fragment;

import im.getsocial.sdk.Callback;
import im.getsocial.sdk.Communities;
import im.getsocial.sdk.FailureCallback;
import im.getsocial.sdk.common.PagingQuery;
import im.getsocial.sdk.common.PagingResult;
import im.getsocial.sdk.communities.User;
import im.getsocial.sdk.communities.UsersQuery;

public class UsersSearchFragment extends BaseUsersListFragment<UsersQuery> {

	@Override
	protected void load(final PagingQuery<UsersQuery> query, final Callback<PagingResult<User>> success, final FailureCallback failure) {
		Communities.getUsers(query, success, failure);
	}

	@Override
	protected UsersQuery createQuery(final SearchObject searchObject) {
		return UsersQuery.find(searchObject.searchTerm);
	}

	@Override
	protected String validate(final CharSequence newText) {
		if (newText.length() < 3) {
			return "Query length should have at least three symbols";
		}
		if (newText.length() > 24) {
			return "Query should be no longer than 24 symbols";
		}
		return super.validate(newText);
	}

	@Override
	public String getFragmentTag() {
		return "users";
	}

	@Override
	public String getTitle() {
		return "Users";
	}
}
