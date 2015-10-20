/*
 * Published under the MIT License (MIT)
 * Copyright: (c) 2015 GetSocial B.V.
 */

package im.getsocial.testapp.ui;

public class CheckboxListViewMenu extends ListViewMenu
{
	private OnCheckboxListViewMenuChecked onCheckChanged;
	private boolean isChecked;
	
	public CheckboxListViewMenu(String title, boolean isChecked, OnCheckboxListViewMenuChecked onCheckChanged)
	{
		super(title);
		this.isChecked = isChecked;
		this.onCheckChanged = onCheckChanged;
	}
	
	public boolean isChecked()
	{
		return isChecked;
	}
	
	public void setIsChecked(boolean isChecked)
	{
		if(isChecked != this.isChecked)
		{
			this.isChecked = isChecked;
			if(onCheckChanged != null)
			{
				onCheckChanged.onCheckChanged(isChecked);
			}
		}
	}
}
