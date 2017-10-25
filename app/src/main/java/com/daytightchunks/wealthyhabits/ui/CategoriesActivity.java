package com.daytightchunks.wealthyhabits.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.daytightchunks.wealthyhabits.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by DayTightChunks on 28/02/2017.
 */

public class CategoriesActivity extends AppCompatActivity {
    private RecyclerView recyclerview;

    private String LOG_TAG = CategoriesActivity.class.getSimpleName();

    private List<MyExpandableListAdapter.Item> data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // if omitted, class won't run its parents' methods == explosion!
        setContentView(R.layout.activity_categories);
        setTitle(R.string.category_list);
        recyclerview = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        data = new ArrayList<>();

        MyExpandableListAdapter.Item food = new MyExpandableListAdapter.Item(MyExpandableListAdapter.HEADER, "Food");
        food.invisibleChildren = new ArrayList<>();
        food.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Food General"));
        food.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Supermarket"));
        food.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Restaurant"));
        food.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Lunch"));

        data.add(food);

        MyExpandableListAdapter.Item entertainment = new MyExpandableListAdapter.Item(MyExpandableListAdapter.HEADER, "Entertainment");
        entertainment.invisibleChildren = new ArrayList<>();
        entertainment.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Entertainment General"));
        entertainment.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Cinema and theatre"));
        entertainment.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Bar"));
        entertainment.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Games"));

        data.add(entertainment);

        MyExpandableListAdapter.Item holiday = new MyExpandableListAdapter.Item(MyExpandableListAdapter.HEADER, "Holiday");
        holiday.invisibleChildren = new ArrayList<>();
        holiday.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Holiday General"));
        holiday.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Accommodation"));
        holiday.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Activities"));
        holiday.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Food"));
        holiday.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Flight"));
        holiday.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Transport"));

        data.add(holiday);

        MyExpandableListAdapter.Item home = new MyExpandableListAdapter.Item(MyExpandableListAdapter.HEADER, "Home");
        home.invisibleChildren = new ArrayList<>();
        home.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Home General"));
        home.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Electricity"));
        home.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Water"));
        home.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Heating"));
        home.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Insurance"));
        home.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Internet"));
        home.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Phone"));
        home.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Repairs"));
        home.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Furniture"));
        home.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Cleaning products"));

        data.add(home);

        MyExpandableListAdapter.Item clothing = new MyExpandableListAdapter.Item(MyExpandableListAdapter.HEADER, "Clothing");
        clothing.invisibleChildren = new ArrayList<>();
        clothing.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Clothing General"));
        clothing.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Pants"));
        clothing.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Shirts"));
        clothing.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Dress"));
        clothing.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Accessories"));
        clothing.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Jewelry"));

        data.add(clothing);

        MyExpandableListAdapter.Item health = new MyExpandableListAdapter.Item(MyExpandableListAdapter.HEADER, "Health");
        health.invisibleChildren = new ArrayList<>();
        health.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Health General"));
        health.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Cosmetics"));
        health.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Doctor"));
        health.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Hairdresser"));
        health.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Nutrients"));
        health.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Pharmacy"));

        data.add(health);

        MyExpandableListAdapter.Item children = new MyExpandableListAdapter.Item(MyExpandableListAdapter.HEADER, "Children");
        children.invisibleChildren = new ArrayList<>();
        children.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Children General"));
        children.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Baby sitting"));
        children.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "School"));
        children.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Clothing"));

        data.add(children);

        MyExpandableListAdapter.Item work = new MyExpandableListAdapter.Item(MyExpandableListAdapter.HEADER, "Work");
        work.invisibleChildren = new ArrayList<>();
        work.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Work General"));
        work.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Salary"));
        work.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Bonus"));
        work.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Other"));

        data.add(work);

        MyExpandableListAdapter.Item transport = new MyExpandableListAdapter.Item(MyExpandableListAdapter.HEADER, "Transport");
        transport.invisibleChildren = new ArrayList<>();
        transport.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Transport General"));
        transport.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Tram and bus"));
        transport.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Taxi"));
        transport.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Train"));

        data.add(transport);

        recyclerview.setAdapter(new MyExpandableListAdapter(data)); // Will be updated if notifyDataSetChanged();

/*  Tab Layout will be done for a newer version
        viewPagerCats = (ViewPager) findViewById(R.id.viewpager_categories);
        if (viewPagerCats != null) {
            // setupViewPager(viewPagerCats);
            setupTabLayout();
        } else {
            Log.d(LOG_TAG, "view pager is not null?");
        }
*/
    }

    public void onClickCalled(String anyValue) {
        // Call another acitivty here and pass some arguments to it.
        Intent resultIntent = new Intent();
        resultIntent.putExtra("some_key", anyValue);
        this.setResult(Activity.RESULT_OK, resultIntent);
        this.finish();
    }

    /**
    // Add fragments
    private void setupViewPager(ViewPager viewPagerCats){
        MyFragmentAdapter myCatsAdapter = new MyFragmentAdapter(getSupportFragmentManager());
        myCatsAdapter.addFragment(new AllCategsFragment());
        viewPagerCats.setAdapter(myCatsAdapter);
        // Log.d(LOG_TAG, "Started setupViewPager");
    }
     */

    /*
//    For a newer Version that enables to save your favourite and add new categories:
    private void setupTabLayout() {
        tabLayout = (TabLayout) findViewById(R.id.sliding_tabs_cats);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setupWithViewPager(viewPagerCats);
        for (int i = 0; i < tabLayout.getTabCount(); i++ ) {
            if (i == 0) {
                tabLayout.getTabAt(i).setIcon(R.drawable.ic_assignment_white_24dp);
            } else if (i == 1) {
                tabLayout.getTabAt(i).setIcon(R.drawable.ic_star_white_24dp);
            }

        }
    }*/

}
