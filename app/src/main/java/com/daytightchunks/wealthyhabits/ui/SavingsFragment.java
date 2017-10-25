package com.daytightchunks.wealthyhabits.ui;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.daytightchunks.wealthyhabits.R;
import com.daytightchunks.wealthyhabits.data.TransContract;
import com.daytightchunks.wealthyhabits.data.TransDbHelper;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarEntry;

import java.security.Key;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

import java.text.SimpleDateFormat;
import java.util.Map;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;

/**
 * Created by DayTightChunks on 03/09/2017.
 */

public class SavingsFragment extends Fragment {

    /** Tag for the log messages */
    public static final String LOG_TAG = SavingsFragment.class.getSimpleName();

    TransDbHelper mDbHelper;
    private LinkedHashMap<String, Object> trackedGlobalMap;


    BarChart barChart;
    Context context;

    /** Identifies a specific loader being used in this component */
    private static final int URL_LOADER = 2;

    public SavingsFragment(){}

    public static SavingsFragment newInstance(LinkedHashMap<String, Object> data){
        SavingsFragment savingsFragment = new SavingsFragment();
        Bundle b = new Bundle();
        b.putSerializable("globalBin", data);
        savingsFragment.setArguments(b);
        return savingsFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null){
            trackedGlobalMap = (LinkedHashMap<String, Object>) bundle.getSerializable("globalBin");
        }
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState){

        View rootView = layoutInflater.inflate(R.layout.fragment_savings, container, false);

        context = getContext();
        mDbHelper = new TransDbHelper(context);

        LinearLayout mOverviewLayout = (LinearLayout) rootView.findViewById(R.id.overview_chart_parent);

        barChart = new BarChart(context);
        barChart.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));
        mOverviewLayout.addView(barChart);

//        loadData();
        drawBarChart();

        return rootView;
    }

//    private void loadData() {
//        // getLoaderManager().initLoader(URL_LOADER, null, this);
//
//        SQLiteDatabase db = mDbHelper.getReadableDatabase();
//
//        // LinkedHashMap<DateString, GlobalBin.(with wallet or savings)>
//        trackedGlobalMap = new LinkedHashMap<>();
//        // Object -> mAccounts
//        LinkedHashMap<String, Float> mAccounts = new LinkedHashMap<>();
//
//        String[] projection = new String[] {
//                TransContract.TransEntry._ID,
//                TransContract.TransEntry.COLUMN_AMOUNT,
//                TransContract.TransEntry.COLUMN_TYPE,
//                TransContract.TransEntry.COLUMN_DATE
//        };
//
//        // How you want the results sorted in the resulting Cursor
//        String orderBy =
//                TransContract.TransEntry.COLUMN_DATE + " ASC";
//
//        Cursor cursor = db.query(
//                TransContract.TransEntry.TABLE_NAME,     // The table to query
//                projection,                              // The columns to return
//                null,                                    // selection, The columns for the WHERE clause == Some string
//                null,                                    // selectionArgs, The values for the WHERE clause == Some value
//                null,                                    // groupBy
//                null,                                    // filter where groups == something.
//                orderBy                                  // sort order (currently in ms since 1970)
//        );
//
//        int listedType;
//        long listedDate;
//        String monthDate;
//        String dictDate;
//        Float listedAmount;
//        SimpleDateFormat formatter;
//
//        GlobalBin globalBin = new GlobalBin("Accounts");
//
//        if (cursor.moveToFirst()) {
//            try {
//                int amountColumnIndex = cursor.getColumnIndex(TransContract.TransEntry.COLUMN_AMOUNT);
//                int typeColumnsIndex = cursor.getColumnIndex(TransContract.TransEntry.COLUMN_TYPE);
//                // int categoryColumnsIndex = cursor.getColumnIndex(TransContract.TransEntry.COLUMN_CATEGORY);
//                int dateColumnsIndex = cursor.getColumnIndex(TransContract.TransEntry.COLUMN_DATE);
//                do {
//                    // Get data info
//                    listedType = cursor.getInt(typeColumnsIndex); // Expense, income or saving
//                    listedDate = cursor.getLong(dateColumnsIndex); // as Unix time
//                    listedAmount = cursor.getFloat(amountColumnIndex);
//
//                    formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
//                    String dateString = formatter.format(new Date(listedDate));
//                    Log.d(LOG_TAG, "dateString = " + dateString);
//
//                    /*
//                    String[] parts = dateString.split("/", 2); // Will store the split date
//                    Log.d(LOG_TAG, "in cursor, parts = " + parts);
//                    monthDate = parts[1]; // position 1, should be "mm/yyyy"
//                    // tokens = listedDate.split(delims);
//                    Log.d(LOG_TAG, "in cursor, monthDate/parts index 1 = " + parts[1]);
//                     */
//
//                    // Check type
//                    if (listedType == TransContract.TransEntry.TYPE_DEBIT) {
//                        /** Will check if money available in either account
//                         *  Will substract from wallet, if not saving, if not wallet will be negative */
//                        // GlobalAccount
//                        // Subtract from savings or wallet?
//                        if (globalBin.getWallet() == 0 | globalBin.getWallet() == null){
//                            if (globalBin.getSavings() == 0 | globalBin.getSavings() == null){
//                                globalBin.subtractFromWallet(listedAmount); // income will now be negative
//                                trackedGlobalMap.put(dateString, mAccounts.put("Wallet", globalBin.getWallet()));
//                            } else {
//                                globalBin.debitSavingAcc(listedAmount); // money taken out of savings if wallet = 0
//                                trackedGlobalMap.put(dateString, mAccounts.put("Savings", globalBin.getSavings()));
//                            }
//                        }
//                    } else if (listedType == TransContract.TransEntry.TYPE_CREDIT){
//                        // GlobalAccount
//                        globalBin.addToWallet(listedAmount);
//                        trackedGlobalMap.put(dateString, mAccounts.put("Wallet", globalBin.getWallet()));
//
//                    } else if (listedType == TransContract.TransEntry.TYPE_TRANSFER){
//                        // GlobalAccount
//                        globalBin.creditSavingAcc(listedAmount);
//                        trackedGlobalMap.put(dateString, mAccounts.put("Savings", globalBin.getSavings()));
//                        globalBin.subtractFromWallet(listedAmount);
//                        trackedGlobalMap.put(dateString, mAccounts.put("Wallet", globalBin.getWallet()));
//                        // TODO: Warning if user brings money from outside!!!
//                        // TODO: Editor should record only expense and income
//                        // TODO: New EditorActivity with Savings' Transfer
//                    }
//
//                } while (cursor.moveToNext());
//            } finally {
//                cursor.close();
//            }
//        }
//
//    }

//    public class GlobalBin {
//        private String binName;
//        private Float wallet;
//        private Float savings;
//        // private HashMap accountMap;
//
//        public GlobalBin(String name){
//            this.binName = name;
//            this.wallet = 0.0f;
//            this.savings = 0.0f;
//            // this.accountMap = new HashMap<>();
//        }
//
//        public String getName(){ return this.binName; }
//
//        public void addToWallet(Float income){
//            if (this.wallet == null || this.wallet == 0){
//                this.wallet = income;
//            } else {
//                this.wallet += income;
//            }
//        }
//
//        public void subtractFromWallet(Float amount){
//            if (this.wallet == null || this.wallet == 0){
//                this.wallet = amount*-1;
//            } else {
//                this.wallet -= amount;
//            }
//        }
//
//        public void creditSavingAcc(Float saving){
//            if (this.savings == null || this.savings == 0){
//                this.savings = saving;
//            } else {
//                this.savings += saving;
//            }
//        }
//
//        public void debitSavingAcc(Float saving){
//            if (this.savings == null || this.savings == 0){
//                this.savings = saving*-1;
//            } else {
//                this.savings -= saving;
//            }
//        }
//
//        public Float getWallet(){ return this.wallet; }
//        public Float getSavings(){
//            return this.savings;
//        }
//
//    }

    public void update(LinkedHashMap<String, Object> data){
        trackedGlobalMap = data;
        drawBarChart();
    }
    private void drawBarChart() {

        int[] colorArray = new int[]{
                ContextCompat.getColor(context, R.color.darkpurple),
                ContextCompat.getColor(context, R.color.darkblue),
                ContextCompat.getColor(context, R.color.darkorange)};

        // List<BarEntry>, "Empty entries to fill in"
        ArrayList<BarEntry> yEntries = new ArrayList<>();
        yEntries.add(new BarEntry(2, 0.0001f));  // Savings #1
        yEntries.add(new BarEntry(1, 0.0001f));  // Savings #2
        yEntries.add(new BarEntry(0, 0.0001f));  // Savings #3

        for (Map.Entry<String, Object> entry : trackedGlobalMap.entrySet()){
            String key = entry.getKey();
            Log.d(LOG_TAG, "Key:" + key);
            Log.d(LOG_TAG, "Object ?:" + trackedGlobalMap.get(key));

//            for (Map.Entry<String, Float> account :  )

        }

        // TODO: Testing returned object.
        Log.d(LOG_TAG, "Key set: " + trackedGlobalMap.keySet());
        Log.d(LOG_TAG, "Map: " + trackedGlobalMap);

        /*
        yEntries.set(2, (new BarEntry(2, totSav1)));
        yEntries.set(1, (new BarEntry(1, totSav2)));
        yEntries.set(0, (new BarEntry(0, totSav3)));
        */

    }
}
