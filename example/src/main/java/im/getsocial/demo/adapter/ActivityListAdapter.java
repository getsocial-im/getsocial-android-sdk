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
import im.getsocial.sdk.communities.GetSocialActivity;
import im.getsocial.sdk.media.MediaAttachment;

public class ActivityListAdapter extends RecyclerView.Adapter<ActivityListAdapter.ActivityViewHolder> {
	private final List<GetSocialActivity> _items;

	public MessageClickListener clickListener;

	public ActivityListAdapter(final List<GetSocialActivity> items) {
		_items = items;
	}

	@Override
	public ActivityViewHolder onCreateViewHolder(final ViewGroup viewGroup, final int position) {
		final View view = LayoutInflater.from(viewGroup.getContext())
						.inflate(R.layout.list_item_activity, viewGroup, false);

		return new ActivityViewHolder(view);
	}

	@Override
	public void onBindViewHolder(final ActivityViewHolder viewHolder, final int position) {
		viewHolder.itemView.setBackgroundColor(Color.LTGRAY);

		GetSocialActivity activity = _items.get(position);
		viewHolder._text.setText("Text:" + activity.getText());
	}

	@Override
	public int getItemCount() {
		return _items.size();
	}

	public class ActivityViewHolder extends RecyclerView.ViewHolder {
		TextView _text;

		ActivityViewHolder(final View view) {
			super(view);
			_text = view.findViewById(R.id.activity_text);
		}

	}
}
