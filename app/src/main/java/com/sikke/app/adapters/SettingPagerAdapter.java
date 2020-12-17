package com.sikke.app.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.sikke.app.R;
import com.sikke.app.fragments.MainFragment;
import com.sikke.app.fragments.WalletGeneralSettingFragment;

import java.util.ArrayList;
import java.util.List;

public class SettingPagerAdapter extends FragmentPagerAdapter {

    private Context mContext;
    private final List<Fragment> mFragmentList = new ArrayList<>();
    private final List<String> mFragmentTitleList = new ArrayList<>();

    public SettingPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    public void addFragment(Fragment fragment) {
        mFragmentList.add(fragment);
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        switch (position) {
            case 0:
                return mContext.getString(R.string.wallet_general_setting);
            case 1:
                return mContext.getString(R.string.wallet_security_setting);
            case 2:
                return mContext.getString(R.string.wallet_developer);
            default:
                return null;
        }
    }
}
