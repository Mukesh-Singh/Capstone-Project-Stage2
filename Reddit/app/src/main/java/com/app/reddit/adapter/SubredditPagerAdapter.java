package com.app.reddit.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.app.reddit.models.Subreddit;
import com.app.reddit.ui.fragments.PostsFragment;

import java.util.List;

/**
 * Created by mukesh on 19/12/16.
 */

public class SubredditPagerAdapter extends FragmentStatePagerAdapter {

    private List<Subreddit> subreddits;
    private FragmentManager fragmentManager;
    private String sort;

    public SubredditPagerAdapter(FragmentManager fragmentManager,List<Subreddit> subreddits,String sort) {
        super(fragmentManager);
        this.subreddits = subreddits;
        this.sort=sort;
    }

    @Override
    public Fragment getItem(int position) {
        return PostsFragment.newInstance(subreddits.get(position).getName(), sort, false);
    }

    @Override
    public int getCount() {
        return subreddits.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return subreddits.get(position).getName();
    }


}