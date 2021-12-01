package im.getsocial.demo.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import im.getsocial.demo.R;
import im.getsocial.sdk.GetSocial;
import im.getsocial.sdk.communities.ChatMessage;
import im.getsocial.sdk.media.MediaAttachment;

public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.MessageViewHolder> {
	private final List<ChatMessage> _items;

	public MessageClickListener clickListener;

	public MessageListAdapter(final List<ChatMessage> items) {
		_items = items;
	}

	public void appendNewMessage(ChatMessage message) {
		_items.add(message);
		notifyItemInserted(_items.size() - 1);
	}

	public void appendNewMessages(List<ChatMessage> newMessages) {
		_items.addAll(newMessages);
		notifyItemInserted(_items.size() - 1);
	}

	public void insertNewMessages(List<ChatMessage> newMessages) {
		for(ChatMessage message : newMessages) {
			_items.add(0, message);
		}
		notifyItemInserted(0);
	}

	@Override
	public MessageViewHolder onCreateViewHolder(final ViewGroup viewGroup, final int position) {
		final View view = LayoutInflater.from(viewGroup.getContext())
						.inflate(R.layout.list_item_message, viewGroup, false);

		return new MessageViewHolder(view);
	}

	@Override
	public void onBindViewHolder(final MessageViewHolder viewHolder, final int position) {
		viewHolder.itemView.setBackgroundColor(Color.LTGRAY);

		ChatMessage message = _items.get(position);
		String senderText = "Sender: " + message.getAuthor().getDisplayName();
		if (GetSocial.getCurrentUser().getId().equals(_items.get(position).getAuthor().getId())) {
			senderText += " (Current)";
		}

		viewHolder._messageSender.setText(senderText);
		viewHolder._messageText.setText("Text:" + message.getText());
		String date = DateFormat.getDateTimeInstance().format(new Date(message.getSentAt() * 1000));
		viewHolder._messageDate.setText("Date:" + date);
		String imageText = "Image: ";
		String videoText = "Video: ";
		if (message.getAttachments().size() > 0) {
			MediaAttachment attachment = message.getAttachments().get(0);
			imageText += attachment.getImageUrl();
			videoText += attachment.getVideoUrl();
		}
		viewHolder._messageImageUrl.setText(imageText);
		viewHolder._messageVideoUrl.setText(videoText);

		viewHolder._showDetailsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (clickListener != null) {
					clickListener.onShowMessageDetails(message);
				}
			}
		});

	}

	@Override
	public int getItemCount() {
		return _items.size();
	}

	public class MessageViewHolder extends RecyclerView.ViewHolder {
		TextView _messageSender;
		TextView _messageText;
		TextView _messageDate;
		TextView _messageImageUrl;
		TextView _messageVideoUrl;
		Button _showDetailsButton;

		MessageViewHolder(final View view) {
			super(view);
			_messageSender = view.findViewById(R.id.message_sender);
			_messageText = view.findViewById(R.id.message_text);
			_messageDate = view.findViewById(R.id.message_date);
			_messageImageUrl = view.findViewById(R.id.message_image);
			_messageVideoUrl = view.findViewById(R.id.message_video);
			_showDetailsButton = view.findViewById(R.id.showDetails);
		}

	}
}
