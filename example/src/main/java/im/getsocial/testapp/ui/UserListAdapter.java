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
public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserListViewHolder>
{
	private List<User> users;

	@Override
	public UserListViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_users_list, parent, false);
		return new UserListViewHolder(view);
	}

	@Override
	public void onBindViewHolder(UserListViewHolder holder, int position)
	{
		User user = users.get(position);
		holder.userNameTextView.setText(user.getDisplayName());
	}

	@Override
	public int getItemCount()
	{
		return users == null ? 0 : users.size();
	}

	public void setUsers(List<User> users)
	{
		this.users = users;
	}

	public class UserListViewHolder extends RecyclerView.ViewHolder
	{
		TextView userNameTextView;

		public UserListViewHolder(View itemView)
		{
			super(itemView);
			userNameTextView = (TextView) itemView.findViewById(R.id.user_name);
		}
	}
}
