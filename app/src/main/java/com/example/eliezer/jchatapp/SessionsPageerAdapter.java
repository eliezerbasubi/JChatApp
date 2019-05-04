package com.example.eliezer.jchatapp;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Eliezer on 22/02/2018.
 */

class SessionsPageerAdapter extends FragmentPagerAdapter{


    private final List<String> icon = new ArrayList <>();
    private final List<Fragment> fragments = new ArrayList <>();

    public SessionsPageerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        /*switch (position){
            case 0 :
                Requests requests = new Requests();
                return requests;
            case 1:
                Chat chat = new Chat();
                return chat;
            case 2:
                Friends friends = new Friends();
                return friends;
                default:
                    return null;
        }*/

        return fragments.get(position);
    }

    @Override
    public int getCount() {
       // return 3;
        return icon.size();
    }

    //GET TAB TITLE
    public CharSequence getPageTitle(int position){
        /*switch (position){
            case 0 :
                return "REQUESTS";
            case 1 :
                return "MESSAGES";
            case 2 :
                return "FRIENDS";
                default:
                    return null;
        }*/
        return icon.get(position);
    }

    public void addFragments(Fragment fragment, String icons){

        fragments.add(fragment);
        icon.add(icons);
    }
}
