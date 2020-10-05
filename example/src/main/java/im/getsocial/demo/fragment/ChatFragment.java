package im.getsocial.demo.fragment;


import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import im.getsocial.demo.R;
import im.getsocial.demo.adapter.MessageListAdapter;
import im.getsocial.sdk.Callback;
import im.getsocial.sdk.GetSocial;
import im.getsocial.sdk.GetSocialException;
import im.getsocial.sdk.actions.Action;
import im.getsocial.sdk.activities.ActivitiesQuery;
import im.getsocial.sdk.activities.ActivityPost;
import im.getsocial.sdk.activities.ActivityPostContent;
import im.getsocial.sdk.activities.PostAuthor;
import im.getsocial.sdk.pushnotifications.NotificationContent;
import im.getsocial.sdk.pushnotifications.NotificationsSummary;

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

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			setArguments(savedInstanceState);
		}
		getValuesFromArguments();
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_friend_chat, container, false);

		_sendBtn = view.findViewById(R.id.message_send);
		_list = view.findViewById(R.id.message_list);
		_messageInput = view.findViewById(R.id.message_input);

		Bundle arguments = getArguments();
		if (arguments != null && arguments.containsKey(KEY_MESSAGE)) {
			final ActivityPost.Builder builder = ActivityPost.builder().content(
					arguments.getString(KEY_MESSAGE, ""),
					null,
					null,
					null);
			builder.author(new PostAuthor.Builder(_recipientId).build());
			initList(Arrays.asList(builder.build()));
		}

		initListeners();
		getMessages();

		return view;
	}

	private void getMessages() {
		GetSocial.getActivities(ActivitiesQuery.postsForFeed(generateFeedId()).withLimit(100), new Callback<List<ActivityPost>>() {
			@Override
			public void onSuccess(List<ActivityPost> result) {
				initList(result);
			}

			@Override
			public void onFailure(GetSocialException exception) {

			}
		});
	}

	private void initListeners() {
		_sendBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				sendMessage();
			}
		});
	}

	private void initList(List<ActivityPost> posts) {
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
	String generateChatId(String ... userIds) {
		Arrays.sort(userIds);

		StringBuilder builder = new StringBuilder("chat");
		for(String id : userIds) {
			builder.append("_").append(id);
		}

		return builder.toString();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString(KEY_RECIPIENT_ID, _recipientId);
		outState.putString(KEY_RECIPIENT_NAME, _recipientName);
		super.onSaveInstanceState(outState);
	}

	private void getValuesFromArguments() {
		Bundle bundle = getArguments();
		if (bundle != null) {
			_recipientName = bundle.getString(KEY_RECIPIENT_NAME);
			_recipientId = bundle.getString(KEY_RECIPIENT_ID);
			_senderId = GetSocial.User.getId();
		}
	}

	private void sendMessage() {
		if (!_messageInput.getText().toString().isEmpty()) {
			ActivityPostContent content = ActivityPostContent.createBuilderWithText(_messageInput.getText().toString()).build();
			showLoading("Sending message", "Wait please...");
			GetSocial.postActivityToFeed(generateFeedId(), content, new Callback<ActivityPost>() {
				@Override
				public void onSuccess(ActivityPost result) {
					sendChatMessageNotification(_messageInput.getText().toString(), _recipientId);
					_messageInput.getText().clear();
					getMessages();
				}

				@Override
				public void onFailure(GetSocialException exception) {
					hideLoading();
				}
			});
		}
	}

	private void sendChatMessageNotification(String messageContent, String recipientId) {
		Map<String, String> messageMetadata = new HashMap<>();
		messageMetadata.put(ACTION_KEY_ID, GetSocial.User.getId());
		messageMetadata.put(ACTION_KEY_NAME, GetSocial.User.getDisplayName());

		// Create custom Notification action, so when we receive we can distinguish that this is chat notification
		Action action = Action.builder(NOTIFICATION_ACTION_OPEN_MESSAGE)
				.addActionData(messageMetadata)
				.build();

		NotificationContent notificationContent = NotificationContent
				.notificationWithText(messageContent)
				.withTitle(GetSocial.User.getDisplayName())
				.withAction(action);

		GetSocial.User.sendNotification(Collections.singletonList(recipientId), notificationContent, new Callback<NotificationsSummary>() {
			@Override
			public void onSuccess(NotificationsSummary result) {
				Log.i("GetSocial", "Chat notification sent");
				hideLoading();
			}

			@Override
			public void onFailure(GetSocialException exception) {
				Log.e("GetSocial", "Failed to send chat notification, error: " + exception.getMessage());
				hideLoading();
			}
		});
	}
}
