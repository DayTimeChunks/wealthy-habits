package com.daytightchunks.wealthyhabits.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by DayTightChunks on 08/02/2017.
 *
 * Testing, BACKUP and upgrades for the future, check:
 *      https://stackoverflow.com/questions/13537800/keeping-sqlite-data-after-update
 */

public class TransDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "transactions.db";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TransContract.TransEntry.TABLE_NAME;

    public TransDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /* Create new tables in database */
    public void onCreate(SQLiteDatabase db) {
        // CREATE TABLE transactions
        //  (_id INTEGER, category TEXT, type INTEGER NOT NULL DEFAULT 0,
        //      title INTEGER, date INTEGER,
        //      recurring_date TEXT, recurring_end_date TEXT, amount TEXT);

        String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + TransContract.TransEntry.TABLE_NAME + " (" +
                        TransContract.TransEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        TransContract.TransEntry.COLUMN_AMOUNT + " REAL, " +
                        TransContract.TransEntry.COLUMN_TYPE + " INTEGER NOT NULL DEFAULT 0, " + // debit, credit (used only for wallets)
                        TransContract.TransEntry.COLUMN_ACCOUNT_NAME_FROM + " TEXT, " + // wallet, savings, more later.
                        TransContract.TransEntry.COLUMN_ACCOUNT_FROM + " INTEGER, " + // defaults from wallet 0
                        TransContract.TransEntry.COLUMN_ACCOUNT_NAME_TO + " TEXT, " + // wallet, savings, more later.
                        TransContract.TransEntry.COLUMN_ACCOUNT_TO + " INTEGER, " + // defaults back to wallet 0
                        TransContract.TransEntry.COLUMN_CATEGORY + " TEXT, " +
                        TransContract.TransEntry.COLUMN_TITLE + " TEXT, " +
                        TransContract.TransEntry.COLUMN_DATE + " INTEGER, " +
                        TransContract.TransEntry.COLUMN_RECURRING + " INTEGER NOT NULL DEFAULT 0," +
                        TransContract.TransEntry.COLUMN_FREQUENCY + " TEXT," +
                        TransContract.TransEntry.COLUMN_NEXT_DATE + " INTEGER," +
                        TransContract.TransEntry.COLUMN_END_DATE + " INTEGER," +
                        TransContract.TransEntry.COLUMN_KIND + " TEXT," + // Not currently used (back up)
                        TransContract.TransEntry.COLUMN_ICON + " INTEGER," + // Not currently used (for later)
                        TransContract.TransEntry.COLUMN_EXTRA_INT_V1 + " INTEGER," + // Not currently used (for later)
                        TransContract.TransEntry.COLUMN_EXTRA_TXT_V1 + " TEXT" + // Not currently used (for later)
                        ");";

        db.execSQL(SQL_CREATE_ENTRIES);

    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Deletes old database and recreates a new one.
        // Is our data lost?
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
