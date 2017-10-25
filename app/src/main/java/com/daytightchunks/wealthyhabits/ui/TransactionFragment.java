package com.daytightchunks.wealthyhabits.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.daytightchunks.wealthyhabits.R;
import com.daytightchunks.wealthyhabits.data.TransContract;
import com.daytightchunks.wealthyhabits.data.TransDbHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;

/**
 * Created by DayTightChunks on 29/08/2017.
 */

public class TransactionFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    /** Tag for the log messages */
    public static final String LOG_TAG = TransactionFragment.class.getSimpleName();

    /** Identifies a specific loader being used in this component */
    private static final int URL_LOADER_EDITOR = 1;

    private boolean isEmpty = false;
    private boolean mTransHasChanged = false;

    private Uri currentTransUri;

    /** EditText field to enter all input fields */
    private EditText mCategoryEditText; // Should also be a spinner
    // private Spinner mTypeSpinner; // Type: Expense or Income

    // For transactions only
    private RadioGroup mTypeRadioGroup;
    private RadioButton mTypeRadioCredit;
    private RadioButton mTypeRadioDebit;

    // For transfers only
    private Spinner mFromAccountSpinner; // Type: from_account
    private Spinner mToAccountSpinner; // Type: to_account
    private int mFromAccount;
    private int mToAccount;

    private EditText mTitleEditText;
    private EditText mCalendarEditText; // Temporary as EditText.

    private EditText mAmountEditText;

    private EditText editDate;

    private CheckBox mRecurringCheckBox;
    private LinearLayout mRecurringDetails;
    private EditText mRecurringFrequencyEditText;
    private EditText mRecurringEndDateEditText;

    // Communicate with parent activity
    LinkedHashMap mEditorEntryFields = new LinkedHashMap();
    Button saveButton;
    Button cancelButton;
    String newCategory = "None";

    Float listedAmount;
    private int mType; // Credit or Debit
    String listedCategory;
    boolean isFirstCreated;
    String listedTitle;
    String stringDate;
    private int mRecurring = TransContract.TransEntry.NOT_RECURRING; // Default
    String listedFreq;

    private String stringEndDate;
    long listedEndDate;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.GERMANY);

    private String TRANSFER = "transfer";
    private Boolean isTransfer = false;

    public int REQUEST_CODE = 1;

    /** NEW
     * Database helper that will provide us access to the database */
    private TransDbHelper mDbHelper;

    /** Interface logic fir Save and Cancel Buttons */
    OnButtonClickListener mButtonCallback;
    public interface OnButtonClickListener{
        // Interface method
        void onButtonSelected(int id, LinkedHashMap editorEntries);
    }

    // Override OnAttach to ensure interface is implemented in container activity
    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        Log.d(LOG_TAG, "onAttach started");
        try {
            mButtonCallback = (OnButtonClickListener) context;
        } catch (ClassCastException e){
            throw new ClassCastException(context.toString() +
                    " must implement OnButtonClickListener interface");
        }
    }


    public TransactionFragment(){

    }

    public static TransactionFragment newInstance(Uri currentTransUri, String origin){

//        Log.d(LOG_TAG, "newInstance called");
        TransactionFragment transactionFragment = new TransactionFragment();
        Bundle bundle = new Bundle();
        if (currentTransUri != null){
            bundle.putString("uri", currentTransUri.toString());
        }
//        Log.d(LOG_TAG, "putting origin: " + origin);
        bundle.putString("origin", origin);
        transactionFragment.setArguments(bundle);
        return transactionFragment;
    }

    @Override
    public void onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        // Implement retainInstance by starting fragment only if null (see fragment onCreate)
        setRetainInstance(true); // Needed so editor fields don't re-query the loader on rotation
//        Log.d(LOG_TAG, "onCreate called");
        Bundle bundle = getArguments();
        if (bundle != null) {
            if (bundle.getString("uri") != null){
                currentTransUri = Uri.parse(bundle.getString("uri"));
                isFirstCreated = true;
            }
            String origin = bundle.getString("origin");
//            Log.d(LOG_TAG, "getting origin: " + origin);
            if (origin.equals(TRANSFER)){
                isTransfer = true;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater,
                             ViewGroup container,
                             Bundle savedInstanceState){

        // Log.d(LOG_TAG, "onCreateView called");
        View rootView;
        // Log.d(LOG_TAG, "isTransfer: " + isTransfer);
        if (isTransfer){
            rootView = layoutInflater.inflate(R.layout.transfer_editor, container, false);
        } else {
            rootView = layoutInflater.inflate(R.layout.transaction_editor, container, false);
        }

        // TODO, BUG:
        // app crashing on rotation
        if (savedInstanceState != null){
            isFirstCreated = savedInstanceState.getBoolean("isFirst");
        }
        if (currentTransUri != null) { // Editing card has data
            if (isFirstCreated){
                getActivity().getSupportLoaderManager().initLoader(URL_LOADER_EDITOR, null, this);
                isFirstCreated = false;
            }


        }

        // Button listeners are on parent Activity
        saveButton = (Button) rootView.findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = v.getId();
                activateSaveButton();
                mButtonCallback.onButtonSelected(id, mEditorEntryFields);
            }
        });

        cancelButton = (Button) rootView.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = v.getId();
                mButtonCallback.onButtonSelected(id, mEditorEntryFields);
            }
        });

        /** Amount */
        mAmountEditText = (EditText) rootView.findViewById(R.id.edit_amount);
        mAmountEditText.setOnTouchListener(mTouchListener);



        /** Title */
        mTitleEditText = (EditText) rootView.findViewById(R.id.edit_title);
        mTitleEditText.setOnTouchListener(mTouchListener);

        /** Calendar entry */
        mCalendarEditText = (EditText) rootView.findViewById(R.id.edit_date);
        mCalendarEditText.setOnTouchListener(mTouchListener);

        // mTypeSpinner = (Spinner) findViewById(R.id.spinner_transaction_type);
        // mTypeSpinner.setOnTouchListener(mTouchListener);
        // setupSpinner();

        editDate = (EditText) rootView.findViewById(R.id.edit_date);

        mCalendarEditText.setOnClickListener(new MyEditTextDatePicker(getContext(), editDate));

        if (isTransfer){ // Setup spinners
            mFromAccountSpinner = (Spinner) rootView.findViewById(R.id.spinner_transfer_from);
            mToAccountSpinner = (Spinner) rootView.findViewById(R.id.spinner_transfer_to);
            mFromAccountSpinner.setOnTouchListener(mTouchListener);
            mToAccountSpinner.setOnTouchListener(mTouchListener);
            setupAccountSpinner();

        } else { // Set up category and type radio buttons

            /** Income or Expense */
            mTypeRadioGroup = (RadioGroup) rootView.findViewById(R.id.radio_type_group);
            mTypeRadioCredit = (RadioButton) rootView.findViewById(R.id.radio_credit);
            mTypeRadioDebit = (RadioButton) rootView.findViewById(R.id.radio_debit);

            mCategoryEditText = (EditText) rootView.findViewById(R.id.edit_category);
            mCategoryEditText.setOnTouchListener(mTouchListener);
            mCategoryEditText.setOnClickListener(myCategoryPicker);


            mTypeRadioGroup.setOnCheckedChangeListener((new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                    switch(checkedId) {
                        case R.id.radio_debit:
                            mType = TransContract.TransEntry.TYPE_DEBIT;
                            break;
                        case R.id.radio_credit:
                            mType = TransContract.TransEntry.TYPE_CREDIT;
                            Toast.makeText(getContext(), "Credit Selected: ",
                                    Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            break;
                    }
                }
            }));

/*           mTypeRadioGroup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    boolean checkedButton = ((RadioButton) view).isChecked();
                    Log.d(LOG_TAG, "onRadioButtonClicked: " + String.valueOf(checked));
                    Toast.makeText(getContext(), "OnCLickListenerSet",
                            Toast.LENGTH_SHORT).show();
                    // Check which radio button was clicked
                    switch(view.getId()) {
                        case R.id.radio_debit:
                            if (checkedButton){
                                Log.d(LOG_TAG, "view.getId: " + String.valueOf(view.getId()));
                            }
                            mType = TransContract.TransEntry.TYPE_DEBIT;
                            break;
                        case R.id.radio_credit:
                            if (checkedButton){
                                Log.d(LOG_TAG, "view.getId: " + String.valueOf(view.getId()));
                            }
                            mType = TransContract.TransEntry.TYPE_CREDIT;
                            Toast.makeText(getContext(), "Credit Selected: ",
                                    Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            break;
                    }
                }
            });
*/
        }


        mRecurringCheckBox = (CheckBox) rootView.findViewById((R.id.checkbox_recurring));
        mRecurringDetails = (LinearLayout) rootView.findViewById((R.id.recurring_details));
        mRecurringDetails.setVisibility(View.INVISIBLE);

        mRecurringEndDateEditText = (EditText) rootView.findViewById(R.id.edit_end_date);
        mRecurringFrequencyEditText = (EditText) rootView.findViewById(R.id.edit_frequency);

        /** Opens recurring details on creation, if recurring is true */
        mRecurringCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                switch (compoundButton.getId()){
                    case R.id.checkbox_recurring:
                        if (checked){
                            mRecurring = TransContract.TransEntry.RECURRING;
                            mRecurringDetails.setVisibility(View.VISIBLE);
                            setEndDatePicker();
                            showFrequencyDialog();
                        } else {
                            mRecurring = TransContract.TransEntry.NOT_RECURRING;
                            mRecurringDetails.setVisibility(View.INVISIBLE);
                        }
                        break;
                }
            }
        });

        // mRecurringDetails.setVisibility(View.INVISIBLE);
        // To access our database, we instantiate our subclass of SQLiteOpenHelper
        // and pass the context, which is the current activity.
        mDbHelper = new TransDbHelper(getContext());
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isFirst", false);
    }

    @Override
    public void onResume() {
        super.onResume();
//        Log.d(LOG_TAG, "onResume started");
        if (isTransfer){
            return;
        } else {
            if (newCategory != null) {
                mCategoryEditText.setText(newCategory);
            }
        }
    }

    private void setEndDatePicker() {
        if (mRecurringEndDateEditText != null) {
            mRecurringEndDateEditText.setOnClickListener(new MyEditTextDatePicker(getContext(), mRecurringEndDateEditText));
        }
    }

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mTransHasChanged = true;
            return false;
        }
    };

    // Used by EditorActivity upon onOptionsItemSelected(R.id.home)
    public boolean getMotion(){
        return mTransHasChanged;
    }

    /**
     * Transfer Only
     * dropdown spinner to select from/to account.
     */
    private void setupAccountSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter typeSpinnerAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.array_accounts, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        typeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mFromAccountSpinner.setAdapter(typeSpinnerAdapter);
        mToAccountSpinner.setAdapter(typeSpinnerAdapter);
        mToAccountSpinner.setSelection(1); // Default to Savings account

        /** Accounts selection, mType will be defined only on activateSaveButton() */
        // Set the integer mSelected to the constant values
        mFromAccountSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.wallet))) {
                        mFromAccount = TransContract.TransEntry.ACCOUNT_WALLET_10;
                    } else if (selection.equals(getString(R.string.savings))) {
                        mFromAccount = TransContract.TransEntry.ACCOUNT_SAVINGS_0;
                    } else {
                        mFromAccount = TransContract.TransEntry.ACCOUNT_WALLET_10; // Default

                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mFromAccount = TransContract.TransEntry.ACCOUNT_WALLET_10; // Expense
            }
        });

        // Set the integer mSelected to the constant values
        mToAccountSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.wallet))) {
                        mToAccount = TransContract.TransEntry.ACCOUNT_WALLET_10;
                    } else if (selection.equals(getString(R.string.savings))) {
                        mToAccount = TransContract.TransEntry.ACCOUNT_SAVINGS_0;
                    } else {
                        mToAccount = TransContract.TransEntry.ACCOUNT_WALLET_10; // Default
                    }
                }
            }
            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mToAccount = TransContract.TransEntry.ACCOUNT_SAVINGS_0; // Savings

            }
        });
    }

    private View.OnClickListener myCategoryPicker = new View.OnClickListener(){

        @Override
        public void onClick(View view) {
//            Intent categoryIntent = new Intent(getActivity(), CategoriesActivity.class);
//            // Gets info back when category is chosen.
//            // Lifecycle's callback: onActivityResult()
//            startActivityForResult(categoryIntent, REQUEST_CODE);
//            Log.d(LOG_TAG, "Started Categories Activity");

            CategoriesFragment categoriesFragment = new CategoriesFragment();
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.editor_fragment_container, categoriesFragment)
                    .addToBackStack(null)
                    .commit();
        }
    };

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        // super.onActivityResult(requestCode, resultCode, data);
//        // Check which request we're responding to
//        if (requestCode == REQUEST_CODE){
//            // Make sure the request was successful
//            if (resultCode == RESULT_OK) {
//                // do something with result.
//                mCategoryEditText.setText(data.getStringExtra("some_key"));
//            }
//        }
//    }

    public void setNewCategory(String newCat){
        mEditorEntryFields.put("listedCategory", newCat);
        newCategory = newCat;
        Log.d(LOG_TAG, "mCategoryEditText: " + mCategoryEditText.getText());
    }

    /** Date picker method */
    public class MyEditTextDatePicker implements
            View.OnClickListener, DatePickerDialog.OnDateSetListener {
        EditText _editText;
        private int _day;
        private int _month;
        private int _birthYear;
        private Context _context;

        /** Constructor
         * We can make an instance of this
         * */
        public MyEditTextDatePicker(Context context, EditText editTextView) {
            Activity act = (Activity) context;
            this._editText = (EditText) editTextView;
            this._editText.setOnClickListener(this);
            this._context = context;
        }

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            _birthYear = year;
            _month = monthOfYear;
            _day = dayOfMonth;
            updateDisplay();
        }
        @Override
        public void onClick(View v) {
            // Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
            Calendar calendar = Calendar.getInstance();

            DatePickerDialog dialog = new DatePickerDialog(_context, this,
                    calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            dialog.show();
        }

        // updates the date in the date EditText
        private void updateDisplay() {
            _editText.setText(new StringBuilder()
                    // Month is 0 based so add 1
                    .append(_day).append("/").append(_month + 1).append("/").append(_birthYear).append(" "));

        }
    }

    public class MyEditTextFrequency implements
            View.OnClickListener {
        EditText _editText;
        private String[] frequency_array;
        private Context _context;

        public MyEditTextFrequency(Context context, int editTextViewID) {
            Activity act = (Activity) context;
            this._editText = (EditText) act.findViewById(editTextViewID);
            this._editText.setOnClickListener(this);
            frequency_array = getResources().getStringArray(R.array.array_frequency_options);
            this._context = context;
        }

        @Override
        public void onClick(View view) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(_context);
//            Log.d(LOG_TAG, "onClick View dialog created");
            dialog.setTitle(R.string.recurring_frequency)
                    // setSingleChoiceItems(R.array.array_frequency_options, int ItemChecked,
                    .setItems(R.array.array_frequency_options,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    _editText.setText(frequency_array[which]);
                                }
                            }
                    )
                    .show();
        }
    }

    private void showFrequencyDialog() {
        if (mRecurringFrequencyEditText != null) {
            mRecurringFrequencyEditText.setOnClickListener(new MyEditTextFrequency(getContext(), R.id.edit_frequency));
        }
    }

    /** Delete */
    private void deleteTransaction() {
        int nRowsDeleted = getContext().getContentResolver().delete(
                currentTransUri,
                null,
                null);
        if (nRowsDeleted > 0) {
            // If the new content URI is null, then there was an error with insertion.
            Toast.makeText(getContext(), getString(R.string.editor_delete_trans_successful),
                    Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the insertion was successful and we can display a toast.
            Toast.makeText(getContext(), getString(R.string.editor_delete_trans_failed),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void activateSaveButton(){
        /** FIELDS = 8 :
         * Amount
         * => Type or (TODO: still need to handle Type for transfers...)
         * => mTo/mFromAccount
         * Date
         * Category
         * Title
         * Recurring
         * Frequency
         * EndDate
         *
         * Other data in db (not updated):
         * Account from
         * account to
         * */
        try {
            listedAmount = Float.parseFloat(mAmountEditText.getText().toString().trim());
        } catch (NumberFormatException e) {
            listedAmount = (float) 0.0;
            System.err.println("java.lang.NumberFormatException: " + e.getMessage());
        }
        mEditorEntryFields.put("listedAmount", listedAmount);

        if (isTransfer){
            mEditorEntryFields.put("mFromAccount", mFromAccount);
            mEditorEntryFields.put("mToAccount", mToAccount);
            listedCategory = "Transfer";
            if (mFromAccount >= 10){ // Wallet's are in 10's
                mType = TransContract.TransEntry.TYPE_CREDIT;
            } else if (mFromAccount < 10){ // Savings are 0 to 9
                mType = TransContract.TransEntry.TYPE_DEBIT;
            }
            mEditorEntryFields.put("mType", mType);
        } else {
            if (mType == TransContract.TransEntry.TYPE_CREDIT){
                mEditorEntryFields.put("mFromAccount", TransContract.TransEntry.ACCOUNT_EXTERNAL_20);
                mEditorEntryFields.put("mToAccount", TransContract.TransEntry.ACCOUNT_WALLET_10);
            } else {
                mEditorEntryFields.put("mFromAccount", TransContract.TransEntry.ACCOUNT_WALLET_10);
                mEditorEntryFields.put("mToAccount", TransContract.TransEntry.ACCOUNT_EXTERNAL_20);
            }
            mEditorEntryFields.put("mType", mType);
            listedCategory = mCategoryEditText.getText().toString().trim();
        }
        mEditorEntryFields.put("listedCategory", listedCategory);

        stringDate = mCalendarEditText.getText().toString().trim(); // dd/MM/YYYY
        mEditorEntryFields.put("stringDate", stringDate);

        listedTitle = mTitleEditText.getText().toString().trim();
        mEditorEntryFields.put("listedTitle", listedTitle);

        mEditorEntryFields.put("recurring", mRecurring);
        listedFreq = mRecurringFrequencyEditText.getText().toString().trim();
        if (listedFreq.isEmpty()){
            listedFreq = getString(R.string.every_month);
        }
        mEditorEntryFields.put("listedFreq", listedFreq);
        // Note: "next date" computed in EditorActivity on saveTrans()

        stringEndDate = mRecurringEndDateEditText.getText().toString().trim();
        mEditorEntryFields.put("stringEndDate", stringEndDate);
    }

    /**
     * Cursor call backs
     * */
    @Override
    public Loader<Cursor> onCreateLoader(int loaderID, Bundle args) {

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
                TransContract.TransEntry.COLUMN_END_DATE
        };

        return new CursorLoader(
                getContext(),
                currentTransUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Populate in inflated editor template
        // mCursorAdapter.swapCursor(data);
        // We have not being using an adapter... so leaving this blank

        if (cursor != null){



        if (cursor.moveToFirst()) {
            try { // finally close cursor
                do {
                    int amountColumnIndex = cursor.getColumnIndex(TransContract.TransEntry.COLUMN_AMOUNT);
                    int typeColumnsIndex = cursor.getColumnIndex(TransContract.TransEntry.COLUMN_TYPE);
                    int fromAccountColumnsIndex = cursor.getColumnIndex(TransContract.TransEntry.COLUMN_ACCOUNT_FROM);
                    int toAccountColumnsIndex = cursor.getColumnIndex(TransContract.TransEntry.COLUMN_ACCOUNT_TO);
                    int dateColumnsIndex = cursor.getColumnIndexOrThrow(TransContract.TransEntry.COLUMN_DATE);
                    int categoryColumnsIndex = cursor.getColumnIndex(TransContract.TransEntry.COLUMN_CATEGORY);
                    int titleColumnIndex = cursor.getColumnIndex(TransContract.TransEntry.COLUMN_TITLE);
                    int recurringColumnsIndex = cursor.getColumnIndex(TransContract.TransEntry.COLUMN_RECURRING);
                    int frequencyColumnIndex = cursor.getColumnIndex(TransContract.TransEntry.COLUMN_FREQUENCY);
                    int endDateColumnIndex = cursor.getColumnIndex(TransContract.TransEntry.COLUMN_END_DATE);

                    // Extract cursor values
                    listedAmount = cursor.getFloat(amountColumnIndex);
                    mAmountEditText.setText(String.format("%.2f", listedAmount));

                    mType = cursor.getInt(typeColumnsIndex);

                    if (isTransfer){

                        mFromAccount = cursor.getInt(fromAccountColumnsIndex);
                        mToAccount = cursor.getInt(toAccountColumnsIndex);

                        switch (mFromAccount){
                            case TransContract.TransEntry.ACCOUNT_WALLET_10:
                                // first position on R.array.array_accounts
                                mFromAccountSpinner.setSelection(0);
                                break;
                            case TransContract.TransEntry.ACCOUNT_SAVINGS_0:
                                mFromAccountSpinner.setSelection(1);
                                break;
                            default:
                                mFromAccountSpinner.setSelection(0);

                        }
                        switch (mToAccount){
                            case TransContract.TransEntry.ACCOUNT_WALLET_10:
                                // first position on R.array.array_accounts
                                mToAccountSpinner.setSelection(0);
                                break;
                            case TransContract.TransEntry.ACCOUNT_SAVINGS_0:
                                mToAccountSpinner.setSelection(1);
                                break;
                            default:
                                mToAccountSpinner.setSelection(1);
                        }

                    } else {

                        listedCategory = cursor.getString(categoryColumnsIndex);
                        mCategoryEditText.setText(listedCategory);

                        switch (mType) {
                            case TransContract.TransEntry.TYPE_DEBIT:
                                // mTypeSpinner.setSelection(0);
                                mTypeRadioDebit.setChecked(true);
                                break;
                            case TransContract.TransEntry.TYPE_CREDIT:
                                // mTypeSpinner.setSelection(1);
                                mTypeRadioCredit.setChecked(true);
                                break;
                            default:
                                // mTypeSpinner.setSelection(0);
                                mTypeRadioDebit.setChecked(true);
                                break;
                        }
                    }

                    /** Date formatting */
                   //  DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
                    long listedDate = cursor.getLong(dateColumnsIndex);
                    Date date = new Date(listedDate);
                    mCalendarEditText.setText(dateFormat.format(date)); // == dateString

                    Log.d(LOG_TAG, "listedDate: " + listedDate);
                    Log.d(LOG_TAG, "TransFragment, Date listedDate: " + date);

                    listedTitle = cursor.getString(titleColumnIndex);
                    mTitleEditText.setText(listedTitle);

                    // Recurring
                    mRecurring = cursor.getInt(recurringColumnsIndex);
                    listedFreq = cursor.getString(frequencyColumnIndex);
                    listedEndDate = cursor.getLong(endDateColumnIndex);
                    String endDateString = dateFormat.format(new Date(listedEndDate));

                    switch (mRecurring) {
                        case TransContract.TransEntry.NOT_RECURRING:
                            mRecurringCheckBox.setChecked(false);
                            break;
                        case TransContract.TransEntry.RECURRING:
                            mRecurringCheckBox.setChecked(true);
                            mRecurringEndDateEditText.setText(endDateString);
                            mRecurringFrequencyEditText.setText(listedFreq);
                            break;
                    }
                } while (cursor.moveToNext());
            } finally {
                cursor.close();
            }
        }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case URL_LOADER_EDITOR:
                loader.abandon();
                // loader.cancelLoad();
                /*
                 * If you have current references to the Cursor,
                 * remove them here.
                 */
                break;
        }

    }

    public LinkedHashMap getEntries(){
        return mEditorEntryFields;
    }



}
