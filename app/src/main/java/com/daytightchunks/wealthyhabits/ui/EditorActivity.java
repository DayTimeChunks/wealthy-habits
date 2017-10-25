package com.daytightchunks.wealthyhabits.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.daytightchunks.wealthyhabits.R;
import com.daytightchunks.wealthyhabits.data.TransContract;
import com.daytightchunks.wealthyhabits.data.TransDbHelper;
import com.daytightchunks.wealthyhabits.services.TransactionService;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;

// import android.icu.util.Calendar; // For API 24 and above (current min is 16)

/**
 * Created by DayTightChunks on 08/02/2017.
 */

/**
 * Allows user to create a expense/income item or edit an existing one.
 */

public class EditorActivity extends AppCompatActivity
        implements
        TransactionFragment.OnButtonClickListener
//        CategoriesFragment.OnCategoryClickListener
{

//    TODO: onResume, has no saedInstanceState, we loose the data
//    if no category was picked.

    // Listener mCalltoMain;

    /** Tag for the log messages */
    public static final String LOG_TAG = EditorActivity.class.getSimpleName();

    /** Identifies a specific loader being used in this component */
    private static final int URL_LOADER_EDITOR = 1;

    private DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    private boolean isEmpty = false;
    private boolean mTransHasChanged = false;

    private Uri currentTransUri;

/*
    * EditText field to enter all input fields
    private EditText mCategoryEditText; // Should also be a spinner to avoid new activity?


    private RadioGroup mTypeRadioGroup;
    private RadioButton mTypeRadioIncome;
    private RadioButton mTypeRadioExpense;
    private RadioButton mTypeRadioTransfer;
    private LinearLayout mTransferContainer;

    private EditText mTitleEditText;
    private EditText mCalendarEditText; // Temporary as EditText.
    private EditText mAmountEditText;

    private CheckBox mRecurringCheckBox;
    private LinearLayout mRecurringDetails;
    private EditText mRecurringFrequencyEditText;
    private EditText mRecurringEndDateEditText;
*/
    private Float amountFloat;
    private int mType;
    public String listedCategory;
    private String listedTitle;
    private int mFromAccount; // 0 Wallet, 10 Savings
    private int mToAccount; // 0 Wallet, 10 Savings
    // private int mKind = TransContract.TransEntry.KIND_NONE;
    private int mRecurring = TransContract.TransEntry.NOT_RECURRING; // Default
    private String listedFreq;
    // private String listedNextDate;
    // private String listedEndDate;

    public int REQUEST_CODE = 1;

    // New strings come from FABs
    private String TRANSACTION = "transactions";
    private String TRANSFER = "transfer";
    private String origin;

    /** NEW
     * Database helper that will provide us access to the database */
    private TransDbHelper mDbHelper;

    private TransactionFragment transactionFragment;
    FragmentManager fragmentManager = getSupportFragmentManager();
    LinkedHashMap mEditorEntryFields;

    private static final String TAG_RETAINED_FRAGMENT = "RetainedFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Use getIntent() or getData to get the associated URI
        Intent extra = getIntent();
        currentTransUri = extra.getData();
        origin = extra.getStringExtra("origin");

        // Implements the retainedInstance of the fragment upon rotation (setRetainInstance(true))
        transactionFragment = (TransactionFragment) fragmentManager.findFragmentById(R.id.editor_fragment_container);
        if (transactionFragment == null){
            transactionFragment =  TransactionFragment.newInstance(currentTransUri, origin);
            fragmentManager.beginTransaction()
                    .add(R.id.editor_fragment_container, transactionFragment)
                    .commit();
        }

        if (origin.equals(TRANSACTION)){
            // New Transaction
            if (currentTransUri == null) {
                setTitle(R.string.title_editor_add_transaction);
            } else {
                setTitle(R.string.title_editor_edit);
            }
        } else if (origin.equals(TRANSFER)){ // (Green + button)
            // New Transfer
            if (currentTransUri == null) {
                setTitle(R.string.title_editor_add_transfer);
            } else {
                setTitle(R.string.title_editor_edit_transfer);
            }
        }

        // To access our database, we instantiate our subclass of SQLiteOpenHelper
        // and pass the context, which is the current activity.
        mDbHelper = new TransDbHelper(this);

    }

/*    private void setEndDatePicker() {
        if (mRecurringEndDateEditText != null) {
            mRecurringEndDateEditText.setOnClickListener(new MyEditTextDatePicker(this, R.id.edit_end_date));
        }
    }*/

    @Override
    public void onButtonSelected(int id, LinkedHashMap mEntryFields) {

        switch (id){
            case R.id.save_button:
                // mEditorEntryFields = transactionFragment.getEntries();
                mEditorEntryFields = mEntryFields;
                fragmentManager.beginTransaction()
                        .remove(fragmentManager.findFragmentById(R.id.editor_fragment_container))
                        .commit();

                saveTrans();
                Intent returnIntent = new Intent();
                returnIntent.putExtra("result", 1);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
                break;
            case R.id.cancel_button:
                fragmentManager.beginTransaction()
                        .remove(fragmentManager.findFragmentById(R.id.editor_fragment_container))
                        .commit();
                finish();
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new pet, hide the "Delete" menu item.
        if (currentTransUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    /** Top menu back button */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            /*
            case R.id.action_save:
                saveTrans();
                // mCalltoMain.onSomeEvent(); // use of interface method
                // Attempting to communicate with MainActivity,
                // eventually to notifyDataSetChanged() on the mChartsAdapter
                Intent returnIntent = new Intent();
                returnIntent.putExtra("result", 1);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
                return true;
                */
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                mTransHasChanged = transactionFragment.getMotion();
                if (mTransHasChanged) {
                    // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                    // Create a click listener to handle the user confirming that
                    // changes should be discarded.
                    DialogInterface.OnClickListener discardButtonClickListener =
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // User clicked "Discard" button, navigate to parent activity.
                                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                                }
                            };
                    // Show a dialog that notifies the user they have unsaved changes
                    showUnsavedChangesDialog(discardButtonClickListener);
                    return true;
                } else if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
                    return true;
                } else {
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }
        }
        return super.onOptionsItemSelected(item);
    }

    /** Button (Hardware) Back Button */
    @Override
    public void onBackPressed() {
        if (mTransHasChanged) {
            Log.d(LOG_TAG, "X0000 mTransHasChanged: " + mTransHasChanged);
            // Otherwise if there are unsaved changes, setup a dialog to warn the user.
            // Create a click listener to handle the user confirming that changes should be discarded.
            DialogInterface.OnClickListener discardButtonClickListener =
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // User clicked "Discard" button, close the current activity, with no changes.
                            Intent returnIntent = new Intent();
                            setResult(Activity.RESULT_CANCELED, returnIntent);
                            finish();
                        }
                    };
            // Show dialog that there are unsaved changes
            showUnsavedChangesDialog(discardButtonClickListener);

        } else if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            Log.d(LOG_TAG, "X0000 Super pressed");
            // super.onBackPressed();
            // return;
            NavUtils.navigateUpFromSameTask(this);
            return;
        }
    }

/*
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mTransHasChanged = true;
            return false;
        }
    };


    private View.OnClickListener myCategoryPicker = new View.OnClickListener(){

        @Override
        public void onClick(View view) {
            Intent categoryIntent = new Intent(EditorActivity.this, CategoriesActivity.class);
            // Gets info back when category is chosen.
            // Lifecycle's callback: onActivityResult()
            startActivityForResult(categoryIntent, REQUEST_CODE);
            // Log.d(LOG_TAG, "Started Categories Activity");
        }
    };
*/
/*

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // super.onActivityResult(requestCode, resultCode, data);
        // Check which request we're responding to
        if (requestCode == REQUEST_CODE){
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // do something with result.
                mCategoryEditText.setText(data.getStringExtra("some_key"));
            }
        }
    }
*/

    /** Date picker method */


    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
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

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteTransaction();
                Intent returnIntent = new Intent();
                returnIntent.putExtra("result", 1);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
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




    // Used in MyExpandableListAdapter
    public void onClickCalled(String categoriesItemSelected) {
        // Returns to EditorActivity from CategoriesActivity
        // Fills in the new category
        listedCategory = categoriesItemSelected;
        fragmentManager.popBackStack(); // Note!! :
        // pop' resumes a transaction(), the
        // frag itself needs onResume() to update any data
        transactionFragment.setNewCategory(listedCategory);
    }


/*    public void onRadioButtonClicked(View view){
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();
        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radio_debit:
                if (checked)
                mType = TransContract.TransEntry.TYPE_DEBIT;
                break;
            case R.id.radio_credit:
                if (checked)
                mType = TransContract.TransEntry.TYPE_CREDIT;
                break;
        }
    }

    public void onCheckboxClicked(View view) {
        // Is view checked?
        boolean checked = ((CheckBox) view).isChecked();
        // Check which checkbox was checked
        switch (view.getId()){
            case R.id.checkbox_recurring:
                if (checked){
                    mRecurring = TransContract.TransEntry.RECURRING;
                    mRecurringDetails.setVisibility(View.VISIBLE);
                } else {
                    mRecurring = TransContract.TransEntry.NOT_RECURRING;
                    mRecurringDetails.setVisibility(View.INVISIBLE);
                }
                break;

        }
    }*/

    private void saveTrans() {

        // Log.d(LOG_TAG, "Entering saveTans()");
        /** Get all information from the UI
         * */
        amountFloat = Float.valueOf(mEditorEntryFields.get("listedAmount").toString().trim());

        mType = (int) mEditorEntryFields.get("mType");
        // Show correct sign for respective type EXPENSE/INCOME
        /*
        if (mType == TransContract.TransEntry.TYPE_DEBIT
                && amountFloat > 0) {
            amountFloat = (amountFloat * -1);
        } else if (mType == TransContract.TransEntry.TYPE_CREDIT
                && amountFloat < 0) {
            amountFloat = (amountFloat * -1);
        }*/
        // Store as positive, sign is implicit in mType.
        if (amountFloat < 0){
            amountFloat = amountFloat * -1;
        }

        mFromAccount = (int) mEditorEntryFields.get("mFromAccount");
        mToAccount = (int) mEditorEntryFields.get("mToAccount");

        // Joda-Time, milliseconds
        DateTimeZone timeZone = DateTimeZone.forID("Europe/Berlin") ;
        DateTime now = DateTime.now(timeZone);
        DateTime todayStart = now.withTimeAtStartOfDay();
        DateTime tomorrowStart = now.plusDays(1).withTimeAtStartOfDay();

        long today = todayStart.getMillis();
        long tomorrow = tomorrowStart.getMillis();

        // Convert dateString back to long for storage
        // long currentTime = System.currentTimeMillis();
        long dateOutput;
        String dateString = (String) mEditorEntryFields.get("stringDate");
        if (TextUtils.isEmpty(dateString)){
            dateOutput = today;
        } else {
            /** Convert string date to unix integer */
            try {
                Date date = dateFormat.parse(dateString);
                // Log.d(LOG_TAG, "Date not empty is: " + date);
                //long output = date.getTime()/1000L;
                dateOutput = date.getTime();
            } catch (ParseException e) {
                dateOutput = today;
                Log.d(LOG_TAG, "Editor activity, " +
                        "dateString from TransactionFragment " +
                        "was empty, long dateOutput is now: " + dateOutput);
                e.printStackTrace();
            }
        }

        listedCategory = (String) mEditorEntryFields.get("listedCategory");
        listedTitle = (String) mEditorEntryFields.get("listedTitle");

        mRecurring = (int) mEditorEntryFields.get("recurring");
        listedFreq = (String) mEditorEntryFields.get("listedFreq");

        // Define next recurring date
        long chosenFrequency = 0;
        boolean SHORT_FREQ = true;
        String stringNextExactDate = "";
        long approximation = 1L;
        long nextDateOutput;

        Log.d(LOG_TAG, "listedFreq: " + listedFreq);

        if (listedFreq.equals(getString(R.string.every_day))) {
            chosenFrequency = 1 * 24 * 60 * 60 * 1000L;
        } else if (listedFreq.equals(getString(R.string.every_week))){
            chosenFrequency = 7 * 24 * 60 * 60 * 1000L;
        } else if (listedFreq.equals(getString(R.string.every_two_weeks))){
            chosenFrequency = 14 * 24 * 60 * 60 * 1000L;
        } else if (listedFreq.equals(getString(R.string.every_three_weeks))){
            chosenFrequency = 21 * 24 * 60 * 60 * 1000L;
        } else if (listedFreq.equals(getString(R.string.every_month))){
            SHORT_FREQ = false;
            stringNextExactDate = getExactStringDate(1, today);
            approximation = today + 1 * 365/12 * 24 * 60 * 60 * 1000L;
        } else if (listedFreq.equals(getString(R.string.every_two_months))){
            SHORT_FREQ = false;
            stringNextExactDate = getExactStringDate(2, today);
            approximation = today + 2 * 365/12 * 24 * 60 * 60 * 1000L;
        } else if (listedFreq.equals(getString(R.string.every_three_months))){
            SHORT_FREQ = false;
            stringNextExactDate = getExactStringDate(3, today);;
            approximation = today + 3 * 365/12 * 24 * 60 * 60 * 1000L;
        } else if (listedFreq.equals(getString(R.string.every_six_months))){
            SHORT_FREQ = false;
            stringNextExactDate = getExactStringDate(6, today);
            approximation = today + 6 * 365/12 * 24 * 60 * 60 * 1000L;
        } else if (listedFreq.equals(getString(R.string.every_year))){
            SHORT_FREQ = false;
            stringNextExactDate = getExactStringDate(12, today);
            approximation = today + 365 * 24 * 60 * 60 * 1000L;
        } else {
            Log.d(LOG_TAG, "frequency comparison worked");
        }

        if (SHORT_FREQ){
            nextDateOutput = today + chosenFrequency;
        } else {
            try {
                Date date = dateFormat.parse(stringNextExactDate);
                nextDateOutput = date.getTime();
            } catch (ParseException e){
                nextDateOutput = approximation;
                Toast.makeText(this, "saveTrans(), careful! Used approximation",
                        Toast.LENGTH_SHORT).show();
                Log.d(LOG_TAG, "saveTrans(), careful! Used approximation");
                e.printStackTrace();
            }
        }

        String stringEndDate = (String) mEditorEntryFields.get("stringEndDate");
        long endDateOutput;
        if (TextUtils.isEmpty(stringEndDate)){
            endDateOutput = today + (5 * 365 * 24 * 60 * 60 * 1000L); // Five years max
        } else {
            /** Convert string date to unix integer */
            try {
                Date date = dateFormat.parse(stringEndDate);
                endDateOutput = date.getTime();
                // Log.d(LOG_TAG, "Editor activity, date not empty long output: " + dateOutput);
            } catch (ParseException e) {
                endDateOutput = today + (5 * 365 * 24 * 60 * 60 * 1000L); // Five years max
                Toast.makeText(this, "saveTrans(), recurring end date did not parse properly",
                        Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }

/*
        if (origin.equals(TRANSACTION)){
            listedCategory = mCategoryEditText.getText().toString().trim();
            boolean incomeChecked = mTypeRadioIncome.isChecked();
            if (incomeChecked) {
                mType = TransContract.TransEntry.TYPE_CREDIT;
            } else {
                mType = TransContract.TransEntry.TYPE_DEBIT;
            }
        } else if (origin.equals(TRANSFER)){
            listedCategory = "Transfer";
            mFromAccount = mFromAccountSpinner.getSelectedItemPosition();
            mToAccount = mToAccountSpinner.getSelectedItemPosition();
        }

        boolean recurringChecked = mRecurringCheckBox.isChecked();
        if (recurringChecked) {
            mRecurring = TransContract.TransEntry.RECURRING;
        } else {
            mRecurring = TransContract.TransEntry.NOT_RECURRING;
        }
*/

        if ((TextUtils.isEmpty(listedTitle)) &&
                ( TextUtils.isEmpty(listedCategory) ) &&
                ( amountFloat == 0 ) &&
                ( TextUtils.isEmpty(dateString) ) ) {
            isEmpty = true;
        }

        /*if (TextUtils.isEmpty(dateString)){
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);


            mCalendarEditText.setText(new StringBuilder()
                    // Month is 0 based so add 1
                    .append(day).append("/").append(month + 1).append("/").append(year).append(" "));
            dateString = mCalendarEditText.getText().toString().trim();

            // DateFormat df = new SimpleDateFormat("dd/MM/yyyy");

            long todayDate = System.currentTimeMillis();
            // Log.d(LOG_TAG, "System Millis: " + todayDate);

            try {
                Date date = df.parse(dateString);
                Log.d(LOG_TAG, "Date  date: " + date);
                //long output = date.getTime()/1000L;
                dateOutput = date.getTime();
                Log.d(LOG_TAG, "Editor activity long  output: " + dateOutput);
            } catch (ParseException e) {
                e.printStackTrace();

            }

            //String str = Long.toString(output);
            //Log.d(LOG_TAG, "String  str: " + str);
            //long timestamp = Long.parseLong(str) * 1000L;
            //Log.d(LOG_TAG, "long  timestamp: " + timestamp);
        } else {
             // convert to unix
        }*/

        /** Pass all collected info onto a ContentValues object */
        // Create a ContentValues object where column names are the keys,
        // and entries attributes are the values.
        ContentValues values = new ContentValues();
        values.put(TransContract.TransEntry.COLUMN_AMOUNT, amountFloat);
        values.put(TransContract.TransEntry.COLUMN_TYPE, mType);
        values.put(TransContract.TransEntry.COLUMN_ACCOUNT_FROM, mFromAccount);
        values.put(TransContract.TransEntry.COLUMN_ACCOUNT_TO, mToAccount);
        values.put(TransContract.TransEntry.COLUMN_DATE, dateOutput);
        values.put(TransContract.TransEntry.COLUMN_CATEGORY, listedCategory);
        values.put(TransContract.TransEntry.COLUMN_TITLE, listedTitle);
        values.put(TransContract.TransEntry.COLUMN_RECURRING, mRecurring);
        if (mRecurring == TransContract.TransEntry.RECURRING) {
            values.put(TransContract.TransEntry.COLUMN_FREQUENCY, listedFreq);
            values.put(TransContract.TransEntry.COLUMN_NEXT_DATE, nextDateOutput);
            values.put(TransContract.TransEntry.COLUMN_END_DATE, endDateOutput);
        }

        /**
         * Perform the DataBase action, with CV object created
         *  */

        /** Insert */
        if (currentTransUri == null) {

            if (isEmpty) { this.finish(); }
            else {
                Uri newUri = this.getContentResolver().insert(TransContract.TransEntry.CONTENT_URI, values);
                if (newUri == null) {
                    // If the new content URI is null, then there was an error with insertion.
                    Toast.makeText(this, getString(R.string.editor_insert_trans_failed),
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the insertion was successful and we can display a toast.
                    Toast.makeText(this, getString(R.string.editor_insert_trans_successful),
                            Toast.LENGTH_SHORT).show();
                }

                /** Quick DB length check */
                Cursor countCursor = getContentResolver().query(TransContract.TransEntry.CONTENT_URI,
                        new String[] {"count(*) AS count"},
                        null,
                        null,
                        null);
                if (countCursor.moveToFirst()){
                    int count = countCursor.getInt(0);
                    // Log.d(LOG_TAG, "DB length: "+ count);
                    if (count == 1){
                        initiateRecurringService();
                    }
                }
            }
        }
        /** Update */
        else {
            int nRowsUpdated = this.getContentResolver().update(
                    currentTransUri,
                    values,
                    null,
                    null);
            // Show a toast message depending on whether or not the insertion was successful
            if (nRowsUpdated <= 0) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_edit_trans_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_edit_trans_success),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    /** Delete */
    private void deleteTransaction() {
        int nRowsDeleted = this.getContentResolver().delete(
                currentTransUri,
                null,
                null);
        if (nRowsDeleted > 0) {
            // If the new content URI is null, then there was an error with insertion.
            Toast.makeText(this, getString(R.string.editor_delete_trans_successful),
                    Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the insertion was successful and we can display a toast.
            Toast.makeText(this, getString(R.string.editor_delete_trans_failed),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /** One-time TransactionService.class launch
     *
     * This JobService will be launched when the first entry in the database occurs.
     * The service will check once daily, when system idle, if there is a recurring
     * date in the database AND if it matches with the system date, the service will
     * issue:
     *  - A new transaction
     *  - A notification to user
     *
     *  */
    private void initiateRecurringService(){
        // Start JobInfo builder and JobScheduler
        ComponentName componentName = new ComponentName(this, TransactionService.class);
        JobScheduler jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        Log.d(LOG_TAG, "all pending jobs:" + jobScheduler.getAllPendingJobs());

        JobInfo.Builder builder = new JobInfo.Builder(001, componentName);

        /*
        Debug periodic job,
        Minimum interval in milliseconds. */
        long MIN_PERIOD_MILLIS = 15 * 60 * 1000L;   // 15 minutes
        long daily = 24 * 60 * 60 * 1000L; // 1 day
        builder.setPeriodic(daily)
                .setPersisted(true)
                .setRequiresDeviceIdle(true);

        JobInfo jobInfo = builder.build();

        // Extras, work duration.
        // PersistableBundle extras = new PersistableBundle();
        // extras.putString("original_date", dateString);
        // extras.putLong("periodic", Long.valueOf(chosenFrequency));
        // builder.setExtras(extras);

        // scheduleJob():
        if ( jobScheduler.schedule(jobInfo) <= 0 ){
            Log.d(LOG_TAG, "something went wrong when sceduling");
        } else {
            Toast.makeText(this, "Recurring job scheduled", Toast.LENGTH_SHORT).show();
        }
    }

    private String getExactStringDate(int months, long today){
        // SimpleDateFormat
        Toast.makeText(this, "Entering getExactStringDate()", Toast.LENGTH_SHORT).show();
        String todayDateString = dateFormat.format(new Date(today));

            /* MasterList Data */
        String[] arrayDayYrMo = todayDateString.split("/"); // Will store the split date
        String mDay = arrayDayYrMo[0]; // position 0, should be "dd"
        Log.d(LOG_TAG, "XOXO EditorSave, recurring mDay: " + mDay);
        String mMo = arrayDayYrMo[1]; // position 1, should be "MM"
        Log.d(LOG_TAG, "XOXO EditorSave, recurring mMo: " + mMo);
        String mYear = arrayDayYrMo[2]; // mMoYear.split("/", 0)[1]; // position 1, should be "YYYY"
        Log.d(LOG_TAG, "XOXO EditorSave, recurring mYear: " + mYear);
        // String mYear = mMoYear.split("/", 0)[1]; // position 1, should be "YYYY"

        Integer newMonth = Integer.valueOf(mMo) + months;
        if (newMonth > 12){
            newMonth = newMonth - 12;
        }

        String newDate = mDay + "/" + newMonth.toString() + "/" + mYear;
        Log.d(LOG_TAG, "XOXO EditorSave, recurring newDate: " + newDate);
        return newDate;
    }


}
