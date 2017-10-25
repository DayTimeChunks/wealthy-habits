package com.daytightchunks.wealthyhabits.ui;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.daytightchunks.wealthyhabits.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by DayTightChunks on 03/03/2017.
 */

public class MyExpandableListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final String LOG_TAG = MyExpandableListAdapter.class.getSimpleName();

    private LayoutInflater mInflaterHead;
    private LayoutInflater mInflaterChild;

    public static final int HEADER = 0;
    public static final int CHILD = 1;

    private List<Item> mData;

    public MyExpandableListAdapter(List<Item> data){
        mData = data;
    }

    /** Define two extension classes to be able to instantiate them
     * on the onCreateVuewHolder method and be able to find the correct
     * view IDs in the respective resource layout file */
    public class HeaderListViewHolder extends RecyclerView.ViewHolder{

        public TextView mHeaderText;
        public ImageView mExpandButton;
        public Item refferalItem;

        public HeaderListViewHolder(View itemView) {
            super(itemView);
            // Find the resource id where this text needs to go.
            mHeaderText = (TextView) itemView.findViewById(R.id.parent_text_view);
            mExpandButton = (ImageView) itemView.findViewById(R.id.btn_expand_toggle);
        }
    }

    public class ChildViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView mChildText;
        // public ImageView mStarImage;
        public Item refferalItem;
        View mChildView;

        public ChildViewHolder(View itemView) {
            super(itemView);
            mChildText = (TextView) itemView.findViewById(R.id.child_text_view);
            mChildView = itemView;
            // mStarImage = (ImageView) itemView.findViewById(R.id.child_star);
            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            int pos = getAdapterPosition();
            Toast.makeText(view.getContext(), "Item: " + String.valueOf(getItemText(pos)), Toast.LENGTH_SHORT).show();
            ((EditorActivity) view.getContext()).onClickCalled(String.valueOf(getItemText(pos)));

        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Need to identify if type is HEADER or CHILD
        switch (viewType) {
            case HEADER:
                // Inflate Header
                // 1. create a LayoutInflater instance
                mInflaterHead = LayoutInflater.from(parent.getContext());
                // 2. Inflate to Layout, inflating requires the parent's ViewGroup
                View vh = mInflaterHead.inflate(R.layout.xpalist_header, parent, false);
                // 3. Create a new instance of the HeaderListViewHolder and return it.
                HeaderListViewHolder header = new HeaderListViewHolder(vh);
                return header;
            case CHILD:
                mInflaterChild = LayoutInflater.from(parent.getContext());
                View vch = mInflaterChild.inflate(R.layout.xpalist_child, parent, false);
                ChildViewHolder child = new ChildViewHolder(vch);
                return child;

        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final Item item = mData.get(position);
        switch (item.mItemType) {
            case HEADER:
                // need to cast the RV.ViewHolder to a HeaderListViewHolder so as to...
                final HeaderListViewHolder customHolder = (HeaderListViewHolder) holder;
                // set text, which is stored by the item instance. But first, give the item the position
                customHolder.refferalItem = item;
                customHolder.mHeaderText.setText(item.mItemText);
                if (item.invisibleChildren == null) {
                    customHolder.mExpandButton.setImageResource(R.drawable.circle_minus);
                } else {
                    customHolder.mExpandButton.setImageResource(R.drawable.circle_plus);
                }
                // Opening and closing will work by moving data
                // from one Array to the Another
                // i.e. data -> item.invisibleChildren
                customHolder.mExpandButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // 1. test if the data has defined an invisible children array
                        // namely, if closed, move data -> item.invisibleChildren
                        if (item.invisibleChildren == null) {
                            // if not, define it:
                            item.invisibleChildren = new ArrayList<Item>();
                            // Set loop counter and header index
                            int count = 0;
                            int headerPosition = mData.indexOf(customHolder.refferalItem);
                            // 1a. test if data size has at least one child
                            while (mData.size() > headerPosition + 1 &&
                                    mData.get(headerPosition + 1).mItemType == CHILD) {
                                // add it to the invisible children list
                                item.invisibleChildren.add(mData.get(headerPosition + 1));
                                // delete it from the source list.
                                mData.remove(headerPosition + 1);
                                count++;
                            } // notify the deletions
                            notifyItemRangeRemoved(headerPosition + 1, count);
                            customHolder.mExpandButton.setImageResource(R.drawable.circle_plus);
                        } else {
                            int headerPosition = mData.indexOf(customHolder.refferalItem);
                            int index = headerPosition + 1;
                            for (Item i : item.invisibleChildren) {
                                mData.add(index, i);
                                index++;
                            } // notify insertions
                            notifyItemRangeInserted(headerPosition + 1, index - headerPosition - 1);
                            customHolder.mExpandButton.setImageResource(R.drawable.circle_minus);
                            item.invisibleChildren = null;
                        }
                    }
                });
                break;
            case CHILD:
                final ChildViewHolder childCustomHolder = (ChildViewHolder) holder;
                childCustomHolder.refferalItem = item;
                childCustomHolder.mChildText.setText(item.mItemText);
                // childCustomHolder.mStarImage.setImageResource(R.drawable.ic_star_grey);
                /**
                childCustomHolder.mChildView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("some_key", "String data");
                        setResult(Activity.RESULT_OK, resultIntent);
                        finish();
                    }
                });
                 */
                break;
        }

    }

    public String getItemText(int position) {return mData.get(position).mItemText; }

    @Override
    public int getItemViewType(int position) {
        return mData.get(position).mItemType;
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class Item {
        public int mItemType;
        public String mItemText;
        public List<Item> invisibleChildren;

        public Item() {
        }

        public Item(int type, String text) {
            mItemType = type;
            mItemText = text;
        }
    }
}
