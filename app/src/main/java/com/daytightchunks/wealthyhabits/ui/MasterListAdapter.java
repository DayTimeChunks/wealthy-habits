package com.daytightchunks.wealthyhabits.ui;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.daytightchunks.wealthyhabits.R;
import com.daytightchunks.wealthyhabits.data.TransContract;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;

/**
 * Created by DayTightChunks on 08/08/2017.
 */

public class MasterListAdapter extends CursorAdapter {

    public MasterListAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    // The newView method is used to inflate a new view and return it,
    // you don't bind any data to the view at this point.
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.item_trans, parent, false);
    }

    // The bindView method is used to bind all data to a given view
    // such as setting the text on a TextView.
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find fields to populate in inflated template
        TextView listedCategoryText = (TextView) view.findViewById(R.id.trans_item_category);
        TextView listedTitleText = (TextView) view.findViewById(R.id.trans_item_title);
        TextView listedAmountText = (TextView) view.findViewById(R.id.trans_item_amount);
        TextView listedDateText = (TextView) view.findViewById(R.id.trans_item_date);

        // Extract properties from cursor
        String listedCategory = cursor.getString(cursor.getColumnIndexOrThrow(TransContract.TransEntry.COLUMN_CATEGORY));
        String listedTitle = cursor.getString(cursor.getColumnIndexOrThrow(TransContract.TransEntry.COLUMN_TITLE));
        Float listedAmount = cursor.getFloat(cursor.getColumnIndex(TransContract.TransEntry.COLUMN_AMOUNT));
        long listedDate = cursor.getLong(cursor.getColumnIndex(TransContract.TransEntry.COLUMN_DATE));
        int listedType = cursor.getInt(cursor.getColumnIndex(TransContract.TransEntry.COLUMN_TYPE));
        int listedFromAccount = cursor.getInt((cursor.getColumnIndex(TransContract.TransEntry.COLUMN_ACCOUNT_FROM)));
        int listedToAccount = cursor.getInt((cursor.getColumnIndex(TransContract.TransEntry.COLUMN_ACCOUNT_TO)));
        // String listedDate = cursor.getString(cursor.getColumnIndex(TransContract.TransEntry.COLUMN_DATE));
        // int listedDate = cursor.getInt(cursor.getColumnIndex(TransContract.TransEntry.COLUMN_DATE));

        if (TextUtils.isEmpty(listedCategory)) {
            listedCategory = context.getString(R.string.set_text_no_category);
        }

        if (TextUtils.isEmpty(listedTitle)) {
            listedTitle = context.getString(R.string.set_text_no_title);
        }

        /**
         if (TextUtils.isEmpty(listedAmount)){
         listedAmount = context.getString(R.string.set_text_no_amount);
         }
         */
        if (listedAmount == null) {
            listedAmount = (float) 0.00;
        }

        /** Text fields */
        // Populate fields with extracted properties
        listedCategoryText.setText(listedCategory);
        listedTitleText.setText(listedTitle);

        /** Currency Amount */
        /// Option 1 - No currency
        // listedAmountText.setText(String.format("%.2f", listedAmount)); // Yes, worked
        // listedAmountText.setText(NumberFormat.getInstance().format(listedAmount));

        // Option 2 - With currency
        NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.getDefault());
        nf.setCurrency(Currency.getInstance("EUR"));

        if (listedCategory.equals("Transfer")){
            // Wallet -> Saving: Blue (else Red)
            if (listedToAccount == TransContract.TransEntry.ACCOUNT_SAVINGS_0){
                // if more than one savings accounts, add them here
                listedAmountText.setText(nf.format(listedAmount));
                listedAmountText.setTextColor(Color.parseColor("#03A9F4")); // Blue
            } else {
                listedAmountText.setText(nf.format(listedAmount*-1));
                listedAmountText.setTextColor(Color.parseColor("#D50000")); // Red
            }

        } else {
            if (listedType == TransContract.TransEntry.TYPE_CREDIT){
                listedAmountText.setText(nf.format(listedAmount));
                listedAmountText.setTextColor(Color.parseColor("#00C853")); // Green
            } else {
                listedAmountText.setText(nf.format(listedAmount*-1));
                listedAmountText.setTextColor(Color.parseColor("#D50000")); // Red
            }
        }



        /** Date formatting */
        // listedDateText.setText(listedDate);
        // Log.d(LOG_TAG, "listedDate: " + listedDate);
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        Date date = new Date(listedDate);
        // Log.d(LOG_TAG, "TransFragment, Date listedDate: " + date);
        listedDateText.setText(df.format(date));

    }
}

