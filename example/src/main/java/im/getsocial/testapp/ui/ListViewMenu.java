/*
 * Published under the MIT License (MIT)
 * Copyright: (c) 2015 GetSocial B.V.
 */

package im.getsocial.testapp.ui;

import java.util.LinkedList;
import java.util.List;

public class ListViewMenu
{
	protected String title;
	protected String subtitle;
	protected ListViewMenu parent;
	protected List<ListViewMenu> menuItems;
	
	public ListViewMenu(String title)
	{
		this.title = title;
		this.menuItems = new LinkedList<ListViewMenu>();
	}
	
	public String getTitle()
	{
		return title;
	}
	
	public void setTitle(String title)
	{
		this.title = title;
	}
	
	public String getSubtitle()
	{
		return subtitle;
	}
	
	public void setSubtitle(String subtitle)
	{
		this.subtitle = subtitle;
	}
	
	public boolean hasParent()
	{
		return parent != null;
	}
	
	public ListViewMenu getParent()
	{
		return parent;
	}
	
	public int size()
	{
		return menuItems.size();
	}
	
	public ListViewMenu getItem(int position)
	{
		return menuItems.get(position);
	}
	
	public ListViewMenu addItem(ListViewMenu menuItem)
	{
		menuItem.parent = this;
		menuItems.add(menuItem);
		
		return  menuItem;
	}

	@Override
	public boolean equals(Object o)
	{
		if(this == o) return true;
		if(o == null || getClass() != o.getClass()) return false;
		
		ListViewMenu that = (ListViewMenu) o;
		
		if(title != null ? !title.equals(that.title) : that.title != null) return false;
		return !(menuItems != null ? !menuItems.equals(that.menuItems) : that.menuItems != null);
		
	}
	
	@Override
	public int hashCode()
	{
		int result = title != null ? title.hashCode() : 0;
		result = 31 * result + (menuItems != null ? menuItems.hashCode() : 0);
		return result;
	}
}
