/*
 * Published under the MIT License (MIT)
 * Copyright: (c) 2015 GetSocial B.V.
 */

package im.getsocial.testapp.ui;

public class ActionableListViewMenu extends ListViewMenu
{
	private ListViewMenuItemAction action;
	
	public ActionableListViewMenu(String title, ListViewMenuItemAction action)
	{
		super(title);
		this.action = action;
	}
	
	public void invokeAction()
	{
		action.execute(this);
	}
}
