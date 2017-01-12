package com.app.reddit.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.app.reddit.R;
import com.app.reddit.api.Callback;
import com.app.reddit.api.RedditRestClient;
import com.app.reddit.events.ViewContentEvent;
import com.app.reddit.models.Comment;
import com.app.reddit.models.Post;
import com.app.reddit.utils.Helpers;
import com.app.reddit.utils.OnItemSelectedListener;
import com.app.reddit.utils.PreferenceUtil;
import com.app.reddit.utils.TextViewLinkHandler;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by mukesh on 26/12/16.
 */

@SuppressWarnings("deprecation")
public class CommentsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements OnItemSelectedListener {


    private final Context context;
    private final PreferenceUtil mPre;
    private int previouslySelectedPosition;
    private int currentlySelectedPosition;
    private final int[] childCommentIndicatorColors;
    private final int upvoteColor;
    private final int downvoteColor;
    private final int primaryTextColor;
    private final int secondaryTextColor;
    private final int opHighlightColor;
    private final int accentColor;
    private int lastParentCommentPosition; // This is used to disable 'next parent comment' button when there are no parent comments left to jump to
    private final ArrayList<Comment> comments=new ArrayList<>();
    private final Post selectedPost;
    private final LinearLayoutManager linearLayoutManager;

    public CommentsAdapter(Context context, ArrayList<Comment> commentArrayList,Post selectedPost,LinearLayoutManager layoutManager) {
        this.context=context;
        comments.clear();
        comments.addAll(commentArrayList);
        this.selectedPost=selectedPost;
        this.linearLayoutManager=layoutManager;
        mPre=new PreferenceUtil(context);
        previouslySelectedPosition = -1;
        currentlySelectedPosition = -1;

        Resources res = context.getResources();

        childCommentIndicatorColors = new int[]{
                res.getColor(R.color.teal_500),
                res.getColor(R.color.blue_500),
                res.getColor(R.color.purple_500),
                res.getColor(R.color.light_green_500),
                res.getColor(R.color.deep_orange_500)
        };

        upvoteColor = res.getColor(R.color.reddit_upvote);
        downvoteColor = res.getColor(R.color.reddit_downvote);
        primaryTextColor = res.getColor(R.color.primary_text_default);
        secondaryTextColor = res.getColor(R.color.secondary_text_default);
        accentColor = res.getColor(R.color.colorAccent);
        opHighlightColor = res.getColor(R.color.blue_700);

        updateLastParentCommentPosition();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);

        if (viewType == 0) {
            return new PostsAdapter.PostViewHolder(context,
                    inflater.inflate(R.layout.n_row_post, parent, false), this, true,mPre);
        } else {
            return new CommentsAdapter.CommentViewHolder(inflater.inflate(R.layout.row_comments_recyclerview, parent, false),mPre);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == 0) {
            ((PostsAdapter.PostViewHolder) holder).bindItem(selectedPost);
        } else {
            ((CommentsAdapter.CommentViewHolder) holder).bindItem(comments.get(position - 1));
        }
    }

    @Override
    public int getItemCount() {
        return 1 + comments.size();
    }

    @Override
    public void onItemSelected(int position) {
        currentlySelectedPosition = position;

        notifyItemChanged(currentlySelectedPosition);
        notifyItemChanged(previouslySelectedPosition);

        previouslySelectedPosition = currentlySelectedPosition;
    }

    public void setComments(List<Comment> com) {
        comments.clear();
        comments.addAll(com);
        updateLastParentCommentPosition();
        notifyDataSetChanged();
    }

    private void updateLastParentCommentPosition() {
        for (int i = comments.size() - 1; i >= 0; i--) {
            if (comments.get(i).getLevel() == 0) {
                lastParentCommentPosition = i;
                break;
            }
        }
    }

    private class CommentViewHolder extends RecyclerView.ViewHolder {

        private final PreferenceUtil mPref;
        private final View rootLayout;
        private final TextView authorTextView;
        private final TextView scoreTextView;
        private final TextView createdTextView;
        private final TextView bodyTextView;
        private final View childCommentIndicator;
        private final View buttonsContainerTop;
        private final Button nextButton;
        private final Button previousButton;
        private final View buttonsContainerBottom;
        private final Button upvoteButton;
        private final Button downvoteButton;

        public CommentViewHolder(View itemView, PreferenceUtil pref) {
            super(itemView);
            mPref=pref;
            rootLayout = itemView.findViewById(R.id.root_layout);
            authorTextView = (TextView) itemView.findViewById(R.id.author_textview);
            scoreTextView = (TextView) itemView.findViewById(R.id.score_textview);
            createdTextView = (TextView) itemView.findViewById(R.id.created_textview);
            bodyTextView = (TextView) itemView.findViewById(R.id.body_textview);
            childCommentIndicator = itemView.findViewById(R.id.child_comment_indicator);
            buttonsContainerTop = itemView.findViewById(R.id.buttons_container_top);
            nextButton = (Button) buttonsContainerTop.findViewById(R.id.next_parent_comment_button);
            previousButton = (Button) buttonsContainerTop.findViewById(R.id.previous_parent_comment_button);
            buttonsContainerBottom = itemView.findViewById(R.id.buttons_container_bottom);
            upvoteButton = (Button) itemView.findViewById(R.id.upvote_button);
            downvoteButton = (Button) itemView.findViewById(R.id.downvote_button);
        }

        public void bindItem(final Comment comment) {
            if (context==null)
                return;
            authorTextView.setText(comment.getAuthor());
            scoreTextView.setText(comment.getScore() + " " +
                    context.getResources().getString(R.string.label_points));
            createdTextView.setText(comment.getCreated());
            // the inner fromHtml unescapes html entities, while the outer fromHtml returns a formatted Spannable
            bodyTextView.setText(
                    Helpers.trimTrailingWhitespace(Html.fromHtml(Html.fromHtml(comment.getBody()).toString()))
            );
            bodyTextView.setMovementMethod(new TextViewLinkHandler() {
                @Override
                public void onLinkClick(String url) {
                    EventBus.getDefault().post(new ViewContentEvent(null, url));
                }
            });
            if (comment.getLevel() == 0) {
                childCommentIndicator.setVisibility(View.GONE);
            } else {
                childCommentIndicator.setVisibility(View.VISIBLE);
            }

            // highlight comment author's name if they are also the post's author
            if (comment.getAuthor().equals(selectedPost.getAuthor())) {
                authorTextView.setTextColor(primaryTextColor);
                authorTextView.setBackgroundColor(opHighlightColor);
            } else {
                authorTextView.setTextColor(accentColor);
                authorTextView.setBackgroundColor(0);
            }

            if (getAdapterPosition() == currentlySelectedPosition) {
                rootLayout.setBackgroundResource(R.color.selected_item_background);
                buttonsContainerTop.setVisibility(View.VISIBLE);
                buttonsContainerBottom.setVisibility(View.VISIBLE);
            } else {
                rootLayout.setBackgroundResource(0);
                buttonsContainerTop.setVisibility(View.GONE);
                buttonsContainerBottom.setVisibility(View.GONE);
            }

            if (getAdapterPosition() <= 1) {
                previousButton.setEnabled(false);
            } else {
                previousButton.setEnabled(true);
            }

            if (getAdapterPosition() > lastParentCommentPosition) {
                nextButton.setEnabled(false);
            } else {
                nextButton.setEnabled(true);
            }

            // set child comment left spacing based on level
            itemView.setPadding((int) (comment.getLevel() *
                    context.getResources().getDimension(R.dimen.comment_left_spacing)), 0, 0, 0);

            // set child comment indicator color based on level
            childCommentIndicator.setBackgroundColor(childCommentIndicatorColors[comment.getLevel() % 5]);

            if (mPref.isUserAuthenticated()) {
                setColorsAccordingToVote(comment.getLikes());
            } else {
                upvoteButton.setEnabled(false);
                downvoteButton.setEnabled(false);
            }

            // set click listeners
            View.OnClickListener onClickListener = new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    switch (view.getId()) {
                        case R.id.root_layout:
                        case R.id.body_textview:
                            onItemSelected(getAdapterPosition());
                            break;
                        case R.id.next_parent_comment_button:
                            for (int i = getAdapterPosition() + 1; i <= comments.size(); i++) {
                                if (comments.get(i - 1).getLevel() == 0) {
                                    onItemSelected(i);
                                    linearLayoutManager.scrollToPositionWithOffset(i, itemView.getTop());
                                    break;
                                }
                            }
                            break;
                        case R.id.previous_parent_comment_button:
                            for (int i = getAdapterPosition() - 1; i > 0; i--) {
                                if (comments.get(i - 1).getLevel() == 0) {
                                    onItemSelected(i);
                                    linearLayoutManager.scrollToPositionWithOffset(i, itemView.getTop());
                                    break;
                                }
                            }
                            break;
                        case R.id.upvote_button:
                            if (comment.getLikes() == -1 || comment.getLikes() == 0) {
                                comment.setLikes(true);
                                comment.setScore(comment.getScore() + 1);
                            } else {
                                comment.setLikes(null);
                                comment.setScore(comment.getScore() - 1);
                            }
                            new RedditRestClient(context).vote(comment.getFullName(), comment.getLikes(), getUpVoteCallback(comment));

                            break;
                        case R.id.downvote_button:
                            if (comment.getLikes() == 1 || comment.getLikes() == 0) {
                                comment.setLikes(false);
                                comment.setScore(comment.getScore() - 1);
                            } else {
                                comment.setLikes(null);
                                comment.setScore(comment.getScore() + 1);
                            }
                            new RedditRestClient(context).vote(comment.getFullName(), comment.getLikes(), getDownVoteCallback(comment));

                            break;
                    }
                }
            };

            itemView.setOnClickListener(onClickListener);
            nextButton.setOnClickListener(onClickListener);
            previousButton.setOnClickListener(onClickListener);
            bodyTextView.setOnClickListener(onClickListener);
            upvoteButton.setOnClickListener(onClickListener);
            downvoteButton.setOnClickListener(onClickListener);
        }

        /**
         * Sets colors for various ui elements within viewholder according to comment's current vote status
         */

        private void setColorsAccordingToVote(int likes) {
            if (likes == 1) {
                upvoteButton.setTextColor(upvoteColor);
                scoreTextView.setTextColor(upvoteColor);
                downvoteButton.setTextColor(primaryTextColor);
            } else if (likes == -1) {
                downvoteButton.setTextColor(downvoteColor);
                scoreTextView.setTextColor(downvoteColor);
                upvoteButton.setTextColor(primaryTextColor);
            } else {
                upvoteButton.setTextColor(primaryTextColor);
                scoreTextView.setTextColor(secondaryTextColor);
                downvoteButton.setTextColor(primaryTextColor);
            }
        }

        private Callback<Void> getUpVoteCallback(final Comment comment) {
            return new Callback<Void>(){

                @Override
                public void onSuccess(Void data) {
                    setColorsAccordingToVote(comment.getLikes());
                    scoreTextView.setText(comment.getScore() + " " +
                            context.getResources().getString(R.string.label_points));
                }

                @Override
                public void onFailure(String message) {
                    Toast.makeText(context, Html.fromHtml(message),Toast.LENGTH_SHORT).show();
                    comment.setLikes(false);
                    comment.setScore(comment.getScore()-1);

                }
            };
        }

        private Callback<Void> getDownVoteCallback(final Comment comment) {
            return new Callback<Void>(){

                @Override
                public void onSuccess(Void data) {
                    setColorsAccordingToVote(comment.getLikes());
                    scoreTextView.setText(comment.getScore() + " " +
                           context.getResources().getString(R.string.label_points));
                }

                @Override
                public void onFailure(String message) {
                    Toast.makeText(context,Html.fromHtml(message),Toast.LENGTH_SHORT).show();
                    comment.setLikes(false);
                    comment.setScore(comment.getScore()+1);

                }
            };
        }

    }
}
