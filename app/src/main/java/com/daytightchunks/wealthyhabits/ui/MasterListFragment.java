package com.daytightchunks.wealthyhabits.ui;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.daytightchunks.wealthyhabits.R;
import com.daytightchunks.wealthyhabits.data.TransContract;
import com.daytightchunks.wealthyhabits.data.TransDbHelper;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by DayTightChunks on 07/08/2017.
 */

public class MasterListFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    /** Tag for the log messages */
    public static final String LOG_TAG = MasterListFragment.class.getSimpleName();


    private int fragProperty;
    private String bundledString;

    private TextView yrMoChoiceView;
    private String yrMoSelected;

    /** Filter choice listeners */
    private ImageButton buttonLeft;
    private ImageButton buttonRight;

    private ListView transListView;

    // SimpleCursorAdapter mMasterListAdapter;
    MasterListAdapter mMasterListAdapter;

    /** Connecting to Database */
    private TransDbHelper mDbHelper;

    /** Identifies a specific loader being used in this component */
    private static final int URL_LOADER = 0;

    protected MainActivity mActivity;

    OnTransactionSelectedListener mListItemCallback;

    /** Interface for List Item START */
    // Container Activity must implement this interface
    public interface OnTransactionSelectedListener {
        void onTransListItemSelected(int position, long id, String category);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mListItemCallback = (OnTransactionSelectedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnTransactionSelectedListener");
        }
    }
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // Send the event to the host activity (i.e. MainActivity, initializes EditorActivity)
        TextView categoryEditText = (TextView) v.findViewById(R.id.trans_item_category);
        String category = (String) categoryEditText.getText();
        mListItemCallback.onTransListItemSelected(position, id, category); // Category will be used for "Transfer" handling
    }
    /** Interface for List Item END */

    public MasterListFragment() {}

    public static MasterListFragment newInstance(String dateSelected) {

        MasterListFragment f = new MasterListFragment();
        Bundle b = new Bundle();
        b.putString("yrMoSelected", dateSelected);
        b.putString("title", "Transactions");
        f.setArguments(b);

        return f;
    }

    /** Method to update fragment on Date Change
     *  implemented by PagerAdapter in MainActivity
     * */
    // @Override
    public void update(String newDate) {
        yrMoSelected = newDate;
        // TODO: not sure if I need these steps before restartLoader;
        mMasterListAdapter = new MasterListAdapter(getContext(), null);
        transListView.setAdapter(mMasterListAdapter);
        getLoaderManager().restartLoader(URL_LOADER, null, this);
    }

    /*** Needed ? */
    public void addProperty(int prop){
        this.fragProperty = prop;
    }

    @Override
    public void onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        // bundledString = bundle.getString("ownID");
        if (bundle != null) {
        yrMoSelected = bundle.getString("yrMoSelected", "Default if null");
        }
//        Log.d(LOG_TAG, "yrMoSelected 1: " + yrMoSelected);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {


//        FrameLayout newContainer = (FrameLayout) container.findViewById(R.id.master_list_container);
//        Activity context = getActivity();

//        LinearLayout newContainer = (LinearLayout) context.findViewById(R.id.list_layout);
//        LinearLayout newContainer = (LinearLayout) container.findViewById(R.id.list_layout);
        // Inflate the layout for this fragment
        // "container" refers to ViewGroup of the activity layout where this fragment will live.
        View rootView;
        if (yrMoSelected != null){
            rootView = inflater.inflate(R.layout.fragment_master_list, container, false); // attachToRoot = false or true (root is ViewGroup)
        } else {
            rootView = inflater.inflate(R.layout.fragment_transfer_list, container, false); // attachToRoot = false or true (root is ViewGroup)
        }


//        newContainer.addView(rootView);

        transListView = (ListView) rootView.findViewById(android.R.id.list);



        // TODO: setOnItemClickListener
        //Activity mMainAct = getActivity();
        //View mMainActivityView = (CoordinatorLayout) getActivity().findViewById(R.id.main_content),

        // yrMoChoiceView = (TextView) mMainActivityView.findViewById(R.id.button_yr_mo_filter);
        // yrMoSelected = (String) yrMoChoiceView.getText();
        //yrMoSelected = (String) yrMoChoiceView.getText();
        // yrMoSelected = getArguments().getString("yrMoSelected");

//        Log.d(LOG_TAG, "yrMoSelected 2: " + yrMoSelected);

        mMasterListAdapter = new MasterListAdapter(getContext(), null);
        transListView.setAdapter(mMasterListAdapter);

        // To access our database, we instantiate our subclass of SQLiteOpenHelper
        // and pass the context, which is the current activity.
//        mDbHelper = new TransDbHelper(getActivity());

        /** Empty view is glitchy */
        View emptyView = rootView.findViewById(R.id.empty_view);
        transListView.setEmptyView(emptyView);

        initializeLoader();

        // Setup FAB to open EditorActivity
//        TODO: Handle FAB hugging last item of listview
//        https://stackoverflow.com/questions/29362284/floating-action-button-blocking-other-components

        return rootView;
    }

    public void initializeLoader() {
        getLoaderManager().initLoader(URL_LOADER, null, this);
    }

    /** getLoaderManager() call this Loader when this fragment  starts (i.e. onCreateView) */
    @Override
    public Loader<Cursor> onCreateLoader (int loaderID, Bundle bundle) {

        // Log.d(LOG_TAG, "TransFragment test 0001: " + yrMoSelected);
        long dateOutput = System.currentTimeMillis();
        long monthlong = Long.parseLong("2592000000");
        long dateOutputEnd = System.currentTimeMillis() + monthlong;
        // Log.d(LOG_TAG, "dateOutput current: " + dateOutput);

        DateFormat df = new SimpleDateFormat("MM/yyyy");
        // 1 month = 1498867200000 - 1496275200000 = 2592000000

        String[] projection = {
                TransContract.TransEntry._ID,
                TransContract.TransEntry.COLUMN_CATEGORY,
                TransContract.TransEntry.COLUMN_TITLE,
                TransContract.TransEntry.COLUMN_AMOUNT,
                TransContract.TransEntry.COLUMN_TYPE,
                TransContract.TransEntry.COLUMN_ACCOUNT_FROM,
                TransContract.TransEntry.COLUMN_ACCOUNT_TO,
                TransContract.TransEntry.COLUMN_DATE
        };

        String selection;
        Date date;
        if (yrMoSelected != null){
            try {
                date = df.parse(yrMoSelected);
                dateOutput = date.getTime();
                dateOutputEnd = dateOutput + monthlong;
                Log.d(LOG_TAG, "dateOutput choice: " + dateOutput);
                Log.d(LOG_TAG, "dateOutputEnd choice: " + dateOutputEnd);
            } catch (ParseException e) {
                e.printStackTrace();
            }
//            selection = "((" +
//                    TransContract.TransEntry.COLUMN_DATE + " > " +
//                    Long.toString(dateOutput) + ") AND (" +
//                    TransContract.TransEntry.COLUMN_DATE + " <= " + Long.toString(dateOutputEnd) + "))";
            selection = "((" +
                    TransContract.TransEntry.COLUMN_DATE + " > " +
                    Long.toString(dateOutput) + ") AND (" +
                    TransContract.TransEntry.COLUMN_DATE + " <= " + Long.toString(dateOutputEnd) + ") AND (" +
                    TransContract.TransEntry.COLUMN_CATEGORY + " != " + "'Transfer'))";
        } else {
            // Handles in Transfer Activity
            date = new Date();
            dateOutputEnd = date.getTime();
            // WHERE
//            selection = "((" + TransContract.TransEntry.COLUMN_CATEGORY + " = " +
//                    "'Transfer'" + ") AND (" +
//                    TransContract.TransEntry.COLUMN_DATE + " <= " + Long.toString(dateOutputEnd)+ "))";
            selection = "((" + TransContract.TransEntry.COLUMN_CATEGORY + " = " +
                    "'Transfer'))";
        }

        // String[] selectionArgs = { Long.toString(dateOutput), Long.toString(dateOutputEnd)};

        /** Takes action based on ID of the loader*/
        switch (loaderID) {
            case URL_LOADER:
                // Returns a new cursor loader
                return new CursorLoader(
                        getActivity(),                              // Parent activity context
                        TransContract.TransEntry.CONTENT_URI,       // Table to query
                        projection,                                 // projection to return
                        selection,                                       // Selection clause
                        null,                                       // Selection arguments
                        TransContract.TransEntry.COLUMN_DATE + " DESC" // Sort order (currently in ms since 1970)
                );
            default:
                // An invalid id was passed in
                return  null;
        }

    }

    /** After the background framework has the Loader Cursor object, it starts
     * querying in the background. When finished, the framework calls onLoadFinished() */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor){

        // transCursorAdapter.changeCursor(cursor);
        mMasterListAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // transCursorAdapter.changeCursor(null);
        mMasterListAdapter.swapCursor(null);
    }

}
