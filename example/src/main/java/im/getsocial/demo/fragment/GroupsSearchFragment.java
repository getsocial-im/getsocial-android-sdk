package im.getsocial.demo.fragment;

import im.getsocial.sdk.communities.GroupsQuery;

public class GroupsSearchFragment extends BaseGroupsFragment {

	@Override
	protected GroupsQuery createQuery(final String query) {
		return GroupsQuery.find(query);
	}

	@Override
	public String getFragmentTag() {
		return "groups";
	}

	@Override
	public String getTitle() {
		return "Groups";
	}
}
