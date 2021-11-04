package im.getsocial.demo.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import im.getsocial.demo.R;
import im.getsocial.sdk.Callback;
import im.getsocial.sdk.Communities;
import im.getsocial.sdk.FailureCallback;
import im.getsocial.sdk.common.PagingQuery;
import im.getsocial.sdk.common.PagingResult;
import im.getsocial.sdk.communities.UserVotes;
import im.getsocial.sdk.communities.VotesQuery;

public class AllVotesFragment extends BaseSearchFragment<VotesQuery, UserVotes> {

    String _activityId;
    public static Fragment votesForActivity(final String activityId) {
        final AllVotesFragment fragment = new AllVotesFragment();
        final Bundle args = new Bundle();
        args.putString("activityId", activityId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            setArguments(savedInstanceState);
        }
        _activityId = getArguments().getString("activityId");
    }

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        _query.setVisibility(View.GONE);
    }

    @Override
    protected BaseSearchAdapter<? extends ViewHolder> createAdapter() {
        return new BaseSearchAdapter<ViewHolder>() {
            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
                final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_uservotes, parent, false);
                return new AllVotesFragment.UserVotesViewHolder(view);
            }
        };
    }

    @Override
    protected void load(final PagingQuery<VotesQuery> query, final Callback<PagingResult<UserVotes>> success, final FailureCallback failure) {
        Communities.getVotes(query, success, failure);
    }

    @Override
    protected VotesQuery createQuery(final SearchObject searchObject) {
        return VotesQuery.forActivity(_activityId);
    }

    @Override
    public String getFragmentTag() {
        return "all_votes";
    }

    @Override
    public String getTitle() {
        return "All Votes";
    }

    public class UserVotesViewHolder extends ViewHolder {
        @BindView(R.id.uservotes_username)
        TextView _username;

        @BindView(R.id.uservotes_votes)
        TextView _votes;

        UserVotesViewHolder(final View view) {
            super(view);
        }

        @Override
        protected void bind(final View itemView) {
            ButterKnife.bind(this, itemView);
        }


        @Override
        protected void invalidate() {
            _username.setText(_item.getUser().getDisplayName());
            _votes.setText(_item.getVotes().toString());
        }

    }
}
