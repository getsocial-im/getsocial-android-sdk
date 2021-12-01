package im.getsocial.demo.fragment;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import im.getsocial.demo.R;
import im.getsocial.demo.utils.DynamicUi;
import im.getsocial.sdk.Communities;
import im.getsocial.sdk.communities.ActivityButton;
import im.getsocial.sdk.communities.CommunitiesAction;
import im.getsocial.sdk.communities.GetSocialActivity;
import im.getsocial.sdk.communities.Group;
import im.getsocial.sdk.communities.GroupContent;
import im.getsocial.sdk.communities.Role;
import im.getsocial.sdk.media.MediaAttachment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.squareup.picasso.Picasso.with;

public class CreateGroupFragment extends BaseFragment {

	private ViewContainer _viewContainer;
	private final List<DynamicUi.DynamicInputHolder> _propertiesHolder = new ArrayList<>();
	private String _groupId;

	public static Fragment updateGroup(final String groupId) {
		final CreateGroupFragment fragment = new CreateGroupFragment();
		final Bundle args = new Bundle();
		args.putString("group", groupId);
		fragment.setArguments(args);
		return fragment;
	}


	@Override
	public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_create_group, container, false);
	}

	@Override
	public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		_viewContainer = new ViewContainer(view);
		final Bundle bundle = getArguments();
		if (bundle != null) {
			_groupId = bundle.getString("group");

			if (_groupId != null) {
				Communities.getGroup(_groupId, group -> {
					_viewContainer.setGroup(group);
				}, error -> {
					Toast.makeText(getContext(), "Failed to edit group: " + error.getMessage(), Toast.LENGTH_SHORT).show();
				});
			}

		}
	}

	@Override
	public String getFragmentTag() {
		return "create_group";
	}

	@Override
	public String getTitle() {
		return _groupId == null ? "Create Group" : "Update Group";
	}

	@Override
	protected void onImagePickedFromDevice(final Uri imageUri, final int requestCode) {
		final ImageView imageView = _viewContainer._groupAvatarImageView;
		_viewContainer._groupAvatarImageView.setVisibility(View.VISIBLE);
		_viewContainer._buttonRemoveGroupAvatarImage.setVisibility(View.VISIBLE);
		with(getContext())
				.load(imageUri)
				.resize(500, 0)
				.memoryPolicy(MemoryPolicy.NO_CACHE)
				.into(imageView);
	}

	private MediaAttachment createMediaAttachment() {
		final Bitmap bitmap = _viewContainer._groupAvatarImageView.getDrawable() == null ? null : ((BitmapDrawable) _viewContainer._groupAvatarImageView.getDrawable()).getBitmap();
		final String imageUrl = _viewContainer._groupAvatarUrl.getText().toString();
		if (bitmap != null) {
			return MediaAttachment.image(bitmap);
		} else {
			return MediaAttachment.imageUrl(imageUrl);
		}
	}

	private void createGroup() {
		final Map<String, String> properties = new HashMap<>();
		for (final DynamicUi.DynamicInputHolder holder : _propertiesHolder) {
			properties.put(holder.getText(0), holder.getText(1));
		}
		if (TextUtils.isEmpty(_viewContainer._id.getText().toString())) {
			showAlert("Error", "Group ID is mandatory");
			return;
		}
		if (TextUtils.isEmpty(_viewContainer._name.getText().toString())) {
			showAlert("Error", "Group Name is mandatory");
			return;
		}
		GroupContent content = GroupContent.create(_viewContainer._id.getText().toString())
						.withTitle(_viewContainer._name.getText().toString())
						.withDescription(_viewContainer._description.getText().toString())
						.withDiscoverable(_viewContainer._discoverable.isChecked())
						.withPrivate(_viewContainer._private.isChecked())
						.withProperties(properties);
		MediaAttachment avatar = createMediaAttachment();
		if (avatar != null) {
			content = content.withAvatar(avatar);
		}
		switch (_viewContainer._post.getSelectedItemPosition()) {
			case 0: {
				content.setPermission(CommunitiesAction.POST, Role.OWNER);
				break;
			}
			case 1: {
				content.setPermission(CommunitiesAction.POST, Role.ADMIN);
				break;
			}
			case 2: {
				content.setPermission(CommunitiesAction.POST, Role.MEMBER);
				break;
			}
		}
		switch (_viewContainer._interact.getSelectedItemPosition()) {
			case 0: {
				content.setPermission(CommunitiesAction.COMMENT, Role.OWNER);
				content.setPermission(CommunitiesAction.REACT, Role.OWNER);
				break;
			}
			case 1: {
				content.setPermission(CommunitiesAction.COMMENT, Role.ADMIN);
				content.setPermission(CommunitiesAction.REACT, Role.ADMIN);
				break;
			}
			case 2: {
				content.setPermission(CommunitiesAction.COMMENT, Role.MEMBER);
				content.setPermission(CommunitiesAction.REACT, Role.MEMBER);
				break;
			}
		}
		String searchText = _viewContainer._labels.getText().toString();
		if (!searchText.isEmpty()) {
			content = content.withLabels(Arrays.asList(searchText.split(",")));
		}

		if (_groupId == null) {
			Communities.createGroup(content, group -> {
				_log.logInfoAndToast("Created group:" + group);
			}, error -> {
				_log.logErrorAndToast(error);
			});
		} else {
			Communities.updateGroup(_groupId, content, group -> {
				_log.logInfoAndToast("Updated group:" + group);
			}, error -> {
				_log.logErrorAndToast(error);
			});

		}
	}

	class ViewContainer {

		@BindView(R.id.group_id)
		EditText _id;

		@BindView(R.id.group_name)
		EditText _name;

		@BindView(R.id.group_description)
		EditText _description;

		@BindView(R.id.group_avatar_url)
		EditText _groupAvatarUrl;

		@BindView(R.id.group_avatar_image)
		ImageView _groupAvatarImageView;

		@BindView(R.id.button_select_group_avatar_image)
		Button _buttonSelectGroupAvatarImage;

		@BindView(R.id.button_remove_group_avatar_image)
		Button _buttonRemoveGroupAvatarImage;

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

		@BindView(R.id.create_group)
		Button _createButton;

		@BindView(R.id.group_labels)
		EditText _labels;

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
			_buttonSelectGroupAvatarImage.setOnClickListener(v -> pickImageFromDevice(0x1));
			_buttonRemoveGroupAvatarImage.setOnClickListener(v -> {
				_groupAvatarImageView.setImageDrawable(null);
				_groupAvatarImageView.setVisibility(View.GONE);
				_buttonRemoveGroupAvatarImage.setVisibility(View.GONE);
			});
		}

		public void setGroup(final Group group) {
			_id.setText(group.getId());
			_name.setText(group.getTitle());
			_description.setText(group.getDescription());
			_groupAvatarUrl.setText(group.getAvatarUrl());
			for (final Map.Entry<String, String> property : group.getSettings().getProperties().entrySet()) {
				createRow(property.getKey(), property.getValue());
			}
			_discoverable.setChecked(group.getSettings().isDiscoverable());
			_private.setChecked(group.getSettings().isPrivate());
			if (group.getSettings().getPermissions().get(CommunitiesAction.POST) != null) {
				_post.setSelection(group.getSettings().getPermissions().get(CommunitiesAction.POST).ordinal());
			}
			if (group.getSettings().getPermissions().get(CommunitiesAction.REACT) != null) {
				_interact.setSelection(group.getSettings().getPermissions().get(CommunitiesAction.REACT).ordinal());
			}
			_labels.setText(TextUtils.join(",", group.getSettings().getLabels()));
			_createButton.setText(_groupId == null ? "Create" : "Update");
		}
	}

}
