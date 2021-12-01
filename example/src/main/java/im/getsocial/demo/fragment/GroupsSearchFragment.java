package im.getsocial.demo.fragment;

import android.os.Bundle;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import im.getsocial.demo.dialog.SortOrderDelegate;
import im.getsocial.demo.dialog.SortOrderDialog;
import im.getsocial.sdk.communities.GroupsQuery;

public class GroupsSearchFragment extends BaseGroupsFragment {

	private boolean _isTrending = false;
	private String _sortKey;
	private String _sortDirection;

	@Override
	protected GroupsQuery createQuery(final SearchObject searchObject) {
		GroupsQuery query = GroupsQuery.find(searchObject.searchTerm);
		if (searchObject.labels != null) {
			query = query.withLabels(searchObject.labels);
		}
		if (searchObject.properties != null) {
			query = query.withProperties(searchObject.properties);
		}
		query = query.onlyTrending(_isTrending);
		return query;
	}

	@Override
	public String getFragmentTag() {
		return "groups";
	}

	@Override
	public String getTitle() {
		return "Groups";
	}

	@Override
	public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		super.showAdvancedSearch();
	}

	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
		menu.add(Menu.NONE, 0x42, Menu.NONE, _isTrending ? "All" : "Trending");
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(final android.view.MenuItem item) {
		if (item.getItemId() == 0x42) {
			_isTrending = !_isTrending;
			_sortDirection = null;
			_sortKey = null;
			loadItems();
			getActivity().invalidateOptionsMenu();
			return true;
		}
		if (item.getItemId() == 0x43) {
			showSortDialog();
			getActivity().invalidateOptionsMenu();
			return true;
		}
		return false;
	}

	private void showSortDialog() {
		List<Pair<String, String>> sortOptions = new ArrayList<>();
		if (this._isTrending) {
		} else {
			sortOptions.add(new Pair<>("id",""));
			sortOptions.add(new Pair<>("id","-"));
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
}
