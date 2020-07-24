package im.getsocial.demo.fragment;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import im.getsocial.demo.R;
import im.getsocial.demo.adapter.MessageListAdapter;
import im.getsocial.sdk.Communities;
import im.getsocial.sdk.GetSocial;
import im.getsocial.sdk.Notifications;
import im.getsocial.sdk.actions.Action;
import im.getsocial.sdk.common.PagingQuery;
import im.getsocial.sdk.communities.ActivitiesQuery;
import im.getsocial.sdk.communities.ActivityContent;
import im.getsocial.sdk.communities.GetSocialActivity;
import im.getsocial.sdk.communities.PostActivityTarget;
import im.getsocial.sdk.communities.UserIdList;
import im.getsocial.sdk.notifications.NotificationContent;
import im.getsocial.sdk.notifications.SendNotificationTarget;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatFragment extends BaseFragment {
	public static final String TAG = "chat_fragment";
	public static final String KEY_RECIPIENT_NAME = "recipient_name";
	public static final String KEY_RECIPIENT_ID = "recipient_id";
	public static final String KEY_MESSAGE = "message";
	public static final String ACTION_KEY_ID = "open_messages_for_id";
	public static final String ACTION_KEY_NAME = "open_messages_for_name";
	public static final String ACTION_KEY_MESSAGE = "open_message_with_message";
	public static final String NOTIFICATION_ACTION_OPEN_MESSAGE = "open_chat_message";

	private String _senderId;
	private String _recipientName;
	private String _recipientId;

	private Button _sendBtn;
	private RecyclerView _list;
	private EditText _messageInput;
	private MessageListAdapter _adapter;

	public static ChatFragment chatWith(final String userId, final String userName) {
		final Bundle args = new Bundle();
		args.putString(KEY_RECIPIENT_ID, userId);
		args.putString(KEY_RECIPIENT_NAME, userName);
		final ChatFragment fragment = new ChatFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(@Nullable final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			setArguments(savedInstanceState);
		}
		getValuesFromArguments();
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.fragment_friend_chat, container, false);

		_sendBtn = view.findViewById(R.id.message_send);
		_list = view.findViewById(R.id.message_list);
		_messageInput = view.findViewById(R.id.message_input);

		initListeners();
		getMessages();

		return view;
	}

	private void getMessages() {
		Communities.getActivities(new PagingQuery<>(ActivitiesQuery.activitiesInTopic(generateFeedId())).withLimit(100), result -> {
			initList(result.getEntries());
		}, exception -> {
			_log.logErrorAndToast(exception);
		});
	}

	private void initListeners() {
		_sendBtn.setOnClickListener(view -> sendMessage());
	}

	private void initList(final List<GetSocialActivity> posts) {
		_list.setHasFixedSize(true);
		_list.setLayoutManager(new LinearLayoutManager(getActivity()));
		_adapter = new MessageListAdapter(posts);
		_list.setAdapter(_adapter);
		_list.scrollToPosition(posts.size() - 1);
	}

	@Override
	public String getFragmentTag() {
		return TAG;
	}

	@Override
	public String getTitle() {
		if (_recipientName == null) {
			getValuesFromArguments();
		}
		return "Chat with " + _recipientName;
	}

	private String generateFeedId() {
		if (_recipientName == null) {
			getValuesFromArguments();
		}
		return generateChatId(_senderId, _recipientId);
	}

	@NonNull
	String generateChatId(final String... userIds) {
		Arrays.sort(userIds);

		final StringBuilder builder = new StringBuilder("chat");
		for (final String id : userIds) {
			builder.append("_").append(id);
		}

		return builder.toString();
	}

	@Override
	public void onSaveInstanceState(final Bundle outState) {
		outState.putString(KEY_RECIPIENT_ID, _recipientId);
		outState.putString(KEY_RECIPIENT_NAME, _recipientName);
		super.onSaveInstanceState(outState);
	}

	private void getValuesFromArguments() {
		final Bundle bundle = getArguments();
		if (bundle != null) {
			_recipientName = bundle.getString(KEY_RECIPIENT_NAME);
			_recipientId = bundle.getString(KEY_RECIPIENT_ID);
			_senderId = GetSocial.getCurrentUser().getId();
		}
	}

	private void sendMessage() {
		if (!_messageInput.getText().toString().isEmpty()) {
			final ActivityContent content = ActivityContent.createWithText(_messageInput.getText().toString());
			showLoading("Sending message", "Wait please...");
			Communities.postActivity(content, PostActivityTarget.topic(generateFeedId()), result -> {
				sendChatMessageNotification(_messageInput.getText().toString(), _recipientId);
				_messageInput.getText().clear();
				getMessages();
			}, exception -> hideLoading());
		}
	}

	private void sendChatMessageNotification(final String messageContent, final String recipientId) {
		final Map<String, String> messageMetadata = new HashMap<>();
		messageMetadata.put(ACTION_KEY_ID, GetSocial.getCurrentUser().getId());
		messageMetadata.put(ACTION_KEY_NAME, GetSocial.getCurrentUser().getDisplayName());

		// Create custom Notification action, so when we receive we can distinguish that this is chat notification
		final Action action = Action.create(NOTIFICATION_ACTION_OPEN_MESSAGE, messageMetadata);

		final NotificationContent notificationContent = NotificationContent
						.notificationWithText(messageContent)
						.withTitle(GetSocial.getCurrentUser().getDisplayName())
						.withAction(action);

		Notifications.send(notificationContent, SendNotificationTarget.users(UserIdList.create(recipientId)), () -> {
			Log.i("GetSocial", "Chat notification sent");
			hideLoading();
		}, error -> {
			Log.e("GetSocial", "Failed to send chat notification, error: " + error);
			hideLoading();
		});
	}
}
