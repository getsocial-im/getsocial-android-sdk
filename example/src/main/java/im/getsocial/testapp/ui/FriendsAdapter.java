package im.getsocial.testapp.ui;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import im.getsocial.sdk.core.User;
import im.getsocial.testapp.R;

/**
 * Created by manuMohan on 16/03/2016.
 */
public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendViewHolder>
{
	private List<User> friends;

	@Override
	public FriendViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_users_list, parent, false);
		return new FriendViewHolder(view);
	}

	@Override
	public void onBindViewHolder(FriendViewHolder holder, int position)
	{
		User user = friends.get(position);
		holder.friendNameTextView.setText(position + ". " + user.getDisplayName());
	}

	@Override
	public int getItemCount()
	{
		return friends == null ? 0 : friends.size();
	}

	public void setFriends(List<User> friends)
	{
		this.friends = friends;
	}

	public class FriendViewHolder extends RecyclerView.ViewHolder
	{
		TextView friendNameTextView;

		public FriendViewHolder(View itemView)
		{
			super(itemView);
			friendNameTextView = (TextView) itemView.findViewById(R.id.user_name);
		}
	}

	public interface FriendsListener
	{
		void friendSelected(User user);
	}
}
