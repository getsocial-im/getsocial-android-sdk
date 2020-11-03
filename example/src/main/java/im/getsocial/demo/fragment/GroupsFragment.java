package im.getsocial.demo.fragment;

import im.getsocial.demo.adapter.MenuItem;

import java.util.Arrays;
import java.util.List;

public class GroupsFragment extends BaseListFragment {
	@Override
	protected List<MenuItem> createListData() {
		return Arrays.asList(
						navigationListItem("Create Group", CreateGroupFragment.class),
						navigationListItem("Find Groups", GroupsSearchFragment.class)
		);
	}

	@Override
	public String getFragmentTag() {
		return null;
	}

	@Override
	public String getTitle() {
		return null;
	}
}
