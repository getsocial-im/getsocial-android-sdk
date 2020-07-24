package im.getsocial.demo.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.annotation.Nullable;
import im.getsocial.sdk.communities.FollowersQuery;

public class GroupFollowersFragment extends FollowersFragment {
	private String _id;
	private String _name;

	public static GroupFollowersFragment create(final String id, final String name) {
		final Bundle bundle = new Bundle();
		bundle.putString("id", id);
		bundle.putString("name", name);
		final GroupFollowersFragment followersFragment = new GroupFollowersFragment();
		followersFragment.setArguments(bundle);
		return followersFragment;
	}

	@Override
	public void onCreate(@Nullable final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			setArguments(savedInstanceState);
		}
		_id = getArguments().getString("id");
		_name = getArguments().getString("name");
	}

	public GroupFollowersFragment() {
	}

	@Override
	protected FollowersQuery createQuery(final String query) {
		return FollowersQuery.ofGroup(_id);
	}

	@Override
	public String getFragmentTag() {
		return "followers_" + _id;
	}

	@SuppressLint("DefaultLocale")
	@Override
	public String getTitle() {
		if (_count == -1) {
			return String.format("Followers of %s", _name);
		}
		return String.format("(%d) Followers of %s", _count, _name);
	}
}
