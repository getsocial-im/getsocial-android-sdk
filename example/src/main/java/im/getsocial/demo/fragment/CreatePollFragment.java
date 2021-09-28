package im.getsocial.demo.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import im.getsocial.demo.R;
import im.getsocial.sdk.Communities;
import im.getsocial.sdk.communities.ActivityContent;
import im.getsocial.sdk.communities.PollContent;
import im.getsocial.sdk.communities.PollOptionContent;
import im.getsocial.sdk.communities.PostActivityTarget;
import im.getsocial.sdk.media.MediaAttachment;

public class CreatePollFragment extends BaseFragment {


    private CreatePollFragment.ViewContainer _viewContainer;

    @Nullable
    private String _postTopic;

    @Nullable
    private String _postGroup;

    private boolean _timeline = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_poll, container, false);
    }

    public static Fragment postToTopic(final String topicId) {
        final CreatePollFragment fragment = new CreatePollFragment();
        final Bundle args = new Bundle();
        args.putString("topic", topicId);
        fragment.setArguments(args);
        return fragment;
    }

    public static Fragment postToGroup(final String id) {
        final CreatePollFragment fragment = new CreatePollFragment();
        final Bundle args = new Bundle();
        args.putString("group", id);
        fragment.setArguments(args);
        return fragment;
    }

    public static Fragment postToTimeline() {
        final CreatePollFragment fragment = new CreatePollFragment();
        final Bundle args = new Bundle();
        args.putBoolean("timeline", true);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        _viewContainer = new CreatePollFragment.ViewContainer(view);
        final Bundle bundle = getArguments();
        if (bundle != null) {
            _postTopic = bundle.getString("topic");
            _postGroup = bundle.getString("group");
            _timeline = bundle.getBoolean("timeline", false);
        }
    }

    @Override
    public String getFragmentTag() {
        return "create_poll";
    }

    @Override
    public String getTitle() {
        return "Create Poll";
    }

    private PostActivityTarget target() {
        if (_postTopic != null) {
            return PostActivityTarget.topic(_postTopic);
        }
        if (_postGroup != null) {
            return PostActivityTarget.group(_postGroup);
        }
        return PostActivityTarget.timeline();
    }

    private void doCreatePoll() {
        String text = _viewContainer._text.getText().toString();
        boolean allowMultipleVotes = _viewContainer._allowMultipleVotes.isChecked();

        ActivityContent content = new ActivityContent();
        content = content.withText(text);

        PollContent pollContent = new PollContent();
        pollContent = pollContent.withAllowMultipleVotes(allowMultipleVotes);
        if (_viewContainer._endDate != null) {
            pollContent = pollContent.withEndDate(_viewContainer._endDate);
        }
        List<PollOptionContent> pollOptions = new ArrayList<>();
        collectPollOptions(pollOptions);
        boolean optionsAreValid = true;
        for (PollOptionContent optionContent: pollOptions) {
            if ((optionContent.getText() == null || optionContent.getText().length() == 0) && optionContent.getAttachment() == null) {
                optionsAreValid = false;
            }
        }
        pollContent = pollContent.addPollOptions(pollOptions);
        content = content.withPoll(pollContent);
        if (!optionsAreValid) {
            showAlert("Error", "Text or MediaAttachment is mandatory");
        } else {
            showLoading("Creating poll", "Please wait");
            Communities.postActivity(content, target(), (activity) -> {
                hideLoading();
                showAlert("Success", "Poll created");
                clearData();
            }, (error -> {
                hideLoading();
                showAlert("Error", "Failed to create poll: " + error);
            }));
        }
    }

    private void clearData() {
        _viewContainer._pollOptionsContainer.removeAllViews();
        _viewContainer._text.getText().clear();
        _viewContainer._allowMultipleVotes.setChecked(false);
        _viewContainer._endDate = null;
    }

    private void collectPollOptions(List<PollOptionContent> pollOptions) {
        for (int i = 0; i< _viewContainer._pollOptionsContainer.getChildCount(); i++) {
            View view = _viewContainer._pollOptionsContainer.getChildAt(i);
            String optionId = ((EditText)view.findViewById(R.id.createPollOption_optionId)).getText().toString();
            String text = ((EditText)view.findViewById(R.id.createPollOption_text)).getText().toString();
            String imageUrl = ((EditText)view.findViewById(R.id.createPollOption_imageUrl)).getText().toString();
            String videoUrl = ((EditText)view.findViewById(R.id.createPollOption_videoUrl)).getText().toString();
            boolean attachImage = ((CheckBox)view.findViewById(R.id.createPollOption_attachImage)).isChecked();
            boolean attachVideo = ((CheckBox)view.findViewById(R.id.createPollOption_attachVideo)).isChecked();
            PollOptionContent content = new PollOptionContent();
            if (!TextUtils.isEmpty(optionId)) {
                content = content.withOptionId(optionId);
            }
            content = content.withText(text);
            if (attachImage) {
                content = content.withAttachment(MediaAttachment.image(BitmapFactory.decodeResource(getContext().getResources(),
                        R.drawable.activity_image)));
            } else if (attachVideo) {
                try {
                    InputStream inStream = getContext().getResources().openRawResource(R.raw.giphy);
                    byte[] video = getBytesFromInputStream(inStream);
                    content = content.withAttachment(MediaAttachment.video(video));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (!TextUtils.isEmpty(imageUrl)) {
                content = content.withAttachment((MediaAttachment.imageUrl(imageUrl)));
            } else if (!TextUtils.isEmpty(videoUrl)) {
                content = content.withAttachment((MediaAttachment.videoUrl(videoUrl)));
            }
            pollOptions.add(content);
        }
    }

    private void showDateTimePicker(final String title, final CreatePollFragment.DateTimePickerCallback callback) {
        final LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        final DatePicker datePicker = new DatePicker(getContext());
        final TimePicker timePicker = new TimePicker(getContext(), null, R.style.CustomDatePickerDialogTheme);

        linearLayout.addView(datePicker, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        linearLayout.addView(timePicker);

        new AlertDialog.Builder(getContext())
                .setTitle(title)
                .setView(linearLayout)
                .setPositiveButton("Set", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        dialog.dismiss();
                        final Calendar calendar = Calendar.getInstance();
                        calendar.set(
                                datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(),
                                timePicker.getCurrentHour(), timePicker.getCurrentMinute()
                        );
                        callback.onDatePicked(calendar.getTime());
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        dialog.dismiss();
                    }
                })
                .create()
                .show();
    }

    private interface DateTimePickerCallback {
        void onDatePicked(Date date);
    }

    public class ViewContainer {

        @BindView(R.id.createPoll_text)
        EditText _text;

        @BindView(R.id.createPoll_allowMultipleVotes)
        CheckBox _allowMultipleVotes;

        @BindView(R.id.createPoll_endDate)
        TextView _endDateText;

        @Nullable
        Date _endDate;

        @BindView(R.id.createPoll_optionsContainer)
        ViewGroup _pollOptionsContainer;

        ViewContainer(final View view) {
            ButterKnife.bind(this, view);
        }

        @OnClick(R.id.createPoll_addOption)
        public void addPollOption() {
            final LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.fragment_create_poll_option, _pollOptionsContainer, false);
            Button removeViewButton = view.findViewById(R.id.createPollOption_remove);
            removeViewButton.setOnClickListener((button) -> {
                _pollOptionsContainer.removeView(view);
            });
            view.setBackgroundColor(Color.GRAY);

            EditText imageUrl = view.findViewById(R.id.createPollOption_imageUrl);
            EditText videoUrl = view.findViewById(R.id.createPollOption_videoUrl);
            imageUrl.setOnFocusChangeListener((view1, focused) -> {
                if (focused) {
                    videoUrl.getText().clear();
                }
            });
            videoUrl.setOnFocusChangeListener((view1, focused) -> {
                if (focused) {
                    imageUrl.getText().clear();
                }
            });

            CheckBox attachImage = view.findViewById(R.id.createPollOption_attachImage);
            CheckBox attachVideo = view.findViewById(R.id.createPollOption_attachVideo);

            attachImage.setOnCheckedChangeListener((button, newValue) -> {
                if (newValue) {
                    attachVideo.setChecked(false);
                }
            });
            attachVideo.setOnCheckedChangeListener((button, newValue) -> {
                if (newValue) {
                    attachImage.setChecked(false);
                }
            });
            _pollOptionsContainer.addView(view);
        }

        @OnClick(R.id.createPoll_changeEndDate)
        public void changeEndDate() {
            showDateTimePicker("Select End Date", new CreatePollFragment.DateTimePickerCallback() {
                @Override
                public void onDatePicked(final Date date) {
                    _endDateText.setText(date.toString());
                    _endDate = date;
                }
            });
        }

        @OnClick(R.id.createPoll_create)
        public void createPoll() {
            doCreatePoll();
        }

    }
}
