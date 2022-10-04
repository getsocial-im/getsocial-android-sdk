package im.getsocial.demo.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.Nullable;

import im.getsocial.sdk.communities.FollowersQuery;

public class TagFollowersFragment extends FollowersFragment {
    private String _tag;

    public static TagFollowersFragment create(final String id) {
        final Bundle bundle = new Bundle();
        bundle.putString("tag", id);
        final TagFollowersFragment followersFragment = new TagFollowersFragment();
        followersFragment.setArguments(bundle);
        return followersFragment;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            setArguments(savedInstanceState);
        }
        _tag = getArguments().getString("tag");
    }

    public TagFollowersFragment() {
    }

    @Override
    protected FollowersQuery createQuery(final SearchObject searchObject) {
        return FollowersQuery.ofTag(_tag).withName(searchObject.searchTerm);
    }

    @Override
    public String getFragmentTag() {
        return "followers_" + _tag;
    }

    @SuppressLint("DefaultLocale")
    @Override
    public String getTitle() {
        if (_count == -1) {
            return String.format("Followers of %s", _tag);
        }
        return String.format("(%d) Followers of %s", _count, _tag);
    }
}
