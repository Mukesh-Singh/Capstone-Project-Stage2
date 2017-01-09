package com.app.reddit.events;


import com.app.reddit.models.Post;

public class ViewCommentsEvent {

    private Post selectedPost;

    public ViewCommentsEvent(Post selectedPost) {
        this.selectedPost = selectedPost;
    }

    public Post getSelectedPost() {
        return selectedPost;
    }
}
