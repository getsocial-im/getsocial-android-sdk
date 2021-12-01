package im.getsocial.demo.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import im.getsocial.demo.R;
import im.getsocial.sdk.communities.GetSocialActivity;
import im.getsocial.sdk.communities.Poll;

public class PollListAdapter extends RecyclerView.Adapter<PollListAdapter.PollViewHolder> {
	private final List<GetSocialActivity> _items;

	public ActivityClickListener clickListener;

	public PollListAdapter(final List<GetSocialActivity> items) {
		_items = items;
	}

	@Override
	public PollViewHolder onCreateViewHolder(final ViewGroup viewGroup, final int position) {
		final View view = LayoutInflater.from(viewGroup.getContext())
						.inflate(R.layout.list_item_poll, viewGroup, false);

		return new PollViewHolder(view);
	}

	@Override
	public void onBindViewHolder(final PollViewHolder viewHolder, final int position) {
		GetSocialActivity activity = _items.get(position);
		viewHolder._text.setText("Text:" + activity.getText());
		Poll poll = activity.getPoll();
		viewHolder._totalCount.setText("Total votes: " + (poll != null ? poll.getTotalVotesCount() : 0));
		viewHolder._actionButton.setOnClickListener((view) -> clickListener.onShowActivity(activity));
	}

	@Override
	public int getItemCount() {
		return _items.size();
	}

	public class PollViewHolder extends RecyclerView.ViewHolder {
		TextView _text;
		TextView _totalCount;
		Button _actionButton;

		PollViewHolder(final View view) {
			super(view);
			_text = view.findViewById(R.id.poll_text);
			_totalCount = view.findViewById(R.id.poll_total_votes);
			_actionButton = view.findViewById(R.id.actions);
		}

	}
}
