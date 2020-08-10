package im.getsocial.demo.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.Nullable;
import im.getsocial.demo.dialog.DialogWithScrollableText;
import im.getsocial.demo.utils.DynamicUi;
import im.getsocial.sdk.Communities;
import im.getsocial.sdk.GetSocialError;
import im.getsocial.sdk.Notifications;
import im.getsocial.sdk.communities.FollowQuery;
import im.getsocial.sdk.communities.UserId;
import im.getsocial.sdk.communities.UserIdList;
import im.getsocial.sdk.notifications.NotificationContent;
import im.getsocial.sdk.notifications.SendNotificationTarget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestUsersByIdFragment extends BaseFragment {

	private final List<MenuOption> _options = Arrays.asList(
					new MenuOption("Add Friends", this::addFriends, 0x10),
					new MenuOption("Remove Friends", this::removeFriends, 0x11),
					new MenuOption("Are Friends", this::areFriends, 0x12),
					new MenuOption("Set Friends", this::setFriends, 0x13),
					new MenuOption("Send Notification", this::sendNotifications, 0x14),
					new MenuOption("Get Users", this::getUsers, 0x15),
					new MenuOption("Is Following", this::isFollowing, 0x16),
					new MenuOption("Follow", this::follow, 0x17),
					new MenuOption("Unfollow", this::unfollow, 0x18)
	);

	private EditText _providerId;
	private final List<DynamicUi.DynamicInputHolder> _userIds = new ArrayList<>();

	@Override
	public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		final LinearLayout layout = new LinearLayout(getContext());
		layout.setOrientation(LinearLayout.VERTICAL);

		_providerId = new EditText(getContext());
		_providerId.setHint("Provider ID(leave empty to use GetSocial ID)");
		layout.addView(_providerId);

		final Button addRow = new Button(getContext());
		addRow.setText("Add user");
		addRow.setOnClickListener(this::addRow);
		layout.addView(addRow);
		return layout;
	}

	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
		for (final MenuOption option : _options) {
			menu.add(Menu.NONE, option._id, Menu.NONE, option._name);
		}
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(final android.view.MenuItem item) {
		for (final MenuOption option : _options) {
			if (item.getItemId() == option._id) {
				option._action.run();
				return true;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	private void addRow(final View view) {
		DynamicUi.createDynamicTextRow(getContext(), (ViewGroup) getView(), _userIds, "User ID");
	}

	private List<String> ids() {
		final List<String> userIds = new ArrayList<>();
		for (final DynamicUi.DynamicInputHolder holder : _userIds) {
			userIds.add(holder.getText(0));
		}
		return userIds;
	}

	private UserIdList userIdList() {
		final String providerId = _providerId.getText().toString();
		if (providerId.isEmpty()) {
			return UserIdList.create(ids());
		} else {
			return UserIdList.createWithProvider(providerId, ids());
		}
	}

	private void addFriends() {
		Communities.addFriends(userIdList(),
						friendsCount -> Toast.makeText(getContext(), "Successfully added friends, new count: " + friendsCount, Toast.LENGTH_SHORT).show(),
						this::onError);
	}

	private void removeFriends() {
		Communities.removeFriends(userIdList(),
						friendsCount -> Toast.makeText(getContext(), "Successfully removed friends, new count: " + friendsCount, Toast.LENGTH_SHORT).show(),
						this::onError);
	}

	private void areFriends() {
		Communities.areFriends(userIdList(),
						areFriends -> DialogWithScrollableText.show(areFriends.toString(), getFragmentManager()),
						this::onError);
	}

	private void sendNotifications() {
		Notifications.send(NotificationContent.notificationWithText("Hey, just testing feature!"), SendNotificationTarget.users(userIdList()),
						() -> Toast.makeText(getContext(), "Successfully sent notifications", Toast.LENGTH_SHORT).show(),
						this::onError);
	}

	private void setFriends() {
		Communities.setFriends(userIdList(),
						friendsCount -> Toast.makeText(getContext(), "Successfully set friends, new count: " + friendsCount, Toast.LENGTH_SHORT).show(),
						this::onError);
	}

	private void getUsers() {
		Communities.getUsers(userIdList(),
						users -> DialogWithScrollableText.show(users.toString(), getFragmentManager()),
						this::onError);
	}

	private void isFollowing() {
		Communities.isFollowing(UserId.currentUser(), FollowQuery.users(userIdList()),
						isFollowing -> DialogWithScrollableText.show(isFollowing.toString(), getFragmentManager()),
						this::onError);
	}

	private void follow() {
		Communities.follow(FollowQuery.users(userIdList()),
						followCount -> Toast.makeText(getContext(), "Successfully followed users, new count: " + followCount, Toast.LENGTH_SHORT).show(),
						this::onError);
	}

	private void unfollow() {
		Communities.unfollow(FollowQuery.users(userIdList()),
						followCount -> Toast.makeText(getContext(), "Successfully unfollowed users, new count: " + followCount, Toast.LENGTH_SHORT).show(),
						this::onError);
	}

	private void onError(final GetSocialError getSocialError) {
		Toast.makeText(getContext(), getSocialError.toString(), Toast.LENGTH_SHORT).show();
	}

	@Override
	public String getFragmentTag() {
		return "users-by-id";
	}

	@Override
	public String getTitle() {
		return "Users By Id";
	}

	static class MenuOption {
		final String _name;
		final Runnable _action;
		final int _id;

		MenuOption(final String name, final Runnable action, final int id) {
			_name = name;
			_action = action;
			_id = id;
		}
	}
}
