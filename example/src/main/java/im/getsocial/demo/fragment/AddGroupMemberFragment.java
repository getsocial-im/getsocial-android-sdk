package im.getsocial.demo.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import im.getsocial.demo.R;
import im.getsocial.sdk.Communities;
import im.getsocial.sdk.communities.AddGroupMembersQuery;
import im.getsocial.sdk.communities.MemberStatus;
import im.getsocial.sdk.communities.Role;
import im.getsocial.sdk.communities.UserIdList;

public class AddGroupMemberFragment extends BaseFragment {

    private ViewContainer _viewContainer;
    private String _groupId;

    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
        final Bundle bundle = getArguments();
        _groupId = bundle.getString("groupId");
        return inflater.inflate(R.layout.fragment_add_group_member, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        _viewContainer = new ViewContainer(view);
    }

    @Override
    public String getFragmentTag() {
        return "add_group_member";
    }

    @Override
    public String getTitle() {
        return "Add Group Member";
    }

    private void addGroupMember() {
        Editable userId = _viewContainer._id.getText();
        if (userId == null || userId.length() == 0) {
            Toast.makeText(getContext(), "User id is mandatory", Toast.LENGTH_SHORT).show();
            return;
        }
        Role role = Role.MEMBER;
        MemberStatus status = MemberStatus.MEMBER;
        switch (_viewContainer._role.getSelectedItemPosition()) {
            case 0: {
                role = Role.OWNER;
                break;
            }
            case 1: {
                role = Role.ADMIN;
                break;
            }
            case 2: {
                role = Role.MEMBER;
                break;
            }
        }
        switch (_viewContainer._status.getSelectedItemPosition()) {
            case 0: {
                status = MemberStatus.APPROVAL_PENDING;
                break;
            }
            case 1: {
                status = MemberStatus.INVITATION_PENDING;
                break;
            }
            case 2: {
                status = MemberStatus.MEMBER;
                break;
            }
        }
        Editable providerId = _viewContainer._providerId.getText();
        UserIdList memberId = providerId != null && providerId.length() > 0 ?
            UserIdList.createWithProvider(providerId.toString(), userId.toString()) : UserIdList.create(userId.toString());
        AddGroupMembersQuery query = AddGroupMembersQuery.create(_groupId, memberId)
                .withRole(role).withMemberStatus(status);
        Communities.addGroupMembers(query,
                result -> Toast.makeText(getContext(), "Group member added", Toast.LENGTH_SHORT).show(),
                error -> Toast.makeText(getContext(), error.toString(), Toast.LENGTH_SHORT).show() );

    }

    public static Fragment create(final String groupId) {
        final AddGroupMemberFragment fragment = new AddGroupMemberFragment();
        final Bundle args = new Bundle();
        args.putString("groupId", groupId);
        fragment.setArguments(args);
        return fragment;
    }

    class ViewContainer {

        @BindView(R.id.addgroupmember_user_id)
        EditText _id;

        @BindView(R.id.addgroupmember_provider_id)
        EditText _providerId;

        @BindView(R.id.addgroupmember_member_role)
        Spinner _role;

        @BindView(R.id.addgroupmember_member_status)
        Spinner _status;

        @OnClick(R.id.add_group_member)
        public void create() {
            addGroupMember();
        }

        public ViewContainer(final View view) {
            ButterKnife.bind(this, view);
            _role.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, Role.values()));
            _role.setSelection(2);
            _status.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, MemberStatus.values()));
            _status.setSelection(2);
        }
    }

}
