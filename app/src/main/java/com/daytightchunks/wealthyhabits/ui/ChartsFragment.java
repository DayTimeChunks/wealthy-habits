package com.daytightchunks.wealthyhabits.ui;

// import android.app.Fragment;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.daytightchunks.wealthyhabits.R;
import com.daytightchunks.wealthyhabits.data.TransDbHelper;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.LargeValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.ArrayList;
import java.util.LinkedHashMap;

//import com.jjoe64.graphview.series.DataPoint;
//import com.jjoe64.graphview.series.LineGraphSeries;

public class ChartsFragment extends Fragment {

    /** Tag for the log messages */
    public static final String LOG_TAG = ChartsFragment.class.getSimpleName();

    private int fragProperty;

    ChartLoaderAdapter mChartsAdapter;
    ListView chartListView;

    private TextView yrChoiceView;
    private String yrChoice;

//    LineGraphSeries<DataPoint> series;
    PieChart pieChart;
    HorizontalBarChart horizontalBarChart;

    private int[] colorArray;

    /** Connecting to Database */
    private TransDbHelper mDbHelper;
    private Uri currentTransUri;
    private Cursor cursorQuery;


    // ChartsRefreshListener mCallback; // Interface instance

    /** Identifies a specific loader being used in this component */
    private static final int URL_LOADER = 1;

    public ChartsFragment() {
        // No instanced needed.
    }

    public View onCreateView(LayoutInflater layoutInflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

//        TODO: you have already inflated it before, just need to replace contents
//        see how I can access the previously inflated layout to avoid redoing it here.
        View rootView = layoutInflater.inflate(R.layout.fragment_master_list, container, false);

        Context context = getContext();

        // topToolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        // topToolbar.setTitle("Summary");
        // ((MainActivity) getActivity()).getSupportActionBar().setTitle("Summary");

        /** Need to set the listView adapter */
        // final
        chartListView = (ListView) rootView.findViewById(android.R.id.list);

        mChartsAdapter = new ChartLoaderAdapter(getContext());
        chartListView.setAdapter(mChartsAdapter);
        yrChoiceView = (TextView) getActivity().findViewById(R.id.button_yr_filter);
        yrChoice = (String) yrChoiceView.getText();

        /** Empty view is glitchy */
        View emptyView = rootView.findViewById(R.id.empty_view);
        chartListView.setEmptyView(emptyView);

        // To access our database, we instantiate our subclass of SQLiteOpenHelper
        // and pass the context, which is the current activity.
        // mDbHelper = new TransDbHelper(getContext());
        loadData();

        return rootView;
    }

    public static ChartsFragment newInstance(String dateSelected){
        ChartsFragment chartsFragment = new ChartsFragment();
        Bundle b = new Bundle();
        b.putString("yearChoice", dateSelected);
        b.putString("title", "Summary");
        chartsFragment.setArguments(b);
        return chartsFragment;
    }

    /** Method to update fragment on Date Change
     *  implemented by PagerAdapter in MainActivity
     * */
    // @Override
    public void update(String newDate) {
        yrChoice = newDate;
        // TODO: not sure if I need these steps before restartLoader;
        mChartsAdapter = new ChartLoaderAdapter(getContext());
        chartListView.setAdapter(mChartsAdapter);
        // TransactionFragment version:
        // mCursorAdapter = new TransCursorAdapter(getContext(), null);
        // transListView.setAdapter(mCursorAdapter);
        // getLoaderManager().restartLoader(URL_LOADER, null, this);
        reLoadData();
    }

    public void addProperty(int prop){
        this.fragProperty = prop;
    }

    @Override
    public void onStart() {
        super.onStart();
        loadData();
        // mChartsAdapter.notifyDataSetInvalidated(); // tried, did not update fragment
    }

    @Override
    public void onResume() {
        super.onResume();
        // ((MainActivity) getActivity()).getSupportActionBar().setTitle("Summary");
//        ((MainActivity) getActivity()).setTitle();
        // mChartsAdapter.notifyDataSetChanged(); // tried, did not update fragment

    }

    private void loadData() {
        // getLoaderManager().initLoader(URL_LOADER, null, this);
        getLoaderManager().initLoader(URL_LOADER, null, loaderCallbacks);
    }

    protected void reLoadData() {
        // getLoaderManager().initLoader(URL_LOADER, null, this);
        getLoaderManager().restartLoader(URL_LOADER, null, loaderCallbacks);
    }

    /**
    // An example of Fragment to Activity communication (not needed now)
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (ChartsRefreshListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement ChartsRefreshListener interface");
        }
    }
     */

    public class ChartLoaderAdapter extends BaseAdapter {


        private LinkedHashMap<String, MonthHashLoader.MonthBin> monthlyData = new LinkedHashMap<>();
        private LinkedHashMap<String, MonthHashLoader.MonthBin> subData = new LinkedHashMap<>();

        private LayoutInflater layoutInflater = null;

        public ChartLoaderAdapter(Context context){

            layoutInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            // Log.d(LOG_TAG, "Adapter Bin with Data Object: " + monthlyData.size());
            return monthlyData.size();
        }

        @Override
        public Object getItem(int position) {
            // Log.d(LOG_TAG, "key set: "+ key);
            // Log.d(LOG_TAG, "key contains year: " + key + " : " + monthlyData.get(key));

            int counter = 0;
            MonthHashLoader.MonthBin currentBin = null;

            for (String key : monthlyData.keySet()) {
                currentBin = monthlyData.get(key); // out is first MonthBin Object (after onLoadFinished)
                if (position == counter){
                    break;
                } else{
                    counter += 1;
                }
            }
            return currentBin; // Returns current month
        }

        @Override
        public long getItemId(int position) {
            // return 0; // before
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {

            // View listItemView = convertView;
            if (convertView == null) {
                // Get an instance of the LayoutInflater (a child of View)
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                convertView = inflater.inflate(R.layout.chart_card, viewGroup, false);
            }
            MonthHashLoader.MonthBin itemBin = (MonthHashLoader.MonthBin) getItem(position); // Each bin is one month == one card view

            // Set the views you need
            chooseChart(itemBin, convertView);
            return convertView;
        }

        public void swapData(LinkedHashMap<String, MonthHashLoader.MonthBin> data) {
            this.monthlyData.clear();
            if (data != null) {
                this.monthlyData.putAll(data);
            }
            notifyDataSetChanged(); // should cal: getItemPosition(Object obj)
        }



    }

    private LoaderManager.LoaderCallbacks<LinkedHashMap<String, MonthHashLoader.MonthBin>> loaderCallbacks =
            new LoaderManager.LoaderCallbacks<LinkedHashMap<String, MonthHashLoader.MonthBin>>(){

        @Override
        public Loader<LinkedHashMap<String, MonthHashLoader.MonthBin>> onCreateLoader(int id, Bundle args) {
            return new MonthHashLoader(getActivity());
        }

        @Override
        public void onLoadFinished(Loader<LinkedHashMap<String, MonthHashLoader.MonthBin>> loader, LinkedHashMap<String, MonthHashLoader.MonthBin> data) {

            // TODO: Check which is more efficient:
            // a) filter out the years not needed here or
            // b) Upon initializing the AsyncTask of MonthHashLoader
            LinkedHashMap<String, MonthHashLoader.MonthBin> subData = new LinkedHashMap<>();
            for (String keyDate: data.keySet()) {
                if (keyDate.contains(yrChoice)) {
                    subData.put(keyDate, data.get(keyDate));
                }
            }
            mChartsAdapter.swapData(subData);
            // mChartsAdapter.swapData(data);
        }

        @Override
        public void onLoaderReset(Loader<LinkedHashMap<String, MonthHashLoader.MonthBin>> loader) {
            // Clear the data in the adapter.
            mChartsAdapter.swapData(null);
            // mChartsAdapter.notifyDataSetChanged(); // tried, did not update fragment
        }
    };

    private void chooseChart(MonthHashLoader.MonthBin itemBin, View listItemView){

        Context context = getContext();

        TextView title = (TextView) listItemView.findViewById(R.id.card_month_title);
        TextView incomeTxtView = (TextView) listItemView.findViewById(R.id.card_income_amount);
        TextView expenseTxtView = (TextView) listItemView.findViewById(R.id.card_expense_amount);

        TextView outcomeTitle = (TextView) listItemView.findViewById(R.id.card_outcome_title);
        TextView outcomeTxtView = (TextView) listItemView.findViewById(R.id.card_outcome_amount);

/*        Float totalIncome = itemBin.getMonthCredits();
        float totInc = 0.0f;
        if (totalIncome != null){
            totInc = totalIncome.floatValue();
        }
        Float totalExpenses = itemBin.getMonthDebits();
        float totExp = 0.0f;
        if (totalExpenses != null) {
            totExp = totalExpenses.floatValue();
        }*/

        /**
         * TODO: Need to define if savings will only be treated as account, not mixed with CREDIT or DEBIT
         * Now just need total monthly: credit(income) and debit(expenses). The itembin should yield the monthly summary
         * */
        Float savings = itemBin.getMonthSavings();
        Float credits = itemBin.getMonthCredits();
        Float debits = itemBin.getMonthDebits();
        Float overdraft = debits - credits; // Want to show negative

        Log.d(LOG_TAG, "Savings: " + savings);
        Log.d(LOG_TAG, "Credits: " + credits);
        Log.d(LOG_TAG, "Debits: " + debits);
        Log.d(LOG_TAG, "Overdraft: " + overdraft);
/*
        Float totalSavings = itemBin.getMonthSavings();
        float totSav = 0.0f;
        if (totalSavings != null) {
            totSav = totalSavings.floatValue();
            // getting a null pointer exception
        }

        float balance = totInc - totExp - totSav;
        float debit = totExp + totSav;
*/
        title.setText(itemBin.getName());
        incomeTxtView.setText(String.valueOf(credits));
        expenseTxtView.setText(String.valueOf(debits));

        LinearLayout myLayout = (LinearLayout) listItemView.findViewById(R.id.chart_parent);

        if (overdraft > 0.0f){
            horizontalBarChart = new HorizontalBarChart(getActivity());
            horizontalBarChart.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT));
            myLayout.addView(horizontalBarChart);

            outcomeTitle.setText(" Overdraft:");
            outcomeTxtView.setText(String.valueOf(overdraft));
            outcomeTxtView.setTextColor(ContextCompat.getColor(context, R.color.darkred));

            drawHorizontalBarChart(context, credits, debits, overdraft); // balance == overdraft

            // TextView overdraftTxtTitle = (TextView) listItemView.findViewById(R.id.card_income_title);
            // creditTxtTitle.setText(" Overdraft:");
        } else {
            pieChart = new PieChart(getActivity());
            pieChart.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT));
            myLayout.addView(pieChart);

            outcomeTitle.setText(" Savings:");
            outcomeTxtView.setText(String.valueOf(savings));
            outcomeTxtView.setTextColor(ContextCompat.getColor(context, R.color.darkblue));

            // pieChart = (PieChart) listItemView.findViewById(R.id.pie);
            drawPieChart(context, credits, debits, savings);
        }
    }

    private void drawHorizontalBarChart(Context context, Float credits, Float debits, Float overdraft) {

        // Put in Dictionary total monthly amounts by type:
//        Map<String, Float> amountsByType = itemBin.getAmountsByType();

        colorArray = new int[]{
                ContextCompat.getColor(context, R.color.darkred),
                ContextCompat.getColor(context, R.color.orange),
                ContextCompat.getColor(context, R.color.darkgreen)};

        // List<BarEntry>, "Cells"
        ArrayList<BarEntry> yEntries = new ArrayList<>();
        yEntries.add(new BarEntry(2, 0.0001f));  // Credit
        yEntries.add(new BarEntry(1, 0.0001f));  // Debit
        yEntries.add(new BarEntry(0, 0.0001f));  // Overdraft

        yEntries.set(2, (new BarEntry(2, credits)));
        yEntries.set(1, (new BarEntry(1, debits)));
        yEntries.set(0, (new BarEntry(0, overdraft)));

        // Fill-in yEntries
        /**
        for (Map.Entry<String, Float> entry : amountsByType.entrySet()) {

            // TODO: Displayed is income, want credit, or remaining
            // Objective, see by how much we have exceeded monthly income
            // and thus savings go down!!
            String key = entry.getKey(); // credit, saving or debit
            float balance = 0.0001f;
            if (key == "Income") {
                if (entry.getValue() != null) {
                    float value = entry.getValue();
                    balance += value;
                    BarEntry barEntry = new BarEntry(value, 0);
                    yEntries.set(0, (new BarEntry(0, value)));
                    Log.d(LOG_TAG, "Bar entry : " + barEntry);
                } else {
                    float value = 0.0001f;
                    yEntries.set(0, (new BarEntry(0, value)));
                }
            } else if (key == "Expense") {
                if (entry.getValue() != null) {
                    float value = entry.getValue();
                    balance -= value;
                    yEntries.set(1, (new BarEntry(1, value)));
                } else {
                    float value = 0.0001f;
                    yEntries.set(1, (new BarEntry(1, value)));
                }
            } else { // Saving
                if (entry.getValue() != null) {
                    float value = entry.getValue();
                    yEntries.set(2, (new BarEntry(2, value)));
                } else {
                    float value = 0.0001f;
                    yEntries.set(2, (new BarEntry(2, value)));
                }
            }
            yEntries.set(3, (new BarEntry(3, balance)));


        }
         */

        BarDataSet dataSet = new BarDataSet(yEntries, "Data Set");
        // dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        // dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setColors(colorArray);
        // dataSet.setDrawValues(true);//give values at the top of bar

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(dataSet);

        BarData data = new BarData(dataSets);

        data.setValueTextSize(10f);
        data.setBarWidth(0.9f);

        YAxis leftAxis = horizontalBarChart.getAxisLeft();
        leftAxis.setLabelCount(3, false);
        // leftAxis.setAxisMinimum(0f);
        //leftAxis.setDrawGridLines(false);
        leftAxis.setValueFormatter(new LargeValueFormatter()); // 1000 -> 1K

        YAxis rightAxis = horizontalBarChart.getAxisRight();
        rightAxis.setAxisMinimum(0f);
        rightAxis.setLabelCount(3, false);
        rightAxis.setDrawGridLines(false);
        rightAxis.setDrawLabels(false);
        rightAxis.setValueFormatter(new LargeValueFormatter()); // 1000 -> 1K

        XAxis xAxis = horizontalBarChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        // xAxis.setPosition(null);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f); // only intervals of 1 (array indices)
        xAxis.setDrawLabels(false);
        // xAxis.setXOffset(10f); // moves X-axis "into" chart area

        Legend l = horizontalBarChart.getLegend();
        l.setDrawInside(false);
        // horizontalBarChart.setDrawValueAboveBar(false);
        // horizontalBarChart.setExtraRightOffset(20f); // if y < 100
        //
        leftAxis.setTextSize(2f);
        horizontalBarChart.setExtraRightOffset(40f); // if y > 10000
        horizontalBarChart.getLegend().setEnabled(false);
        horizontalBarChart.setDescription(null);    // Hide the description
        horizontalBarChart.setData(data); // set the data and list of labels into chart

        // Styling
        // https://github.com/PhilJay/MPAndroidChart/blob/master/MPChartExample/src/com/xxmassdeveloper/mpchartexample/BarChartActivity.java


    }

    private void drawPieChart(Context context, Float credits, Float debits, Float savings) {

        // Put in Dictionary total monthly amounts by type:
//        Map<String, Float> amountsByType = itemBin.getAmountsByType();

        colorArray = new int[]{
                ContextCompat.getColor(context, R.color.darkgreen),
                ContextCompat.getColor(context, R.color.orange),
                ContextCompat.getColor(context, R.color.darkblue)};


        // ArrayList<PieEntry> yData = new ArrayList<>(); // Expense and Saving Amounts
        float[] yData = {credits, debits, savings};
        ArrayList<String> xData = new ArrayList<>(); // Type Strings
        xData.add("Credit");
        xData.add("Debit");
        xData.add("Saving");

        // iterate over the key:value set
        // and add the x,y data for the PieChart
        /**
         *
         *
        for (Map.Entry<String, Float> entry : amountsByType.entrySet()) {
            String key = entry.getKey(); // should be a type
            if (key == "Expense") {
                if (entry.getValue() != null && totInc > 0){
                    float value = (entry.getValue()/totInc)*100;
                    // Log.d(LOG_TAG, "onValue value, " + value);
                    // Log.d(LOG_TAG, "onValue total expenses, " + totInc);
                    yData[1] = value; // Index[2] = Expense as %

                    // yData.add(2, new PieEntry(value));
                } else {
                    float value = 0.0f;
                    yData[1] = value;
                    // yData.add(2, new PieEntry(value));
                }
            } else if (key == "Saving") {
                if (entry.getValue() != null && totInc > 0){
                    float value = (entry.getValue()/totInc)*100;
                    // Log.d(LOG_TAG, "onValue value, " + value);
                    // Log.d(LOG_TAG, "onValue total expenses, " + totInc);
                    // yData.add(1, new PieEntry(value));
                    yData[2] = value;
                } else {
                    float value = 0.0f;
                    // yData.add(1, new PieEntry(value));
                    yData[2] = value;
                }
            } else { // == Income
                float leftOverIncome = entry.getValue().floatValue() - totExp - totSav;
                if (leftOverIncome >= 0){
                    float value = (leftOverIncome/totInc)*100;
                    yData[0] = value;
                } else {
                    yData[0] = 0.0f;
                }

                // Log.d(LOG_TAG, "onValue income prct: " + value);
                // Log.d(LOG_TAG, "onValue left over income: " + leftOverIncome);
                // yData.add(0, new PieEntry(value));

            }

        }
         */
        // Convert to chart object: PieEntry
        final String[] typesList = xData.toArray(new String[xData.size()]);
        ArrayList<PieEntry> yEntries = new ArrayList<>();

        for (int i = 0; i < yData.length; i++){
            yEntries.add(new PieEntry(yData[i]));
            // Log.d(LOG_TAG, "PieEntry value, " + yData[i]);
        }
        final ArrayList<Float> valuesList = new ArrayList<>();
        for (PieEntry object : yEntries){
            valuesList.add(object.getValue());
            // Log.d(LOG_TAG, "PieEntry value, " + object.getValue());
        }


        // Draw your horizontalBarChart here
        pieChart.setDescription(null);
        // horizontalBarChart.setHoleRadius(20.0f);
        pieChart.setTransparentCircleAlpha(0);
        // horizontalBarChart.setCenterText("Super cool horizontalBarChart");
        // horizontalBarChart.setCenterTextSize(10);
        pieChart.setDrawEntryLabels(false);
        pieChart.setUsePercentValues(true); // May need to use ValueFormatter class
        // horizontalBarChart.setExtraLeftOffset(15);
        pieChart.setExtraTopOffset(4);
        pieChart.setExtraBottomOffset(-14);
        // horizontalBarChart.setExtraBottomOffset(2);

        // create data set
        PieDataSet pieDataSet = new PieDataSet(yEntries, null);
        // new PieDataSet (yData, "Income Distribution");

        pieDataSet.setSliceSpace(2);
        // pieDataSet.isDrawIconsEnabled();
        // pieDataSet.isDrawValuesEnabled();
        // pieDataSet.setValueTextSize(12);

        // add colors to dataset
        pieDataSet.setColors(colorArray);

        // add legend
        Legend legend = pieChart.getLegend();
        legend.setForm(Legend.LegendForm.NONE);
        // legend.setForm(Legend.LegendForm.SQUARE);
        // legend.setPosition(Legend.LegendPosition.LEFT_OF_CHART);
        // legend.setExtra(colors, xData);

        // create pie data object
        PieData pieData = new PieData(pieDataSet);
        pieChart.notifyDataSetChanged();
        pieChart.setData(pieData);
        pieChart.invalidate(); // shows pie horizontalBarChart

        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                // Log.d(LOG_TAG, "onValueSelected e, " + e.toString());
                // Log.d(LOG_TAG, "onValueSelected h, " + h.toString());

                // Use a substring, from "e" is (see Logcat)
                int pos1 = e.toString().indexOf("y: ");
                Log.d(LOG_TAG, "pos1: " + pos1);
                String prct = e.toString().substring(pos1 + 2);
                Log.d(LOG_TAG, "yValue: " + prct);

                for(int i = 0; i < valuesList.size(); i++){
                    try {
                        if(valuesList.get(i) == Float.parseFloat(prct)){
                            pos1 = i;
                            break;
                        }
                    }
                    catch (NumberFormatException err){
                        // Log.d(LOG_TAG, "error: " + err);
                    }
                }
                String typeString = typesList[pos1];
                // Log.d(LOG_TAG, "Type selected, " + typeString.toString());
                if (typeString == null || typeString.isEmpty()){
                    typeString = "No Type.";
                }
                Toast.makeText(getContext(), "Type: " + typeString + "\n"
                                + "Expense: " + prct + "%"
                        , Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected() {

            }
        });
    }


}



