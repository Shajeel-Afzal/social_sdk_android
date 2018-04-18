/*
 *
 * Copyright 2017 Rozdoum
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package sumatodev.com.social.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
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
import android.widget.EditText;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

import sumatodev.com.social.R;
import sumatodev.com.social.model.Comment;
import sumatodev.com.social.views.mention.Mentionable;
import sumatodev.com.social.views.mention.Mentions;
import sumatodev.com.social.views.mention.QueryListener;
import sumatodev.com.social.views.mention.SuggestionsListener;

/**
 * Created by alexey on 12.05.17.
 */

public class EditCommentDialog extends DialogFragment {
    public static final String TAG = EditCommentDialog.class.getSimpleName();
    public static final String COMMENT_TEXT_KEY = "EditCommentDialog.COMMENT_TEXT_KEY";
    public static final String COMMENT_ID_KEY = "EditCommentDialog.COMMENT_ID_KEY";
    public static final String COMMENT_KEY = "EditCommentDialog.COMMENT_KEY";

    private CommentDialogCallback callback;
    private Comment comment;
    private Mentions mentions;
    //    private String commentText;
//    private String commentId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (getActivity() instanceof CommentDialogCallback) {
            callback = (CommentDialogCallback) getActivity();
        } else {
            throw new RuntimeException(getActivity().getTitle() + " should implements CommentDialogCallback");
        }

//        commentText = (String) getArguments().get(COMMENT_TEXT_KEY);
//        commentId = (String) getArguments().get(COMMENT_ID_KEY);
        comment = (Comment) getArguments().getSerializable(COMMENT_KEY);

        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.dialog_edit_comment, null);

        final EditText editCommentEditText = view.findViewById(R.id.editCommentEditText);

        if (comment.getText() != null) {
            editCommentEditText.setText(comment.getText());
            editCommentEditText.setSelection(editCommentEditText.getText().length());

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

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view)
                .setTitle(R.string.title_edit_comment)
                .setNegativeButton(R.string.button_title_cancel, null)
                .setPositiveButton(R.string.button_title_save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newCommentText = editCommentEditText.getText().toString();

                        if (!newCommentText.equals(comment.getText()) && callback != null) {

                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("id", comment.getId());
                            hashMap.put("text", newCommentText);
                            hashMap.put("mentions", mentions.getInsertedMentions());
                            callback.onCommentChanged(hashMap);
                        }

                        dialog.cancel();
                    }
                });

        return builder.create();
    }


    public interface CommentDialogCallback {
        void onCommentChanged(HashMap<String, Object> hashMap);
    }
}
