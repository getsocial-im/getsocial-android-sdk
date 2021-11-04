package im.getsocial.demo.fragment;

import im.getsocial.sdk.communities.GroupsQuery;
import im.getsocial.sdk.communities.UserId;

public class MyGroupsFragment extends BaseGroupsFragment {

	@Override
	protected GroupsQuery createQuery(final SearchObject searchObject) {
		return GroupsQuery.find(searchObject.searchTerm).withMember(UserId.currentUser());
	}

	@Override
	public String getFragmentTag() {
		return "my_groups";
	}

	@Override
	public String getTitle() {
		return "My Groups";
	}
}
