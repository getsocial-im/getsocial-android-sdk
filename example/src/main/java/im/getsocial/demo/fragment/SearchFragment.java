package im.getsocial.demo.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import im.getsocial.demo.R;
import im.getsocial.demo.dialog.DialogWithScrollableText;
import im.getsocial.demo.dialog.action_dialog.ActionDialog;
import im.getsocial.sdk.Communities;
import im.getsocial.sdk.GetSocialError;
import im.getsocial.sdk.common.PagingQuery;
import im.getsocial.sdk.communities.CommunitiesEntityType;
import im.getsocial.sdk.communities.GetSocialActivity;
import im.getsocial.sdk.communities.Group;
import im.getsocial.sdk.communities.Label;
import im.getsocial.sdk.communities.SearchQuery;
import im.getsocial.sdk.communities.SearchResult;
import im.getsocial.sdk.communities.Tag;
import im.getsocial.sdk.communities.Topic;
import im.getsocial.sdk.communities.TopicsQuery;
import im.getsocial.sdk.communities.User;
import im.getsocial.sdk.communities.UserId;

class SearchEntity {
    private final String _id;
    private final String _title;
    private final String _description;
    private final Map<String, String> _properties;
    private final List<String> _labels;
    private final String _details;

    SearchEntity(String id, String title, String description, Map<String, String> properties, List<String> labels, String details) {
        _id = id;
        _title = title;
        _description = description;
        _properties = properties;
        _labels = labels;
        _details = details;
    }

    public String getId() {
        return _id;
    }

    public String getTitle() {
        return _title;
    }

    public String getDescription() {
        return _description;
    }

    public Map<String, String> getProperties() {
        return _properties;
    }

    public List<String> getLabels() {
        return _labels;
    }

    public String getDetails() {
        return _details;
    }
}


public class SearchFragment extends BaseFragment implements AdapterView.OnItemSelectedListener {

    private final Timer _timer = new Timer();
    private final boolean _isTrending = false;
    @BindView(R.id.swiperefresh)
    public SwipeRefreshLayout _swipeRefreshLayout;
    @BindView(R.id.search_text)
    public EditText _query;
    @BindView(R.id.search_labels)
    public EditText _labelsSearch;
    @BindView(R.id.search_properties)
    public EditText _propertiesSearch;
    @BindView(R.id.search_entities)
    public Spinner _entities;
    @BindView(R.id.list)
    public RecyclerView _listView;
    @BindView(R.id.error)
    public TextView _error;
    private TimerTask _pendingTask;
    private im.getsocial.demo.fragment.SearchFragment.StringAdapter _adapter;

    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        final LinearLayoutManager manager = new LinearLayoutManager(getContext());
        _listView.setLayoutManager(manager);
        final DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        _listView.addItemDecoration(dividerItemDecoration);
        _adapter = new im.getsocial.demo.fragment.SearchFragment.StringAdapter();
        _listView.setAdapter(_adapter);
        _swipeRefreshLayout.setOnRefreshListener(this::loadItems);

        ArrayList<String> entities = new ArrayList<>();

        entities.add("Topics");
        entities.add("Groups");
        entities.add("Users");
        entities.add("Activities");
        entities.add("Tags");
        entities.add("Labels");

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, entities);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        _entities.setAdapter(adapter);
        _entities.setOnItemSelectedListener(this);

        loadItems();
    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        switch (parent.getItemAtPosition(pos).toString()) {
            case "Groups":
                _adapter._currentEntity = CommunitiesEntityType.GROUP;
                break;
            case "Users":
                _adapter._currentEntity = CommunitiesEntityType.USER;
                break;
            case "Activities":
                _adapter._currentEntity = CommunitiesEntityType.ACTIVITY;
                break;
            case "Tags":
                _adapter._currentEntity = CommunitiesEntityType.HASHTAG;
                break;
            case "Labels":
                _adapter._currentEntity = CommunitiesEntityType.LABEL;
                break;
            default:
                _adapter._currentEntity = CommunitiesEntityType.TOPIC;
        }
        _listView.getAdapter().notifyDataSetChanged();
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    private void filter() {
        getActivity().invalidateOptionsMenu();
        loadItems();
    }


    protected void loadItems() {
        _swipeRefreshLayout.setRefreshing(true);
        Communities.search(new PagingQuery<>(createQuery()), this::saveResult, this::onError);
    }


    protected void onError(final GetSocialError error) {
        Toast.makeText(getContext(), error.toString(), Toast.LENGTH_SHORT).show();
    }

    private void saveResult(final SearchResult result) {
        _swipeRefreshLayout.setRefreshing(false);
        _adapter._topics.clear();
        if (result.getTopics() != null) {
            _adapter._topics.addAll(result.getTopics().getEntries());
        }
        _adapter._groups.clear();
        if (result.getGroups() != null) {
            _adapter._groups.addAll(result.getGroups().getEntries());
        }
        _adapter._users.clear();
        if (result.getUsers() != null) {
            _adapter._users.addAll(result.getUsers().getEntries());
        }
        _adapter._activities.clear();
        if (result.getActivities() != null) {
            _adapter._activities.addAll(result.getActivities().getEntries());
        }
        _adapter._labels.clear();
        if (result.getLabels() != null) {
            _adapter._labels.addAll(result.getLabels().getEntries());
        }
        _adapter._tags.clear();
        if (result.getTags() != null) {
            _adapter._tags.addAll(result.getTags().getEntries());
        }

        _adapter.notifyDataSetChanged();
    }

    private String getSearchText() {
        return _query.getText().toString();
    }

    @Nullable
    private List<String> getSearchLabels() {
        String searchText = _labelsSearch.getText().toString();
        if (searchText.isEmpty()) {
            return null;
        }
        return Arrays.asList(searchText.split(","));
    }

    @Nullable
    private Map<String, String> getSearchProperties() {
        String searchText = _propertiesSearch.getText().toString();
        if (searchText.isEmpty()) {
            return null;
        }
        String[] entries = searchText.split(",");
        Map<String, String> result = new HashMap<>();
        for(String entry: entries) {
            String[] elements = entry.split("=");
            if (elements.length == 2) {
                String key = elements[0];
                String value = elements[1];
                result.put(key, value);
            }
        }
        return result;
    }

    @OnClick(R.id.execute_search)
    public void onSearchButtonClick() {
        if (_pendingTask != null) {
            _pendingTask.cancel();
        }
        _error.setVisibility(View.GONE);
        _pendingTask = new TimerTask() {
            @Override
            public void run() {
                getActivity().runOnUiThread(SearchFragment.this::loadItems);
            }
        };
        _timer.schedule(_pendingTask, 300);
    }

    protected SearchQuery createQuery() {
        SearchQuery query = null;
        List<CommunitiesEntityType> entities = new ArrayList<>();
        entities.add(CommunitiesEntityType.TOPIC);
        entities.add(CommunitiesEntityType.ACTIVITY);
        entities.add(CommunitiesEntityType.LABEL);
        entities.add(CommunitiesEntityType.GROUP);
        entities.add(CommunitiesEntityType.HASHTAG);
        entities.add(CommunitiesEntityType.USER);
        if (getSearchText() != "") {
            query = SearchQuery.find(getSearchText()).inEntities(entities);
        } else {
            query = SearchQuery.all().inEntities(entities);
        }

        List<String> labels = getSearchLabels();
        if (labels != null) {
            query = query.withLabels(labels);
        }

        Map<String, String> properties = getSearchProperties();
        if (properties != null) {
            query = query.withProperties(properties);
        }

        return query;
    }

    @OnTextChanged(R.id.search_text)
    public void onSearchTextChange(final CharSequence newText) {
        if (_pendingTask != null) {
            _pendingTask.cancel();
        }
        final String validation = validate(newText);
        if (validation != null) {
            _error.setVisibility(View.VISIBLE);
            _error.setText(validation);
            return;
        }
        _error.setVisibility(View.GONE);
        _pendingTask = new TimerTask() {
            @Override
            public void run() {
                getActivity().runOnUiThread(() -> loadItems());
            }
        };
        _timer.schedule(_pendingTask, 300);
    }

    protected String validate(final CharSequence newText) {
        return null;
    }

    @Override
    public String getFragmentTag() {
        return "search";
    }

    @Override
    public String getTitle() {
        return "Search";
    }

    protected class StringAdapter extends RecyclerView.Adapter<im.getsocial.demo.fragment.SearchFragment.StringViewHolder> {
        private final List<Topic> _topics = new ArrayList<>();
        private final List<Group> _groups = new ArrayList<>();
        private final List<User> _users = new ArrayList<>();
        private final List<GetSocialActivity> _activities = new ArrayList<>();
        private final List<Label> _labels = new ArrayList<>();
        private final List<Tag> _tags = new ArrayList<>();
        private CommunitiesEntityType _currentEntity = CommunitiesEntityType.TOPIC;

        @NonNull
        @Override
        public im.getsocial.demo.fragment.SearchFragment.StringViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
            final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_search, parent, false);
            return new im.getsocial.demo.fragment.SearchFragment.StringViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final im.getsocial.demo.fragment.SearchFragment.StringViewHolder holder, final int position) {
            SearchEntity searchEntity;

            switch (_currentEntity) {
                case GROUP: {
                    Group e = _groups.get(position);
                    searchEntity = new SearchEntity(e.getId(), e.getTitle(), e.getDescription(), e.getSettings().getProperties(), e.getSettings().getLabels(), e.toString());
                }
                break;
                case USER: {
                    User e = _users.get(position);
                    searchEntity = new SearchEntity(e.getId(), e.getDisplayName(), null, e.getPublicProperties(), null, e.toString());
                }
                break;
                case ACTIVITY: {
                    GetSocialActivity e = _activities.get(position);
                    searchEntity = new SearchEntity(e.getId(), e.getText(), null, e.getProperties(), e.getLabels(), e.toString());
                }
                break;
                case LABEL: {
                    Label e = _labels.get(position);
                    searchEntity = new SearchEntity(e.getName(), e.getName(), null, null, null, e.toString());
                }
                break;
                case HASHTAG: {
                    Tag e = _tags.get(position);
                    searchEntity = new SearchEntity(e.getName(), e.getName(), null, null, null, e.toString());
                }
                break;
                default:
                    Topic e = _topics.get(position);
                    searchEntity = new SearchEntity(e.getId(), e.getTitle(), e.getDescription(), e.getSettings().getProperties(), e.getSettings().getLabels(), e.toString());

            }
            holder.set(searchEntity);
        }

        @Override
        public int getItemCount() {

            switch (_currentEntity) {
                case GROUP:
                    return _groups.size();
                case USER:
                    return _users.size();
                case ACTIVITY:
                    return _activities.size();
                case LABEL:
                    return _labels.size();
                case HASHTAG:
                    return _tags.size();
                default:
                    return _topics.size();
            }

        }
    }

    protected class StringViewHolder extends im.getsocial.demo.fragment.SearchFragment.ViewHolder {

        @BindView(R.id.search_title)
        TextView _title;

        @BindView(R.id.search_description)
        TextView _description;

        @BindView(R.id.search_labels)
        TextView _labels;

        @BindView(R.id.search_properties)
        TextView _properties;

        StringViewHolder(@NonNull final View itemView) {
            super(itemView);
        }

        @Override
        protected void bind(final View itemView) {
            ButterKnife.bind(this, itemView);
        }

        @Override
        protected void invalidate() {
            _title.setText(_item.getTitle());
            _description.setText(_item.getDescription());
            if (_item.getLabels() != null) {
                _labels.setText("Labels: " + joinElements(_item.getLabels()));
            }
            if (_item.getProperties() != null) {
                _properties.setText("Properties: " + joinElements(_item.getProperties()));
            }
        }

        @OnClick(R.id.feed)
        public void showActions() {
            showAlert("Details", _item.getDetails());
        }

    }

    protected abstract class ViewHolder extends RecyclerView.ViewHolder {

        protected final View _parent;
        protected SearchEntity _item;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);
            _parent = itemView;
            bind(itemView);
            _parent.setOnClickListener(view -> showInfo());
        }

        private void showInfo() {
            DialogWithScrollableText.show(formatDescription(), getFragmentManager());
        }

        private String formatDescription() {
            return _item.getTitle();
        }

        protected abstract void bind(View itemView);

        public void set(final SearchEntity item) {
            _item = item;
            invalidate();
        }

        protected abstract void invalidate();
    }
}

