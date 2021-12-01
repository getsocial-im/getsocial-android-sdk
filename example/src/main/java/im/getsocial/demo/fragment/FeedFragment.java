package im.getsocial.demo.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import im.getsocial.demo.R;
import im.getsocial.demo.dialog.action_dialog.ActionDialog;
import im.getsocial.demo.utils.EditTextWOCopyPaste;
import im.getsocial.sdk.Callback;
import im.getsocial.sdk.Communities;
import im.getsocial.sdk.CompletionCallback;
import im.getsocial.sdk.FailureCallback;
import im.getsocial.sdk.GetSocialError;
import im.getsocial.sdk.common.PagingQuery;
import im.getsocial.sdk.common.PagingResult;
import im.getsocial.sdk.communities.ActivitiesQuery;
import im.getsocial.sdk.communities.GetSocialActivity;

public class FeedFragment extends BaseSearchFragment<ActivitiesQuery, GetSocialActivity> {

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        _query.setVisibility(View.GONE);
    }

    @Override
    protected BaseSearchAdapter<? extends ViewHolder> createAdapter() {
        return new BaseSearchAdapter<ViewHolder>() {
            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
                final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_activity, parent, false);
                return new FeedFragment.ActivityViewHolder(view);
            }
        };
    }

    @Override
    protected void load(final PagingQuery<ActivitiesQuery> query, final Callback<PagingResult<GetSocialActivity>> success, final FailureCallback failure) {
        Communities.getActivities(query, success, failure);
    }

    @Override
    protected ActivitiesQuery createQuery(final SearchObject searchObject) {
        return ActivitiesQuery.activitiesInTopic("DemoFeed");
    }

    @Override
    public String getFragmentTag() {
        return "feed";
    }

    @Override
    public String getTitle() {
        return "Feed";
    }

    interface ReactionDialogCallback {
        void reactionEntered(String reaction);
        void cancel();
    }

    public class ActivityViewHolder extends ViewHolder {
        @BindView(R.id.activity_author_name)
        TextView _authorName;

        @BindView(R.id.activity_text)
        TextView _activityText;

        @BindView(R.id.activity_reactions)
        TextView _myReactions;

        ActivityViewHolder(final View view) {
            super(view);
        }

        @Override
        protected void bind(final View itemView) {
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.actions)
        void openActions() {

            final ActionDialog dialog = new ActionDialog(getContext());

            dialog.addAction(new ActionDialog.Action("Reaction Details") {
                @Override
                public void execute() {
                    String reactions = _item.getReactions().toString();
                    String myReactions = _item.getMyReactions().toString();
                    String reactionsCount = _item.getReactionsCount().toString();
                    showAlert("Details", String.format("Known reactors: %s, my reactions: %s, reactions count: %s", reactions, myReactions, reactionsCount));
                }
            });
            dialog.addAction(new ActionDialog.Action("Comment Details") {
                @Override
                public void execute() {
                    String commenters = _item.getCommenters().toString();
                    int commentsCount = _item.getCommentsCount();
                    showAlert("Details", String.format("Known commenters: %s, comments count: %s", commenters, commentsCount));
                }
            });
            dialog.addAction(new ActionDialog.Action("Add reaction") {
                @Override
                public void execute() {
                    showReactionInput("Add reaction", new ReactionDialogCallback() {
                        @Override
                        public void reactionEntered(String reaction) {
                            FailureCallback failureCallback = error -> {
                                hideLoading();
                                _log.logErrorAndToast("Failed to add reaction, error: " + error.getMessage());
                            };
                            Communities.addReaction(reaction, _item.getId(), new CompletionCallback() {
                                @Override
                                public void onSuccess() {
                                    updateActivity(_item, new CompletionCallback() {
                                        @Override
                                        public void onSuccess() {
                                            hideLoading();
                                            showAlert("Success", "Reaction added");
                                        }
                                    }, failureCallback);
                                }
                            }, failureCallback);
                        }

                        @Override
                        public void cancel() {
                            // do nothing
                        }
                    });
                }
            });
            dialog.addAction(new ActionDialog.Action("Set reaction") {
                @Override
                public void execute() {
                    showReactionInput("Set reaction", new ReactionDialogCallback() {
                        @Override
                        public void reactionEntered(String reaction) {
                            FailureCallback failureCallback = error -> {
                                hideLoading();
                                _log.logErrorAndToast("Failed to set reaction, error: " + error.getMessage());
                            };

                            Communities.setReaction(reaction, _item.getId(), new CompletionCallback() {
                                @Override
                                public void onSuccess() {
                                    updateActivity(_item, new CompletionCallback() {
                                        @Override
                                        public void onSuccess() {
                                            hideLoading();
                                            showAlert("Success", "Reaction set");
                                        }
                                    }, failureCallback);
                                }
                            }, failureCallback);
                        }

                        @Override
                        public void cancel() {
                            // do nothing
                        }
                    });
                }
            });
            dialog.addAction(new ActionDialog.Action("Remove reaction") {
                @Override
                public void execute() {
                    showReactionInput("Remove reaction", new ReactionDialogCallback() {
                        @Override
                        public void reactionEntered(String reaction) {
                            FailureCallback failureCallback = error -> {
                                hideLoading();
                                _log.logErrorAndToast("Failed to remove reaction, error: " + error.getMessage());
                            };
                            Communities.removeReaction(reaction, _item.getId(), new CompletionCallback() {
                                @Override
                                public void onSuccess() {
                                    updateActivity(_item, new CompletionCallback() {
                                        @Override
                                        public void onSuccess() {
                                            hideLoading();
                                            showAlert("Success", "Reaction removed");
                                        }
                                    }, failureCallback);
                                }
                            }, new FailureCallback() {
                                @Override
                                public void onFailure(GetSocialError error) {
                                    _log.logErrorAndToast("Failed to remove reaction, error: " + error.getMessage());
                                }
                            });
                        }

                        @Override
                        public void cancel() {
                            // do nothing
                        }
                    });
                }
            });
            dialog.show();

        }

        void showReactionInput(String title, ReactionDialogCallback callback) {
            final LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            final View view = layoutInflater.inflate(R.layout.dialog_reaction, null, false);

            final TextView viewTitle = view.findViewById(R.id.reaction_dialog_title);
            viewTitle.setText(title);
            final EditTextWOCopyPaste reactionText = view.findViewById(R.id.reaction_dialog_reaction);

            final AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                    .setView(view)
                    .setPositiveButton("Ok", (dialog, which) -> {
                                String reaction = reactionText.getText().toString();
                                callback.reactionEntered(reaction);
                            }
                    )
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        callback.cancel();
                    });
            builder.show();
        }

        @Override
        protected void invalidate() {
            _authorName.setText(_item.getAuthor().getDisplayName());
            _activityText.setText(_item.getText());
            _myReactions.setText(_item.getMyReactions().toString());
        }

        private void updateActivity(GetSocialActivity activity, CompletionCallback callback, FailureCallback failureCallback) {
            Communities.getActivity(activity.getId(), new Callback<GetSocialActivity>() {
                @Override
                public void onSuccess(GetSocialActivity result) {
                    updateItem(activity, result);
                    callback.onSuccess();
                }
            }, new FailureCallback() {
                @Override
                public void onFailure(GetSocialError error) {
                    failureCallback.onFailure(error);
                }
            });
        }
    }
}
