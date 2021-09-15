package im.getsocial.demo.fragment;

import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import im.getsocial.demo.R;
import im.getsocial.demo.dialog.SortOrderDelegate;
import im.getsocial.demo.dialog.SortOrderDialog;
import im.getsocial.sdk.Callback;
import im.getsocial.sdk.Communities;
import im.getsocial.sdk.FailureCallback;
import im.getsocial.sdk.common.PagingQuery;
import im.getsocial.sdk.common.PagingResult;
import im.getsocial.sdk.communities.ActivitiesQuery;
import im.getsocial.sdk.communities.GetSocialActivity;

public class ActivitiesListFragment extends BaseSearchFragment<ActivitiesQuery, GetSocialActivity> {

    String _topicId;
    String _groupId;
    private boolean _isTrending = false;
    private String _sortKey;
    private String _sortDirection;

    public static Fragment inTopic(final String topicId) {
        final ActivitiesListFragment fragment = new ActivitiesListFragment();
        final Bundle args = new Bundle();
        args.putString("topicId", topicId);
        fragment.setArguments(args);
        return fragment;
    }

    public static Fragment inGroup(final String groupId) {
        final ActivitiesListFragment fragment = new ActivitiesListFragment();
        final Bundle args = new Bundle();
        args.putString("groupId", groupId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            setArguments(savedInstanceState);
        }
        _topicId = getArguments().getString("topicId");
        _groupId = getArguments().getString("groupId");
    }


    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        menu.add(Menu.NONE, 0x43, Menu.NONE, _isTrending ? "All" : "Trending");
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(final android.view.MenuItem item) {
        if (item.getItemId() == 0x43) {
            _isTrending = !_isTrending;
            _sortDirection = null;
            _sortKey = null;
            loadItems();
            getActivity().invalidateOptionsMenu();
            return true;
        }
        if (item.getItemId() == 0x44) {
            showSortDialog();
            getActivity().invalidateOptionsMenu();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showSortDialog() {
        List<Pair<String, String>> sortOptions = new ArrayList<>();
        if (this._isTrending) {
        } else {
            sortOptions.add(new Pair<>("createdAt",""));
            sortOptions.add(new Pair<>("createdAt","-"));
        }
        SortOrderDialog.show(getFragmentManager(), sortOptions, new SortOrderDelegate() {
            @Override
            public void onSortKeySelected(Pair<String, String> pair) {
                _sortKey = pair.first;
                _sortDirection = pair.second;
                loadItems();
            }
        });
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
                final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_activity_item, parent, false);
                return new ActivitiesListFragment.ActivityListItemHolder(view);
            }
        };
    }

    @Override
    protected void load(final PagingQuery<ActivitiesQuery> query, final Callback<PagingResult<GetSocialActivity>> success, final FailureCallback failure) {
        Communities.getActivities(query, success, failure);
    }

    @Override
    protected ActivitiesQuery createQuery(final String searchTerm) {
        ActivitiesQuery query = _topicId == null ? ActivitiesQuery.activitiesInGroup(_groupId) : ActivitiesQuery.activitiesInTopic(_topicId);
        query = query.onlyTrending(_isTrending);
        return query;
    }

    @Override
    public String getFragmentTag() {
        return "activities_list";
    }

    @Override
    public String getTitle() {
        return "Activities";
    }

    public class ActivityListItemHolder extends ViewHolder {
        @BindView(R.id.activity_author_name)
        TextView _username;

        @BindView(R.id.activity_text)
        TextView _text;

        @BindView(R.id.activity_createdat)
        TextView _createdAt;

        @BindView(R.id.activity_score)
        TextView _score;

        ActivityListItemHolder(final View view) {
            super(view);
        }

        @Override
        protected void bind(final View itemView) {
            ButterKnife.bind(this, itemView);
        }


        @Override
        protected void invalidate() {
            _username.setText("Author: " + _item.getAuthor().getDisplayName());
            _text.setText("Text: " + _item.getText());
            String date = DateFormat.getDateTimeInstance().format(new Date(_item.getCreatedAt() * 1000));

            _createdAt.setText("Created: " + date);
            _score.setText("Popularity:" + _item.getPopularity());
        }

    }
}