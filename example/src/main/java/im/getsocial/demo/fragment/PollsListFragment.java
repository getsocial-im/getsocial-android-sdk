package im.getsocial.demo.fragment;

import android.app.Activity;
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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import im.getsocial.demo.R;
import im.getsocial.demo.adapter.ActivityListAdapter;
import im.getsocial.demo.adapter.MessageListAdapter;
import im.getsocial.demo.adapter.PollListAdapter;
import im.getsocial.demo.adapter.VoteListAdapter;
import im.getsocial.demo.dialog.action_dialog.ActionDialog;
import im.getsocial.demo.utils.EndlessRecyclerViewScrollListener;
import im.getsocial.sdk.Communities;
import im.getsocial.sdk.common.PagingQuery;
import im.getsocial.sdk.communities.ActivitiesQuery;
import im.getsocial.sdk.communities.AnnouncementsQuery;
import im.getsocial.sdk.communities.Chat;
import im.getsocial.sdk.communities.ChatId;
import im.getsocial.sdk.communities.ChatMessage;
import im.getsocial.sdk.communities.ChatMessageContent;
import im.getsocial.sdk.communities.ChatMessagesPagingQuery;
import im.getsocial.sdk.communities.ChatMessagesQuery;
import im.getsocial.sdk.communities.CommunitiesAction;
import im.getsocial.sdk.communities.GetSocialActivity;
import im.getsocial.sdk.communities.MemberStatus;
import im.getsocial.sdk.communities.PollStatus;
import im.getsocial.sdk.communities.Role;
import im.getsocial.sdk.communities.UserId;
import im.getsocial.sdk.media.MediaAttachment;

public class PollsListFragment extends BaseFragment {
	public static final String TAG = "polls_fragment";

	private Button _all;
	private Button _voted;
	private Button _notVoted;
	private RecyclerView _list;
	private PollListAdapter _adapter;
	@Nullable
	private String _topicId;

	@Nullable
	private String _groupId;

	private boolean _announcements = false;

	public static Fragment inTopic(final String topicId) {
		final PollsListFragment fragment = new PollsListFragment();
		final Bundle args = new Bundle();
		args.putString("topic", topicId);
		args.putBoolean("announcements", false);
		fragment.setArguments(args);
		return fragment;
	}

	public static Fragment inGroup(final String id) {
		final PollsListFragment fragment = new PollsListFragment();
		final Bundle args = new Bundle();
		args.putString("group", id);
		args.putBoolean("announcements", false);
		fragment.setArguments(args);
		return fragment;
	}

	public static Fragment inTopicAnnouncements(final String topicId) {
		final PollsListFragment fragment = new PollsListFragment();
		final Bundle args = new Bundle();
		args.putString("topic", topicId);
		args.putBoolean("announcements", true);
		fragment.setArguments(args);
		return fragment;
	}

	public static Fragment inGroupAnnouncements(final String id) {
		final PollsListFragment fragment = new PollsListFragment();
		final Bundle args = new Bundle();
		args.putString("group", id);
		args.putBoolean("announcements", true);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		final Bundle bundle = getArguments();
		if (bundle != null) {
			_topicId = bundle.getString("topic");
			_groupId = bundle.getString("group");
			_announcements = bundle.getBoolean("announcements");
		}
		getPolls(PollStatus.WITH_POLL);
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
		final View view = inflater.inflate(R.layout.fragment_polls, container, false);

		_list = view.findViewById(R.id.polls_activitiesList);
		_all = view.findViewById(R.id.polls_showAll);
		_voted = view.findViewById(R.id.polls_showVoted);
		_notVoted = view.findViewById(R.id.polls_showNotVoted);

		final DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
		_list.addItemDecoration(dividerItemDecoration);

		final LinearLayoutManager manager = new LinearLayoutManager(getContext());
		_list.setLayoutManager(manager);

		initListeners();
		_all.setEnabled(false);

		return view;
	}

	private void getPolls(PollStatus pollStatus) {
		if (_announcements) {
			AnnouncementsQuery query = _topicId == null ? AnnouncementsQuery.inGroup(_groupId) : AnnouncementsQuery.inTopic(_topicId);
			query = query.withPollStatus(pollStatus);

			Communities.getAnnouncements(query, result -> {
				initList(result);
			}, exception -> {
				_log.logErrorAndToast(exception);
			});

		} else {
			ActivitiesQuery query = _topicId == null ? ActivitiesQuery.activitiesInGroup(_groupId) : ActivitiesQuery.activitiesInTopic(_topicId);
			query = query.withPollStatus(pollStatus);

			Communities.getActivities(new PagingQuery(query), result -> {
				initList(result.getEntries());
			}, exception -> {
				_log.logErrorAndToast(exception);
			});
		}
	}

	private void initListeners() {
		_all.setOnClickListener(view -> {
			_all.setEnabled(false);
			_voted.setEnabled(true);
			_notVoted.setEnabled(true);
			getPolls(PollStatus.WITH_POLL);
		});
		_voted.setOnClickListener(view -> {
			_all.setEnabled(true);
			_voted.setEnabled(false);
			_notVoted.setEnabled(true);
			getPolls(PollStatus.WITH_POLL_VOTED_BY_ME);
		});
		_notVoted.setOnClickListener(view -> {
			_all.setEnabled(true);
			_voted.setEnabled(true);
			_notVoted.setEnabled(false);
			getPolls(PollStatus.WITH_POLL_NOT_VOTED_BY_ME);
		});
	}

	private void initList(final List<GetSocialActivity> activities) {
		_list.setHasFixedSize(true);
		_list.setLayoutManager(new LinearLayoutManager(getActivity()));
		_adapter = new PollListAdapter(activities);
		_adapter.clickListener = activity -> {
			showActions(activity);
			};
		_list.setAdapter(_adapter);
	}

	private void showActions(GetSocialActivity activity) {
		final ActionDialog dialog = new ActionDialog(getContext());
		dialog.addAction(new ActionDialog.Action("Details") {
			@Override
			public void execute() {
				String knownVoters = activity.getPoll().getVoters().toString();
				showAlert("Details", "Known voters: " + knownVoters);
			}
		});
		if (activity.getSource() != null && activity.getSource().isActionAllowed(CommunitiesAction.REACT)) {
			dialog.addAction(new ActionDialog.Action("Vote") {
				@Override
				public void execute() {
					addContentFragment(VotesFragment.voteForActivity(activity.getId()));
				}
			});
		}
		dialog.addAction(new ActionDialog.Action("Show Votes") {
			@Override
			public void execute() {
				addContentFragment(AllVotesFragment.votesForActivity(activity.getId()));
			}
		});

		dialog.show();
	}

	@Override
	public String getFragmentTag() {
		return TAG;
	}

	@Override
	public String getTitle() {
		return "Polls";
	}
}
