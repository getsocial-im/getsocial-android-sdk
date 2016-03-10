/*
 *    	Copyright 2015-2016 GetSocial B.V.
 *
 *	Licensed under the Apache License, Version 2.0 (the "License");
 *	you may not use this file except in compliance with the License.
 *	You may obtain a copy of the License at
 *
 *    	http://www.apache.org/licenses/LICENSE-2.0
 *
 *	Unless required by applicable law or agreed to in writing, software
 *	distributed under the License is distributed on an "AS IS" BASIS,
 *	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *	See the License for the specific language governing permissions and
 *	limitations under the License.
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
