package im.getsocial.demo.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import im.getsocial.demo.R;
import im.getsocial.demo.adapter.PollListAdapter;
import im.getsocial.demo.adapter.VoteListAdapter;
import im.getsocial.demo.dialog.action_dialog.ActionDialog;
import im.getsocial.sdk.Communities;
import im.getsocial.sdk.common.PagingQuery;
import im.getsocial.sdk.communities.ActivitiesQuery;
import im.getsocial.sdk.communities.GetSocialActivity;
import im.getsocial.sdk.communities.PollOption;
import im.getsocial.sdk.communities.PollStatus;

public class VotesFragment extends BaseFragment {
	public static final String TAG = "votes_fragment";

	private Button _add;
	private Button _set;
	private Button _remove;
	private RecyclerView _list;
	private VoteListAdapter _adapter;
	private String _activityId;

	public static Fragment voteForActivity(final String activityId) {
		final VotesFragment fragment = new VotesFragment();
		final Bundle args = new Bundle();
		args.putString("activityId", activityId);
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
		final View view = inflater.inflate(R.layout.fragment_votes, container, false);

		_list = view.findViewById(R.id.votes_votesList);
		_add = view.findViewById(R.id.votes_add);
		_set = view.findViewById(R.id.votes_set);
		_remove = view.findViewById(R.id.votes_remove);

		final DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
		_list.addItemDecoration(dividerItemDecoration);

		final LinearLayoutManager manager = new LinearLayoutManager(getContext());
		_list.setLayoutManager(manager);

		initListeners();
		return view;
	}

	@Override
	public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		final Bundle bundle = getArguments();
		_activityId = bundle.getString("activityId");

		initList();
		getVotes();
	}

	private boolean _allowMultipleVotes = false;
	private List<PollOption> _pollOptions = new ArrayList<>();
	private List<String> _selectedPollOptions = new ArrayList<>();
	private List<String> _myVotes = new ArrayList<>();

	private void getVotes() {
		_selectedPollOptions.clear();
		_myVotes.clear();
		Communities.getActivity(_activityId, result -> {
			_allowMultipleVotes = result.getPoll().areMultipleVotesAllowed();
			_pollOptions = result.getPoll().getPollOptions();
			for(PollOption option: _pollOptions) {
				if (option.isVotedByMe()) {
					_selectedPollOptions.add(option.getOptionId());
					_myVotes.add(option.getOptionId());
				}
			}
			_adapter.reload(_pollOptions);
			updateRemoveButtonState();
		}, exception -> {
			_log.logErrorAndToast(exception);
		});
	}

	private void updateRemoveButtonState() {
		boolean result = false;
		for (String myVote: _myVotes) {
			if (_selectedPollOptions.contains(myVote)) {
				result = true;
			}
		}
		_remove.setEnabled(result);
	}

	private void initListeners() {
		_add.setOnClickListener(view -> {
			Communities.addVotes(new HashSet<>(_selectedPollOptions), _activityId, () -> {
				showAlert("Success", "Votes added");
				getVotes();
			}, (error) -> {
				showAlert("Error", "Failed to add votes: " + error.getMessage());
			});
		});
		_set.setOnClickListener(view -> {
			Communities.setVotes(new HashSet<>(_selectedPollOptions), _activityId, () -> {
				showAlert("Success", "Votes set");
				getVotes();
			}, (error) -> {
				showAlert("Error", "Failed to set votes: " + error.getMessage());
			});
		});
		_remove.setOnClickListener(view -> {
			Communities.removeVotes(new HashSet<>(_selectedPollOptions), _activityId, () -> {
				showAlert("Success", "Votes removed");
				getVotes();
			}, (error) -> {
				showAlert("Error", "Failed to remove votes: " + error.getMessage());
			});
		});
	}

	private void initList() {
		_list.setHasFixedSize(true);
		_list.setLayoutManager(new LinearLayoutManager(getActivity()));
		_adapter = new VoteListAdapter(_pollOptions, _selectedPollOptions, _allowMultipleVotes);
		_adapter.clickListener = (optionId, selected) -> {
			if (selected) {
				_selectedPollOptions.add(optionId);
			} else {
				_selectedPollOptions.remove(optionId);
			}
			updateRemoveButtonState();
		};
		_list.setAdapter(_adapter);
	}

	@Override
	public String getFragmentTag() {
		return TAG;
	}

	@Override
	public String getTitle() {
		return "Votes";
	}
}
