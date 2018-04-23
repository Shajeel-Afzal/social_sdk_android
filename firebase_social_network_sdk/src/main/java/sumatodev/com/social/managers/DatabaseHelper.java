/*
 *  Copyright 2017 Rozdoum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package sumatodev.com.social.managers;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import id.zelory.compressor.Compressor;
import sumatodev.com.social.ApplicationHelper;
import sumatodev.com.social.Constants;
import sumatodev.com.social.R;
import sumatodev.com.social.enums.Consts;
import sumatodev.com.social.enums.FollowStatus;
import sumatodev.com.social.managers.listeners.OnCommentChangedListener;
import sumatodev.com.social.managers.listeners.OnDataChangedListener;
import sumatodev.com.social.managers.listeners.OnFollowStatusChanged;
import sumatodev.com.social.managers.listeners.OnObjectChangedListener;
import sumatodev.com.social.managers.listeners.OnObjectExistListener;
import sumatodev.com.social.managers.listeners.OnPostChangedListener;
import sumatodev.com.social.managers.listeners.OnPostListChangedListener;
import sumatodev.com.social.managers.listeners.OnProfileCreatedListener;
import sumatodev.com.social.managers.listeners.OnTaskCompleteListener;
import sumatodev.com.social.model.AccountStatus;
import sumatodev.com.social.model.Comment;
import sumatodev.com.social.model.CommentListResult;
import sumatodev.com.social.model.CommentStatus;
import sumatodev.com.social.model.Friends;
import sumatodev.com.social.model.Like;
import sumatodev.com.social.model.Mention;
import sumatodev.com.social.model.Post;
import sumatodev.com.social.model.PostListResult;
import sumatodev.com.social.model.PostStyle;
import sumatodev.com.social.model.Profile;
import sumatodev.com.social.model.UsersPublic;
import sumatodev.com.social.utils.FileUtil;
import sumatodev.com.social.utils.LogUtil;
import sumatodev.com.social.utils.PreferencesUtil;
import sumatodev.com.social.views.mention.Mentionable;

/**
 * Created by Kristina on 10/28/16.
 */

public class DatabaseHelper {

    public static final String TAG = DatabaseHelper.class.getSimpleName();

    private static DatabaseHelper instance;

    private Context context;
    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private FirebaseAuth firebaseAuth;
    private Map<ValueEventListener, DatabaseReference> activeListeners = new HashMap<>();


    public static DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context);
        }

        return instance;
    }

    public DatabaseHelper(Context context) {
        this.context = context;
        firebaseAuth = FirebaseAuth.getInstance();
    }

    public void init() {
        database = FirebaseDatabase.getInstance();
        database.setPersistenceEnabled(true);
        storage = FirebaseStorage.getInstance();

//        Sets the maximum time to retry upload operations if a failure occurs.
        storage.setMaxUploadRetryTimeMillis(Constants.Database.MAX_UPLOAD_RETRY_MILLIS);
    }

    public DatabaseReference getDatabaseReference() {
        return database.getReference();
    }

    public String getCurrentUser() {
        return firebaseAuth.getCurrentUser() == null ? null : firebaseAuth.getCurrentUser().getUid();
    }

    public void closeListener(ValueEventListener listener) {
        if (activeListeners.containsKey(listener)) {
            DatabaseReference reference = activeListeners.get(listener);
            reference.removeEventListener(listener);
            activeListeners.remove(listener);
            LogUtil.logDebug(TAG, "closeListener(), listener was removed: " + listener);
        } else {
            LogUtil.logDebug(TAG, "closeListener(), listener not found :" + listener);
        }
    }

    public void closeAllActiveListeners() {
        for (ValueEventListener listener : activeListeners.keySet()) {
            DatabaseReference reference = activeListeners.get(listener);
            reference.removeEventListener(listener);
        }

        activeListeners.clear();
    }

    public void createOrUpdateProfile(final Profile profile, final OnProfileCreatedListener onProfileCreatedListener) {

        HashMap<String, Object> userPublicMap = new HashMap<>();
        userPublicMap.put("id", profile.getId());
        userPublicMap.put("username", profile.getUsername().toLowerCase());
        if (profile.getPhotoUrl() != null) {
            userPublicMap.put("photoUrl", profile.getPhotoUrl());
        }

        Map userPrivateMap = new ObjectMapper().convertValue(profile, Map.class);
        Map<String, Object> result = new HashMap<>();

        result.put(Consts.FIREBASE_LOCATION_USERS + "/" + profile.getId(), userPrivateMap);
        result.put(Consts.FIREBASE_PUBLIC_USERS + "/" + profile.getId(), userPublicMap);

        DatabaseReference reference = database.getReference();
        Task<Void> task = reference.updateChildren(result);
        task.addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                onProfileCreatedListener.onProfileCreated(task.isSuccessful());
                addRegistrationToken(FirebaseInstanceId.getInstance().getToken(), profile.getId());
                LogUtil.logDebug(TAG, "createOrUpdateProfile, success: " + task.isSuccessful());
            }
        });

    }


    public void updateRegistrationToken(final String token) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            final String currentUserId = firebaseUser.getUid();

            getProfileSingleValue(currentUserId, new OnObjectChangedListener<Profile>() {
                @Override
                public void onObjectChanged(Profile obj) {
                    if (obj != null) {
                        addRegistrationToken(token, currentUserId);
                    } else {
                        LogUtil.logError(TAG, "updateRegistrationToken",
                                new RuntimeException("Profile is not found"));
                    }
                }
            });
        }
    }

    public void addRegistrationToken(String token, String userId) {
        DatabaseReference databaseReference = ApplicationHelper.getDatabaseHelper().getDatabaseReference();
        Task<Void> task = databaseReference.child("profiles").child(userId).child("notificationTokens").child(token).setValue(true);
        task.addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                LogUtil.logDebug(TAG, "addRegistrationToken, success: " + task.isSuccessful());
            }
        });
    }

    public void removeRegistrationToken(String token, String userId) {
        DatabaseReference databaseReference = ApplicationHelper.getDatabaseHelper().getDatabaseReference();
        DatabaseReference tokenRef = databaseReference.child("profiles").child(userId).child("notificationTokens").child(token);
        Task<Void> task = tokenRef.removeValue();
        task.addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                LogUtil.logDebug(TAG, "removeRegistrationToken, success: " + task.isSuccessful());
            }
        });
    }

    public String generatePostId() {
        DatabaseReference databaseReference = database.getReference();
        return databaseReference.child("posts").push().getKey();
    }


    public void createOrUpdatePost(Post post) {
        try {
            if (post.getImagePath() != null) {
                post.setPostType("image");
            } else if (post.getTitle() != null && post.getImagePath() != null) {
                post.setPostType("text_image");
            }

            post.setCommentStatus(new CommentStatus(true));
            DatabaseReference databaseReference = database.getReference();

            Map<String, Object> postValues = post.toMap();
            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put("/posts/" + post.getId(), postValues);

            databaseReference.updateChildren(childUpdates);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public Task<Void> createNewPost(Post post) {

        if (post.getTitle() != null) {
            post.setPostType("text");
        }
        post.setCommentStatus(new CommentStatus(true));
        DatabaseReference databaseReference = database.getReference();

        Map<String, Object> postValues = post.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/posts/" + post.getId(), postValues);

        return databaseReference.updateChildren(childUpdates);
    }


    public Task<Void> removePost(Post post) {
        DatabaseReference databaseReference = database.getReference();
        DatabaseReference postRef = databaseReference.child("posts").child(post.getId());
        return postRef.removeValue();
    }

    public void updateProfileLikeCountAfterRemovingPost(Post post) {
        DatabaseReference profileRef = database.getReference("profiles/" + post.getAuthorId() + "/likesCount");
        final long likesByPostCount = post.getLikesCount();

        profileRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Integer currentValue = mutableData.getValue(Integer.class);
                if (currentValue != null && currentValue >= likesByPostCount) {
                    mutableData.setValue(currentValue - likesByPostCount);
                }

                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                LogUtil.logInfo(TAG, "Updating likes count transaction is completed.");
            }
        });

    }

    public Task<Void> removeImage(String imageTitle) {
        StorageReference storageRef = storage.getReferenceFromUrl(context.getResources().getString(R.string.storage_link));
        StorageReference desertRef = storageRef.child("images/" + imageTitle);
        return desertRef.delete();
    }

    public void createComment(final Comment comment, final OnTaskCompleteListener onTaskCompleteListener) {
        try {
//            String authorId = firebaseAuth.getCurrentUser().getUid();
            DatabaseReference mCommentsReference = database.getReference().child("post-comments/" + comment.getPostId());
            String commentId = mCommentsReference.push().getKey();

            if (comment.getId() == null) {
                comment.setId(commentId);
            }

            mCommentsReference.child(commentId).setValue(comment, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError == null) {
                        incrementCommentsCount(comment.getPostId());
                    } else {
                        LogUtil.logError(TAG, databaseError.getMessage(), databaseError.toException());
                    }
                }

                private void incrementCommentsCount(String postId) {
                    DatabaseReference postRef = database.getReference("posts/" + postId + "/commentsCount");
                    postRef.runTransaction(new Transaction.Handler() {
                        @Override
                        public Transaction.Result doTransaction(MutableData mutableData) {
                            Integer currentValue = mutableData.getValue(Integer.class);
                            if (currentValue == null) {
                                mutableData.setValue(1);
                            } else {
                                mutableData.setValue(currentValue + 1);
                            }

                            return Transaction.success(mutableData);
                        }

                        @Override
                        public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                            LogUtil.logInfo(TAG, "Updating comments count transaction is completed.");
                            if (onTaskCompleteListener != null) {
                                onTaskCompleteListener.onTaskComplete(true);
                            }
                        }
                    });
                }
            });
        } catch (Exception e) {
            LogUtil.logError(TAG, "createComment()", e);
        }
    }

    public void updateComment(String commentId, String commentText, String postId, final OnTaskCompleteListener onTaskCompleteListener) {
        DatabaseReference mCommentReference = database.getReference().child("post-comments")
                .child(postId).child(commentId).child("text");
        mCommentReference.setValue(commentText).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                if (onTaskCompleteListener != null) {
                    onTaskCompleteListener.onTaskComplete(true);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (onTaskCompleteListener != null) {
                    onTaskCompleteListener.onTaskComplete(false);
                }
                LogUtil.logError(TAG, "updateComment", e);
            }
        });
    }

    public void updateSingleComment(String postId, HashMap<String, Object> hashMap, final OnTaskCompleteListener onTaskCompleteListener) {

        String commentId = (String) hashMap.get("id");

        DatabaseReference mCommentReference = database.getReference().child("post-comments")
                .child(postId).child(commentId);

        mCommentReference.updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                if (onTaskCompleteListener != null) {
                    onTaskCompleteListener.onTaskComplete(true);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (onTaskCompleteListener != null) {
                    onTaskCompleteListener.onTaskComplete(false);
                }
                LogUtil.logError(TAG, "updateComment", e);
            }
        });
    }


    public void decrementCommentsCount(String postId, final OnTaskCompleteListener onTaskCompleteListener) {
        DatabaseReference postRef = database.getReference("posts/" + postId + "/commentsCount");
        postRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Integer currentValue = mutableData.getValue(Integer.class);
                if (currentValue != null && currentValue >= 1) {
                    mutableData.setValue(currentValue - 1);
                }

                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                LogUtil.logInfo(TAG, "Updating comments count transaction is completed.");
                if (onTaskCompleteListener != null) {
                    onTaskCompleteListener.onTaskComplete(true);
                }
            }
        });
    }

    public Task<Void> removeComment(String commentId, String postId) {
        DatabaseReference databaseReference = database.getReference();
        DatabaseReference postRef = databaseReference.child("post-comments").child(postId).child(commentId);
        return postRef.removeValue();
    }

    public Task<Void> setCommentsStatus(String postId, boolean status) {
        DatabaseReference reference = database.getReference(Consts.POSTS_REF)
                .child(postId).child(Consts.COMMENT_STATUS_REF);
        return reference.setValue(new CommentStatus(status));
    }

    public void onNewLikeAddedListener(ChildEventListener childEventListener) {
        DatabaseReference mLikesReference = database.getReference().child("post-likes");
        mLikesReference.addChildEventListener(childEventListener);
    }

    public void createOrUpdateLike(final String postId, final String postAuthorId) {
        try {
            String authorId = firebaseAuth.getCurrentUser().getUid();
            DatabaseReference mLikesReference = database.getReference().child("post-likes").child(postId).child(authorId);
            mLikesReference.push();
            String id = mLikesReference.push().getKey();
            Like like = new Like(authorId);
            like.setId(id);

            mLikesReference.child(id).setValue(like, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError == null) {
                        DatabaseReference postRef = database.getReference("posts/" + postId + "/likesCount");
                        incrementLikesCount(postRef);

                        DatabaseReference profileRef = database.getReference("profiles/" + postAuthorId + "/likesCount");
                        incrementLikesCount(profileRef);
                    } else {
                        LogUtil.logError(TAG, databaseError.getMessage(), databaseError.toException());
                    }
                }

                private void incrementLikesCount(DatabaseReference postRef) {
                    postRef.runTransaction(new Transaction.Handler() {
                        @Override
                        public Transaction.Result doTransaction(MutableData mutableData) {
                            Integer currentValue = mutableData.getValue(Integer.class);
                            if (currentValue == null) {
                                mutableData.setValue(1);
                            } else {
                                mutableData.setValue(currentValue + 1);
                            }

                            return Transaction.success(mutableData);
                        }

                        @Override
                        public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                            LogUtil.logInfo(TAG, "Updating likes count transaction is completed.");
                        }
                    });
                }

            });
        } catch (Exception e) {
            LogUtil.logError(TAG, "createOrUpdateLike()", e);
        }

    }


    public void updateCommentLike(final String postId, final String commentId) {
        try {
            String authorId = firebaseAuth.getCurrentUser().getUid();
            DatabaseReference mLikesReference = database.getReference().child("comment-likes").child(postId)
                    .child(commentId).child(authorId);
            mLikesReference.push();
            String id = mLikesReference.push().getKey();
            Like like = new Like(authorId);
            like.setId(id);

            mLikesReference.child(id).setValue(like, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError == null) {
                        DatabaseReference commentRef = database.getReference("post-comments/" + postId + "/"
                                + commentId + "/likesCount");
                        incrementLikesCount(commentRef);

                    } else {
                        LogUtil.logError(TAG, databaseError.getMessage(), databaseError.toException());
                    }
                }

                private void incrementLikesCount(DatabaseReference postRef) {
                    postRef.runTransaction(new Transaction.Handler() {
                        @Override
                        public Transaction.Result doTransaction(MutableData mutableData) {
                            Integer currentValue = mutableData.getValue(Integer.class);
                            if (currentValue == null) {
                                mutableData.setValue(1);
                            } else {
                                mutableData.setValue(currentValue + 1);
                            }

                            return Transaction.success(mutableData);
                        }

                        @Override
                        public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                            LogUtil.logInfo(TAG, "Updating likes count transaction is completed.");
                        }
                    });
                }

            });
        } catch (Exception e) {
            LogUtil.logError(TAG, "createOrUpdateLike()", e);
        }

    }

    public void incrementWatchersCount(String postId) {
        DatabaseReference postRef = database.getReference("posts/" + postId + "/watchersCount");
        postRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Integer currentValue = mutableData.getValue(Integer.class);
                if (currentValue == null) {
                    mutableData.setValue(1);
                } else {
                    mutableData.setValue(currentValue + 1);
                }

                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                LogUtil.logInfo(TAG, "Updating Watchers count transaction is completed.");
            }
        });
    }

    public void removeLike(final String postId, final String postAuthorId) {
        String authorId = firebaseAuth.getCurrentUser().getUid();
        DatabaseReference mLikesReference = database.getReference().child("post-likes").child(postId).child(authorId);
        mLikesReference.removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    DatabaseReference postRef = database.getReference("posts/" + postId + "/likesCount");
                    decrementLikesCount(postRef);

                    DatabaseReference profileRef = database.getReference("profiles/" + postAuthorId + "/likesCount");
                    decrementLikesCount(profileRef);
                } else {
                    LogUtil.logError(TAG, databaseError.getMessage(), databaseError.toException());
                }
            }

            private void decrementLikesCount(DatabaseReference postRef) {
                postRef.runTransaction(new Transaction.Handler() {
                    @Override
                    public Transaction.Result doTransaction(MutableData mutableData) {
                        Long currentValue = mutableData.getValue(Long.class);
                        if (currentValue == null) {
                            mutableData.setValue(0);
                        } else {
                            mutableData.setValue(currentValue - 1);
                        }

                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                        LogUtil.logInfo(TAG, "Updating likes count transaction is completed.");
                    }
                });
            }
        });
    }

    public void removeCommentLike(final String postId, final String commentId) {
        String authorId = firebaseAuth.getCurrentUser().getUid();
        DatabaseReference mLikesReference = database.getReference().child("comment-likes").child(postId)
                .child(commentId).child(authorId);
        mLikesReference.removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    DatabaseReference commentRef = database.getReference("post-comments/" + postId + "/"
                            + commentId + "/likesCount");
                    decrementLikesCount(commentRef);

                } else {
                    LogUtil.logError(TAG, databaseError.getMessage(), databaseError.toException());
                }
            }

            private void decrementLikesCount(DatabaseReference postRef) {
                postRef.runTransaction(new Transaction.Handler() {
                    @Override
                    public Transaction.Result doTransaction(MutableData mutableData) {
                        Long currentValue = mutableData.getValue(Long.class);
                        if (currentValue == null) {
                            mutableData.setValue(0);
                        } else {
                            mutableData.setValue(currentValue - 1);
                        }

                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                        LogUtil.logInfo(TAG, "Updating likes count transaction is completed.");
                    }
                });
            }
        });
    }

    public UploadTask uploadImage(Uri imageUri, String imageTitle) {
        StorageReference storageRef = storage.getReferenceFromUrl(ApplicationHelper.storageBucketLink);
        StorageReference riversRef = storageRef.child("images/" + imageTitle);
        // Create file metadata including the content type
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setCacheControl("max-age=7776000, Expires=7776000, public, must-revalidate")
                .build();

        Log.d(TAG, "compressedImage: " + Arrays.toString(getCompressedImage(imageUri)));

        return riversRef.putBytes(getCompressedImage(imageUri), metadata);
    }

    public byte[] getCompressedImage(Uri imageUri) {

        try {
            File mImageFile = FileUtil.from(context, imageUri);

            Bitmap bitmap = new Compressor(context)
                    .setQuality(75)
                    .setCompressFormat(Bitmap.CompressFormat.WEBP)
                    .setDestinationDirectoryPath(Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_PICTURES).getAbsolutePath())
                    .compressToBitmap(mImageFile);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, e.getMessage());
        }
        return null;
    }

    public void getPostList(final OnPostListChangedListener<Post> onDataChangedListener, long date) {
        DatabaseReference databaseReference = database.getReference("posts");
        Query postsQuery;
        if (date == 0) {
            postsQuery = databaseReference.limitToLast(Constants.Post.POST_AMOUNT_ON_PAGE).orderByChild("createdDate");
        } else {
            postsQuery = databaseReference.limitToLast(Constants.Post.POST_AMOUNT_ON_PAGE).endAt(date).orderByChild("createdDate");
        }

        postsQuery.keepSynced(true);
        postsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "datasnapshot post: " + dataSnapshot.getValue());
                Map<String, Object> objectMap = (Map<String, Object>) dataSnapshot.getValue();
                PostListResult result = parsePostList(objectMap);

                if (result.getPosts().isEmpty() && result.isMoreDataAvailable()) {
                    getPostList(onDataChangedListener, result.getLastItemCreatedDate() - 1);
                } else {
                    onDataChangedListener.onListChanged(parsePostList(objectMap));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogUtil.logError(TAG, "getPostList(), onCancelled", new Exception(databaseError.getMessage()));
                onDataChangedListener.onCanceled(context.getString(R.string.permission_denied_error));
            }
        });
    }


    public void getPostListByUser(final OnDataChangedListener<Post> onDataChangedListener, String userId) {
        DatabaseReference databaseReference = database.getReference("posts");
        Query postsQuery;
        postsQuery = databaseReference.orderByChild("authorId").equalTo(userId);

        postsQuery.keepSynced(true);
        postsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    PostListResult result = parsePostList((Map<String, Object>) dataSnapshot.getValue());
                    onDataChangedListener.onListChanged(result.getPosts());
                } else {
                    onDataChangedListener.inEmpty(true, "empty");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                onDataChangedListener.inEmpty(false, databaseError.getMessage());
                LogUtil.logError(TAG, "getPostListByUser(), onCancelled", new Exception(databaseError.getMessage()));
            }
        });
    }

    public ValueEventListener getPost(final String id, final OnPostChangedListener listener) {
        DatabaseReference databaseReference = getDatabaseReference().child("posts").child(id);
        ValueEventListener valueEventListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    Log.d(TAG, "Post Value: " + dataSnapshot.getValue());
                    Post post = dataSnapshot.getValue(Post.class);
                    if (post != null) {
                        post.setId(id);
                    }
                    listener.onObjectChanged(post);
                } else {
                    listener.onObjectChanged(null);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogUtil.logError(TAG, "getPost(), onCancelled", new Exception(databaseError.getMessage()));
            }
        });

        activeListeners.put(valueEventListener, databaseReference);
        return valueEventListener;
    }

    public void getSinglePost(final String id, final OnPostChangedListener listener) {
        DatabaseReference databaseReference = getDatabaseReference().child("posts").child(id);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (isPostValid((Map<String, Object>) dataSnapshot.getValue())) {
                    Post post = dataSnapshot.getValue(Post.class);
                    if (post != null) {
                        post.setId(id);
                    }
                    listener.onObjectChanged(post);
                } else {
                    listener.onError(String.format(context.getString(R.string.error_general_post), id));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogUtil.logError(TAG, "getSinglePost(), onCancelled", new Exception(databaseError.getMessage()));
            }
        });
    }

    public void getSingleComment(String postId, final String id, final OnCommentChangedListener onCommentChangedListener) {
        DatabaseReference reference = getDatabaseReference().child(Consts.POST_COMMENTS_REF).child(postId).child(id);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    HashMap hashMap = (HashMap) dataSnapshot.getValue();
                    if (hashMap != null) {
                        Comment comment = new Comment();
                        comment.setId(id);
                        comment.setPostId((String) hashMap.get("postId"));
                        comment.setAuthorId((String) hashMap.get("authorId"));
                        comment.setText((String) hashMap.get("text"));
                        comment.setLikesCount((long) hashMap.get("likesCount"));
                        comment.setCreatedDate((long) hashMap.get("createdDate"));

                        if (hashMap.containsKey("mentions")) {
                            GenericTypeIndicator<List<Mention>> indicator = new GenericTypeIndicator<List<Mention>>() {
                            };
                            List<Mention> mentionList = dataSnapshot.child("mentions").getValue(indicator);
                            if (mentionList != null) {
                                List<Mentionable> mentionables = new ArrayList<Mentionable>(mentionList);
                                comment.setMentions(mentionables);

                                Log.d(TAG, "Mentions: " + mentionables);
                            }
                        }

                        onCommentChangedListener.onObjectChanged(comment);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogUtil.logError(TAG, "getSingleComment(), onCancelled", new Exception(databaseError.getMessage()));
            }
        });
    }

    private PostListResult parsePostList(Map<String, Object> objectMap) {
        PostListResult result = new PostListResult();
        List<Post> list = new ArrayList<>();
        boolean isMoreDataAvailable = true;
        long lastItemCreatedDate = 0;

        if (objectMap != null) {
            isMoreDataAvailable = Constants.Post.POST_AMOUNT_ON_PAGE == objectMap.size();

            for (String key : objectMap.keySet()) {
                Object obj = objectMap.get(key);
                if (obj instanceof Map) {
                    Map<String, Object> mapObj = (Map<String, Object>) obj;

                    if (!isPostValid(mapObj)) {
                        LogUtil.logDebug(TAG, "Invalid post, id: " + key);
                        continue;
                    }

                    boolean hasComplain = mapObj.containsKey("hasComplain") && (boolean) mapObj.get("hasComplain");
                    long createdDate = (long) mapObj.get("createdDate");

                    if (lastItemCreatedDate == 0 || lastItemCreatedDate > createdDate) {
                        lastItemCreatedDate = createdDate;
                    }


                    if (!hasComplain) {
                        Post post = new Post();
                        post.setId(key);

                        if (mapObj.containsKey("title")) {
                            post.setTitle((String) mapObj.get("title"));
                        }
                        if (mapObj.containsKey("imagePath")) {
                            post.setImagePath((String) mapObj.get("imagePath"));
                            post.setImageTitle((String) mapObj.get("imageTitle"));
                        }
                        post.setAuthorId((String) mapObj.get("authorId"));
                        post.setPostType((String) mapObj.get("postType"));
                        post.setCreatedDate(createdDate);

                        if (mapObj.containsKey("postStyle")) {
                            HashMap hashMap = (HashMap) mapObj.get("postStyle");
                            long bg_color = (long) hashMap.get("bg_color");
                            post.setPostStyle(new PostStyle((int) bg_color));
                        }

                        if (mapObj.containsKey("link")) {
                            post.setLink((String) mapObj.get("link"));
                        }
                        if (mapObj.containsKey("commentStatus")) {
                            HashMap hashMap = (HashMap) mapObj.get("commentStatus");
                            post.setCommentStatus(new CommentStatus((boolean) hashMap.get("commentStatus")));
                        }
                        if (mapObj.containsKey("commentsCount")) {
                            post.setCommentsCount((long) mapObj.get("commentsCount"));
                        }
                        if (mapObj.containsKey("likesCount")) {
                            post.setLikesCount((long) mapObj.get("likesCount"));
                        }
                        if (mapObj.containsKey("watchersCount")) {
                            post.setWatchersCount((long) mapObj.get("watchersCount"));
                        }

                        list.add(post);
                    }
                }
            }

            Collections.sort(list, new Comparator<Post>() {
                @Override
                public int compare(Post lhs, Post rhs) {
                    return ((Long) rhs.getCreatedDate()).compareTo(lhs.getCreatedDate());
                }
            });

            result.setPosts(list);
            result.setLastItemCreatedDate(lastItemCreatedDate);
            result.setMoreDataAvailable(isMoreDataAvailable);
        }

        return result;
    }

    private boolean isPostValid(Map<String, Object> post) {
        return post.containsKey("title")
                || post.containsKey("link")
                || post.containsKey("imagePath")
                && post.containsKey("authorId");
    }

    public void getProfileSingleValue(String id, final OnObjectChangedListener<Profile> listener) {
        DatabaseReference databaseReference = getDatabaseReference().child("profiles").child(id);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Profile profile = dataSnapshot.getValue(Profile.class);
                listener.onObjectChanged(profile);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogUtil.logError(TAG, "getProfileSingleValue(), onCancelled", new Exception(databaseError.getMessage()));
            }
        });
    }

    public void getUserPublicProfile(String id, final OnObjectChangedListener<UsersPublic> listener) {
        DatabaseReference databaseReference = getDatabaseReference().child(Consts.FIREBASE_PUBLIC_USERS).child(id);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UsersPublic profile = dataSnapshot.getValue(UsersPublic.class);
                listener.onObjectChanged(profile);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogUtil.logError(TAG, "getProfileSingleValue(), onCancelled", new Exception(databaseError.getMessage()));
            }
        });
    }

    public ValueEventListener getProfile(String id, final OnObjectChangedListener<Profile> listener) {
        DatabaseReference databaseReference = getDatabaseReference().child("profiles").child(id);
        ValueEventListener valueEventListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Profile profile = dataSnapshot.getValue(Profile.class);
                listener.onObjectChanged(profile);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogUtil.logError(TAG, "getProfile(), onCancelled", new Exception(databaseError.getMessage()));
            }
        });
        activeListeners.put(valueEventListener, databaseReference);
        return valueEventListener;
    }

    public ValueEventListener getPublicProfile(String id, final OnObjectChangedListener<UsersPublic> listener) {
        DatabaseReference databaseReference = getDatabaseReference().child(Consts.FIREBASE_PUBLIC_USERS).child(id);
        ValueEventListener valueEventListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UsersPublic profile = dataSnapshot.getValue(UsersPublic.class);
                listener.onObjectChanged(profile);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogUtil.logError(TAG, "getPublicProfile(), onCancelled", new Exception(databaseError.getMessage()));
            }
        });
        activeListeners.put(valueEventListener, databaseReference);
        return valueEventListener;
    }

    public void deleteAccount(final OnTaskCompleteListener onTaskCompleteListener) {
        DatabaseReference reference = database.getReference(Consts.FIREBASE_LOCATION_USERS)
                .child(getCurrentUser()).child(Consts.ACCOUNT_STATUS);

        reference.setValue(new AccountStatus(Consts.ACCOUNT_DISABLED)).addOnCompleteListener
                (new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        PreferencesUtil.setProfileActive(context, false);
                        onTaskCompleteListener.onTaskComplete(true);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                onTaskCompleteListener.onTaskComplete(false);
            }
        });
    }

    public void setAccountStatusActive(final OnTaskCompleteListener onTaskCompleteListener) {
        DatabaseReference reference = database.getReference(Consts.FIREBASE_LOCATION_USERS)
                .child(getCurrentUser()).child(Consts.ACCOUNT_STATUS);
        reference.setValue(new AccountStatus(Consts.ACCOUNT_ACTIVE)).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                onTaskCompleteListener.onTaskComplete(true);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                onTaskCompleteListener.onTaskComplete(false);
            }
        });
    }

    public void checkAccountStatus(String userKey, final OnObjectChangedListener<AccountStatus> objectChangedListener) {
        DatabaseReference reference = database.getReference(Consts.FIREBASE_LOCATION_USERS)
                .child(userKey).child(Consts.ACCOUNT_STATUS);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                AccountStatus status = dataSnapshot.getValue(AccountStatus.class);
                objectChangedListener.onObjectChanged(status);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public ValueEventListener getCommentsList(String postId, final OnDataChangedListener<Comment> onDataChangedListener) {
        DatabaseReference databaseReference = database.getReference("post-comments").child(postId);
        ValueEventListener valueEventListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Comment> list = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Comment comment = snapshot.getValue(Comment.class);
                    list.add(comment);
                }

                Collections.sort(list, new Comparator<Comment>() {
                    @Override
                    public int compare(Comment lhs, Comment rhs) {
                        return ((Long) rhs.getCreatedDate()).compareTo(lhs.getCreatedDate());
                    }
                });

                onDataChangedListener.onListChanged(list);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogUtil.logError(TAG, "getCommentsList(), onCancelled", new Exception(databaseError.getMessage()));
            }
        });

        activeListeners.put(valueEventListener, databaseReference);
        return valueEventListener;
    }


    public void getCommentList(String postId, final OnDataChangedListener<Comment> onDataChangedListener) {
        DatabaseReference databaseReference = database.getReference("post-comments").child(postId);
        final List<Comment> list = new ArrayList<>();
        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot != null) {
                    HashMap hashMap = (HashMap) dataSnapshot.getValue();
                    if (hashMap != null) {
                        Comment comment = new Comment();
                        comment.setId((String) hashMap.get("id"));
                        comment.setPostId((String) hashMap.get("postId"));
                        comment.setAuthorId((String) hashMap.get("authorId"));
                        comment.setText((String) hashMap.get("text"));
                        comment.setLikesCount((long) hashMap.get("likesCount"));
                        comment.setCreatedDate((long) hashMap.get("createdDate"));

                        if (hashMap.containsKey("mentions")) {
                            GenericTypeIndicator<List<Mention>> indicator = new GenericTypeIndicator<List<Mention>>() {
                            };
                            List<Mention> mentionList = dataSnapshot.child("mentions").getValue(indicator);
                            if (mentionList != null) {
                                List<Mentionable> mentionables = new ArrayList<Mentionable>(mentionList);
                                comment.setMentions(mentionables);

                                Log.d(TAG, "Mentions: " + mentionables);
                            }
                        }

                        list.add(comment);
                    }


                    Collections.sort(list, new Comparator<Comment>() {
                        @Override
                        public int compare(Comment lhs, Comment rhs) {
                            return ((Long) rhs.getCreatedDate()).compareTo(lhs.getCreatedDate());
                        }
                    });

                    onDataChangedListener.onListChanged(list);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private CommentListResult parseCommentList(Map<String, Object> objectMap) {
        CommentListResult result = new CommentListResult();
        List<Comment> list = new ArrayList<>();
        boolean isMoreDataAvailable = true;
        long lastItemCreatedDate = 0;

        if (objectMap != null) {
            isMoreDataAvailable = Constants.Post.POST_AMOUNT_ON_PAGE == objectMap.size();

            for (String key : objectMap.keySet()) {
                Object obj = objectMap.get(key);
                if (obj instanceof Map) {
                    Map<String, Object> mapObj = (Map<String, Object>) obj;

                    long createdDate = (long) mapObj.get("createdDate");


                    if (lastItemCreatedDate == 0 || lastItemCreatedDate > createdDate) {
                        lastItemCreatedDate = createdDate;
                    }

                    Comment comment = new Comment();
                    comment.setId(key);
                    comment.setPostId((String) mapObj.get("postId"));
                    comment.setText((String) mapObj.get("text"));
                    comment.setCreatedDate(createdDate);
                    comment.setLikesCount((long) mapObj.get("likesCount"));
                    comment.setAuthorId((String) mapObj.get("authorId"));

                    list.add(comment);
                }
            }

            Collections.sort(list, new Comparator<Comment>() {
                @Override
                public int compare(Comment lhs, Comment rhs) {
                    return ((Long) rhs.getCreatedDate()).compareTo(lhs.getCreatedDate());
                }
            });

            result.setComments(list);
            result.setLastItemCreatedDate(lastItemCreatedDate);
            result.setMoreDataAvailable(isMoreDataAvailable);
        }

        return result;
    }


    public ValueEventListener hasCurrentUserLike(String postId, String userId, final OnObjectExistListener<Like> onObjectExistListener) {
        DatabaseReference databaseReference = database.getReference("post-likes").child(postId).child(userId);
        ValueEventListener valueEventListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                onObjectExistListener.onDataChanged(dataSnapshot.exists());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogUtil.logError(TAG, "hasCurrentUserLike(), onCancelled", new Exception(databaseError.getMessage()));
            }
        });

        activeListeners.put(valueEventListener, databaseReference);
        return valueEventListener;
    }


    public void hasCurrentUserLikeSingleValue(String postId, String userId, final OnObjectExistListener<Like> onObjectExistListener) {
        DatabaseReference databaseReference = database.getReference("post-likes").child(postId).child(userId);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                onObjectExistListener.onDataChanged(dataSnapshot.exists());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogUtil.logError(TAG, "hasCurrentUserLikeSingleValue(), onCancelled", new Exception(databaseError.getMessage()));
            }
        });
    }

    public void isCurrentPostColored(String id, final OnObjectChangedListener<PostStyle> currentPostColored) {
        DatabaseReference reference = database.getReference(Consts.POSTS_REF).child(id).child("postStyle");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    PostStyle style = dataSnapshot.getValue(PostStyle.class);
                    currentPostColored.onObjectChanged(style);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void hasCurrentUserLikeCommentValue(Comment comment,
                                               final OnObjectExistListener<Like> onObjectExistListener) {
        DatabaseReference databaseReference = database.getReference(Consts.COMMENTS_LIKES_REF).child(comment.getPostId())
                .child(comment.getId()).child(getCurrentUser());
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                onObjectExistListener.onDataChanged(dataSnapshot.exists());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogUtil.logError(TAG, "hasCurrentUserLikeSingleValue(), onCancelled", new Exception(databaseError.getMessage()));
            }
        });
    }

    public ValueEventListener getFriendsList(String userKey, String listType, final OnDataChangedListener<Friends> onDataChangedListener) {

        DatabaseReference reference = database.getReference(Consts.FRIENDS_REF)
                .child(userKey).child(listType);

        final List<Friends> list = new ArrayList<>();
        ValueEventListener eventListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "Friends List: " + dataSnapshot.getValue());
                if (dataSnapshot.getValue() != null) {
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        Friends friends = child.getValue(Friends.class);
                        list.add(friends);
                    }

                    Collections.sort(list, new Comparator<Friends>() {
                        @Override
                        public int compare(Friends o1, Friends o2) {
                            return (o2.getCreatedDate()).compareTo(o1.getCreatedDate());
                        }
                    });

                    onDataChangedListener.onListChanged(list);
                } else {
                    onDataChangedListener.inEmpty(true, null);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                onDataChangedListener.inEmpty(false, databaseError.getMessage());
            }
        });

        activeListeners.put(eventListener, reference);
        return eventListener;
    }

    private void parceFriendsList(HashMap<String, Object> hashMap) {

        List<Friends> list = new ArrayList<>();
        if (hashMap != null) {
            for (String key : hashMap.keySet()) {
                Object object = hashMap.get(key);
                if (object instanceof HashMap) {
                    HashMap<String, Object> mapObj = (HashMap<String, Object>) object;

                    Friends friends = new Friends();
                    friends.setId((String) hashMap.get("id"));
                    friends.setCreatedDate((Long) hashMap.get("createdDate"));
                    friends.setType((String) hashMap.get("type"));

                    list.add(friends);
                }
            }

            Collections.sort(list, new Comparator<Friends>() {
                @Override
                public int compare(Friends o1, Friends o2) {
                    return (o2.getCreatedDate()).compareTo(o1.getCreatedDate());
                }
            });
        }

    }

    public void addComplainToPost(Post post) {
        DatabaseReference databaseReference = getDatabaseReference();
        databaseReference.child("posts").child(post.getId()).child("hasComplain").setValue(true);
    }

    public void isPostExistSingleValue(String postId, final OnObjectExistListener<Post> onObjectExistListener) {
        DatabaseReference databaseReference = database.getReference("posts").child(postId);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                onObjectExistListener.onDataChanged(dataSnapshot.exists());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogUtil.logError(TAG, "isPostExistSingleValue(), onCancelled", new Exception(databaseError.getMessage()));
            }
        });
    }

    public void isCommentExistSingleValue(String postId, String commentId, final OnObjectExistListener<Comment> onObjectExistListener) {
        DatabaseReference databaseReference = database.getReference(Consts.POST_COMMENTS_REF).child(postId).child(commentId);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                onObjectExistListener.onDataChanged(dataSnapshot.exists());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogUtil.logError(TAG, "isCommentExistSingleValue(), onCancelled", new Exception(databaseError.getMessage()));
            }
        });
    }

    public ValueEventListener checkFollowStatus(String userKey, final OnFollowStatusChanged onFollowStatusChanged) {
        DatabaseReference databaseReference = database.getReference(Consts.FRIENDS_REF).child(userKey);
        ValueEventListener valueEventListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(Consts.FOLLOWERS_LIST_REF).hasChild(getCurrentUser())) {
                    onFollowStatusChanged.isStatus(Consts.FOLLOWERS_LIST_REF);
                    Log.d(TAG, "following");
                } else if (dataSnapshot.child(Consts.REQUEST_LIST_REF).hasChild(getCurrentUser())) {
                    onFollowStatusChanged.isStatus(Consts.REQUEST_LIST_REF);
                    Log.d(TAG, "requested");
                } else {
                    onFollowStatusChanged.isStatus(Consts.FOLLOW_KEY);
                    Log.d(TAG, "follow");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        activeListeners.put(valueEventListener, databaseReference);
        return valueEventListener;
    }

    public void updateFollowRequest(String userKey, final OnTaskCompleteListener onTaskCompleteListener) {
        DatabaseReference reference = database.getReference(Consts.FRIENDS_REF).child(userKey)
                .child(Consts.REQUEST_LIST_REF);
        Friends friends = new Friends(getCurrentUser(), "request");
        reference.child(getCurrentUser()).setValue(friends, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    onTaskCompleteListener.onTaskComplete(true);
                } else {
                    onTaskCompleteListener.onTaskComplete(false);
                }
            }
        });
    }

    public void removeFollowRequest(String userKey, final OnTaskCompleteListener onTaskCompleteListener) {
        DatabaseReference reference = database.getReference(Consts.FRIENDS_REF).child(userKey)
                .child(Consts.REQUEST_LIST_REF);
        reference.child(getCurrentUser()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                onTaskCompleteListener.onTaskComplete(true);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                onTaskCompleteListener.onTaskComplete(false);
            }
        });
    }

    public void removeFromFollowing(String userKey, final OnTaskCompleteListener onTaskCompleteListener) {
        DatabaseReference reference = database.getReference(Consts.FRIENDS_REF).child(userKey)
                .child(Consts.FOLLOWERS_LIST_REF);
        reference.child(getCurrentUser()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                onTaskCompleteListener.onTaskComplete(true);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                onTaskCompleteListener.onTaskComplete(false);
            }
        });
    }

    public ValueEventListener getSearchList(String string, final OnDataChangedListener<UsersPublic> onDataChangedListener) {
        DatabaseReference reference = database.getReference(Consts.FIREBASE_PUBLIC_USERS);
        Query query = reference.orderByChild("username").startAt(string).endAt(string + "\uf8ff");

        ValueEventListener valueEventListener = query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    List<UsersPublic> list = new ArrayList<>();
                    if (dataSnapshot.getValue() != null) {
                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            UsersPublic usersPublic = child.getValue(UsersPublic.class);
                            list.add(usersPublic);
                        }
                    }
                    onDataChangedListener.onListChanged(list);
                } else {
                    onDataChangedListener.inEmpty(true, "empty");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                onDataChangedListener.inEmpty(false, databaseError.getMessage());
            }
        });

        activeListeners.put(valueEventListener, reference);
        return valueEventListener;
    }

    public ValueEventListener getAllUsersList(final OnDataChangedListener<UsersPublic> onDataChangedListener) {
        DatabaseReference reference = database.getReference(Consts.FIREBASE_PUBLIC_USERS);
        ValueEventListener valueEventListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    List<UsersPublic> list = new ArrayList<>();
                    if (dataSnapshot.getValue() != null) {
                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            UsersPublic usersPublic = child.getValue(UsersPublic.class);
                            list.add(usersPublic);
                        }
                    }
                    onDataChangedListener.onListChanged(list);
                } else {
                    onDataChangedListener.inEmpty(true, "empty");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                onDataChangedListener.inEmpty(false, databaseError.getMessage());
            }
        });
        activeListeners.put(valueEventListener, reference);
        return valueEventListener;
    }

    public void subscribeToNewPosts() {
        FirebaseMessaging.getInstance().subscribeToTopic("postsTopic");
    }

}
