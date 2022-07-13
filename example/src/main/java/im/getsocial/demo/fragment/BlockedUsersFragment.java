package im.getsocial.demo.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.squareup.picasso.Picasso;

import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import im.getsocial.demo.R;
import im.getsocial.demo.dialog.action_dialog.ActionDialog;
import im.getsocial.sdk.Callback;
import im.getsocial.sdk.Communities;
import im.getsocial.sdk.FailureCallback;
import im.getsocial.sdk.common.PagingQuery;
import im.getsocial.sdk.common.PagingResult;
import im.getsocial.sdk.communities.BlockedUsersQuery;
import im.getsocial.sdk.communities.User;
import im.getsocial.sdk.communities.UserIdList;

public class BlockedUsersFragment extends BaseSearchFragment<BlockedUsersQuery, User> {

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        _query.setVisibility(View.GONE);
    }

    @Override
    protected BaseSearchAdapter<? extends ViewHolder> createAdapter() {
        return new BaseSearchAdapter<ViewHolder>() {
            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
                final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_user, parent, false);
                return new GroupMemberViewHolder(view);
            }
        };
    }

    @Override
    protected void load(PagingQuery<BlockedUsersQuery> query, Callback<PagingResult<User>> success, FailureCallback failure) {
        Communities.getBlockedUsers(query, success, failure);
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        menu.add(Menu.NONE, 0x42, Menu.NONE, "Add");
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(final android.view.MenuItem item) {
        if (item.getItemId() == 0x42) {
            blockUsers();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void blockUsers() {
        addContentFragment(BlockUserFragment.create());
    }

    @Override
    protected BlockedUsersQuery createQuery(SearchObject searchObject) {
        return null;
    }

    @Override
    public String getFragmentTag() {
        return "blockedusers_fragment";
    }

    @Override
    public String getTitle() {
        return "Blocked users";
    }

    public static Fragment create() {
        final BlockedUsersFragment fragment = new BlockedUsersFragment();
        return fragment;
    }

    public class GroupMemberViewHolder extends ViewHolder {

        @BindView(R.id.user_id)
        TextView _userId;

        @BindView(R.id.user_title)
        TextView _userTitle;

        @BindView(R.id.user_description)
        TextView _description;

        @BindView(R.id.user_avatar)
        ImageView _avatar;

        GroupMemberViewHolder(final View view) {
            super(view);
        }

        @Override
        protected void bind(final View itemView) {
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.actions)
        public void openActions() {
            final ActionDialog dialog = new ActionDialog(getContext());
            dialog.addAction(new ActionDialog.Action("Details") {
                @Override
                public void execute() {
                    showAlert("Details", _item.toString());
                }
            });
            dialog.addAction(new ActionDialog.Action("Unblock") {
                @Override
                public void execute() {
                    unblock();
                }
            });
            dialog.show();
        }

        private void unblock() {
            final String userId = _item.getId();
            Communities.unblockUsers(UserIdList.create(userId), () -> {
                removeItem(_item);
                Toast.makeText(getContext(), "User unblocked", Toast.LENGTH_SHORT).show();
            }, error -> Toast.makeText(getContext(), error.toString(), Toast.LENGTH_SHORT).show());
        }

        private String formatUserDescription(final User user) {
            final StringBuilder sb = new StringBuilder();
            sb.append("Identities: ");
            for (final Map.Entry<String, String> identity : user.getIdentities().entrySet()) {
                sb.append(identity.getKey()).append("=").append(identity.getValue()).append(";");
            }
            sb.append("\nProperties: ");
            for (final Map.Entry<String, String> property : user.getPublicProperties().entrySet()) {
                sb.append(property.getKey()).append("=").append(property.getValue()).append(";");
            }
            return sb.toString();
        }

        @Override
        protected void invalidate() {
            _userId.setText(_item.getId());
            _userTitle.setText(_item.getDisplayName());
            _description.setText(formatUserDescription(_item));
            final String avatar = _item.getAvatarUrl();
            if (avatar == null || avatar.isEmpty()) {
                _avatar.setImageResource(R.drawable.avatar_default);
            } else {
                Picasso.with(getContext()).load(avatar).into(_avatar);
            }
        }
    }
}
