/*
*    	Copyright 2015-2017 GetSocial B.V.
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

package im.getsocial.demo.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import butterknife.ButterKnife;
import im.getsocial.demo.R;
import im.getsocial.demo.adapter.MenuAdapter;
import im.getsocial.demo.adapter.MenuItem;

import java.util.List;

import static im.getsocial.demo.R.id.recyclerView;

public abstract class BaseListFragment extends BaseFragment {

	private ViewContainer _viewContainer;

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_menu, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		_viewContainer = new ViewContainer(view);
	}

	@Override
	public void onResume() {
		super.onResume();
		invalidateList();
	}

	protected MenuItem navigationListItem(String name, Class<? extends Fragment> fragmentClass) {
		return navigationListItem(name, fragmentClass, null);
	}

	protected MenuItem navigationListItem(String name, Class<? extends Fragment> fragmentClass, @Nullable NavigationItemDecorator decorator) {
		MenuItem.Builder builder = MenuItem.builder(name).withAction(new OpenFragmentAction(fragmentClass));
		if (decorator != null) {
			decorator.decorate(builder);
		}
		return builder.build();
	}


	public void invalidateList() {
		_viewContainer._recyclerView.getAdapter().notifyDataSetChanged();
	}

	protected abstract List<MenuItem> createListData();

	class ViewContainer {
		private final LinearLayoutManager _linearLayoutManager;
		@BindView(recyclerView)
		RecyclerView _recyclerView;

		ViewContainer(View view) {
			ButterKnife.bind(this, view);

			_linearLayoutManager = new LinearLayoutManager(getContext());
			_recyclerView.setLayoutManager(_linearLayoutManager);

			DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(),
					_linearLayoutManager.getOrientation());
			_recyclerView.addItemDecoration(dividerItemDecoration);

			MenuAdapter adapter = new MenuAdapter(createListData());
			_recyclerView.setAdapter(adapter);
		}
	}

	protected class OpenFragmentAction implements MenuItem.Action {

		private final Class<? extends Fragment> _fragmentClass;

		OpenFragmentAction(Class<? extends Fragment> fragmentClass) {
			_fragmentClass = fragmentClass;
		}

		@Override
		public void execute() {
			try {
				addContentFragment(_fragmentClass.newInstance());
			} catch (Exception exception) {
				throw new RuntimeException(exception);
			}
		}
	}

	protected interface NavigationItemDecorator {
		void decorate(MenuItem.Builder builder);
	}
}
