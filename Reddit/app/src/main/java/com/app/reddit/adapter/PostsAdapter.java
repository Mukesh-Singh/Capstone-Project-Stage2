package com.app.reddit.adapter;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.reddit.R;
import com.app.reddit.api.Callback;
import com.app.reddit.api.RedditRestClient;
import com.app.reddit.events.ShareContentEvent;
import com.app.reddit.events.ViewCommentsEvent;
import com.app.reddit.events.ViewContentEvent;
import com.app.reddit.events.ViewSubredditPostsEvent;
import com.app.reddit.models.Post;
import com.app.reddit.utils.OnItemSelectedListener;
import com.app.reddit.utils.PreferenceUtil;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by mukesh on 19/12/16.
 */

@SuppressWarnings("DefaultFileTemplate")
public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.PostViewHolder> implements OnItemSelectedListener {
    private final PreferenceUtil mPre;
    private final Context mContext;
    private ArrayList<Post> posts = new ArrayList<>();

    public PostsAdapter(Context context, ArrayList<Post> list) {
        this.mContext = context;
        mPre = new PreferenceUtil(context);
        posts.clear();
        if (posts != null)
            this.posts.addAll(list);
    }

    @Override
    public PostViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.n_row_post, viewGroup, false);
        return new PostViewHolder(mContext, itemView, this, true, mPre);
    }

    @Override
    public void onBindViewHolder(PostViewHolder postViewHolder, int i) {
        postViewHolder.bindItem(posts.get(i));
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }


    public void addPosts(List<Post> posts) {
        this.posts.addAll(posts);
        this.notifyDataSetChanged();
    }

    public void setPosts(List<Post> posts) {
        this.posts = new ArrayList<>(posts);
        this.notifyDataSetChanged();
    }

    public ArrayList<Post> getPosts() {
        return posts;
    }


    @Override
    public void onItemSelected(int position) {
        Log.e("Item Clicked: ", position + "");
    }


    @SuppressWarnings("deprecation")
    public static class PostViewHolder extends RecyclerView.ViewHolder {


        private final PreferenceUtil mPref;
        private final Context context;
        private final OnItemSelectedListener onItemSelectedListener;
        private final int upvoteColor;
        private final int downvoteColor;
        private final int primaryTextColor;
        private final View rootLayout;
        private final TextView scoreTextView;
        private final TextView titleTextView;
        private final TextView subredditTextView;
        private final TextView numCommentsTextView;
        private final TextView createdTextView;
        private final ImageView thumbnailImageView;
        private final Button commentsButton;
        private final ImageView upvoteButton;
        private final ImageView downvoteButton;
        private final Button moreOptionsButton;

        private final Drawable upvoteDraw;
        private final Drawable downvoteDraw;
        private final Drawable defdrawable;
        private boolean goToComment;


        public PostViewHolder(Context context, View itemView, OnItemSelectedListener onItemSelectedListener, boolean goToComment, PreferenceUtil pref) {
            super(itemView);

            this.context = context;
            this.onItemSelectedListener = onItemSelectedListener;
            this.mPref = pref;
            this.goToComment = goToComment;
            Resources res = context.getResources();

            upvoteColor = res.getColor(R.color.reddit_upvote);
            downvoteColor = res.getColor(R.color.reddit_downvote);
            upvoteDraw = res.getDrawable(R.drawable.shape_circle_upvote_color);
            downvoteDraw = res.getDrawable(R.drawable.shape_circle_downvote);
            defdrawable = res.getDrawable(R.drawable.shape_circle_transparent);


            primaryTextColor = res.getColor(android.R.color.black);
            //res.getDrawable(android.R.drawable.arrow_down_float);

            rootLayout = itemView.findViewById(R.id.post_title_container);
            scoreTextView = (TextView) itemView.findViewById(R.id.score_textview);
            titleTextView = (TextView) itemView.findViewById(R.id.title_textview);
            subredditTextView = (TextView) itemView.findViewById(R.id.subreddit_textview);
            numCommentsTextView = (TextView) itemView.findViewById(R.id.num_comments_textview);
            createdTextView = (TextView) itemView.findViewById(R.id.created_textview);
            thumbnailImageView = (ImageView) itemView.findViewById(R.id.thumbnail_imageview);
            commentsButton = (Button) itemView.findViewById(R.id.comments_button);
            upvoteButton = (ImageView) itemView.findViewById(R.id.upvote_button);
            downvoteButton = (ImageView) itemView.findViewById(R.id.downvote_button);
            moreOptionsButton = (Button) itemView.findViewById(R.id.more_options_button);
        }

        public void bindItem(final Post post) {
            scoreTextView.setText(String.valueOf(post.getScore()));
            titleTextView.setText(post.getTitle());
            subredditTextView.setText(post.getSubreddit());
            numCommentsTextView.setText(post.getNumComments() + " " + context.getResources().getString(R.string.label_comments));
            createdTextView.setText(post.getCreated());
            thumbnailImageView.setVisibility(View.GONE);


            if (post.getThumbnail() != null) {
                thumbnailImageView.setVisibility(View.VISIBLE);
                Glide.with(context).load(post.getThumbnail()).crossFade().into(thumbnailImageView);
            }


            if (mPref.isUserAuthenticated()) {
                setColorsAccordingToVote(post.getLikes());
            } else {
                upvoteButton.setEnabled(false);
                downvoteButton.setEnabled(false);
            }

            // set click listeners
            View.OnClickListener onClickListener = new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    onItemSelectedListener.onItemSelected(getAdapterPosition());
                    switch (view.getId()) {


                        case R.id.comments_button:
                            if (goToComment) {
                                ViewCommentsEvent event = new ViewCommentsEvent(post);
                                event.setCommentView(rootLayout);
                                EventBus.getDefault().post(event);
                            }
                            break;
                        case R.id.post_title_container:
                        case R.id.thumbnail_imageview:
                            EventBus.getDefault().post(new ViewContentEvent(post.getTitle(), post.getUrl()));
                            break;
                        case R.id.upvote_button:
                            if (post.getLikes() == -1 || post.getLikes() == 0) {
                                post.setLikes(true);
                                post.setScore(post.getScore() + 1);
                            } else {
                                post.setLikes(null);
                                post.setScore(post.getScore() - 1);
                            }
                            new RedditRestClient(context).vote(post.getFullName(), post.getLikes(), getUpVoteCallback(post));
                            break;
                        case R.id.downvote_button:
                            if (post.getLikes() == 1 || post.getLikes() == 0) {
                                post.setLikes(false);
                                post.setScore(post.getScore() - 1);
                            } else {
                                post.setLikes(null);
                                post.setScore(post.getScore() + 1);
                            }
                            new RedditRestClient(context).vote(post.getFullName(), post.getLikes(), getDownVoteCallback(post));
                            break;
                        case R.id.more_options_button:
                            AlertDialog moreOptionsDialog = new AlertDialog.Builder(context)
                                    .setItems(R.array.more_post_options, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            if (i == 0) {  // View subreddit
                                                EventBus.getDefault().post(new ViewSubredditPostsEvent(post.getSubreddit(),
                                                        context.getResources().getString(R.string.action_sort_hot)));
                                            } else if (i == 1) { // Share link
                                                EventBus.getDefault().post(new ShareContentEvent(post.getUrl()));
                                            } else if (i == 2) { // Copy link
                                                ClipboardManager clipboard =
                                                        (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                                                clipboard.setPrimaryClip(ClipData.newPlainText(null, post.getUrl()));
                                                Toast.makeText(context, R.string.success_post_link_copied_to_clipboard,
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    })
                                    .create();
                            moreOptionsDialog.show();
                            break;


                    }
                }
            };

            rootLayout.setOnClickListener(onClickListener);
            commentsButton.setOnClickListener(onClickListener);
            thumbnailImageView.setOnClickListener(onClickListener);
            upvoteButton.setOnClickListener(onClickListener);
            downvoteButton.setOnClickListener(onClickListener);
            moreOptionsButton.setOnClickListener(onClickListener);
        }

        private Callback<Void> getUpVoteCallback(final Post post) {
            return new Callback<Void>() {

                @Override
                public void onSuccess(Void data) {
                    setColorsAccordingToVote(post.getLikes());
                    scoreTextView.setText(String.valueOf(post.getScore()));
                }

                @Override
                public void onFailure(String message) {
                    Toast.makeText(context, Html.fromHtml(message), Toast.LENGTH_SHORT).show();
                    post.setLikes(false);
                    post.setScore(post.getScore() - 1);

                }
            };
        }

        private Callback<Void> getDownVoteCallback(final Post post) {
            return new Callback<Void>() {

                @Override
                public void onSuccess(Void data) {
                    setColorsAccordingToVote(post.getLikes());
                    scoreTextView.setText(String.valueOf(post.getScore()));
                }

                @Override
                public void onFailure(String message) {
                    Toast.makeText(context, Html.fromHtml(message), Toast.LENGTH_SHORT).show();
                    post.setLikes(false);
                    post.setScore(post.getScore() + 1);

                }
            };
        }

        /**
         * Sets colors for various ui elements within viewholder according to post's current vote status
         */

        private void setColorsAccordingToVote(int likes) {
            if (likes == 1) {
                setBackGround(upvoteButton, upvoteDraw);
                scoreTextView.setTextColor(upvoteColor);
                setBackGround(downvoteButton, defdrawable);
            } else if (likes == -1) {
                setBackGround(downvoteButton, downvoteDraw);
                scoreTextView.setTextColor(downvoteColor);
                setBackGround(upvoteButton, defdrawable);
            } else {
                setBackGround(upvoteButton, defdrawable);
                scoreTextView.setTextColor(primaryTextColor);
                setBackGround(downvoteButton, defdrawable);
            }
        }

        private void setBackGround(ImageView imageView, Drawable drawable) {
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                imageView.setBackgroundDrawable(drawable);
            } else {
                imageView.setBackground(drawable);
            }
        }


    }
}