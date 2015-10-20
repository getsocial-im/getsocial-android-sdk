/*
 * Published under the MIT License (MIT)
 * Copyright: (c) 2015 GetSocial B.V.
 */

package im.getsocial.testapp.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class ListViewMenuAdapter extends BaseAdapter
{
	private Context context;
	private ListViewMenu menu;
	
	public ListViewMenuAdapter(Context context, ListViewMenu menu)
	{
		this.context = context;
		this.menu = menu;
	}
	
	@Override
	public int getCount()
	{
		return menu.size();
	}
	
	@Override
	public Object getItem(int position)
	{
		return menu.getItem(position);
	}
	
	@Override
	public long getItemId(int position)
	{
		return menu.getItem(position).hashCode();
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		if(convertView == null)
		{
			convertView = new ListViewMenuItemView(context);
		}
		
		ListViewMenuItemView menuItemView = (ListViewMenuItemView) convertView;
		menuItemView.bind(menu.getItem(position));
		
		return convertView;
	}
	
	public void setMenu(ListViewMenu menu)
	{
		this.menu = menu;
		notifyDataSetChanged();
	}
	
	public boolean isRootMenu()
	{
		return !menu.hasParent();
	}
	
	public boolean onBackPressed()
	{
		if(menu.hasParent())
		{
			setMenu(menu.getParent());
			return true;
		}
		
		return false;
	}
	
	public ListViewMenu getMenu()
	{
		return menu;
	}
}
