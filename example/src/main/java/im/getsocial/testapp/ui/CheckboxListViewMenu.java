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
