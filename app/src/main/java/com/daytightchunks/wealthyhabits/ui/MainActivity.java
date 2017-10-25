package com.daytightchunks.wealthyhabits.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
//import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TabWidget;
import android.widget.TextView;

import com.daytightchunks.wealthyhabits.R;
import com.daytightchunks.wealthyhabits.data.TransContract;
import com.daytightchunks.wealthyhabits.data.TransDbHelper;

import java.text.ParseException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.Date;

import java.util.LinkedHashMap;
import java.util.Locale;

// TODO:

/*
* Currently Working on the fragment_editor logic. Need to:
 * 1) Set onclicklisteners on new save, cancel & delete buttons, plus their actions
 *    from the EditorActivity class in the old app:
 *      - saveTransaction()
 *      - onBackPressed() replacement to cancel()
 *      - showDeleteConfirmationDialog() on deleteButton()
 * 2) New logic to fill fragment_editor when launched from MainActivity's MasterListFragment onClick item
 *      -
 * 3) Adapt XML layout to tablet */

public class MainActivity extends AppCompatActivity
        implements
        MasterListFragment.OnTransactionSelectedListener{

    /** Tag for the log messages */
    public static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final int PAGE_COUNT = 3;
    private static final String STATE_FRAG = "fragNumber";
    private int mCurrentFragNum = 0;
    private CharSequence title;

    private DrawerLayout mDrawerLayout;

    /** Database helper that will provide us access to the database */
    private TransDbHelper mDbHelper;

    private TabLayout tabLayout;
    private ViewPager viewPager; // (i.e. horizontal navigation tabs)
    private MyPagerAdapter mViewPagerAdapter;
    private Toolbar topToolbar;
    TextView textViewTitle;

    /** Filter selector */
    private static final String STATE_COUNTER_YRMO = "yr_mo_counter";
    private static final String STATE_COUNTER_YR = "yr_counter";
    private static final String STATE_YRMO_LEFT = "yr_mo_button_left";
    private static final String STATE_YRMO_RIGHT = "yr_mo_button_right";
    private static final String STATE_YR_LEFT = "yr_button_left";
    private static final String STATE_YR_RIGHT = "yr_button_right";
    public int mYrMoCounter; // a counter to track the position of mYrMonths array
    public int mYrCounter; // a counter to track the position of mYears array
    public LinearLayout yrMoFilterGroup; // visible during Transactions view
    public LinearLayout yrFilterGroup; // visible during Summary view
    public TextView yrFilterChoice; // the label to show transactions filter array
    public TextView yrMoFilterChoice; // the label to show transactions filter array
    private String dateString;
    private long currentDate;
    private SimpleDateFormat formatter;
    private Date mDateFormatted;
    private String[] partsYrMo;
    private String mYrMo;
    private String mYear;

    public ArrayList mYrMonths;
    public ArrayList mYears;
    public String yrMoSelected;
    public String yrSelected;

    public ImageButton yrMoButtonLeft;
    public ImageButton yrMoButtonRight;

    public ImageButton yrButtonLeft;
    public ImageButton yrButtonRight;

    public boolean mTwoPane;
    MasterListFragment mMasterListFrag;
    ChartsFragment mChartListFrag;
    SavingsFragment mSavingsFrag;
    FragmentManager fragmentManager = getSupportFragmentManager();

    private FragmentTabHost mTabHost;
    private TabWidget tabWidget;
    private HorizontalScrollView horizontalScrollView;

    /** Data needed for SavingsFragment
     *  Generated on:
     *  setupDateSelector() -> loadFilterArrays() */
    private LinkedHashMap<String, Object> trackedGlobalMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDbHelper = new TransDbHelper(this);

        Log.d(LOG_TAG, "Entering onCreate");

        topToolbar = (Toolbar) findViewById(R.id.top_toolbar); // in: include_list_viewpager.xml (widget.Toolbar)
        setSupportActionBar(topToolbar); // Needed to remove Theme: .DarkActionBar @style.

        final ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout); // in activity_main.xml (widget.DrawerLayout)

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view); // activity_main.xml (widget.NavigationView)
        if (navigationView != null) { setupDrawerContent(navigationView); }


        yrMoFilterGroup = (LinearLayout) findViewById(R.id.yr_mo_filter_group);
        yrMoButtonLeft = (ImageButton) findViewById(R.id.button_yr_mo_left);
        yrMoButtonRight = (ImageButton) findViewById(R.id.button_yr_mo_right);

        yrFilterGroup = (LinearLayout) findViewById(R.id.year_filter_group);
        yrButtonLeft = (ImageButton) findViewById(R.id.button_yr_left);
        yrButtonRight = (ImageButton) findViewById(R.id.button_yr_right);

        setupDateSelector();

        // Restore state of date selector
        if (savedInstanceState != null) {
            mYrMoCounter = savedInstanceState.getInt(STATE_COUNTER_YRMO, 0); // 0 is default value, if null.
            mYrCounter = savedInstanceState.getInt(STATE_COUNTER_YR, 0); // 0 is default value, if null.

            if (savedInstanceState.getInt(STATE_YRMO_LEFT, 0) == 1) {
                yrMoButtonLeft.setVisibility(View.VISIBLE);
            } else {
                yrMoButtonLeft.setVisibility(View.INVISIBLE);
            }

            if (savedInstanceState.getInt(STATE_YRMO_RIGHT, 0) == 1) {
                yrMoButtonRight.setVisibility(View.VISIBLE);
            } else {
                yrMoButtonRight.setVisibility(View.INVISIBLE);
            }

            if (savedInstanceState.getInt(STATE_YR_LEFT, 0) == 1) {
                yrButtonLeft.setVisibility(View.VISIBLE);
            } else {
                yrButtonLeft.setVisibility(View.INVISIBLE);
            }

            if (savedInstanceState.getInt(STATE_YR_RIGHT, 0) == 1) {
                yrButtonRight.setVisibility(View.VISIBLE);
            } else {
                yrButtonRight.setVisibility(View.INVISIBLE);
            }

            mCurrentFragNum = savedInstanceState.getInt(STATE_FRAG, 0);

            // mCurrentFragNum = savedInstanceState.getInt(STATE_FRAG, 0);
        } else {
            mYrMoCounter = 0;
            mYrCounter = 0;
        }


        if (findViewById(R.id.two_fragment_layout) != null ){
            mTwoPane = true;
            // DO stuff
//            viewPager = (ViewPager) findViewById(R.id.viewpager);

            setupTabLayout();

        } else {
            mTwoPane = false;

            // Find ViewPager that will allow user to swipe between fragments
            viewPager = (ViewPager) findViewById(R.id.viewpager); // include_list_viewpager.xml (view ViewPager)
            if (viewPager != null) {
                mViewPagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
                viewPager.setAdapter(mViewPagerAdapter); // Will be updated if notifyDataSetChanged();
                setupTabLayout(); // Add icons to each tab

                if (viewPager.getCurrentItem() == 0){
                    topToolbar.setTitle("Transactions");
                    yrFilterGroup.setVisibility(View.INVISIBLE);
                    // yrMoFilterGroup.setVisibility(View.VISIBLE);
                } else if (viewPager.getCurrentItem() == 1){
                    topToolbar.setTitle("Summary");
                } else if (viewPager.getCurrentItem() == 2){
                    topToolbar.setTitle("Savings Overview");
                }

                viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    }

                    @Override
                    public void onPageSelected(int position) {
                        if (position == 0) {
                            topToolbar.setTitle("Transactions");
                            yrMoFilterGroup.setVisibility(View.VISIBLE);
                            yrFilterGroup.setVisibility(View.INVISIBLE);
                        } else if (position == 1){
                            topToolbar.setTitle("Summary");
                            yrFilterGroup.setVisibility(View.VISIBLE);
                            yrMoFilterGroup.setVisibility(View.INVISIBLE);
                        } else if (position == 2){
                            topToolbar.setTitle("Savings Overview");
                            yrFilterGroup.setVisibility(View.INVISIBLE);
                            yrMoFilterGroup.setVisibility(View.INVISIBLE);
                        }
                    }

                    @Override
                    public void onPageScrollStateChanged(int state) {

                    }
                });
            }
        }

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mTwoPane){
                    TransactionFragment transactionFragment = new TransactionFragment();
                    fragmentManager.beginTransaction()
                            .add(R.id.editor_head_container, transactionFragment)
                            .commit();
                } else {
                    Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                    intent.putExtra("origin", "transactions");
                    startActivityForResult(intent, 1); // If dataSet changes, EditorActivity will return "1"
                    // implements callback: onActivityResult() ...below
                }
            }
        });
//        TODO: Handle FAB hugging last item of listview
//        https://stackoverflow.com/questions/29362284/floating-action-button-blocking-other-components
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) { // RequestCode tags the EditorActivity response?
            if(resultCode == Activity.RESULT_OK){
                int result = data.getIntExtra("result", 0);

                // TODO: The DateSelector now has no values in the array
                // So both buttons disappear until onResume gets called with
                // orientation change.
                // Removing the 0 leads to an index out of range if data one month is deleted.
                // mMoCounter = 0;
                setupDateSelector();
                notifyViewPagerDataSetChanged();
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
//            TODO: Not necessary for now, could handle this with either requestCode response.
        } else if (requestCode == 2){ // RequestCode tags the TransferActivity response?
            if(resultCode == Activity.RESULT_OK){
                setupDateSelector();
                notifyViewPagerDataSetChanged();
            }
        }
    }

    private void notifyViewPagerDataSetChanged() {
        mViewPagerAdapter.notifyDataSetChanged(); // Will update views based on getItemPosition new override.
        setupTabLayout();
    }

    /**
     * MasterListFragment Interface implementation */
//    @Override
    public void onTransListItemSelected(int position, long id, String category) {

        Log.d(LOG_TAG, "ListItem int position: " + position);
        Log.d(LOG_TAG, "ListItem long id: " + id);
        Log.d(LOG_TAG, "ListItem Category: " + category);

        Uri currentTransUri = ContentUris.withAppendedId(TransContract.TransEntry.CONTENT_URI, id);

        if (mTwoPane) {
            TransactionFragment transactionFragmentWithData = TransactionFragment.newInstance(currentTransUri, "temporary string");
            fragmentManager.beginTransaction()
                    .add(R.id.editor_head_container, transactionFragmentWithData)
                    .commit();
        } else {
            // Otherwise, we're in the one-pane layout and must swap frags...
            Intent editIntent = new Intent(this, EditorActivity.class);
            editIntent.setData(currentTransUri); // pass content_uri to the new intent as extra data
            editIntent.putExtra("finishActivityOnSaveCompleted", true);

            // Get information about the URI...
            // as could be both transfer or transaction item
            if (category.equals("Transfer")){
                editIntent.putExtra("origin", "transfer");
            } else {
                editIntent.putExtra("origin", "transactions");
            }
            startActivity(editIntent);

            /*
            Currently not ready for Fragment switch in phone view due to LayoutLogic
            TransactionFragment editorEmptyFragment = new TransactionFragment();
            fragmentManager.beginTransaction()
                    .replace(R.id.parent_activity_frame, editorEmptyFragment)
                    .addToBackStack(null)
                    .commit();
                    */
        }
    }



    /**
     * Add fragments to MainActivity.
     */
    private class MyPagerAdapter extends FragmentPagerAdapter {

        public MyPagerAdapter(FragmentManager fm){
            super(fm);
        }
        @Override
        public Fragment getItem(int pos){
            switch (pos){
                case 0:
                    MasterListFragment f1 = MasterListFragment.newInstance(yrMoSelected);
//                    MasterListFragment f1 = new MasterListFragment();
                    // Bundle b1 = f1.getArguments();
                    // title = b1.getString("title");
//                    Bundle b1 = new Bundle();
//                    b1.putString("yrMoChoice", yrMoSelected);
//                    f1.setArguments(b1);
                    // f1.addProperty(1);
                    return f1;
                case 1:
                    ChartsFragment f2 = ChartsFragment.newInstance(yrSelected);
//                    Bundle b2 = f2.getArguments();
//                    title = b2.getString("title");
//                    f2.addProperty(2);
                    return f2;
                case 2:
                    SavingsFragment f3 = SavingsFragment.newInstance(trackedGlobalMap);
                    return f3;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        /** Custom addition
         *
         *  Called when the PagerAdapter gets notifyDataSetChanged();
         *
         * */
        @Override
        public int getItemPosition(Object object) {
            if (object instanceof MasterListFragment){
                MasterListFragment f = (MasterListFragment) object;
                if (f != null) {
                    f.update(yrMoSelected);
                    // Bundle b1 = f.getArguments();
                    // title = (CharSequence) b1.get("title");
                    // topToolbar.setTitle(title);
                }
            }
            if (object instanceof ChartsFragment){
                ChartsFragment cf = (ChartsFragment) object;
                if( cf != null){
                    cf.update(yrSelected);
                    cf.reLoadData();
                }
            }
            if (object instanceof SavingsFragment){
                SavingsFragment sf = (SavingsFragment) object;
                if( sf != null){
                    sf.update(trackedGlobalMap);
                }
            }

            return super.getItemPosition(object);
        }
    }

    private void setupTabLayout() {
        tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        if (mTwoPane){
            // replace setup method
            tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_assignment_white_24dp));
            tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_pie_chart_white_24dp));

            TabLayout.Tab currentTab = tabLayout.getTabAt(mCurrentFragNum);
            Log.d(LOG_TAG, "Getting tab position on setUp: " + currentTab.getPosition());

            if (mCurrentFragNum == 0){
                topToolbar.setTitle("Transactions");
                yrMoFilterGroup.setVisibility(View.VISIBLE);
                yrFilterGroup.setVisibility(View.INVISIBLE);
                currentTab.select();

                if (mMasterListFrag == null){
                    mMasterListFrag = MasterListFragment.newInstance(yrMoSelected);
                }
                fragmentManager.beginTransaction()
                        .replace(R.id.master_fragment_container, mMasterListFrag)
                        .commit();

            } else if (mCurrentFragNum == 1){
                topToolbar.setTitle("Summary");
                yrFilterGroup.setVisibility(View.VISIBLE);
                yrMoFilterGroup.setVisibility(View.INVISIBLE);
                currentTab.select();
                if (mChartListFrag == null ){
                    mChartListFrag = ChartsFragment.newInstance(yrSelected);
                }
                fragmentManager.beginTransaction()
                        .replace(R.id.master_fragment_container, mChartListFrag)
                        .commit();
            } else if (mCurrentFragNum == 2) {
                topToolbar.setTitle("Savings Overview");
                yrFilterGroup.setVisibility(View.INVISIBLE);
                yrMoFilterGroup.setVisibility(View.INVISIBLE);
                currentTab.select();
                if (mSavingsFrag == null) {
                    mSavingsFrag = SavingsFragment.newInstance(trackedGlobalMap);
                }
                fragmentManager.beginTransaction()
                        .replace(R.id.master_fragment_container, mSavingsFrag)
                        .commit();
            }

            // TODO: Add SavingsFrag logic
            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    if(tabLayout.getSelectedTabPosition() == 0){
                        topToolbar.setTitle("Transactions");
                        yrMoFilterGroup.setVisibility(View.VISIBLE);
                        yrFilterGroup.setVisibility(View.INVISIBLE);
                        mCurrentFragNum = 0;
                        if (mMasterListFrag == null){
                            mMasterListFrag = MasterListFragment.newInstance(yrMoSelected);
                        }
                        fragmentManager.beginTransaction()
                                .replace(R.id.master_fragment_container, mMasterListFrag)
                                .commit();

                    } else if(tabLayout.getSelectedTabPosition() == 1){
                        topToolbar.setTitle("Summary");
                        yrFilterGroup.setVisibility(View.VISIBLE);
                        yrMoFilterGroup.setVisibility(View.INVISIBLE);
                        mCurrentFragNum = 1;
                        if (mChartListFrag == null){
                            mChartListFrag = ChartsFragment.newInstance(yrSelected);
                        }
                        fragmentManager.beginTransaction()
                                .replace(R.id.master_fragment_container, mChartListFrag)
                                .commit();
                    }
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {
                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {
                }
            });

        } else {
            // TODO third fragment not showing...
            tabLayout.setupWithViewPager(viewPager);
            for (int i = 0; i < tabLayout.getTabCount(); i++ ) {
                if (i == 0) {
                    tabLayout.getTabAt(i).setIcon(R.drawable.ic_assignment_white_24dp);
                } else if (i == 1) {
                    tabLayout.getTabAt(i).setIcon(R.drawable.ic_pie_chart_white_24dp);
                } else if (i == 2) {
                    tabLayout.getTabAt(i).setIcon(R.drawable.ic_piggy_bank_white_24dp);
                }
            }
        }

    }

    private void setupDateSelector(){
        // TODO: Changed filter array search to be ascending! May need to adjust array direction check
        /** Filter choice set-up
         *  id's in: include_viewpager.xml
         * */
        loadFilterArrays(); // Query database, set up available filters

        yrMoFilterChoice = (TextView) findViewById(R.id.button_yr_mo_filter);
        yrFilterChoice = (TextView) findViewById(R.id.button_yr_filter);

        yrMoFilterChoice.setTextColor(0xFFFFFFFF);
        yrFilterChoice.setTextColor(0xFFFFFFFF);

        // Set year-month
        if (mYrMonths.size() == 0){ // If not data

//            long dateOutput = System.currentTimeMillis();
            long monthlong = Long.parseLong("2592000000");
            long dateOutputEnd = System.currentTimeMillis() + monthlong;
            String dateString = new SimpleDateFormat("MM/yyyy")
                    .format(new Date(dateOutputEnd))
                    .toString();

            yrMoFilterChoice.setText(dateString);
            yrMoButtonLeft.setVisibility(View.INVISIBLE);
            yrMoButtonRight.setVisibility(View.INVISIBLE);
            yrMoSelected = (String) yrMoFilterChoice.getText();

        } else if ( mYrMoCounter < mYrMonths.size() ){
            yrMoFilterChoice.setText((CharSequence) mYrMonths.get(mYrMoCounter));
            yrMoSelected = (String) yrMoFilterChoice.getText();

            // TODO: Check if this logic is also needed elsewhere
            if (mYrMoCounter < mYrMonths.size() - 1){ // mYrMoCounter min. is always 0
                yrMoButtonLeft.setVisibility(View.VISIBLE);
            }
        } else { // mYrCounter is out of bounds
            // Restart counter, as array is now shorter
            mYrMoCounter = 0;
            yrMoFilterChoice.setText((CharSequence) mYrMonths.get(mYrMoCounter));
            yrMoSelected = (String) yrMoFilterChoice.getText();
            yrMoButtonRight.setVisibility(View.INVISIBLE);
        }

        // Set year
        if (mYears.size() == 0) { // If not data
            long now = System.currentTimeMillis();
            String yrString = new SimpleDateFormat("yyyy")
                    .format(new Date(now))
                    .toString();
            yrFilterChoice.setText(yrString);
            yrButtonLeft.setVisibility(View.INVISIBLE);
            yrButtonRight.setVisibility(View.INVISIBLE);
        } else if (mYears.size() == 1){
            yrFilterChoice.setText((CharSequence) mYears.get(mYrCounter));
            yrSelected = (String) yrFilterChoice.getText();
            yrButtonRight.setVisibility(View.INVISIBLE);
            yrButtonLeft.setVisibility(View.INVISIBLE);

        } else if ( mYrCounter < mYears.size() ){
            yrFilterChoice.setText((CharSequence) mYears.get(mYrCounter));
            yrSelected = (String) yrFilterChoice.getText();
            if (mYrCounter < mYears.size() - 1){ // mYrCounter min. is always 0
                yrButtonLeft.setVisibility(View.VISIBLE);
            }
        } else { // mYrCounter is out of bounds
            mYrCounter = 0;
            yrFilterChoice.setText((CharSequence) mYears.get(mYrCounter));
            yrSelected = (String) yrFilterChoice.getText();
            yrButtonRight.setVisibility(View.INVISIBLE);
        }

        // Put Date string together for query
        // dateSelected = moSelected + "/" + yrSelected;
        /**
         if (mYrMoCounter == 0){
         yrMoButtonRight.setVisibility(View.INVISIBLE);
         }
         if (mYrMoCounter == mYrMonths.size()-1) {
         // if (mMoCounter == mYrMonths.length - 1) {
         yrButtonLeft.setVisibility(View.INVISIBLE);
         }

         if (mYrCounter == 0){
         yrButtonRight.setVisibility(View.INVISIBLE);
         }
         if (mYrCounter == mYears.size()-1) {
         yrButtonLeft.setVisibility(View.INVISIBLE);
         }
         */

        yrMoButtonLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mYrMoCounter < mYrMonths.size() - 1){ // Max last index on array
                    mYrMoCounter++; // Array is sorted as DESC
                    yrMoFilterChoice.setText((CharSequence) mYrMonths.get(mYrMoCounter));
                    yrMoSelected = (String) yrMoFilterChoice.getText();
                    // dateSelected = moSelected + "/" + yrSelected;

                    if (mTwoPane) {
                        // Replacement method to update masterListView
                    } else {
                        notifyViewPagerDataSetChanged();
                    }


                    /** CHECK */
                    if(mYrMonths.size() <= 1){
                        yrMoButtonLeft.setVisibility(View.INVISIBLE);
                        yrMoButtonRight.setVisibility(View.INVISIBLE);
                    } else{
                        if (mYrMoCounter == mYrMonths.size() - 1) { // End of the array
                            yrMoButtonRight.setVisibility(View.VISIBLE);
                        }
                        if (mYrMoCounter == mYrMonths.size() - 1) {
                            yrMoButtonLeft.setVisibility(View.INVISIBLE);
                        }
                    }
                }
            }
        });
        yrMoButtonRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mYrMoCounter > 0) { // Can never get to -1
                    mYrMoCounter--;
                    yrMoFilterChoice.setText((CharSequence) mYrMonths.get(mYrMoCounter));
                    yrMoSelected = (String) yrMoFilterChoice.getText();
                    if (mTwoPane) {
                        // Replacement method to update masterListView
                    } else {
                        notifyViewPagerDataSetChanged();
                    }
                    if (mYrMoCounter == 0){ // If boundary condition...
                        yrMoButtonRight.setVisibility(View.INVISIBLE);
                        yrMoButtonLeft.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        yrButtonLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mYrCounter < mYears.size() - 1){
                    mYrCounter++; // // Array is sorted as DESC (Add to get older date)
                    yrFilterChoice.setText((CharSequence) mYears.get(mYrCounter));
                    yrSelected = (String) yrFilterChoice.getText();
                    if (mTwoPane) {
                        // Replacement method to update masterListView
                    } else {
                        notifyViewPagerDataSetChanged();
                    }

                    /** CHECK */
                    if (mYrCounter == mYears.size() - 1){ // End of the array
                        yrButtonRight.setVisibility(View.VISIBLE);
                    }
                    if (mYrCounter == mYears.size() - 1) {
                        yrButtonLeft.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });
        yrButtonRight.setOnClickListener(new View.OnClickListener() {/**/
            @Override
            public void onClick(View v) {
                if (mYrCounter > 0) {
                    mYrCounter--; // Subs. to get younger date
                    yrButtonLeft.setVisibility(View.VISIBLE);
                    yrFilterChoice.setText((CharSequence) mYears.get(mYrCounter));
                    yrSelected = (String) yrFilterChoice.getText();
                    // dateSelected = moSelected + "/" + yrSelected;
                    if (mTwoPane) {
                        // Replacement method to update masterListView
                    } else {
                        notifyViewPagerDataSetChanged();
                    }

                    /** CHECK */
                    if (mYears.size() <= 1){
                        yrButtonRight.setVisibility(View.INVISIBLE);
                        yrButtonLeft.setVisibility(View.INVISIBLE);
                    } else {
                        if (mYrCounter == 0){
                            yrButtonRight.setVisibility(View.INVISIBLE);
                        }
                    }

                }
            }
        });
    }

    // Called in setUpDateSelector() above
    private void loadFilterArrays() {
        Log.d(LOG_TAG, "Entering loaderFilterArray");
        /** Query database to check which months to display */
        // To access our database, we instantiate our subclass of SQLiteOpenHelper
        // and pass the context, which is the current activity.

        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Log.d(LOG_TAG, "readable db: " + db);
        // Filter results WHERE "title" = 'My Title'
        // String selection = TransContract.TransEntry.COLUMN_TYPE + " = ?";
        // String selection = TransContract.TransEntry.COLUMN_TYPE;
        // String[] selectionArgs = { Integer.toString(TransContract.TransEntry.TYPE_DEBIT)};

        mYrMonths = new ArrayList();
        mYears = new ArrayList();
        formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.GERMANY);

        // LinkedHashMap<DateString, GlobalBin.(with wallet or savings)>
        trackedGlobalMap = new LinkedHashMap<>();
        // Object -> mAccounts
        LinkedHashMap<String, Float> mAccounts = new LinkedHashMap<>();

        String[] projection = new String[] {
                TransContract.TransEntry._ID,
                TransContract.TransEntry.COLUMN_AMOUNT,
                TransContract.TransEntry.COLUMN_TYPE,
                TransContract.TransEntry.COLUMN_ACCOUNT_FROM,
                TransContract.TransEntry.COLUMN_ACCOUNT_TO,
                TransContract.TransEntry.COLUMN_DATE
        };
        /*
        * String[] projection = {
                TransContract.TransEntry.COLUMN_DATE
        };
        * */

        // How you want the results sorted in the resulting Cursor
        String orderBy =
//                TransContract.TransEntry.COLUMN_DATE + " DESC";
                  TransContract.TransEntry.COLUMN_DATE + " ASC";

        Cursor cursor = db.query(
                TransContract.TransEntry.TABLE_NAME,     // The table to query
                projection,                              // The columns to return
                null,                                    // selection, The columns for the WHERE clause == Some string
                null,                                    // selectionArgs, The values for the WHERE clause == Some value
                null,                                    // groupBy
                null,                                    // filter where groups == something.
                orderBy                                  // sort order
        );

        int dateColumnIndex = cursor.getColumnIndex(TransContract.TransEntry.COLUMN_DATE);
        int indexMo = 0;
        int indexYr = 0;

        /* SavingsFrag. Data */
        Float listedAmount;
        int accountFrom;
        int accountTo;

        int listedType;
        int amountColumnIndex = cursor.getColumnIndex(TransContract.TransEntry.COLUMN_AMOUNT);
        int typeColumnsIndex = cursor.getColumnIndex(TransContract.TransEntry.COLUMN_TYPE);
        int accountFromIndex = cursor.getColumnIndex(TransContract.TransEntry.COLUMN_ACCOUNT_FROM);
        int accountToIndex = cursor.getColumnIndex(TransContract.TransEntry.COLUMN_ACCOUNT_TO);

        GlobalBin globalBin = new GlobalBin("Accounts");


        if (cursor.moveToFirst()) {
            try {
//                cursor.moveToPosition(-1); // Handles case when db length == 1
//                while (cursor.moveToNext()) {
                do {
                    currentDate = cursor.getLong(dateColumnIndex); // as Unix time
                    dateString = formatter.format(new Date(currentDate));

                    /* MasterList Data */
                    partsYrMo = dateString.split("/", 2); // Will store the split date
                    mYrMo = partsYrMo[1]; // position 1, should be "MM/YYYY"
                    mYear = mYrMo.split("/", 0)[1]; // position 1, should be "YYYY"

                    /* SavingsFrag. Data */
                    listedType = cursor.getInt(typeColumnsIndex); // Expense, income or saving
                    listedAmount = cursor.getFloat(amountColumnIndex);

                    accountFrom = cursor.getInt(accountFromIndex);
                    accountTo = cursor.getInt(accountToIndex);

                    // Check type
                    if (listedType == TransContract.TransEntry.TYPE_DEBIT) {
                        /** Will check if money available in either account
                         *  Will substract from wallet, if not saving, if not wallet will be negative */
                        // GlobalAccount
                        // Expenses always subtracted from wallet (i.e. can go into overdraft)
                        // Money to/from savings will be done through "Transfer" orders.
                        globalBin.updateWalletMap(dateString, listedAmount);
//                        Log.d(LOG_TAG, "Global bin after subtract: " + globalBin.getWalletMap());

                    } else if (listedType == TransContract.TransEntry.TYPE_CREDIT) {
                        // GlobalAccount
                        globalBin.updateWalletMap(dateString, listedAmount);
                    } else {
                        throw new java.lang.RuntimeException("@loadFilterArrays, too many TYPEs other?");
                    }

                    /*
                    * else if (listedType == TransContract.TransEntry.TYPE_TRANSFER){

                        if (accountFrom == TransContract.TransEntry.ACCOUNT_WALLET_10 &&
                                accountTo == TransContract.TransEntry.ACCOUNT_SAVINGS_0) {
                            // GlobalAccount
                            globalBin.updateWalletMap(dateString, -listedAmount); // Debit wallet
                            globalBin.updateSavingsMap(dateString, listedAmount); // Credit savings

                        } else if (accountFrom == TransContract.TransEntry.ACCOUNT_SAVINGS_0 &&
                                accountTo == TransContract.TransEntry.ACCOUNT_WALLET_10) {
                            globalBin.updateSavingsMap(dateString, -listedAmount); // Debit savings
                            globalBin.updateWalletMap(dateString, listedAmount); // Credit wallet
                        }
                        // (i.e. external transfer) Editor should record only expense and income
                    }
                    * */

                    /* MasterList Data / DateSelector */
                    if (mYrMonths.contains(mYrMo)){
                        continue;
                    } else {
                        mYrMonths.add(indexMo, mYrMo); // can also use .add(object) only
                        indexMo ++;
                    }
                    if (mYears.contains(mYear)) {
                        continue;
                    } else {
                        mYears.add(indexYr, mYear);
                        indexYr ++;
                    }
                } while (cursor.moveToNext());
            } finally {
                cursor.close();
                // Log.d(LOG_TAG, "MonthsArray= " + mYrMonths);
            }
        }
    }

    /** Save the index of the date selector array when
     * changing orientation (i.e. )*/
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_COUNTER_YRMO, mYrMoCounter);
        outState.putInt(STATE_COUNTER_YR, mYrCounter);
        outState.putInt(STATE_FRAG, mCurrentFragNum);

        if (yrMoButtonLeft.isShown()){
            outState.putInt(STATE_YRMO_LEFT, 1);
        } else {
            outState.putInt(STATE_YRMO_LEFT, 0);
        }

        if (yrMoButtonRight.isShown()){
            outState.putInt(STATE_YRMO_RIGHT, 1);
        } else {
            outState.putInt(STATE_YRMO_RIGHT, 0);
        }

        if (yrButtonLeft.isShown()){
            outState.putInt(STATE_YR_LEFT, 1);
        } else {
            outState.putInt(STATE_YR_LEFT, 0);
        }
        if (yrButtonRight.isShown()){
            outState.putInt(STATE_YR_RIGHT, 1);
        } else {
            outState.putInt(STATE_YR_RIGHT, 0);
        }

    }

    public void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        menuItem.setChecked(true);
                        if (mTwoPane){
                            TabLayout.Tab currentTab;
                            switch (menuItem.getItemId()){
                                case R.id.menu_transactions:
                                    currentTab = tabLayout.getTabAt(0);
                                    currentTab.select();
                                    break;
                                case R.id.menu_charts:
                                    currentTab = tabLayout.getTabAt(1);
                                    currentTab.select();
                                    break;
                                default:
                                    break;
                            }
                        } else {
                            switch (menuItem.getItemId()){
                                case R.id.menu_transactions:
                                    viewPager.setCurrentItem(0);
                                    break;
                                case R.id.menu_charts:
                                    viewPager.setCurrentItem(1);
                                    break;
                                case R.id.menu_savings:
                                    viewPager.setCurrentItem(2);
                                    break;
                                case R.id.menu_transfers:
                                    Intent intent = new Intent(MainActivity.this, TransferActivity.class);
                                    intent.putExtra("origin", "transfer");
                                    startActivityForResult(intent, 2); // If dataSet changes, TransferActivity will return "2"
                                    // implements callback: onActivityResult() ...below
                                    break;
                                default:
                                    break;
                            }
                        }
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sample_actions, menu);
        return true;
    }

    // First horizontal menu bar in the app.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.menu_settings:
                return true;
            case R.id.menu_about:
                return true;
            case R.id.action_insert_dummy_data:
                try {
                    insertTransTest();

                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return true;
            case R.id.action_delete_all_data:
                showDeleteAllConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void insertTransTest() throws ParseException {

        // Create fake data
        String str_date = "09/12/2011";
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        Date date = df.parse(str_date);
        long dateOutput = date.getTime();
        // Log.d(LOG_TAG, "long  output: " + dateOutput);

        // Create a ContentValues object where
        // column names are the keys, and
        // attributes are the values.
        ContentValues values = new ContentValues();
        values.put(TransContract.TransEntry.COLUMN_AMOUNT, -72.3);
        values.put(TransContract.TransEntry.COLUMN_TYPE, TransContract.TransEntry.TYPE_DEBIT);
//        values.put(TransContract.TransEntry.COLUMN_KIND, TransContract.TransEntry.KIND_NEED);
        values.put(TransContract.TransEntry.COLUMN_CATEGORY, "Shopping");
        values.put(TransContract.TransEntry.COLUMN_TITLE, "Grote Koop");
        values.put(TransContract.TransEntry.COLUMN_DATE, dateOutput);
        values.put(TransContract.TransEntry.COLUMN_RECURRING, TransContract.TransEntry.RECURRING);

        Uri newUri = getContentResolver().insert(TransContract.TransEntry.CONTENT_URI, values);
        // myFragmentAdapter.notifyDataSetChanged(); -> causes to loose the TabLayout
        // viewPager.setAdapter(myFragmentAdapter);
        Log.d(LOG_TAG, "Inserted transaction");
        setupDateSelector();
        if (mTwoPane) {
            // Replacement method to update masterListView
            if (mCurrentFragNum == 0) {
                mMasterListFrag.update(yrMoSelected);
            } else if (mCurrentFragNum == 1) {
                mChartListFrag.update(yrSelected);
            } else if (mCurrentFragNum == 2) {
                mChartListFrag.update(yrSelected);
            }
        } else {
            notifyViewPagerDataSetChanged();
        }

    }

    private void deleteAllTransactions() {
        int nRowsDeleted = getContentResolver().delete(
                TransContract.TransEntry.CONTENT_URI,
                null,
                null);
        Log.d(LOG_TAG, "Deleted transaction");
        setupDateSelector();
        if (mTwoPane) {
            // Replacement method to update masterListView
        } else {
            notifyViewPagerDataSetChanged();
        }

    }

    private void showDeleteAllConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_all_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteAllTransactions();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
