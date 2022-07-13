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

public class BlockUserFragment extends BaseFragment {

    private ViewContainer _viewContainer;

    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_block_user, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        _viewContainer = new ViewContainer(view);
    }

    @Override
    public String getFragmentTag() {
        return "block_user";
    }

    @Override
    public String getTitle() {
        return "Block User";
    }

    private void blockUser() {
        Editable userId = _viewContainer._id.getText();
        if (userId == null || userId.length() == 0) {
            Toast.makeText(getContext(), "User id is mandatory", Toast.LENGTH_SHORT).show();
            return;
        }
        Editable providerId = _viewContainer._providerId.getText();
        UserIdList blockUserId = providerId != null && providerId.length() > 0 ?
            UserIdList.createWithProvider(providerId.toString(), userId.toString()) : UserIdList.create(userId.toString());

        Communities.blockUsers(blockUserId,
                () -> Toast.makeText(getContext(), "User blocked", Toast.LENGTH_SHORT).show(),
                error -> Toast.makeText(getContext(), error.toString(), Toast.LENGTH_SHORT).show() );

    }

    public static Fragment create() {
        final BlockUserFragment fragment = new BlockUserFragment();
        return fragment;
    }

    class ViewContainer {

        @BindView(R.id.addgroupmember_user_id)
        EditText _id;

        @BindView(R.id.addgroupmember_provider_id)
        EditText _providerId;

        @OnClick(R.id.block_user)
        public void create() {
            blockUser();
        }

        public ViewContainer(final View view) {
            ButterKnife.bind(this, view);
        }
    }

}
