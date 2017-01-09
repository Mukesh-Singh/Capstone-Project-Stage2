package com.app.reddit.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.app.reddit.R;
import com.app.reddit.adapter.CommentsAdapter;
import com.app.reddit.api.Callback;
import com.app.reddit.api.RedditRestClient;
import com.app.reddit.models.Comment;
import com.app.reddit.models.Post;
import com.app.reddit.utils.AppUtils;

import java.util.ArrayList;
import java.util.List;

public class CommentsFragment extends Fragment {

    private Toolbar toolbar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView commentsRecyclerView;
    private CommentsAdapter commentsAdapter;
    private static final String SELECTED_POST_BUNDLE_KEY = "selected_subreddit_key";
    private static final String SORT_BUNDLE_KEY = "sort_bundle_key";
    private static final String COMMENTS_BUNDLE_KEY = "comments_key";
    private Post selectedPost;
    private String sort;
    private ArrayList<Comment> comments;

    public static CommentsFragment newInstance(Post selectedPost) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(SELECTED_POST_BUNDLE_KEY, selectedPost);
        CommentsFragment commentsFragment = new CommentsFragment();
        commentsFragment.setArguments(bundle);

        return commentsFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_comments, container, false);

        /**
         * Find views
         */

        toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        commentsRecyclerView = (RecyclerView) view.findViewById(R.id.comments_recyclerview);

        /**
         * Retrieve info required to load comments
         */

        selectedPost = getArguments().getParcelable(SELECTED_POST_BUNDLE_KEY);

        if (savedInstanceState == null) {
            sort = getResources().getString(R.string.action_sort_best);
            comments = new ArrayList<Comment>();
        } else {
            sort = savedInstanceState.getString(SORT_BUNDLE_KEY);
            comments = savedInstanceState.getParcelableArrayList(COMMENTS_BUNDLE_KEY);
        }

        /**
         * Configure recycler view
         */


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        commentsRecyclerView.setLayoutManager(linearLayoutManager);
        commentsAdapter = new CommentsAdapter(getActivity(),comments,selectedPost, linearLayoutManager);
        commentsRecyclerView.setAdapter(commentsAdapter);

        /**
         * Configure swipe refresh layout
         */

        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                loadComments();
            }
        });

        /**
         * Setup toolbar
         */

        updateToolbarTitles(selectedPost.getTitle(), sort);
        toolbar.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });
        toolbar.inflateMenu(R.menu.menu_comments_fragment);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.action_sort_best || id == R.id.action_sort_top || id == R.id.action_sort_new ||
                        id == R.id.action_sort_controversial || id == R.id.action_sort_old) {
                    sort = item.getTitle().toString();
                    updateToolbarTitles(selectedPost.getTitle(), sort);
                    loadComments();

                    return true;
                }

                return false;
            }
        });

        /**
         * Load comments
         */

        if (savedInstanceState == null) {
            loadComments();
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(SORT_BUNDLE_KEY, sort);
        outState.putParcelableArrayList(COMMENTS_BUNDLE_KEY, comments);
    }

    private void updateToolbarTitles(String title, String subtitle) {
        toolbar.setTitle(title);
        toolbar.setSubtitle(subtitle);
    }

    private void loadComments() {
        setRefreshIndicatorVisiblity(true);
        // only hide recycler view if post item is the only item (i.e. when comments are being loadded for the first time)
        if (commentsAdapter.getItemCount() == 1) {
            commentsRecyclerView.setVisibility(View.INVISIBLE);
        }

        new RedditRestClient(getActivity()).getComments(selectedPost.getSubreddit(), selectedPost.getId(), sort, new Callback<List<Comment>>() {

            @Override
            public void onSuccess(List<Comment> data) {
                if (getActivity() != null) {
                    setRefreshIndicatorVisiblity(false);
                    commentsRecyclerView.setVisibility(View.VISIBLE);

                    if (data.size() > 0) {
                        commentsAdapter.setComments(data);
                    } else {
                        AppUtils.showToastLong(getString( R.string.label_no_comments));
                    }
                }
            }

            @Override
            public void onFailure(String message) {
                if (getActivity() != null) {
                    setRefreshIndicatorVisiblity(false);
                    commentsRecyclerView.setVisibility(View.VISIBLE);

                    if (message != null)
                        AppUtils.showToastLong(message);
                }
            }
        });
    }

    private void setRefreshIndicatorVisiblity(final boolean visiblity) {
        swipeRefreshLayout.post(new Runnable() {

            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(visiblity);
            }
        });
    }

}
