package im.getsocial.demo.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import im.getsocial.demo.R;
import im.getsocial.sdk.communities.GetSocialActivity;
import im.getsocial.sdk.communities.PollOption;
import im.getsocial.sdk.media.MediaAttachment;

public class VoteListAdapter extends RecyclerView.Adapter<VoteListAdapter.PollViewHolder> {
	private final List<PollOption> _items;
	private final List<String> _selectedItems;
	private final boolean _allowMultipleVotes;

	public PollOptionClickListener clickListener;

	public VoteListAdapter(final List<PollOption> items, final List<String> selectedOptions, boolean allowMultipleVotes) {
		_items = items;
		_selectedItems = selectedOptions;
		_allowMultipleVotes = allowMultipleVotes;
	}

	@Override
	public PollViewHolder onCreateViewHolder(final ViewGroup viewGroup, final int position) {
		final View view = LayoutInflater.from(viewGroup.getContext())
						.inflate(R.layout.list_item_vote, viewGroup, false);

		return new PollViewHolder(view);
	}

	@Override
	public void onBindViewHolder(final PollViewHolder viewHolder, final int position) {
		PollOption option = _items.get(position);
		MediaAttachment attachment = option.getAttachment();
		String imageUrl = "";
		String videoUrl = "";
		if (attachment != null) {
			if (attachment.getImageUrl() != null) {
				imageUrl = attachment.getImageUrl();
			}
			if (attachment.getVideoUrl() != null) {
				videoUrl = attachment.getVideoUrl();
			}
		}
		viewHolder._optionId.setText("Id:" + option.getOptionId());
		viewHolder._text.setText("Text:" + option.getText());
		viewHolder._imageUrl.setText("Image URL:" + imageUrl);
		viewHolder._videoUrl.setText("Video URL:" + videoUrl);
		viewHolder._voteCount.setText("Vote count:" + option.getVoteCount());
		viewHolder._selected.setChecked(_selectedItems.contains(option.getOptionId()));
		viewHolder._selected.setOnCheckedChangeListener((compoundButton, selected) -> {
			if (!_allowMultipleVotes) {
				_selectedItems.clear();
			}
			clickListener.onOptionSelected(option.getOptionId(), selected);
		});
	}

	@Override
	public int getItemCount() {
		return _items.size();
	}

	public void reload(List<PollOption> options) {
		_items.clear();
		_items.addAll(options);
		notifyDataSetChanged();
	}

	public class PollViewHolder extends RecyclerView.ViewHolder {
		TextView _optionId;
		TextView _text;
		TextView _imageUrl;
		TextView _videoUrl;
		TextView _voteCount;
		CheckBox _selected;

		PollViewHolder(final View view) {
			super(view);
			_optionId = view.findViewById(R.id.vote_optionId);
			_text = view.findViewById(R.id.vote_text);
			_imageUrl = view.findViewById(R.id.vote_imageUrl);
			_videoUrl = view.findViewById(R.id.vote_videoUrl);
			_voteCount = view.findViewById(R.id.vote_voteCount);
			_selected = view.findViewById(R.id.vote_selected);
		}

	}
}
