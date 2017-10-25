package com.daytightchunks.wealthyhabits.services;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.daytightchunks.wealthyhabits.data.TransContract;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;



/**
 * Created by DayTightChunks on 26/09/2017.
 */

public class TransactionService extends JobService {

    private BackgroundTask mBackgroundTask;
    // private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.GERMANY);
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    /** Tag for the log messages */
    public static final String LOG_TAG = TransactionService.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "Service created");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "Service destroyed");
    }

    @Override
    public boolean onStartJob(final JobParameters params) {
        // return false if method is very simple (runs on main thread)
        // return true if an async task needs to be launched but
        // remember to call jobFinished() once task is finished,
        // from new job thread otherwise memory leak!
        String dateOfEntry = params.getExtras().getString("original_date");
        long repeatOn = params.getExtras().getLong("periodic");
        boolean condition = true;
        if (condition) {
            mBackgroundTask = new BackgroundTask(dateOfEntry, repeatOn){
                @Override
                protected void onPostExecute(String string){
                    Toast.makeText(getApplicationContext(), string, Toast.LENGTH_SHORT).show();
                    jobFinished(params, false); // return true if need to re-schedule due to failure
                }
            };

            mBackgroundTask.execute();
        } else {
            jobFinished(params, true);
        }
        return true;

    }

    @Override
    public boolean onStopJob(JobParameters params) {
        /* System calls this method if background job is cancelled before finishing
        * i.e. if job parameters are no longer available
        *
        * - You can clear the unfinished job resources here
        * - Return true from this method if you want to reschedule the same job again */
        mBackgroundTask.cancel(true);
        return false;
    }

    public class BackgroundTask extends AsyncTask<Void, Void, String> {

        private String dateOfEntry;
        private long repeatFreq;

        private BackgroundTask(String date, long periodic){
            dateOfEntry = date;
            repeatFreq = periodic;
        }

        @Override
        protected String doInBackground(Void... params) {

            String[] projection = {
                    TransContract.TransEntry._ID,
                    TransContract.TransEntry.COLUMN_AMOUNT,
                    TransContract.TransEntry.COLUMN_TYPE,
                    TransContract.TransEntry.COLUMN_ACCOUNT_FROM,
                    TransContract.TransEntry.COLUMN_ACCOUNT_TO,
                    TransContract.TransEntry.COLUMN_DATE,
                    TransContract.TransEntry.COLUMN_CATEGORY,
                    TransContract.TransEntry.COLUMN_TITLE,
                    TransContract.TransEntry.COLUMN_RECURRING,
                    TransContract.TransEntry.COLUMN_FREQUENCY,
                    TransContract.TransEntry.COLUMN_NEXT_DATE,
                    TransContract.TransEntry.COLUMN_END_DATE
            };

            String selection = "(" + TransContract.TransEntry.COLUMN_RECURRING + " = " +
                    TransContract.TransEntry.RECURRING + ")";

            // Query for all recurring dates
            Cursor cursor = getContentResolver().query(TransContract.TransEntry.CONTENT_URI,
                    projection,
                    selection,
                    null,
                    null);

            int idColumnIndex = cursor.getColumnIndex(TransContract.TransEntry._ID);
            int amountColumnIndex = cursor.getColumnIndex(TransContract.TransEntry.COLUMN_AMOUNT);
            int typeColumnsIndex = cursor.getColumnIndex(TransContract.TransEntry.COLUMN_TYPE);
            int fromAccountColumnsIndex = cursor.getColumnIndex(TransContract.TransEntry.COLUMN_ACCOUNT_FROM);
            int toAccountColumnsIndex = cursor.getColumnIndex(TransContract.TransEntry.COLUMN_ACCOUNT_TO);
            int dateColumnsIndex = cursor.getColumnIndex(TransContract.TransEntry.COLUMN_DATE);
            int categoryColumnsIndex = cursor.getColumnIndex(TransContract.TransEntry.COLUMN_CATEGORY);
            int titleColumnsIndex = cursor.getColumnIndex(TransContract.TransEntry.COLUMN_TITLE);
            int recurringColumnsIndex = cursor.getColumnIndex(TransContract.TransEntry.COLUMN_RECURRING);
            int freqColumnsIndex = cursor.getColumnIndex(TransContract.TransEntry.COLUMN_FREQUENCY);
            int nextDateColumnsIndex = cursor.getColumnIndex(TransContract.TransEntry.COLUMN_NEXT_DATE);
            int endDateColumnsIndex = cursor.getColumnIndex(TransContract.TransEntry.COLUMN_END_DATE);

            int listedID;
            Float listedAmount;
            int listedType;
            long listedDate;
            int mFromAccount;
            int mToAccount;
            String listedCategory;
            String listedTitle;
            int listedRecurring;
            String listedFreq;
            long listedNextDate;
            long listedEndDate;

            // Joda-Time, milliseconds
            DateTimeZone timeZone = DateTimeZone.forID("Europe/Berlin") ;
            DateTime now = DateTime.now(timeZone);
            DateTime todayStart = now.withTimeAtStartOfDay();
            DateTime tomorrowStart = now.plusDays(1).withTimeAtStartOfDay();

            long today = todayStart.getMillis();
            long tomorrow = tomorrowStart.getMillis();

            // Check all entries for recurring
            if (cursor.moveToFirst()){
                try {
                    do {
                        listedRecurring = cursor.getInt(recurringColumnsIndex);
                        if (listedRecurring == TransContract.TransEntry.RECURRING){

                            listedNextDate = cursor.getLong(nextDateColumnsIndex);
                            if (listedNextDate >= today &&
                                    listedNextDate < tomorrow){

                                // Create values for new entry
                                listedAmount = cursor.getFloat(amountColumnIndex);
                                listedType = cursor.getInt(typeColumnsIndex);
                                listedCategory = cursor.getString(categoryColumnsIndex);
                                listedTitle = cursor.getString(titleColumnsIndex);
                                mFromAccount = cursor.getInt(fromAccountColumnsIndex);
                                mToAccount = cursor.getInt(toAccountColumnsIndex);
                                listedFreq = cursor.getString(freqColumnsIndex);
                                listedEndDate =  cursor.getLong(endDateColumnsIndex);

                                // Update new:
                                //      entry date long,
                                //      new recurring int
                                //      next date long
                                listedDate = today;
                                long chosenFrequency = 0;
                                boolean SHORT_FREQ = true;
                                String stringNextExactDate = "";
                                long approximation = 1L;
                                switch (listedFreq) {
                                    case "every_day":
                                        chosenFrequency = 1 * 24 * 60 * 60 * 1000L;
                                        break;
                                    case "every_week":
                                        chosenFrequency = 7 * 24 * 60 * 60 * 1000L;
                                        break;
                                    case "every_two_weeks":
                                        chosenFrequency = 14 * 24 * 60 * 60 * 1000L;
                                        break;
                                    case "every_three_weeks":
                                        chosenFrequency = 21 * 24 * 60 * 60 * 1000L;
                                        break;
                                    case "every_month":
                                        SHORT_FREQ = false;
                                        stringNextExactDate = getExactStringDate(1, today);
                                        approximation = today + 1 * 365/12 * 24 * 60 * 60 * 1000L;
                                        break;
                                    case "every_two_months":
                                        SHORT_FREQ = false;
                                        stringNextExactDate = getExactStringDate(2, today);
                                        approximation = today + 2 * 365/12 * 24 * 60 * 60 * 1000L;
                                        break;
                                    case "every_three_months":
                                        SHORT_FREQ = false;
                                        stringNextExactDate = getExactStringDate(3, today);;
                                        approximation = today + 3 * 365/12 * 24 * 60 * 60 * 1000L;
                                        break;
                                    case "every_six_months":
                                        SHORT_FREQ = false;
                                        stringNextExactDate = getExactStringDate(6, today);
                                        approximation = today + 6 * 365/12 * 24 * 60 * 60 * 1000L;
                                        break;
                                    case "every_year":
                                        SHORT_FREQ = false;
                                        stringNextExactDate = getExactStringDate(12, today);
                                        approximation = today + 365 * 24 * 60 * 60 * 1000L;
                                        break;
                                    default:
                                        break;
                                }

                                if (SHORT_FREQ){
                                    listedNextDate = today + chosenFrequency;
                                } else {
                                    try {
                                        Date date = dateFormat.parse(stringNextExactDate);
                                        listedNextDate = date.getTime();
                                    } catch (ParseException e){
                                        listedNextDate = approximation;
                                        e.printStackTrace();
                                    }
                                }

                                ContentValues newValues = new ContentValues();
                                newValues.put(TransContract.TransEntry.COLUMN_AMOUNT, listedAmount);
                                newValues.put(TransContract.TransEntry.COLUMN_DATE, listedDate);
                                newValues.put(TransContract.TransEntry.COLUMN_TYPE, listedType);
                                newValues.put(TransContract.TransEntry.COLUMN_CATEGORY, listedCategory);
                                newValues.put(TransContract.TransEntry.COLUMN_TITLE, listedTitle);
                                newValues.put(TransContract.TransEntry.COLUMN_ACCOUNT_FROM, mFromAccount);
                                newValues.put(TransContract.TransEntry.COLUMN_ACCOUNT_TO, mToAccount);
                                newValues.put(TransContract.TransEntry.COLUMN_END_DATE, listedEndDate);

                                if (listedEndDate > listedNextDate){
                                    newValues.put(TransContract.TransEntry.COLUMN_RECURRING, TransContract.TransEntry.RECURRING);
                                    newValues.put(TransContract.TransEntry.COLUMN_NEXT_DATE, listedNextDate);
                                } else {
                                    newValues.put(TransContract.TransEntry.COLUMN_RECURRING, TransContract.TransEntry.NOT_RECURRING);
                                }
                                // Add to database
                                Uri newUri = getContentResolver().insert(TransContract.TransEntry.CONTENT_URI, newValues);

                                // Update current entry's recurring to non-recurring
                                listedID = cursor.getInt(idColumnIndex);
                                Uri currentTransUri = ContentUris.withAppendedId(TransContract.TransEntry.CONTENT_URI, listedID );
                                ContentValues oldValues = new ContentValues();
                                oldValues.put(TransContract.TransEntry.COLUMN_RECURRING, TransContract.TransEntry.NOT_RECURRING);
                                int nRowsUpdated = getContentResolver().update(
                                        currentTransUri,
                                        oldValues,
                                        null,
                                        null);
                            }
                        }
                    } while (cursor.moveToNext());
                } finally {
                    // Notification NEEDED
                    cursor.close();
                }
            }
/*
            int accountColumnsIndex = cursor.getColumnIndex(TransContract.TransEntry.COLUMN_ACCOUNT);
            int accountFromIndex = cursor.getColumnIndex(TransContract.TransEntry.COLUMN_ACCOUNT_FROM);
            int accountToIndex = cursor.getColumnIndex(TransContract.TransEntry.COLUMN_ACCOUNT_TO);
*/

            return "Long running task finished with newUri: "; // + newUri.toString();
        }

        protected void onPostExecute(String s){

        }

        private String getExactStringDate(int months, long today){
            // SimpleDateFormat
            String todayDateString = dateFormat.format(new Date(today));

            /* MasterList Data */
            String[] arrayDayYrMo = todayDateString.split("/"); // Will store the split date
            String mDay = arrayDayYrMo[0]; // position 0, should be "dd"
            String mMo = arrayDayYrMo[1]; // position 1, should be "MM"
            String mYear = arrayDayYrMo[2]; // mMoYear.split("/", 0)[1]; // position 1, should be "YYYY"
            // String mYear = mMoYear.split("/", 0)[1]; // position 1, should be "YYYY"

            Integer newMonth = Integer.valueOf(mMo) + months;
            if (newMonth > 12){
                newMonth = newMonth - 12;
            }

            String newDate = mDay + "/" + newMonth.toString() + "/" + mYear;

            return newDate;
        }

    }
}


