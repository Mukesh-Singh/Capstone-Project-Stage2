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
import android.widget.Toast;

import com.app.reddit.R;
import com.app.reddit.adapter.PostsAdapter;
import com.app.reddit.api.Callback;
import com.app.reddit.api.RedditRestClient;
import com.app.reddit.models.Post;

import java.util.ArrayList;
import java.util.List;

public class PostsFragment extends Fragment {

    public static final String TAG = PostsFragment.class.getSimpleName();
    private static final String SUBREDDIT_BUNDLE_KEY = "subreddit_key";
    private static final String SORT_BUNDLE_KEY = "sort_key";
    private static final String SHOULD_USE_TOOLBAR_BUNDLE_KEY = "should_use_toolbar_key";
    private static final String POSTS_BUNDLE_KEY = "posts_key";
    private static final String AFTER_BUNDLE_KEY = "after_key";
    private Toolbar toolbar;
    private LinearLayoutManager linearLayoutManager;
    private SwipeRefreshLayout swipeRefreshLayout;
    private PostsAdapter postsAdapter;
    private String subreddit;
    private String sort;
    private String after;
    private boolean canLoadMorePosts = true;

    public static PostsFragment newInstance(String subreddit, String sort, boolean shouldUseToolbar) {
        Bundle bundle = new Bundle();
        bundle.putString(SUBREDDIT_BUNDLE_KEY, subreddit);
        bundle.putString(SORT_BUNDLE_KEY, sort);
        bundle.putBoolean(SHOULD_USE_TOOLBAR_BUNDLE_KEY, shouldUseToolbar);

        PostsFragment postsFragment = new PostsFragment();
        postsFragment.setArguments(bundle);

        return postsFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_posts, container, false);

        /**
         * Find views
         */

        toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        RecyclerView postsRecyclerView = (RecyclerView) view.findViewById(R.id.posts_recyclerview);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);

        /**
         * Retrieve info required to load posts
         */

        Bundle bundle = getArguments();
        subreddit = bundle.getString(SUBREDDIT_BUNDLE_KEY);
        final boolean shouldUseToolbar = bundle.getBoolean(SHOULD_USE_TOOLBAR_BUNDLE_KEY);

        ArrayList<Post> posts;
        if (savedInstanceState == null) {
            sort = bundle.getString(SORT_BUNDLE_KEY);
            after = null;
            posts = new ArrayList<>();
        } else {
            sort = savedInstanceState.getString(SORT_BUNDLE_KEY);
            after = savedInstanceState.getString(AFTER_BUNDLE_KEY);
            posts = savedInstanceState.getParcelableArrayList(POSTS_BUNDLE_KEY);
        }

        /**
         * Configure recycler view
         */

        linearLayoutManager = new LinearLayoutManager(getActivity());
        postsRecyclerView.setLayoutManager(linearLayoutManager);
//        postsRecyclerView.getItemAnimator().setSupportsChangeAnimations(false);
        postsAdapter = new PostsAdapter(getActivity(), posts);
        postsRecyclerView.setAdapter(postsAdapter);
        // enable endless scrolling of posts
        postsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int visibleItemThreshold = 3;
                int totalItemCount = linearLayoutManager.getItemCount();
                int lastVisibleItemPos = linearLayoutManager.findLastVisibleItemPosition();

                if (!swipeRefreshLayout.isRefreshing() && (lastVisibleItemPos > (totalItemCount - visibleItemThreshold))
                        && canLoadMorePosts) {

                    loadPosts(true);
                }
            }
        });

        /**
         * Configure swipe refresh
         */

        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                loadPosts(false);
            }
        });

        /**
         * Setup toolbar if it is to be used
         */

        if (shouldUseToolbar) {
            toolbar.setVisibility(View.VISIBLE);

            updateToolbarTitles();
            toolbar.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    getActivity().onBackPressed();
                }
            });
            toolbar.inflateMenu(R.menu.menu_posts_fragment);
            toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {

                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    int id = item.getItemId();

                    if (id == R.id.action_sort_hot || id == R.id.action_sort_new || id == R.id.action_sort_rising ||
                            id == R.id.action_sort_controversial || id == R.id.action_sort_top) {
                        sort = item.getTitle().toString();
                        updateToolbarTitles();
                        loadPosts(false);

                        return true;
                    }

                    return false;
                }
            });
        } else {
            toolbar.setVisibility(View.GONE);
        }

        /**
         * Load posts
         */

        if (savedInstanceState == null) {
            loadPosts(false);
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(SORT_BUNDLE_KEY, sort);
        outState.putString(AFTER_BUNDLE_KEY, after);
        if (postsAdapter!=null)
        outState.putParcelableArrayList(POSTS_BUNDLE_KEY, postsAdapter.getPosts());
    }

    private void loadPosts(final boolean shouldAppend) {
        setRefreshIndicatorVisiblity(true);

        new RedditRestClient(getActivity()).getPosts(subreddit, sort, shouldAppend ? this.after : null, new Callback<List<Post>>() {

            @Override
            public void onSuccess(List<Post> data) {
                // only proceed if fragment is still attached to its parent activity
                // this would prevent null pointer exception when adapter tries to use activity context
                if (getActivity() != null) {
                    setRefreshIndicatorVisiblity(false);

                    if (shouldAppend) {
                        postsAdapter.addPosts(data);
                    } else {
                        postsAdapter.setPosts(data);
                    }

                    if (data.size() > 0)
                        after = data.get(data.size() - 1).getAfter();

                    canLoadMorePosts = (after != null);
                }
            }

            @Override
            public void onFailure(String message) {
                if (getActivity() != null) {
                    setRefreshIndicatorVisiblity(false);

                    if (message != null)
                        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * This method is called by the host activity when the user chooses a different sort value.
     */

    public void updateSort(String sort) {
        this.sort = sort;

        loadPosts(false);
    }

    private void setRefreshIndicatorVisiblity(final boolean visiblity) {
        swipeRefreshLayout.post(new Runnable() {

            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(visiblity);
            }
        });
    }

    private void updateToolbarTitles() {
        subreddit=subreddit.substring(0, 1).toUpperCase() + subreddit.substring(1);
        sort=sort.substring(0, 1).toUpperCase() + sort.substring(1);
        toolbar.setTitle(subreddit);
        toolbar.setSubtitle(sort);
    }


}
