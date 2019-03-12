package im.getsocial.demo.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import im.getsocial.demo.R;
import im.getsocial.sdk.GetSocial;
import im.getsocial.sdk.activities.ActivityPost;

public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.ViewHolder> {
	private List<ActivityPost> _items;

	public MessageListAdapter(List<ActivityPost> items) {
		_items = items;
		Collections.reverse(_items);
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
		View view = LayoutInflater.from(viewGroup.getContext())
				.inflate(R.layout.list_item_message, viewGroup, false);

		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(ViewHolder viewHolder, int position) {
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)viewHolder._messageText.getLayoutParams();

		if (GetSocial.User.getId().equals(_items.get(position).getAuthor().getId())) {
			viewHolder._messageText.setBackgroundResource(R.drawable.message_my_background);
			params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		} else {
			viewHolder._messageText.setBackgroundResource(R.drawable.message_user_background);
			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
			params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		}

		viewHolder._messageText.setLayoutParams(params);
		viewHolder._messageText.setText(_items.get(position).getText());
	}

	@Override
	public int getItemCount() {
		return _items.size();
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		TextView _messageText;

		ViewHolder(View view) {
			super(view);
			_messageText = view.findViewById(R.id.item_message_text);
		}
	}
}
