package com.daytightchunks.wealthyhabits.ui;

import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.daytightchunks.wealthyhabits.R;
import com.daytightchunks.wealthyhabits.data.TransContract;

/**
 * Created by DayTightChunks on 12/09/2017.
 */

// TODO: navigate up to here when inside the EditorActivity
// https://developer.android.com/training/implementing-navigation/temporal.html

public class TransferActivity extends AppCompatActivity
        implements MasterListFragment.OnTransactionSelectedListener {

    /** Tag for the log messages */
    public static final String LOG_TAG = TransferActivity.class.getSimpleName();

    private Toolbar topToolbar;
    private DrawerLayout mDrawerLayout;
    public boolean mTwoPane;

    MasterListFragment mTransactionListFrag;
    FragmentManager fragmentManager = getSupportFragmentManager();

    @Override
    public void onTransListItemSelected(int position, long id, String category) {
        Log.d(LOG_TAG, "ListItem int position: " + position);
        Log.d(LOG_TAG, "ListItem long id: " + id);
        Log.d(LOG_TAG, "ListItem Category: " + category);

        Uri currentTransUri = ContentUris.withAppendedId(TransContract.TransEntry.CONTENT_URI, id);

        if (mTwoPane) {
            TransactionFragment transactionFragmentWithData = TransactionFragment.newInstance(currentTransUri, "temporary string");
            fragmentManager.beginTransaction()
                    .add(R.id.editor_head_container, transactionFragmentWithData)
                    .commit();
        } else {
            // Otherwise, we're in the one-pane layout and must swap frags...
            Intent editIntent = new Intent(this, EditorActivity.class);
            editIntent.setData(currentTransUri); // pass content_uri to the new intent as extra data
            editIntent.putExtra("finishActivityOnSaveCompleted", true);

            // Get information about the URI...
            // as could be both transfer or transaction item
            if (category.equals("Transfer")){
                editIntent.putExtra("origin", "transfer");
            } else {
                editIntent.putExtra("origin", "transactions");
            }

            startActivity(editIntent);

            /*
            Currently not ready for Fragment switch in phone view due to LayoutLogic
            TransactionFragment editorEmptyFragment = new TransactionFragment();
            fragmentManager.beginTransaction()
                    .replace(R.id.parent_activity_frame, editorEmptyFragment)
                    .addToBackStack(null)
                    .commit();
                    */
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);

        mTransactionListFrag = new MasterListFragment();
        fragmentManager.beginTransaction()
                .add(R.id.transfer_list_container, mTransactionListFrag)
                .commit();

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mTwoPane){
                    TransactionFragment editorFragment = new TransactionFragment();
                    fragmentManager.beginTransaction()
                            .add(R.id.editor_head_container, editorFragment)
                            .commit();
                } else {
                    Intent intent = new Intent(TransferActivity.this, EditorActivity.class);
                    intent.putExtra("origin", "transfer");
                    startActivityForResult(intent, 1); // If dataSet changes, EditorActivity will return "1"

                    // implements callback: onActivityResult() ...below
                }
            }
        });


    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }



}
