package im.getsocial.demo.fragment;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import im.getsocial.demo.R;
import im.getsocial.demo.adapter.MessageClickListener;
import im.getsocial.demo.adapter.MessageListAdapter;
import im.getsocial.demo.utils.EndlessRecyclerViewScrollListener;
import im.getsocial.sdk.communities.Chat;
import im.getsocial.sdk.Communities;
import im.getsocial.sdk.communities.ChatId;
import im.getsocial.sdk.communities.ChatMessage;
import im.getsocial.sdk.communities.ChatMessageContent;
import im.getsocial.sdk.communities.ChatMessagesPagingQuery;
import im.getsocial.sdk.communities.ChatMessagesQuery;
import im.getsocial.sdk.communities.UserId;
import im.getsocial.sdk.media.MediaAttachment;

public class ChatMessagesFragment extends BaseFragment {
	public static final String TAG = "chat_fragment";

	private Button _sendBtn;
	private Button _refreshBtn;
	private RecyclerView _list;
	private EditText _messageInput;
	private EditText _messageImageUrl;
	private EditText _messageVideoUrl;
	private CheckBox _sendImage;
	private CheckBox _sendVideo;
	private MessageListAdapter _adapter;
	private ChatId _chatId;
	private String _title;
	private EndlessRecyclerViewScrollListener _scrollListener;
	private ChatMessagesQuery _query;
	private String _newMessagesCursor;
	private String _refreshCursor;
	private String _oldMessagesCursor;

	public SwipeRefreshLayout _swipeRefreshLayout;

	public static ChatMessagesFragment openChat(final String chatId) {
		final Bundle args = new Bundle();
		args.putString("CHATID", chatId);
		final ChatMessagesFragment fragment = new ChatMessagesFragment();
		fragment.setArguments(args);
		return fragment;
	}

	public static ChatMessagesFragment chatWith(final String userId) {
		final Bundle args = new Bundle();
		args.putString("USERID", userId);
		final ChatMessagesFragment fragment = new ChatMessagesFragment();
		fragment.setArguments(args);
		return fragment;
	}

	public static ChatMessagesFragment chatWith(final Chat chat) {
		final Bundle args = new Bundle();
		args.putString("CHATID", chat.getId());
		args.putString("TITLE", chat.getTitle());
		final ChatMessagesFragment fragment = new ChatMessagesFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(@Nullable final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			setArguments(savedInstanceState);
		}
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.fragment_friend_chat, container, false);
		_swipeRefreshLayout = view.findViewById(R.id.swiperefresh);

		_sendBtn = view.findViewById(R.id.message_send);
		_list = view.findViewById(R.id.message_list);
		final DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
		_list.addItemDecoration(dividerItemDecoration);
		_messageInput = view.findViewById(R.id.message_text);
		_messageImageUrl = view.findViewById(R.id.message_image);
		_messageVideoUrl = view.findViewById(R.id.message_video);
		_sendImage = view.findViewById(R.id.send_image);
		_sendVideo = view.findViewById(R.id.send_video);
		_refreshBtn = view.findViewById(R.id.refresh_messages);

		final LinearLayoutManager manager = new LinearLayoutManager(getContext());
		_list.setLayoutManager(manager);
		_swipeRefreshLayout.setOnRefreshListener(() -> {
			loadOlderMessages();
			_swipeRefreshLayout.setRefreshing(false);
		});

		getValuesFromArguments(getArguments());

		initListeners();
		getMessages();

		return view;
	}

	private void getValuesFromArguments(Bundle savedBundle) {
		if (savedBundle.containsKey("CHATID")) {
			this._chatId = ChatId.create(savedBundle.getString("CHATID"));
			this._query = ChatMessagesQuery.messagesInChat(this._chatId);
			String title = savedBundle.getString("TITLE");
			if (title != null) {
				this._title = title;
			} else {
				Communities.getChat(this._chatId, (chat) -> {
					this._title = chat.getTitle();
					((ActivityListener)getActivity()).invalidateUi();
				}, (error) -> {
					this._title = "not found";
					((ActivityListener)getActivity()).invalidateUi();
				});
			}
		} else {
			String userIdStr = savedBundle.getString("USERID");
			UserId userId = UserId.create(userIdStr);
			this._chatId = ChatId.createWithUserId(userId);
			this._query = ChatMessagesQuery.messagesInChat(this._chatId);
			Communities.getUser(userId, (user) -> {
				this._title = user.getDisplayName();
				((ActivityListener)getActivity()).invalidateUi();
			}, (error) -> {
				System.out.println("Failed to load user, no title");
			});
		}
	}

	private void loadOlderMessages() {
		if (TextUtils.isEmpty(this._oldMessagesCursor)) {
			return;
		}
		ChatMessagesPagingQuery pagingQuery = new ChatMessagesPagingQuery(this._query).withPreviousMessagesCursor(this._oldMessagesCursor);
		showLoading("Wait", "Loading older messages");
		Communities.getChatMessages(pagingQuery, result -> {
			_oldMessagesCursor = result.previousMessagesCursor();
			if (result.getEntries().size() != 0) {
				_adapter.insertNewMessages(result.getEntries());
				_list.scrollToPosition(0);
			}
			hideLoading();
		}, exception -> {
			hideLoading();
			_log.logErrorAndToast(exception);
		});
	}


	private void loadMore() {
		ChatMessagesPagingQuery pagingQuery = new ChatMessagesPagingQuery(this._query).withNextMessagesCursor(this._refreshCursor);
		showLoading("Wait", "Loading new messages");
		Communities.getChatMessages(pagingQuery, result -> {
			_newMessagesCursor = result.nextMessagesCursor();
			_refreshCursor = result.refreshCursor();
			if (result.getEntries().size() != 0) {
				_adapter.appendNewMessages(result.getEntries());
				_list.scrollToPosition(_adapter.getItemCount() - 1);
			}
			hideLoading();
		}, exception -> {
			hideLoading();
			_log.logErrorAndToast(exception);
		});
	}

	private void getMessages() {
		ChatMessagesPagingQuery pagingQuery = new ChatMessagesPagingQuery(this._query);
		Communities.getChatMessages(pagingQuery, result -> {
			_newMessagesCursor = result.nextMessagesCursor();
			_refreshCursor = result.refreshCursor();
			_oldMessagesCursor = result.previousMessagesCursor();
			initList(result.getEntries());
		}, exception -> {
			_log.logErrorAndToast(exception);
		});
	}

	private void initListeners() {
		_sendBtn.setOnClickListener(view -> sendMessage());
		_refreshBtn.setOnClickListener(view -> loadMore());
	}

	private void initList(final List<ChatMessage> messages) {
		_list.setHasFixedSize(true);
		_list.setLayoutManager(new LinearLayoutManager(getActivity()));
		_adapter = new MessageListAdapter(messages);
		_adapter.clickListener = message -> showAlert("Details", message.toString());
		_list.setAdapter(_adapter);
		_list.scrollToPosition(messages.size() - 1);
	}

	@Override
	public String getFragmentTag() {
		return TAG;
	}

	@Override
	public String getTitle() {
		return this._title == null ? "Unknown" : this._title;
	}

	private void sendMessage() {
		if (!_messageInput.getText().toString().isEmpty() || _sendImage.isChecked() || _sendVideo.isChecked() || !TextUtils.isEmpty(_messageImageUrl.getText()) || !TextUtils.isEmpty(_messageVideoUrl.getText())) {
			final ChatMessageContent content = ChatMessageContent.createWithText(_messageInput.getText().toString());
			if (_sendImage.isChecked()) {
				content.addAttachment(MediaAttachment.image(BitmapFactory.decodeResource(getContext().getResources(),
						R.drawable.activity_image)));
			}
			if (_sendVideo.isChecked()) {
				try {
					InputStream inStream = getContext().getResources().openRawResource(R.raw.giphy);
					byte[] video = getBytesFromInputStream(inStream);
					content.addAttachment(MediaAttachment.video(video));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (!TextUtils.isEmpty(_messageImageUrl.getText())) {
				content.addAttachment(MediaAttachment.imageUrl(_messageImageUrl.getText().toString()));
			}
			if (!TextUtils.isEmpty(_messageVideoUrl.getText())) {
				content.addAttachment(MediaAttachment.videoUrl(_messageVideoUrl.getText().toString()));
			}
			showLoading("Sending message", "Please wait...");
			Communities.sendChatMessage(content, this._chatId, result -> {
				hideLoading();
				_messageInput.getText().clear();
				_messageImageUrl.getText().clear();
				_messageVideoUrl.getText().clear();
				_adapter.appendNewMessage(result);
				_list.scrollToPosition(_adapter.getItemCount() - 1);
			}, exception -> hideLoading());
		} else {
			showAlert("Error", "Text, image or video is mandatory");
		}
	}

	private byte[] getBytesFromInputStream(InputStream is) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		byte[] buffer = new byte[0xFFFF];
		for (int len = is.read(buffer); len != -1; len = is.read(buffer)) {
			os.write(buffer, 0, len);
		}
		return os.toByteArray();
	}
}
