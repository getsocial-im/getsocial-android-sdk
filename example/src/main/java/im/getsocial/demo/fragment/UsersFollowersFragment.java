package im.getsocial.demo.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.annotation.Nullable;
import im.getsocial.sdk.communities.FollowersQuery;
import im.getsocial.sdk.communities.UserId;

public class UsersFollowersFragment extends FollowersFragment {

	private String _userId;
	private String _userName;

	public static UsersFollowersFragment create(final String id, final String name) {
		final Bundle bundle = new Bundle();
		bundle.putString("id", id);
		bundle.putString("name", name);
		final UsersFollowersFragment followersFragment = new UsersFollowersFragment();
		followersFragment.setArguments(bundle);
		return followersFragment;
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
	protected FollowersQuery createQuery(final String query) {
		return FollowersQuery.ofUser(UserId.create(_userId));
	}

	@Override
	public String getFragmentTag() {
		return "user_followers";
	}

	@SuppressLint("DefaultLocale")
	@Override
	public String getTitle() {
		if (_count == -1) {
			return String.format("Followers of %s", _userName);
		}
		return String.format("(%d) Followers of %s", _count, _userName);
	}
}
