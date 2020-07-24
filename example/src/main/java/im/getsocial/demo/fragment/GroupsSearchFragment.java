package im.getsocial.demo.fragment;

import android.view.Menu;
import android.view.MenuInflater;
import im.getsocial.sdk.GetSocial;
import im.getsocial.sdk.communities.CurrentUser;
import im.getsocial.sdk.communities.GroupsQuery;

public class GroupsSearchFragment extends BaseGroupsFragment {
	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
		menu.add(Menu.NONE, 0x42, Menu.NONE, "My groups only");
		menu.add(Menu.NONE, 0x43, Menu.NONE, "Followed by me");
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(final android.view.MenuItem item) {
		final CurrentUser user = GetSocial.getCurrentUser();
		if (item.getItemId() == 0x42) {
			if (user == null) {
				return true;
			}
			addContentFragment(GroupsFollowedOrWithMemberFragment.create(user.getId(), user.getDisplayName(), true));
			return true;
		} else if (item.getItemId() == 0x43) {
			if (user == null) {
				return true;
			}
			addContentFragment(GroupsFollowedOrWithMemberFragment.create(user.getId(), user.getDisplayName(), false));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

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
