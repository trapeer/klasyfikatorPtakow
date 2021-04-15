package com.example.klasyfikatorptakow;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class ViewPagerAdapter extends FragmentPagerAdapter {

    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public static CameraFragment cameraFragment;
    public static ResultsFragment resultsFragment;
    @Override
    public Fragment getItem(int position) {
        switch (position)
        {
            case 0:
                return new CameraFragment();
            case 1:
                return new ResultsFragment();
        }
        return null; //does not happen
    }

    @Override
    public CharSequence  getPageTitle(int position){
        switch(position)
        {
            case 0:
                return "camera";
            case 1:
                return "results";
        }
        return null;
    }

    @Override
    public int getCount() {
        return 2;
    }
}