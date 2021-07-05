package im.getsocial.demo.adapter;
import im.getsocial.sdk.communities.PollOption;

public interface VoteOptionClickListener {
    void onVoteSelected(PollOption pollOption);
}