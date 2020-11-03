package im.getsocial.demo.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import androidx.annotation.Nullable;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import im.getsocial.demo.R;
import im.getsocial.demo.utils.DynamicUi;
import im.getsocial.sdk.communities.CommunitiesAction;
import im.getsocial.sdk.communities.GroupContent;
import im.getsocial.sdk.communities.Role;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateGroupFragment extends BaseFragment {

	private ViewContainer _viewContainer;
	private final List<DynamicUi.DynamicInputHolder> _propertiesHolder = new ArrayList<>();

	@Override
	public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_create_group, container, false);
	}

	@Override
	public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		_viewContainer = new ViewContainer(view);
	}

	@Override
	public String getFragmentTag() {
		return "create_group";
	}

	@Override
	public String getTitle() {
		return "Create Group";
	}

	private void createGroup() {
		final Map<String, String> properties = new HashMap<>();
		for (final DynamicUi.DynamicInputHolder holder : _propertiesHolder) {
			properties.put(holder.getText(0), holder.getText(1));
		}
		final GroupContent content = GroupContent.create(_viewContainer._id.getText().toString())
						.withTitle(_viewContainer._name.getText().toString())
						.withDescription(_viewContainer._description.getText().toString())
						.withDiscoverable(_viewContainer._discoverable.isChecked())
						.withPrivate(_viewContainer._private.isChecked())
						.withProperties(properties);
		content.setPermission(CommunitiesAction.POST, Role.values()[_viewContainer._post.getSelectedItemPosition()]);
		content.setPermission(CommunitiesAction.COMMENT, Role.values()[_viewContainer._interact.getSelectedItemPosition()]);
		content.setPermission(CommunitiesAction.REACT, Role.values()[_viewContainer._interact.getSelectedItemPosition()]);
//		Communities.createGroup(content, group -> {
//			_log.logInfoAndToast("Created group:" + group);
//		}, error -> {
//			_log.logErrorAndToast(error);
//		});
	}

	class ViewContainer {

		@BindView(R.id.group_id)
		EditText _id;

		@BindView(R.id.group_name)
		EditText _name;

		@BindView(R.id.group_description)
		EditText _description;

		@BindView(R.id.is_private)
		CheckBox _private;

		@BindView(R.id.discoverable)
		CheckBox _discoverable;

		@BindView(R.id.properties)
		LinearLayout _properties;

		@BindView(R.id.permission_interact)
		Spinner _interact;

		@BindView(R.id.permission_post)
		Spinner _post;

		@OnClick(R.id.create_group)
		public void create() {
			createGroup();
		}

		@OnClick(R.id.add_property)
		void addProperty() {
			createRow("", "");
		}

		private void createRow(final String k, final String v) {
			final DynamicUi.DynamicInputHolder inputHolder = DynamicUi.createDynamicTextRow(getContext(), _properties, _propertiesHolder, "Key", "Value");
			final EditText key = inputHolder.getView(0);
			final EditText val = inputHolder.getView(1);
			key.setText(k);
			val.setText(v);
		}


		public ViewContainer(final View view) {
			ButterKnife.bind(this, view);
			_interact.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, Role.values()));
			_interact.setSelection(2);
			_post.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, Role.values()));
			_post.setSelection(2);
		}
	}

}
