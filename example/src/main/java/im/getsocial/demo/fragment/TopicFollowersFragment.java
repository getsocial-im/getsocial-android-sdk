package im.getsocial.demo.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.annotation.Nullable;
import im.getsocial.sdk.communities.FollowersQuery;

public class TopicFollowersFragment extends FollowersFragment {
	private String _topicId;
	private String _topicName;

	public static TopicFollowersFragment create(final String id, final String name) {
		final Bundle bundle = new Bundle();
		bundle.putString("id", id);
		bundle.putString("name", name);
		final TopicFollowersFragment followersFragment = new TopicFollowersFragment();
		followersFragment.setArguments(bundle);
		return followersFragment;
	}

	@Override
	public void onCreate(@Nullable final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			setArguments(savedInstanceState);
		}
		_topicId = getArguments().getString("id");
		_topicName = getArguments().getString("name");
	}

	public TopicFollowersFragment() {
	}

	@Override
	protected FollowersQuery createQuery(final SearchObject searchObject) {
		return FollowersQuery.ofTopic(_topicId);
	}

	@Override
	public String getFragmentTag() {
		return "followers_" + _topicId;
	}

	@SuppressLint("DefaultLocale")
	@Override
	public String getTitle() {
		if (_count == -1) {
			return String.format("Followers of %s", _topicName);
		}
		return String.format("(%d) Followers of %s", _count, _topicName);
	}
}
