package com.daytightchunks.wealthyhabits.ui;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.AsyncTaskLoader;

import com.daytightchunks.wealthyhabits.data.TransContract;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;

/**
 * Objective:
 * Query the Transactions data,
 * Build a nested dictionary containing:
 * Month1: { TotalExpense: XXXX, TotalNeeds: XXXX, TotalWants: XXXX, TotalDesire: XXXX
 *           TotalIncome: XXXX, TotalSaved: XXXX, TotalInvested: XXXXX}
 *
 * Creates "MonthBin", a class that stores all
 * transactions in one month.
 */

public class MonthHashLoader extends AsyncTaskLoader<LinkedHashMap<String, MonthHashLoader.MonthBin>> {

    /** Tag for the log messages */
    public static final String LOG_TAG = MonthHashLoader.class.getSimpleName();

    LinkedHashMap<String, MonthBin> mLoadMap;

    public MonthHashLoader(Context context) {
        super(context);
    }

    /**
     * Handles a request to start the Loader.
     */
    @Override
    protected void onStartLoading(){
        if (mLoadMap != null) {
            // If we currently have a result available, deliver it
            // immediately.
            deliverResult(mLoadMap);
        } else {
            forceLoad(); // Forces the loadInBackground to be called
        }
    }

    /**
     * Handles a request to stop the Loader.
     */
    @Override
    protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    /**
     * Handles a request to cancel a load.
     */
    @Override
    public void onCanceled(LinkedHashMap<String, MonthBin> data) {
        super.onCanceled(data);

        // At this point we can release the resources associated with 'data'
        // if needed.
        onReleaseResources(data); // Does nothing (as it is no cursor)
    }

    /**
     * Handles a request to completely reset the Loader.
     */
    @Override
    protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();

        // At this point we can release the resources associated with 'apps'
        // if needed.
        if (mLoadMap != null) {
            onReleaseResources(mLoadMap); // Does nothing (as it is no cursor)
            mLoadMap = null;
        }
    }

    /**
     * Return date in specified format.
     * @return String representing date in specified format
     */

    // @RequiresApi(api = Build.VERSION_CODES.N) // Used for the "formatter.format"
    @Override
    public LinkedHashMap<String, MonthBin> loadInBackground() {

        // TODO: Make sure design is efficient for this method, which is tied to the
        // loaderCallback in ChartsFragment. Should we rather only query the relevant year here? or
        // Filter out the relevant year once loaderCallback has loaded the full MonthHasLoader.Bin ?
        ContentResolver resolver = getContext().getContentResolver();

        String[] projection = new String[] {
                TransContract.TransEntry._ID,
                TransContract.TransEntry.COLUMN_AMOUNT,
                TransContract.TransEntry.COLUMN_TYPE,
                TransContract.TransEntry.COLUMN_ACCOUNT_FROM,
                TransContract.TransEntry.COLUMN_ACCOUNT_TO,
                TransContract.TransEntry.COLUMN_CATEGORY, // e.g. shopping
                TransContract.TransEntry.COLUMN_DATE
        };

        Cursor cursor = resolver.query(TransContract.TransEntry.CONTENT_URI,
                        projection,
                        null,
                        null,
                        null);

        LinkedHashMap<String, MonthBin> monthlyMap = new LinkedHashMap<>();

        int listedType;
        long listedDate;
        String month;
        String dictDate;
        String listedCategory;
        Float listedAmount;
        int listedAccount;
        int accountFrom;
        int accountTo;
        SimpleDateFormat formatter;

        int amountColumnIndex = cursor.getColumnIndex(TransContract.TransEntry.COLUMN_AMOUNT);
        int typeColumnsIndex = cursor.getColumnIndex(TransContract.TransEntry.COLUMN_TYPE);
//        int accountColumnsIndex = cursor.getColumnIndex(TransContract.TransEntry.COLUMN_ACCOUNT);
        int categoryColumnsIndex = cursor.getColumnIndex(TransContract.TransEntry.COLUMN_CATEGORY);
        int dateColumnsIndex = cursor.getColumnIndex(TransContract.TransEntry.COLUMN_DATE);
        int accountFromIndex = cursor.getColumnIndex(TransContract.TransEntry.COLUMN_ACCOUNT_FROM);
        int accountToIndex = cursor.getColumnIndex(TransContract.TransEntry.COLUMN_ACCOUNT_TO);


        if (cursor.moveToFirst()) {
            try {
                do {
                    // Get data info
                    listedType = cursor.getInt(typeColumnsIndex); // credit, debit
//                    listedAccount = cursor.getInt(accountColumnsIndex);
                    //listedDate = cursor.getString(dateColumnsIndex);
                    listedDate = cursor.getLong(dateColumnsIndex); // as Unix time
                    listedCategory = cursor.getString(categoryColumnsIndex); // Shopping or rent...
                    listedAmount = cursor.getFloat(amountColumnIndex);
                    accountFrom = cursor.getInt(accountFromIndex);
                    accountTo = cursor.getInt(accountToIndex);

                    formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
                    String dateString = formatter.format(new Date(listedDate));


                    String[] parts = dateString.split("/", 2); // Will store the split date
                    // Log.d(LOG_TAG, "in cursor, token index 1 = " + parts);
                    month = parts[1]; // position 1, should be "mm/yyyy"
                    // tokens = listedDate.split(delims);
                    //Log.d(LOG_TAG, "in cursor, token index 1 = " + parts[1]);..

                    // Store all entries from database by Month-Year in a MonthlyMap
                    // Will find the type, then check if the MonthKey exists and add the amount to the
                    // previous value or create a new MonthKey if not existing
                    if (listedType == TransContract.TransEntry.TYPE_DEBIT) {
                        // MonthlyData
                        if (monthlyMap.containsKey(month)) {
                            MonthBin oldMonth = monthlyMap.get(month);

                            // Global tracker
                            oldMonth.addDebit(listedAmount);
                            // Category tracker
                            oldMonth.addCategoryExpense(listedCategory, listedAmount);
                            // Update accounts
                            if (accountFrom == TransContract.TransEntry.ACCOUNT_WALLET_10){
                                oldMonth.debitWalletAcc(accountFrom, listedAmount);
                            } else if (accountFrom == TransContract.TransEntry.ACCOUNT_SAVINGS_0){
                                oldMonth.debitSavingAcc(accountFrom, listedAmount);
                            }

                        } else {
                            MonthBin newMonth = new MonthBin(month);
                            monthlyMap.put(month, newMonth);
                            newMonth.addDebit(listedAmount);
                            newMonth.addCategoryExpense(listedCategory, listedAmount);
                            if (accountFrom == TransContract.TransEntry.ACCOUNT_WALLET_10){
                                newMonth.debitWalletAcc(accountFrom, listedAmount);
                            } else if (accountFrom == TransContract.TransEntry.ACCOUNT_SAVINGS_0) {
                                newMonth.debitSavingAcc(accountFrom, listedAmount);
                            }
                        }
                    } else if (listedType == TransContract.TransEntry.TYPE_CREDIT){

                        // MonthlyData
                        if (monthlyMap.containsKey(month)) {
                            MonthBin oldMonth = monthlyMap.get(month);

                            oldMonth.addCredit(listedAmount);
                            /** Still no byCategory Income, but could be good have */
                            if (accountTo == TransContract.TransEntry.ACCOUNT_WALLET_10){
                                oldMonth.creditWalletAcc(accountTo, listedAmount);
                            } else if (accountTo == TransContract.TransEntry.ACCOUNT_SAVINGS_0){
                                oldMonth.creditSavingAcc(accountTo, listedAmount);
                            }
                        } else {
                            MonthBin newMonth = new MonthBin(month);
                            monthlyMap.put(month, newMonth);
                            newMonth.addCredit(listedAmount);
                            if (accountTo == TransContract.TransEntry.ACCOUNT_WALLET_10){
                                newMonth.creditWalletAcc(accountTo, listedAmount);
                            } else if (accountTo == TransContract.TransEntry.ACCOUNT_SAVINGS_0){
                                newMonth.creditSavingAcc(accountTo, listedAmount);
                            }
                        }
                    } else {
                        throw new java.lang.RuntimeException("@MonthHashLoader, Credit or Debit types, any other?");
                    }

                } while (cursor.moveToNext());
            } finally {
                cursor.close();
            }
        }
        return monthlyMap;
    }

    @Override
    public void deliverResult(LinkedHashMap<String, MonthBin> data){
        if (isReset()){
            // An async query came in while the loader is stopped.  We
            // don't need the result.
            if (data != null){
                onReleaseResources(data); // Does nothing (as it is no cursor)
            }
        }
        LinkedHashMap<String, MonthBin> oldData = mLoadMap;
        mLoadMap = data;
        if (isStarted()) {
            // If the Loader is currently started, we can immediately
            // deliver its results.
            super.deliverResult(data);
        }

        // At this point we can release the resources associated with
        // 'oldApps' if needed; now that the new result is delivered we
        // know that it is no longer in use.
        if (oldData != null) {
            onReleaseResources(oldData); // Does nothing (as it is no cursor)
        }
    }


    /**
     * Helper function to take care of releasing resources associated
     * with an actively loaded data set.
     */
    private void onReleaseResources(LinkedHashMap<String, MonthBin> data) {
        // For a simple List<> there is nothing to do.  For something
        // like a Cursor, we would close it here.
    }


    public class MonthBin {
        private String binName;
        private String[] binCategories;
        private int binType;
        private Float binAmount;
        private Float allExpenses;

        // Global Trackers
        private Float debits;
        private Float credits;
        private Float savings;

        // Account Trackers
        private HashMap<Integer, Float> walletAcc;
        private HashMap<Integer, Float> savingAcc;

        // Category Trackers
        private HashMap<String, Float> byCategory;
        private HashMap<String, Float> byType;


        private Float expense; // Checking's
//        private Float saving; // Savings

        public MonthBin(String month){
            this.binName = month;
            this.byCategory = new HashMap<>();

            this.credits = 0.0f;
            this.debits = 0.0f;
            this.savings = 0.0f;

            this.walletAcc = new LinkedHashMap<>();
            this.savingAcc = new LinkedHashMap<>();

            walletAcc.put(TransContract.TransEntry.ACCOUNT_WALLET_10, 0.0f);
            savingAcc.put(TransContract.TransEntry.ACCOUNT_SAVINGS_0, 0.0f);
        }

        public String getName(){
            return this.binName;
        }


        public void addDebit(Float expense){
            if (this.debits == 0){
                this.debits = expense;
            } else {
                this.debits += expense;
            }
        }

        public void addCredit(Float income){
            if (this.credits == 0){
                this.credits = income;
            } else {
                this.credits += income;
            }
        }

        public void creditSavings(Float credit){
            this.savings += credit;
        }

        public void debitSavings(Float debit){
            this.savings -= debit;
        }

        public void creditSavingAcc(Integer accountID, Float amount){
            Float balance;
            // Iterate over all savingAcc accounts
            for (Integer key : savingAcc.keySet()){
                if (accountID == key){
                    balance = savingAcc.get(key);
                    balance += amount;
                    savingAcc.put(key, balance);
                } else {
                    throw new java.lang.RuntimeException("Savings account not found when adding amount");
                }
            }
        }

        public void debitSavingAcc(Integer accountID, Float amount){
            Float balance;
            // Iterate over all savingAcc accounts
            for (Integer key : savingAcc.keySet()){
                if (accountID == key){
                    balance = savingAcc.get(key);
                    balance -= amount;
                    savingAcc.put(key, balance);
                } else {
                    throw new java.lang.RuntimeException("Savings account not found when subtracting amount");
                }
            }
        }

        public void creditWalletAcc(Integer accountID, Float amount){
            Float balance;
            // Iterate over all savingAcc accounts
            for (Integer key : walletAcc.keySet()){
                if (accountID == key){
                    balance = walletAcc.get(key);
                    balance += amount;
                    walletAcc.put(key, balance);
                } else {
                    throw new java.lang.RuntimeException("Wallet account not found when adding amount");
                }
            }
        }

        public void debitWalletAcc(Integer accountID, Float amount){
            Float balance;
            // Iterate over all savingAcc accounts
            for (Integer key : walletAcc.keySet()){
                if (accountID == key){
                    balance = walletAcc.get(key);
                    balance -= amount;
                    walletAcc.put(key, balance);
                } else {
                    throw new java.lang.RuntimeException("Wallet account not found when subtracting amount");
                }
            }
        }

        public void addCategoryExpense(String category, Float expense){
            if (this.byCategory.containsKey(category)){
                Float oldExpense = this.byCategory.get(category);
                oldExpense += expense;
                this.byCategory.put(category, oldExpense);
            } else if (category == null || category.isEmpty()){
                category = "No category";
                this.byCategory.put(category, expense);
            } else {
                this.byCategory.put(category, expense);
            }

        }


        public HashMap<String, Float> getExpensesByCategory(){
            return this.byCategory;
        }

        public Float getMonthDebits(){ return this.debits; }
        public Float getMonthCredits(){
            return this.credits;
        }
        public Float getMonthSavings() { return this.savings; }

        // Get account by its ID
        public Float getWalletAcc(Integer ID){ return walletAcc.get(ID);}
        public Float getSavingAcc(Integer ID){ return savingAcc.get(ID);}
    }
}
