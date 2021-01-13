package im.getsocial.demo.fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import im.getsocial.demo.R;
import im.getsocial.sdk.Callback;
import im.getsocial.sdk.communities.Chat;
import im.getsocial.sdk.Communities;
import im.getsocial.sdk.FailureCallback;
import im.getsocial.sdk.common.PagingQuery;
import im.getsocial.sdk.common.PagingResult;
import im.getsocial.sdk.common.SimplePagingQuery;
import im.getsocial.sdk.communities.ChatMessage;
import im.getsocial.sdk.media.MediaAttachment;

public class ChatsFragment extends BaseSearchFragment<SimplePagingQuery, Chat> {

    @Override
    public String getFragmentTag() {
        return "chats";
    }

    @Override
    public String getTitle() {
        return "Chats";
    }

    @Override
    protected BaseSearchAdapter<? extends ViewHolder> createAdapter() {
        _query.setVisibility(View.GONE);
        return new BaseSearchAdapter<ViewHolder>() {
            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
                final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_chat, parent, false);
                return new ChatViewHolder(view);
            }
        };
    }

    @Override
    protected void load(PagingQuery<SimplePagingQuery> query, Callback<PagingResult<Chat>> success, FailureCallback failure) {
        Communities.getChats(query.getQuery(), success, failure);
    }

    @Override
    protected SimplePagingQuery createQuery(String query) {
        return SimplePagingQuery.simple(50);
    }

    public class ChatViewHolder extends ViewHolder {
        @BindView(R.id.chat_title)
        TextView _title;

        @BindView(R.id.chat_last_message)
        TextView _lastMessageText;

        @BindView(R.id.chat_last_image)
        TextView _lastMessageImage;

        @BindView(R.id.chat_last_video)
        TextView _lastMessageVideo;

        ChatViewHolder(final View view) {
            super(view);
        }

        @OnClick(R.id.actions)
        void openChat() {
            ChatMessagesFragment messages = ChatMessagesFragment.chatWith(_item);
            addContentFragment(messages);
        }

        @OnClick(R.id.showDetails)
        void showDetails() {
            showAlert("Details", _item.toString());
        }

        @Override
        protected void bind(final View itemView) {
            ButterKnife.bind(this, itemView);
        }

        @Override
        protected void invalidate() {
            _title.setText(_item.getTitle());
            ChatMessage lastMessage = _item.getLastMessage();
            String lastText = "Text: ";
            String lastImage = "Image: ";
            String lastVideo = "Video: ";
            if (lastMessage != null) {
                lastText += lastMessage.getText();
                if (lastMessage.getAttachments().size() > 0) {
                    MediaAttachment lastAttachment = lastMessage.getAttachments().get(0);
                    lastImage += lastAttachment.getImageUrl();
                    lastVideo += lastAttachment.getVideoUrl();
                }
            }
            _lastMessageText.setText(lastText);
            _lastMessageImage.setText(lastImage);
            _lastMessageVideo.setText(lastVideo);
        }
    }
}
