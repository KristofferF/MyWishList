package se.kfreiholtz.mywishlist.adapters;

/**
 * Created by Stationary on 2014-10-18.
 */

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.Locale;

import se.kfreiholtz.mywishlist.R;
import se.kfreiholtz.mywishlist.userinterfaces.InboxFragment;
import se.kfreiholtz.mywishlist.userinterfaces.MyListsFragment;

/**
 * A {@link android.support.v13.app.FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    protected Context mContext;

    public SectionsPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).

        switch (position) {
            case 0:
                return new InboxFragment();
            case 1:
                return new MyListsFragment();
        }
        return null;
    }

    @Override
    public int getCount() {
        // Show 2 total pages.
        return 2;
    }

    /**
     * Gets the strings for the inbox and list fragment tabs
     * Use instead of getIcon() if you want names instead of icons on the tabs
     */
    @Override
    public CharSequence getPageTitle(int position) {
        Locale l = Locale.getDefault();
        switch (position) {
            case 0:
                return mContext.getString(R.string.title_section1).toUpperCase(l);
            case 1:
                return mContext.getString(R.string.title_section2).toUpperCase(l);
        }
        return null;
    }

    /**
     * Gets the icon for the inbox and list fragment tabs
     */
    public int getIcon(int position) {
        switch (position) {
            case 0:
                return R.drawable.ic_inbox;
            case 1:
                return R.drawable.ic_list;
        }
        return R.drawable.ic_inbox;

    }
}
