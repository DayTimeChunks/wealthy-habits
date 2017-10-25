package com.daytightchunks.wealthyhabits.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.daytightchunks.wealthyhabits.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by DayTightChunks on 15/09/2017.
 */

public class CategoriesFragment extends Fragment {
    /** Tag for the log messages */
    public static final String LOG_TAG = CategoriesFragment.class.getSimpleName();
    private RecyclerView recyclerView;
    private List<MyExpandableListAdapter.Item> data;

    public CategoriesFragment(){}


    /** Interface logic for Category selection,
     *
     *
     *    TODO:      Should this be in the Adapter instead??
     *
     *
    OnCategoryClickListener mCategoryCallback;

    public interface OnCategoryClickListener{
        // Interface method
        void onCategorySelected(String newCategory);
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        try {
            mCategoryCallback = (OnCategoryClickListener) context;
        } catch (ClassCastException e){
            throw new ClassCastException(context.toString() +
                    " must implement OnCategoryClickListener interface");
        }
    }
     */

    @Override
    public View onCreateView(LayoutInflater layoutInflater,
                             ViewGroup container,
                             Bundle savedInstanceState){

        View rootView = layoutInflater.inflate(R.layout.fragment_categories, container, false);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        recyclerView.
                setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        data = new ArrayList<>();

        MyExpandableListAdapter.Item food = new MyExpandableListAdapter.Item(MyExpandableListAdapter.HEADER, "Food");
        food.invisibleChildren = new ArrayList<>();
        food.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Food General"));
        food.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Supermarket"));
        food.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Restaurant"));
        food.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Lunch"));

        data.add(food);

        MyExpandableListAdapter.Item entertainment = new MyExpandableListAdapter.Item(MyExpandableListAdapter.HEADER, "Entertainment");
        entertainment.invisibleChildren = new ArrayList<>();
        entertainment.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Entertainment General"));
        entertainment.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Cinema and theatre"));
        entertainment.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Bar"));
        entertainment.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Games"));

        data.add(entertainment);

        MyExpandableListAdapter.Item holiday = new MyExpandableListAdapter.Item(MyExpandableListAdapter.HEADER, "Holiday");
        holiday.invisibleChildren = new ArrayList<>();
        holiday.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Holiday General"));
        holiday.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Accommodation"));
        holiday.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Activities"));
        holiday.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Food"));
        holiday.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Flight"));
        holiday.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Transport"));

        data.add(holiday);

        MyExpandableListAdapter.Item home = new MyExpandableListAdapter.Item(MyExpandableListAdapter.HEADER, "Home");
        home.invisibleChildren = new ArrayList<>();
        home.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Home General"));
        home.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Electricity"));
        home.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Water"));
        home.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Heating"));
        home.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Insurance"));
        home.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Internet"));
        home.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Phone"));
        home.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Repairs"));
        home.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Furniture"));
        home.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Cleaning products"));

        data.add(home);

        MyExpandableListAdapter.Item clothing = new MyExpandableListAdapter.Item(MyExpandableListAdapter.HEADER, "Clothing");
        clothing.invisibleChildren = new ArrayList<>();
        clothing.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Clothing General"));
        clothing.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Pants"));
        clothing.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Shirts"));
        clothing.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Dress"));
        clothing.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Accessories"));
        clothing.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Jewelry"));

        data.add(clothing);

        MyExpandableListAdapter.Item health = new MyExpandableListAdapter.Item(MyExpandableListAdapter.HEADER, "Health");
        health.invisibleChildren = new ArrayList<>();
        health.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Health General"));
        health.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Cosmetics"));
        health.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Doctor"));
        health.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Hairdresser"));
        health.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Nutrients"));
        health.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Pharmacy"));

        data.add(health);

        MyExpandableListAdapter.Item children = new MyExpandableListAdapter.Item(MyExpandableListAdapter.HEADER, "Children");
        children.invisibleChildren = new ArrayList<>();
        children.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Children General"));
        children.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Baby sitting"));
        children.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "School"));
        children.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Clothing"));

        data.add(children);

        MyExpandableListAdapter.Item work = new MyExpandableListAdapter.Item(MyExpandableListAdapter.HEADER, "Work");
        work.invisibleChildren = new ArrayList<>();
        work.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Work General"));
        work.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Salary"));
        work.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Bonus"));
        work.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Other"));

        data.add(work);

        MyExpandableListAdapter.Item transport = new MyExpandableListAdapter.Item(MyExpandableListAdapter.HEADER, "Transport");
        transport.invisibleChildren = new ArrayList<>();
        transport.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Transport General"));
        transport.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Tram and bus"));
        transport.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Taxi"));
        transport.invisibleChildren.add(new MyExpandableListAdapter.Item(MyExpandableListAdapter.CHILD, "Train"));

        data.add(transport);

        recyclerView.setAdapter(new MyExpandableListAdapter(data));

        return rootView;
    }



}
