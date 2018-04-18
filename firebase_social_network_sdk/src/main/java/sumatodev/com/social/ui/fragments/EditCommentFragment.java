package sumatodev.com.social.ui.fragments;


import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import sumatodev.com.social.R;
import sumatodev.com.social.dialogs.EditCommentDialog;
import sumatodev.com.social.managers.ProfileManager;
import sumatodev.com.social.managers.listeners.OnObjectChangedListener;
import sumatodev.com.social.model.Comment;
import sumatodev.com.social.model.Profile;
import sumatodev.com.social.model.UsersPublic;
import sumatodev.com.social.views.mention.Mentionable;
import sumatodev.com.social.views.mention.Mentions;
import sumatodev.com.social.views.mention.QueryListener;
import sumatodev.com.social.views.mention.SuggestionsListener;

import static java.sql.DriverManager.println;

/**
 * A simple {@link Fragment} subclass.
 */
public class EditCommentFragment extends DialogFragment implements View.OnClickListener {


    public static final String TAG = EditCommentFragment.class.getSimpleName();
    public static final String COMMENT_KEY = "EditCommentFragment.COMMENT_KEY";

    private CircleImageView authorImage;
    private EditText editCommentEditText;
    private Button cancel_btn, update_btn;

    private CommentDialogCallback callback;
    private Comment comment;
    private int orange;
    private Mentions mentions;

    public EditCommentFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setStyle(DialogFragment.STYLE_NORMAL, R.style.fullScreenDialog);
        if (getActivity() instanceof CommentDialogCallback) {
            callback = (CommentDialogCallback) getActivity();
        } else {
            throw new RuntimeException(getActivity().getTitle() + " should implements CommentDialogCallback");
        }
        if (getArguments() != null) {
            comment = (Comment) getArguments().getSerializable(COMMENT_KEY);
        }
        orange = ContextCompat.getColor(getActivity(), R.color.mentions_default_color);

        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_edit_comment, container, false);
        findViews(view);
        return view;
    }

    private void findViews(View view) {

        authorImage = view.findViewById(R.id.avatarImageView);
        editCommentEditText = view.findViewById(R.id.editCommentEditText);
        cancel_btn = view.findViewById(R.id.cancel_action);
        update_btn = view.findViewById(R.id.update_action);

        cancel_btn.setOnClickListener(this);
        update_btn.setOnClickListener(this);

    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        editCommentEditText.addTextChangedListener(commentTextWatcher);

        if (comment.getText() != null) {
            editCommentEditText.setText(comment.getText());
            editCommentEditText.setSelection(editCommentEditText.getText().length());
        }

        if (comment.getAuthorId() != null) {
            ProfileManager.getInstance(getActivity()).getUserPublicValue(getActivity()
                    , comment.getAuthorId(), createOnProfileChangeListener(authorImage));
        }

        mentions = new Mentions.Builder(getActivity(), editCommentEditText)
                .suggestionsListener(new SuggestionsListener() {
                    @Override
                    public void displaySuggestions(boolean display) {

                    }
                })
                .queryListener(new QueryListener() {
                    @Override
                    public void onQueryReceived(String query) {

                    }
                })
                .build();

        mentions.addMentions(comment.getMentions());
    }

    private OnObjectChangedListener<UsersPublic> createOnProfileChangeListener(final CircleImageView avatarImageView) {
        return new OnObjectChangedListener<UsersPublic>() {
            @Override
            public void onObjectChanged(UsersPublic obj) {

                if (obj.getPhotoUrl() != null) {
                    Picasso.with(getContext())
                            .load(obj.getPhotoUrl())
                            .into(avatarImageView);
                }
            }
        };
    }

    @Override
    public void onClick(View v) {
        if (v == cancel_btn) {
            dismiss();
        } else if (v == update_btn) {
            onUpdateComment();
        }
    }

    private void onUpdateComment() {

        String newCommentText = editCommentEditText.getText().toString();
        if (callback != null) {

        }
    }

    private final TextWatcher commentTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            String str = s.toString();
            if (str.equalsIgnoreCase(comment.getText())) {

                update_btn.setEnabled(false);
                update_btn.setFocusable(false);

            } else {

                update_btn.setFocusable(true);
                update_btn.setEnabled(true);
            }

        }
    };

    public interface CommentDialogCallback {
        void onCommentChanged(String newText, String commentId);
    }
}
