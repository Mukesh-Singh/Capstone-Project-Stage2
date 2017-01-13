package com.app.reddit.events;


import android.view.View;

import com.app.reddit.models.Post;

public class ViewCommentsEvent {

    private final Post selectedPost;
    private View commentView;

    public ViewCommentsEvent(Post selectedPost) {
        this.selectedPost = selectedPost;
    }

    public Post getSelectedPost() {
        return selectedPost;
    }

    public View getCommentView() {
        return commentView;
    }

    public void setCommentView(View commentView) {
        this.commentView = commentView;
    }
}
