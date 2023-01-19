package im.getsocial.demo.fragment;

import static android.view.View.GONE;

import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import butterknife.OnClick;
import im.getsocial.demo.R;
import im.getsocial.demo.dialog.SortOrderDelegate;
import im.getsocial.demo.dialog.SortOrderDialog;
import im.getsocial.demo.dialog.action_dialog.ActionDialog;
import im.getsocial.sdk.Callback;
import im.getsocial.sdk.Communities;
import im.getsocial.sdk.FailureCallback;
import im.getsocial.sdk.common.PagingQuery;
import im.getsocial.sdk.common.PagingResult;
import im.getsocial.sdk.communities.ActivitiesQuery;
import im.getsocial.sdk.communities.GetSocialActivity;
import im.getsocial.sdk.communities.RemoveActivitiesQuery;
import im.getsocial.sdk.communities.ReportingReason;
import im.getsocial.sdk.communities.UserId;

public class ActivitiesListFragment extends BaseSearchFragment<ActivitiesQuery, GetSocialActivity> {

    String _topicId;
    String _groupId;
    String _activityId;
    boolean _timeline = false;
    boolean _staticActivities = false;
    boolean _currentUserFeed = false;
    private boolean _isTrending = false;
    private String _sortKey;
    private String _sortDirection;
    static List<GetSocialActivity> _activities;

    public static Fragment bookmarkedActivities() {
        final ActivitiesListFragment fragment = new ActivitiesListFragment();
        final Bundle args = new Bundle();
        args.putBoolean("currentuserfeed", true);
        fragment.setArguments(args);
        return fragment;
    }

    public static Fragment currentUserFeed() {
        final ActivitiesListFragment fragment = new ActivitiesListFragment();
        final Bundle args = new Bundle();
        args.putBoolean("currentuserfeed", true);
        fragment.setArguments(args);
        return fragment;
    }

    public static Fragment timeline() {
        final ActivitiesListFragment fragment = new ActivitiesListFragment();
        final Bundle args = new Bundle();
        args.putBoolean("timeline", true);
        fragment.setArguments(args);
        return fragment;
    }

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

    public static Fragment commentsToActivity(final String activityId) {
        final ActivitiesListFragment fragment = new ActivitiesListFragment();
        final Bundle args = new Bundle();
        args.putString("activityId", activityId);
        fragment.setArguments(args);
        return fragment;
    }

    public static Fragment activities(final List<GetSocialActivity> activities) {
        final ActivitiesListFragment fragment = new ActivitiesListFragment();
        final Bundle args = new Bundle();
        _activities = activities;
        args.putBoolean("staticActivities", true);
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
        _topicId = getArguments().getString("topicId");
        _groupId = getArguments().getString("groupId");
        _timeline = getArguments().getBoolean("timeline", false);
        _currentUserFeed = getArguments().getBoolean("currentuserfeed", false);
        _staticActivities = getArguments().getBoolean("staticActivities", false);
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        if (!_staticActivities) {
            menu.add(Menu.NONE, 0x43, Menu.NONE, _isTrending ? "All" : "Trending");
        }
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
            sortOptions.add(new Pair<>("createdAt", ""));
            sortOptions.add(new Pair<>("createdAt", "-"));
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

        if (!_staticActivities) {
            super.showAdvancedSearch();
        } else {
            _query.setVisibility(View.GONE);
        }
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
        if (!_staticActivities) {
            Communities.getActivities(query, success, failure);
        } else {
            PagingResult<GetSocialActivity> pagingResult = new PagingResult<>(_activities, "");
            success.onSuccess(pagingResult);
        }
    }

    @Override
    protected ActivitiesQuery createQuery(final SearchObject searchObject) {
        ActivitiesQuery query = null;
        if (_timeline) {
            query = ActivitiesQuery.timeline();
        } else if (_currentUserFeed) {
            query = ActivitiesQuery.feedOf(UserId.currentUser());
        } else if (_groupId != null) {
            query = ActivitiesQuery.activitiesInGroup(_groupId);
        } else if (_topicId != null) {
            query = ActivitiesQuery.activitiesInTopic(_topicId);
        } else if (_activityId != null) {
            query = ActivitiesQuery.commentsToActivity(_activityId);
        }

        if (query != null) {
            query = query.onlyTrending(_isTrending);
            if (searchObject.searchTerm != null) {
                query = query.withText(searchObject.searchTerm);
            }
            if (searchObject.labels != null) {
                query = query.withLabels(searchObject.labels);
            }
            if (searchObject.properties != null) {
                query = query.withProperties(searchObject.properties);
            }
            return query.includeComments(3);
        }
        return null;
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

        @BindView(R.id.activity_comments)
        TextView _comments;

        @BindView(R.id.activity_labels)
        TextView _labels;

        @BindView(R.id.activity_properties)
        TextView _properties;

        @BindView(R.id.actions)
        Button _actions;

        ActivityListItemHolder(final View view) {
            super(view);
        }

        @Override
        protected void bind(final View itemView) {
            ButterKnife.bind(this, itemView);
        }


        @OnClick(R.id.actions)
        void openActions() {
            final ActionDialog dialog = new ActionDialog(getContext());
            dialog.addAction(new ActionDialog.Action("View included comments") {
                @Override
                public void execute() {
                    addContentFragment(ActivitiesListFragment.activities(_item.getComments()));
                }
            });
            dialog.addAction(new ActionDialog.Action("View all comments") {
                @Override
                public void execute() {
                    addContentFragment(ActivitiesListFragment.commentsToActivity(_item.getId()));
                }
            });
            dialog.addAction(new ActionDialog.Action("Comment") {
                @Override
                public void execute() {
                    addContentFragment(PostActivityFragment.postComment(_item.getId()));
                }
            });

            dialog.addAction(new ActionDialog.Action("Remove") {
                @Override
                public void execute() {
                    Communities.removeActivities(RemoveActivitiesQuery.activities(_item.getId()),
                            () -> showAlert("Success", "Activity removed"),
                            error -> _log.logErrorAndToast("Failed to remove activity, error: " + error.getMessage())
                    );
                }
            });

            if (_item.getMyReactions().isEmpty()) {
                dialog.addAction(new ActionDialog.Action("React (like)") {
                    @Override
                    public void execute() {
                        Communities.setReaction("like", _item.getId(),
                                () -> showAlert("Success", "Activity reacted"),
                                error -> _log.logErrorAndToast("Failed to react activity, error: " + error.getMessage())
                        );
                    }
                });
            } else {
                dialog.addAction(new ActionDialog.Action("Unreact (like)") {
                    @Override
                    public void execute() {
                        Communities.removeReaction("like", _item.getId(),
                                () -> showAlert("Success", "Activity unreacted"),
                                error -> _log.logErrorAndToast("Failed to unreact activity, error: " + error.getMessage())
                        );
                    }
                });
            }

            dialog.addAction(new ActionDialog.Action("Report") {
                @Override
                public void execute() {
                    Communities.reportActivity(_item.getId(), ReportingReason.INAPPROPRIATE_CONTENT, "Testing from SDK",
                            () -> showAlert("Success", "Activity reported"),
                            error -> _log.logErrorAndToast("Failed to report activity, error: " + error.getMessage())
                    );
                }
            });


            dialog.show();
        }

        @Override
        protected void invalidate() {
            _username.setText("Author: " + _item.getAuthor().getDisplayName());
            _text.setText("Text: " + _item.getText());
            String date = DateFormat.getDateTimeInstance().format(new Date(_item.getCreatedAt() * 1000));

            _createdAt.setText("Created: " + date);
            _score.setText("Popularity:" + _item.getPopularity());
            _comments.setText("Comments:" + _item.getCommentsCount());
            _labels.setText("Labels: " + joinElements(_item.getLabels()));
            _properties.setText("Properties: " + joinElements(_item.getProperties()));

            if (_staticActivities) {
                _actions.setVisibility(GONE);
            }

        }

    }
}
