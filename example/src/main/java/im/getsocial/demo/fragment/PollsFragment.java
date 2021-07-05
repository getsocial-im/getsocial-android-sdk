package im.getsocial.demo.fragment;

import java.util.Arrays;
import java.util.List;

import im.getsocial.demo.adapter.MenuItem;

public class PollsFragment extends BaseListFragment {
	@Override
	protected List<MenuItem> createListData() {
		return Arrays.asList(
						navigationListItem("Create Poll", CreatePollFragment.class)
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
