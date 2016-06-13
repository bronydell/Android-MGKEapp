package ru.equestriadev.mgke;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import org.json.JSONArray;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * The class is used to manage navigation through multiple stacks of fragments, as well as coordinate
 * fragments that may appear on screen
 * Created by niccapdevila on 3/21/16.
 */
public class FragNavController {
    //Declare the constants
    public static final int TAB1 = 0;
    public static final int TAB2 = 1;
    public static final int TAB3 = 2;
    public static final int TAB4 = 3;
    public static final int TAB5 = 4;


    private static final String EXTRA_TAG_COUNT = FragNavController.class.getName() + ":EXTRA_TAG_COUNT";
    private static final String EXTRA_SELECTED_TAB_INDEX = FragNavController.class.getName() + ":EXTRA_SELECTED_TAB_INDEX";
    private static final String EXTRA_CURRENT_FRAGMENT = FragNavController.class.getName() + ":EXTRA_CURRENT_FRAGMENT";
    private static final String EXTRA_FRAGMENT_STACK = FragNavController.class.getName() + ":EXTRA_FRAGMENT_STACK";

    private final List<Stack<Fragment>> mFragmentStacks;
    private final FragmentManager mFragmentManager;
    @TabIndex
    int mSelectedTabIndex = -1;
    private int mTagCount;
    private Fragment mCurrentFrag;
    private NavListener mNavListener;
    private
    @IdRes
    int mContainerId;
    int mTransitionMode = FragmentTransaction.TRANSIT_UNSET;

    public FragNavController(Bundle savedInstanceState, @NonNull FragmentManager fragmentManager, @IdRes int containerId, @NonNull List<Fragment> baseFragments) {
        mFragmentManager = fragmentManager;
        mContainerId = containerId;
        mFragmentStacks = new ArrayList<>(baseFragments.size());

        //Initialize
        if (savedInstanceState == null) {
            for (Fragment fragment : baseFragments) {
                Stack<Fragment> stack = new Stack<>();
                stack.add(fragment);
                mFragmentStacks.add(stack);
            }
        } else {
            onRestoreFromBundle(savedInstanceState, baseFragments);
        }
    }

    public FragNavController(Bundle savedInstanceState, @NonNull FragmentManager fragmentManager, @IdRes int containerId, @NonNull List<Fragment> baseFragments, @Transit int transitionMode){
        this(savedInstanceState, fragmentManager,containerId,baseFragments);
        mTransitionMode = transitionMode;
    }

    @IntDef({FragmentTransaction.TRANSIT_NONE, FragmentTransaction.TRANSIT_FRAGMENT_OPEN, FragmentTransaction.TRANSIT_FRAGMENT_CLOSE, FragmentTransaction.TRANSIT_FRAGMENT_FADE})
    @Retention(RetentionPolicy.SOURCE)
    private @interface Transit {}

    public void setNavListener(NavListener navListener) {
        mNavListener = navListener;
    }

    /**
     * Switch to a different tab. Should not be called on the current tab.
     *
     * @param index the index of the tab to switch to
     */
    @TabIndex
    public void switchTab(@TabIndex int index) {
        //Check to make sure the tab is within range
        if (index >= mFragmentStacks.size()) {
            throw new IndexOutOfBoundsException("Can't switch to a tab that hasn't been initialized. " +
                    "Make sure to create all of the tabs you need in the Constructor");
        }
        if (mSelectedTabIndex != index) {
            mSelectedTabIndex = index;

            FragmentTransaction ft = mFragmentManager.beginTransaction();
            ft.setTransition(mTransitionMode);

            detachCurrentFragment(ft);

            //Attempt to reattach previous fragment
            Fragment fragment = reattachPreviousFragment(ft);
            if (fragment != null) {
                ft.commit();
            } else {
                fragment = mFragmentStacks.get(mSelectedTabIndex).peek();
                ft.add(mContainerId, fragment, generateTag(fragment));
                ft.commit();
            }

            mCurrentFrag = fragment;
            if (mNavListener != null) {
                mNavListener.onTabTransaction(mCurrentFrag, mSelectedTabIndex);
            }
        }
    }

    /**
     * Push a fragment onto the current stack
     *
     * @param fragment The fragment that is to be pushed
     */
    public void push(Fragment fragment) {
        if (fragment != null) {

            FragmentTransaction ft = mFragmentManager.beginTransaction();
            ft.setTransition(mTransitionMode);
            detachCurrentFragment(ft);
            ft.add(mContainerId, fragment, generateTag(fragment));
            ft.commit();

            mFragmentManager.executePendingTransactions();
            mFragmentStacks.get(mSelectedTabIndex).push(fragment);

            mCurrentFrag = fragment;
            if (mNavListener != null) {
                mNavListener.onFragmentTransaction(mCurrentFrag);
            }

        }
    }

    /**
     * Pop the current fragment from the current tab
     */
    public void pop() {
        Fragment poppingFrag = getCurrentFrag();
        if (poppingFrag != null) {
            FragmentTransaction ft = mFragmentManager.beginTransaction();
            ft.setTransition(mTransitionMode);
            ft.remove(poppingFrag);

            //overly cautious fragment pop
            Stack<Fragment> fragmentStack = mFragmentStacks.get(mSelectedTabIndex);
            if (!fragmentStack.isEmpty()) {
                fragmentStack.pop();
            }

            //Attempt reattach, if we can't, try to pop from the stack and push that on
            Fragment fragment = reattachPreviousFragment(ft);
            if (fragment == null && !fragmentStack.isEmpty()) {
                fragment = fragmentStack.peek();
                ft.add(mContainerId, fragment, fragment.getTag());
            }

            //Commit our transactions
            ft.commit();
            mFragmentManager.executePendingTransactions();

            mCurrentFrag = fragment;
            if (mNavListener != null) {
                mNavListener.onFragmentTransaction(mCurrentFrag);
            }
        }
    }

    /**
     * Clears the current tab's stack to get to just the bottom Fragment.
     */
    public void clearStack() {
        //Grab Current stack
        Stack<Fragment> fragmentStack = mFragmentStacks.get(mSelectedTabIndex);

        // Only need to start popping and reattach if the stack is greater than 1
        if (fragmentStack.size() > 1) {
            Fragment fragment;
            FragmentTransaction ft = mFragmentManager.beginTransaction();
            ft.setTransition(mTransitionMode);

            //Pop all of the fragments on the stack and remove them from the FragmentManager
            while (fragmentStack.size() > 1) {
                fragment = mFragmentManager.findFragmentByTag(fragmentStack.peek().getTag());
                if (fragment != null) {
                    fragmentStack.pop();
                    ft.remove(fragment);
                }
            }

            //Attempt to reattach previous fragment
            fragment = reattachPreviousFragment(ft);
            //If we can't reattach, either pull from the stack, or create a new base fragment
            if (fragment != null) {
                ft.commit();
            } else {
                if (!fragmentStack.isEmpty()) {
                    fragment = fragmentStack.peek();
                    ft.add(mContainerId, fragment, fragment.getTag());
                    ft.commit();
                }
            }

            //Update the stored version we have in the list
            mFragmentStacks.set(mSelectedTabIndex, fragmentStack);

            mCurrentFrag = fragment;
            if (mNavListener != null) {
                mNavListener.onFragmentTransaction(mCurrentFrag);
            }
        }
    }

    /**
     * Call this in your Activity's onSaveInstanceState(Bundle outState) method to save the instance's state.
     *
     * @param outState The Bundle to save state information to
     */
    public void onSaveInstanceState(Bundle outState) {

        // Write tag count
        outState.putInt(EXTRA_TAG_COUNT, mTagCount);

        // Write select tab
        outState.putInt(EXTRA_SELECTED_TAB_INDEX, mSelectedTabIndex);

        // Write current fragment
        if (mCurrentFrag != null) {
            outState.putString(EXTRA_CURRENT_FRAGMENT, mCurrentFrag.getTag());
        }

        // Write stacks
        try {
            final JSONArray stackArrays = new JSONArray();

            for (Stack<Fragment> stack : mFragmentStacks) {
                final JSONArray stackArray = new JSONArray();

                for (Fragment fragment : stack) {
                    stackArray.put(fragment.getTag());
                }

                stackArrays.put(stackArray);
            }

            outState.putString(EXTRA_FRAGMENT_STACK, stackArrays.toString());
        } catch (Throwable t) {
            // Nothing we can do
        }
    }

    //Todo handle other types of fragments
/*    public void showBottomSheet(BottomSheetDialogFragment bottomSheetDialogFragment) {
        bottomSheetDialogFragment.show(mFragmentManager, bottomSheetDialogFragment.getClass().getName() + ++mTagCount);
    }*/

    /**
     * Will attempt to reattach a previous fragment in the FragmentManager, or return null if not able to,
     *
     * @param ft current fragment transaction
     * @return Fragment if we were able to find and reattach it
     */
    @Nullable
    private Fragment reattachPreviousFragment(FragmentTransaction ft) {
        Stack<Fragment> fragmentStack = mFragmentStacks.get(mSelectedTabIndex);
        Fragment fragment = null;
        if (!fragmentStack.isEmpty()) {
            fragment = mFragmentManager.findFragmentByTag(fragmentStack.peek().getTag());
            if (fragment != null) {
                ft.attach(fragment);
            }
        }
        return fragment;
    }

    /**
     * Attemps to detach any current fragment if it exists, and if none is found, returns;
     *
     * @param ft the current transaction being performed
     */
    private void detachCurrentFragment(FragmentTransaction ft) {
        Fragment oldFrag = getCurrentFrag();
        if (oldFrag != null) {
            ft.detach(oldFrag);
        }
    }


    @Nullable
    private Fragment getCurrentFrag() {
        //Attempt to used stored current fragment
        if (mCurrentFrag != null) {
            return mCurrentFrag;
        }
        //if not, try to pull it from the stack
        else {
            Stack<Fragment> fragmentStack = mFragmentStacks.get(mSelectedTabIndex);
            if (!fragmentStack.isEmpty()) {
                return mFragmentManager.findFragmentByTag(mFragmentStacks.get(mSelectedTabIndex).peek().getTag());
            } else {
                return null;
            }
        }
    }

    /**
     * Create a unique fragment tag so that we can grab the fragment later from the FragmentManger
     *
     * @param fragment The fragment that we're creating a unique tag for
     * @return a unique tag using the fragment's class name
     */
    private String generateTag(Fragment fragment) {
        return fragment.getClass().getName() + ++mTagCount;
    }

    /**
     * Restores this instance to the state specified by the contents of savedInstanceState
     *
     * @param savedInstanceState The bundle to restore from
     * @param baseFragments List of base fragments from which to initialize empty stacks
     */
    private void onRestoreFromBundle(Bundle savedInstanceState, List<Fragment> baseFragments) {

        // Restore selected tab
        switch (savedInstanceState.getInt(EXTRA_SELECTED_TAB_INDEX, -1)) {
            case TAB1:
                mSelectedTabIndex = TAB1;
                break;
            case TAB2:
                mSelectedTabIndex = TAB2;
                break;
            case TAB3:
                mSelectedTabIndex = TAB3;
                break;
            case TAB4:
                mSelectedTabIndex = TAB4;
                break;
            case TAB5:
                mSelectedTabIndex = TAB5;
                break;
        }

        // Restore tag count
        mTagCount = savedInstanceState.getInt(EXTRA_TAG_COUNT, 0);

        // Restore current fragment
        mCurrentFrag = mFragmentManager.findFragmentByTag(savedInstanceState.getString(EXTRA_CURRENT_FRAGMENT));

        // Restore fragment stacks
        try {
            final JSONArray stackArrays = new JSONArray(savedInstanceState.getString(EXTRA_FRAGMENT_STACK));

            for (int x = 0; x < stackArrays.length(); x++) {
                final JSONArray stackArray = stackArrays.getJSONArray(x);
                final Stack<Fragment> stack = new Stack<>();

                if (stackArray.length() == 1) {
                    final String tag = stackArray.getString(0);
                    final Fragment fragment;

                    if (tag == null || "null".equalsIgnoreCase(tag)) {
                        fragment = baseFragments.get(x);
                    } else {
                        fragment = mFragmentManager.findFragmentByTag(tag);
                    }

                    if (fragment != null) {
                        stack.add(fragment);
                    }
                } else {
                    for (int y = 0; y < stackArray.length(); y++) {
                        final String tag = stackArray.getString(y);

                        if (tag != null && !"null".equalsIgnoreCase(tag)) {
                            final Fragment fragment = mFragmentManager.findFragmentByTag(tag);

                            if (fragment != null) {
                                stack.add(fragment);
                            }
                        }
                    }
                }

                mFragmentStacks.add(stack);
            }
        } catch (Throwable t) {
            // Nothing we can do
        }
    }

    public Stack<Fragment> getCurrentStack() {
        return mFragmentStacks.get(mSelectedTabIndex);
    }

    //Define the list of accepted constants
    @IntDef({TAB1, TAB2, TAB3, TAB4, TAB5})

    //Tell the compiler not to store annotation data in the .class file
    @Retention(RetentionPolicy.SOURCE)

    //Declare the TabIndex annotation
    public @interface TabIndex {
    }

    public interface NavListener {
        void onTabTransaction(Fragment fragment, int index);

        void onFragmentTransaction(Fragment fragment);
    }
}