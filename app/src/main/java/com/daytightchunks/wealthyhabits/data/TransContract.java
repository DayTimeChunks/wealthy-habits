package com.daytightchunks.wealthyhabits.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by DayTightChunks on 08/02/2017.
 */

// Make it "final" so it can't be extended,
// as it is only a class that provides constants.

public final class TransContract {

    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private TransContract() {}

    public static final String CONTENT_AUTHORITY = "com.daytightchunks.wealthyhabits";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // This constant stores the path for each of the tables which will be appended
    // to the base content URI.
    public static final String PATH_TRANSACTIONS = "transactions";

    /* Inner class that defines the table contents
    * By implementing the BaseColumns interface,
    * your inner class can inherit a primary key field called _ID that
    * some Android classes such as cursor adaptors will expect it to have.*/
    public static abstract class TransEntry implements BaseColumns {

        // Complete content URI:
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_TRANSACTIONS);

        public static final String TABLE_NAME = "transactions";

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_AMOUNT = "amount";
        public static final String COLUMN_TYPE = "type"; // expense, income or transfer
        public static final String COLUMN_ACCOUNT_NAME_FROM = "account_name_from";
        public static final String COLUMN_ACCOUNT_FROM = "account_from";
        public static final String COLUMN_ACCOUNT_NAME_TO = "account_name_to";
        public static final String COLUMN_ACCOUNT_TO = "account_to";

        public static final String COLUMN_CATEGORY = "category";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_RECURRING = "recurring";

        public static final String COLUMN_FREQUENCY = "frequency";
        public static final String COLUMN_NEXT_DATE = "recurring_next_date";
        public static final String COLUMN_END_DATE = "recurring_end_date";

        // Not currently in use (but written into structure)
        public static final String COLUMN_KIND = "kind";
        public static final String COLUMN_ICON = "icon";

        public static final String COLUMN_EXTRA_INT_V1 = "extra_int_v1"; // extras for db version 1
        public static final String COLUMN_EXTRA_TXT_V1 = "extra_txt_v1"; // extras for db version 1

        /* Constant values */
        public static final int TYPE_DEBIT = 0;
        public static final int TYPE_CREDIT = 1;

        public static final int ACCOUNT_SAVINGS_0 = 0;
        public static final int ACCOUNT_WALLET_10 = 10;
        public static final int ACCOUNT_EXTERNAL_20 = 20;
        public static final int KIND_NONE = 0;
        public static final int KIND_ONE = 1;
        public static final int KIND_TWO = 2;
        public static final int KIND_THREE = 3;

        public static final int NOT_RECURRING = 0;
        public static final int RECURRING = 1;
        public static final int REMINDER = 1;


        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of transactions.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRANSACTIONS;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single transaction.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRANSACTIONS;

        /**
         * Database Logic
         *
         * Debit and credit capture subtraction and addition from an account,
         * e.g. Adding to the savings account will be recorded as a Debit on the Wallet account.
         *
         * Therefore, mType is relevant only for Wallet-type accounts.
         *
         * If need to query a Saving's account state, need to select all
         * mFrom and mToAccounts matching the accounts name (i.e. integer)
         * */
    }
}
