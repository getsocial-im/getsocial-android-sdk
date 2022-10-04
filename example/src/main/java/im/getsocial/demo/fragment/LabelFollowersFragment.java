package im.getsocial.demo.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.Nullable;

import im.getsocial.sdk.communities.FollowersQuery;

public class LabelFollowersFragment extends FollowersFragment {
    private String _label;

    public static LabelFollowersFragment create(final String id) {
        final Bundle bundle = new Bundle();
        bundle.putString("label", id);
        final LabelFollowersFragment followersFragment = new LabelFollowersFragment();
        followersFragment.setArguments(bundle);
        return followersFragment;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            setArguments(savedInstanceState);
        }
        _label = getArguments().getString("label");
    }

    public LabelFollowersFragment() {
    }

    @Override
    protected FollowersQuery createQuery(final SearchObject searchObject) {
        return FollowersQuery.ofLabel(_label).withName(searchObject.searchTerm);
    }

    @Override
    public String getFragmentTag() {
        return "followers_" + _label;
    }

    @SuppressLint("DefaultLocale")
    @Override
    public String getTitle() {
        if (_count == -1) {
            return String.format("Followers of %s", _label);
        }
        return String.format("(%d) Followers of %s", _count, _label);
    }
}
