package im.getsocial.demo.fragment;

import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import im.getsocial.demo.R;
import im.getsocial.demo.dialog.DialogWithScrollableText;
import im.getsocial.demo.dialog.SortOrderDelegate;
import im.getsocial.demo.dialog.SortOrderDialog;
import im.getsocial.demo.dialog.action_dialog.ActionDialog;
import im.getsocial.sdk.Communities;
import im.getsocial.sdk.GetSocialError;
import im.getsocial.sdk.common.PagingQuery;
import im.getsocial.sdk.common.PagingResult;
import im.getsocial.sdk.communities.ActivitiesQuery;
import im.getsocial.sdk.communities.FollowQuery;
import im.getsocial.sdk.communities.Tag;
import im.getsocial.sdk.communities.TagsQuery;
import im.getsocial.sdk.communities.UserId;
import im.getsocial.sdk.ui.communities.ActivityFeedViewBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class TagsListFragment extends BaseFragment {

	private final Timer _timer = new Timer();
	private TimerTask _pendingTask;

	@BindView(R.id.swiperefresh)
	public SwipeRefreshLayout _swipeRefreshLayout;

	@BindView(R.id.search_text)
	public EditText _query;

	@BindView(R.id.list)
	public RecyclerView _listView;

	@BindView(R.id.error)
	public TextView _error;

	private StringAdapter _adapter;

	private boolean _myTagsOnly;
	private boolean _isTrending = false;
	private String _sortKey;
	private String _sortDirection;

	@Override

	public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		return inflater.inflate(R.layout.fragment_base_search, container, false);
	}

	@Override
	public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		ButterKnife.bind(this, view);
		final LinearLayoutManager manager = new LinearLayoutManager(getContext());
		_listView.setLayoutManager(manager);
		final DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
		_listView.addItemDecoration(dividerItemDecoration);
		_adapter = new StringAdapter();
		_listView.setAdapter(_adapter);
		_swipeRefreshLayout.setOnRefreshListener(this::loadItems);

		loadItems();
	}

	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
		menu.add(Menu.NONE, 0x42, Menu.NONE, _myTagsOnly ? "Show all" : "Followed by me");
		menu.add(Menu.NONE, 0x43, Menu.NONE, _isTrending ? "All" : "Trending");
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(final android.view.MenuItem item) {
		if (item.getItemId() == 0x42) {
			filter();
			return true;
		}
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
			sortOptions.add(new Pair<>("name",""));
			sortOptions.add(new Pair<>("name","-"));
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

	private void filter() {
		_myTagsOnly = !_myTagsOnly;
		getActivity().invalidateOptionsMenu();
		_query.setVisibility(_myTagsOnly ? View.GONE : View.VISIBLE);
		loadItems();
	}


	protected void loadItems() {
		_swipeRefreshLayout.setRefreshing(true);
		TagsQuery query = null;
		if (_myTagsOnly) {
			query = TagsQuery.followedByUser(UserId.currentUser());
		} else {
			query = TagsQuery.find(query());
		}
		query = query.onlyTrending(_isTrending);

		Communities.getTags(new PagingQuery<>(query), this::saveResult, this::onError);
	}

	protected void onError(final GetSocialError error) {
		Toast.makeText(getContext(), error.toString(), Toast.LENGTH_SHORT).show();
	}

	private void saveResult(final PagingResult<Tag> result) {
		_swipeRefreshLayout.setRefreshing(false);
		_adapter._items.clear();
		_adapter._items.addAll(result.getEntries());
		_adapter.notifyDataSetChanged();
	}

	private String query() {
		return _query.getText().toString();
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

	protected class StringAdapter extends RecyclerView.Adapter<StringViewHolder> {

		private final List<Tag> _items = new ArrayList<>();

		@NonNull
		@Override
		public StringViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
			final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_tag, parent, false);
			return new StringViewHolder(view);
		}

		@Override
		public void onBindViewHolder(@NonNull final StringViewHolder holder, final int position) {
			holder.set(_items.get(position));
		}

		@Override
		public int getItemCount() {
			return _items.size();
		}
	}

	protected class StringViewHolder extends ViewHolder {

		@BindView(R.id.name)
		TextView _name;

		@BindView(R.id.tag_score)
		TextView _score;

		@BindView(R.id.tag_activityCount)
		TextView _activityCount;

		@BindView(R.id.tag_followersCount)
		TextView _followersCount;

		boolean _isFollowing;

		StringViewHolder(@NonNull final View itemView) {
			super(itemView);
		}

		@Override
		protected void bind(final View itemView) {
			ButterKnife.bind(this, itemView);
		}

		@Override
		protected void invalidate() {
			_name.setText("Name: " + _item.getName());
			_score.setText("Popularity: " + _item.getPopularity());
			_activityCount.setText("Activity count: " + _item.getActivitiesCount());
			_followersCount.setText("Followers count: " + _item.getFollowersCount());
			_isFollowing = _item.isFollower();
		}

		@OnClick(R.id.feed)
		public void showActions() {
			final ActionDialog dialog = new ActionDialog(getContext());
			dialog.addAction(new ActionDialog.Action("Details") {
				@Override
				public void execute() {
					showAlert("Details", _item.toString());
				}
			});
			dialog.addAction(new ActionDialog.Action("Feed UI") {
				@Override
				public void execute() {
					openFeed();
				}
			});
			dialog.addAction(new ActionDialog.Action(_isFollowing ? "Unfollow" : "Follow") {
				@Override
				public void execute() {
					followTag();
				}
			});
			dialog.addAction(new ActionDialog.Action("Followers") {
				@Override
				public void execute() {
					openFollowers();
				}
			});
			dialog.show();
		}

		void openFeed() {
			ActivitiesQuery query = ActivitiesQuery.everywhere().withTag(_item.getName());
			ActivityFeedViewBuilder builder = ActivityFeedViewBuilder.create(query);
			builder.show();
		}
		void followTag() {
			final String tag = _item.getName();

			if (_isFollowing) {
				Communities.unfollow(FollowQuery.tags(tag), result -> {
					if (_item.getName().equals(tag)) {
						_isFollowing = false;
					}
				}, error -> Toast.makeText(getContext(), error.toString(), Toast.LENGTH_SHORT).show());
			} else {
				Communities.follow(FollowQuery.tags(tag), result -> {
					if (_item.getName().equals(tag)) {
						_isFollowing = true;
					}
				}, error -> Toast.makeText(getContext(), error.toString(), Toast.LENGTH_SHORT).show());
			}
		}

		void openFollowers() {
			final TagFollowersFragment fragment = TagFollowersFragment.create(_item.getName());
			addContentFragment(fragment);
		}

	}

	protected abstract class ViewHolder extends RecyclerView.ViewHolder {

		protected final View _parent;
		protected Tag _item;

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
			return _item.getName();
		}

		protected abstract void bind(View itemView);

		public void set(final Tag item) {
			_item = item;
			invalidate();
		}

		protected abstract void invalidate();
	}

	@Override
	public String getFragmentTag() {
		return "tags";
	}

	@Override
	public String getTitle() {
		return "Tags";
	}
}
