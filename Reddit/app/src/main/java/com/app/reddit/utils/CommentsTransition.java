package com.app.reddit.utils;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.transition.ChangeBounds;
import android.transition.ChangeImageTransform;
import android.transition.ChangeTransform;
import android.transition.TransitionSet;

/**
 * Created by mukesh on 13/1/17.
 */

@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class CommentsTransition extends TransitionSet {
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CommentsTransition() {
        setOrdering(ORDERING_TOGETHER);
        addTransition(new ChangeBounds()).
                addTransition(new ChangeTransform()).
                addTransition(new ChangeImageTransform());
    }
}