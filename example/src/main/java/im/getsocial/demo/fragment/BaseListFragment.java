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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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
	public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_menu, container, false);
	}

	@Override
	public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		_viewContainer = new ViewContainer(view);
	}

	@Override
	public void onResume() {
		super.onResume();
		invalidateList();
	}

	protected MenuItem navigationListItem(final String name, final Class<? extends Fragment> fragmentClass) {
		return navigationListItem(name, fragmentClass, null);
	}

	protected MenuItem navigationListItem(final String name, final Class<? extends Fragment> fragmentClass, @Nullable final NavigationItemDecorator decorator) {
		final MenuItem.Builder builder = MenuItem.builder(name).withAction(new OpenFragmentAction(fragmentClass));
		if (decorator != null) {
			decorator.decorate(builder);
		}
		return builder.build();
	}


	public void invalidateList() {
		if (_viewContainer == null || _viewContainer._recyclerView == null || _viewContainer._recyclerView.getAdapter() == null) {
			return;
		}
		_viewContainer._recyclerView.getAdapter().notifyDataSetChanged();
	}

	protected abstract List<MenuItem> createListData();

	protected interface NavigationItemDecorator {
		void decorate(MenuItem.Builder builder);
	}

	class ViewContainer {

		@BindView(recyclerView)
		RecyclerView _recyclerView;

		ViewContainer(final View view) {
			ButterKnife.bind(this, view);

			final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
			_recyclerView.setLayoutManager(linearLayoutManager);

			final DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(),
							linearLayoutManager.getOrientation());
			_recyclerView.addItemDecoration(dividerItemDecoration);

			final MenuAdapter adapter = new MenuAdapter(createListData());
			_recyclerView.setAdapter(adapter);
		}
	}

	protected class OpenFragmentAction implements MenuItem.Action {

		private final Class<? extends Fragment> _fragmentClass;

		OpenFragmentAction(final Class<? extends Fragment> fragmentClass) {
			_fragmentClass = fragmentClass;
		}

		@Override
		public void execute() {
			try {
				addContentFragment(_fragmentClass.newInstance());
			} catch (final Exception exception) {
				throw new RuntimeException(exception);
			}
		}
	}
}
