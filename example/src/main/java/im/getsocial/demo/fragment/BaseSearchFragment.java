package im.getsocial.demo.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
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
import butterknife.OnTextChanged;
import im.getsocial.demo.R;
import im.getsocial.demo.dialog.DialogWithScrollableText;
import im.getsocial.demo.utils.EndlessRecyclerViewScrollListener;
import im.getsocial.sdk.Callback;
import im.getsocial.sdk.FailureCallback;
import im.getsocial.sdk.GetSocialError;
import im.getsocial.sdk.common.PagingQuery;
import im.getsocial.sdk.common.PagingResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public abstract class BaseSearchFragment<Query, Item> extends BaseFragment {

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

	private String _nextCursor;
	private boolean _isLastPage;
	private BaseSearchAdapter<? extends ViewHolder> _adapter;
	private EndlessRecyclerViewScrollListener _scrollListener;

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
		_adapter = createAdapter();
		_listView.setAdapter(_adapter);
		_scrollListener = new EndlessRecyclerViewScrollListener(manager) {
			@Override
			public void onLoadMore(final int page, final int totalItemsCount, final RecyclerView view) {
				loadNext();
			}
		};
		_listView.addOnScrollListener(_scrollListener);
		_swipeRefreshLayout.setOnRefreshListener(this::loadItems);

		loadItems();
	}

	protected abstract BaseSearchAdapter<? extends ViewHolder> createAdapter();

	public BaseSearchAdapter<? extends ViewHolder> getAdapter() {
		return _adapter;
	}

	public void removeItem(Item item) {
		int position = _adapter._items.indexOf(item);
		_adapter._items.remove(position);
		_adapter.notifyItemRemoved(position);
	}
	public void updateItem(Item oldItem, Item newItem) {
		int position = _adapter._items.indexOf(oldItem);
		_adapter._items.set(position, newItem);
		_adapter.notifyItemChanged(position);
	}

	protected void loadItems() {
		_scrollListener.resetState();
		_swipeRefreshLayout.setRefreshing(true);
		load(new PagingQuery<>(createQuery(query())), result -> saveResult(result, true), this::onError);
	}

	private void loadNext() {
		if (!_isLastPage) {
			_swipeRefreshLayout.setRefreshing(true);
			load(new PagingQuery<>(createQuery(query())).next(_nextCursor), result -> saveResult(result, false), this::onError);
		}
	}

	protected void onError(final GetSocialError error) {
		Toast.makeText(getContext(), error.toString(), Toast.LENGTH_SHORT).show();
	}

	private void saveResult(final PagingResult<Item> result, final boolean shouldReplace) {
		_swipeRefreshLayout.setRefreshing(false);
		_nextCursor = result.nextCursor();
		_isLastPage = result.isLastPage();
		if (shouldReplace) {
			_adapter._items.clear();
		}
		_adapter._items.addAll(result.getEntries());
		_adapter.notifyDataSetChanged();
		onDataChanged(_adapter._items);
	}

	protected void onDataChanged(final List<Item> items) {
		// override in children
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
				getActivity().runOnUiThread(BaseSearchFragment.this::loadItems);
			}
		};
		_timer.schedule(_pendingTask, 300);
	}

	protected String validate(final CharSequence newText) {
		return null;
	}

	protected abstract void load(PagingQuery<Query> query, Callback<PagingResult<Item>> success, FailureCallback failure);

	protected abstract Query createQuery(String query);

	protected abstract class BaseSearchAdapter<VH extends ViewHolder> extends RecyclerView.Adapter<VH> {

		private final List<Item> _items = new ArrayList<>();

		@Override
		public void onBindViewHolder(@NonNull final VH holder, final int position) {
			holder.set(_items.get(position));
		}

		@Override
		public int getItemCount() {
			return _items.size();
		}

		public int getItemPosition(Item item) {
			return _items.indexOf(item);
		}
	}

	protected abstract class ViewHolder extends RecyclerView.ViewHolder {

		protected final View _parent;
		protected Item _item;

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
			return _item.toString();
		}

		protected abstract void bind(View itemView);

		public void set(final Item item) {
			_item = item;
			invalidate();
		}

		protected abstract void invalidate();
	}
}
