package com.daytightchunks.wealthyhabits.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

/**
 * Created by DayTightChunks on 09/02/2017.
 */

public class TransProvider extends ContentProvider {

    /* Database Helper Object */
    private TransDbHelper mDbHelper;

    /** Tag for the log messages */
    public static final String LOG_TAG = TransProvider.class.getSimpleName();

    // Creates a UriMatcher object.
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Set up matcher constants for the Uri Matcher.
    public static final int TRANSACTIONS = 100;
    public static final int TRANSACTION_ID = 101;

    static {
        sUriMatcher.addURI(TransContract.CONTENT_AUTHORITY, TransContract.PATH_TRANSACTIONS, TRANSACTIONS);
        sUriMatcher.addURI("com.daytightchunks.wealthyhabits", TransContract.PATH_TRANSACTIONS + "/#", TRANSACTION_ID);
    }

    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {
        // Create and initialize a DbHelper object to gain access to the transactions database.
        // Make sure the variable is a global variable, so it can be referenced from other
        // ContentProvider methods.
        mDbHelper = new TransDbHelper(getContext());
        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    // Implements ContentProvider.query()
    public Cursor query(
            Uri uri,
            String[] projection,
            String selection,
            String[] selectionArgs,
            String sortOrder) {

        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        Cursor cursor;

        int match = sUriMatcher.match(uri);

        switch (match) {
            case TRANSACTIONS:
                cursor = database.query(
                        TransContract.TransEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case TRANSACTION_ID:
                selection = TransContract.TransEntry._ID + "=?";
                selectionArgs = new String[] { ( String.valueOf(ContentUris.parseId(uri)) ) };
                cursor = database.query(
                        TransContract.TransEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI" + uri);
        }

        // Set notification uri on the cursor
        // so we know where content URI the cursor was created for
        // If the data at this URI changes, then we know we need to update the cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case TRANSACTIONS:
                return insertTrans(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a transaction into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertTrans(Uri uri, ContentValues values) {
        // SANITY CHECKS
        // Check that the name is not null
        String name = values.getAsString(TransContract.TransEntry.COLUMN_CATEGORY);
        if (name == null) {
            throw new IllegalArgumentException("Transaction requires a category");
        }

        // TODO: Insert other sanity checks...

        // Start of the insertion
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new row, returning the primary key value of the new row
        long id = database.insert(TransContract.TransEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the transaction content URI
        // "content://com.example.android.wealthyhabits/transactions"
        getContext().getContentResolver().notifyChange(uri, null);

        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case TRANSACTIONS:
                return updateTrans(uri, contentValues, selection, selectionArgs);
            case TRANSACTION_ID:
                // For the PET_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = TransContract.TransEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateTrans(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update pets in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more pets).
     * Return the number of rows that were successfully updated.
     */
    private int updateTrans(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        if (values.size() == 0) {
            return 0;
        }
        // check that the category value is not null.
        if (values.containsKey(TransContract.TransEntry.COLUMN_CATEGORY)) {
            String name = values.getAsString(TransContract.TransEntry.COLUMN_CATEGORY);
            if (name == null) {
                throw new IllegalArgumentException("Transaction requires a category");
            }
        }

        /**
        // check that the value is valid.
        if (values.containsKey(TransContract.TransEntry.COLUMN_AMOUNT)) {
            // Check that the weight is greater than or equal to 0 kg
            Float amount = values.getAsFloat(TransContract.TransEntry.COLUMN_AMOUNT);
            if (amount != null) {
                throw new IllegalArgumentException("Transaction requires valid amount");
            }
        }
         */

        ContentValues mUpdatedValues = values;

        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        // Defines a variable to contain the number of updated rows
        int mRowsUpdated;

        mRowsUpdated = database.update(
                TransContract.TransEntry.TABLE_NAME,   // the user dictionary content URI
                mUpdatedValues,                           // the columns to update
                selection,                               // the column to select on
                selectionArgs                           // the value to compare to
        );

        // Notify all listeners that the data has changed
        if (mRowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return mRowsUpdated;
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int mRowsDeleted;
        switch (match) {
            case TRANSACTIONS:
                mRowsDeleted = database.delete(
                        TransContract.TransEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;

            case TRANSACTION_ID:
                selection = TransContract.TransEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                mRowsDeleted = database.delete(
                        TransContract.TransEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Delete is not supported for " + uri);

        }
        // Notify all listeners that the data has changed
        if (mRowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return mRowsDeleted;
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case TRANSACTIONS:
                return TransContract.TransEntry.CONTENT_LIST_TYPE;
            case TRANSACTION_ID:
                return TransContract.TransEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

}
