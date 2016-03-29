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
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import im.getsocial.testapp.R;

public class ListViewMenuItemView extends RelativeLayout implements CompoundButton.OnCheckedChangeListener
{
	private ListViewMenu menuItem;
	
	private TextView titleTextView;
	private TextView subtitleTextView;
	private CheckBox checkBox;
	
	public ListViewMenuItemView(Context context)
	{
		super(context);
		
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		RelativeLayout rootView = (RelativeLayout) inflater.inflate(R.layout.list_view_menu_row, this, true);
		
		titleTextView = (TextView) rootView.findViewById(R.id.title_textView);
		subtitleTextView = (TextView) rootView.findViewById(R.id.subtitle_textView);
		checkBox = (CheckBox) rootView.findViewById(R.id.menuItem_checkBox);
		
		checkBox.setOnCheckedChangeListener(this);
		
		setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
	}
	
	public ListViewMenuItemView(Context context, ListViewMenu menuItem)
	{
		super(context);
		this.menuItem = menuItem;
	}
	
	public void bind(ListViewMenu menuItem)
	{
		this.menuItem = menuItem;
		titleTextView.setText(menuItem.getTitle());
		
		String subtitle = menuItem.getSubtitle();
		boolean isSubtitleVisible = subtitle != null && !subtitle.isEmpty();
		subtitleTextView.setText(isSubtitleVisible ? subtitle : "");
		subtitleTextView.setVisibility(isSubtitleVisible ? VISIBLE : GONE);
		
		boolean isCheckBoxView = menuItem instanceof CheckboxListViewMenu;
		checkBox.setVisibility(isCheckBoxView ? VISIBLE : GONE);
		if(isCheckBoxView)
		{
			checkBox.setChecked(((CheckboxListViewMenu) menuItem).isChecked());
		}

		titleTextView.setContentDescription(this.menuItem.getTitle());
		subtitleTextView.setContentDescription(this.menuItem.getSubtitle());
	}
	
	public ListViewMenu getMenuItem()
	{
		return menuItem;
	}
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
	{
		((CheckboxListViewMenu)menuItem).setIsChecked(isChecked);
	}
}
