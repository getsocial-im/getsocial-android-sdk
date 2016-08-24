package im.getsocial.testapp.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import im.getsocial.sdk.core.GetSocial;
import im.getsocial.sdk.core.UI.builder.UserListViewBuilder;
import im.getsocial.sdk.core.User;
import im.getsocial.testapp.R;


public class UserListFragment extends DialogFragment
{
	private OnFragmentInteractionListener mListener;
	private UserListAdapter userListAdapter;
	private RecyclerView friendsRecyclerView;
	private TextView emptyView;


	public UserListFragment()
	{
		// Required empty public constructor
	}

	public static UserListFragment newInstance()
	{
		UserListFragment friendsFragment = new UserListFragment();
		friendsFragment.setStyle(DialogFragment.STYLE_NORMAL, R.style.GetSocialDialog);
		return friendsFragment;

	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState)
	{
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_friends, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);
		setup(view);
	}

	private void setup(View view)
	{
		userListAdapter = new UserListAdapter();
		friendsRecyclerView = (RecyclerView) view.findViewById(R.id.friends_list);
		friendsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		friendsRecyclerView.setAdapter(userListAdapter);

		emptyView = (TextView) view.findViewById(R.id.empty_view);
	}

	@Override
	public void onAttach(Context context)
	{
		super.onAttach(context);
	}

	@Override
	public void onDetach()
	{
		super.onDetach();
		mListener = null;
	}


	public void setUsers(List<User> users)
	{
		getDialog().setTitle("Users");

		userListAdapter.setUsers(users);
		userListAdapter.notifyDataSetChanged();
		if(users == null || users.size() == 0)
		{
			emptyView.setVisibility(View.VISIBLE);
		}
		else
		{
			emptyView.setVisibility(View.GONE);
		}
	}


	public interface OnFragmentInteractionListener
	{

	}
}
