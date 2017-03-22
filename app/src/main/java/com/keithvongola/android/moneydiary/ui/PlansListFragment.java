package com.keithvongola.android.moneydiary.ui;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.daimajia.swipe.util.Attributes;
import com.keithvongola.android.moneydiary.R;
import com.keithvongola.android.moneydiary.Utility;
import com.keithvongola.android.moneydiary.adapter.PlansSwipeAdapter;
import com.keithvongola.android.moneydiary.databases.MoneyContract.PlansEntry;
import com.keithvongola.android.moneydiary.pojo.Plan;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.keithvongola.android.moneydiary.Utility.ARG_IS_MAIN_PLAN;
import static com.keithvongola.android.moneydiary.Utility.ARG_URI;
import static com.keithvongola.android.moneydiary.Utility.FRAG_TAG_EDIT;

public class PlansListFragment extends Fragment implements LoaderCallbacks<Cursor>{
    @BindView(R.id.plans_list_view)RecyclerView plansListView;
    @BindView(R.id.empty_view) TextView emptyPlansView;
    private static final int PLANS_LOADER = 0;

    private Uri plansUri;
    private PlansSwipeAdapter plansSwipeAdapter;

    private boolean isCompleted;

    public PlansListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Determine current fragment to show Main Budgets or Sub Budgets
        //show Main Budgets by default
        if (getArguments() != null) {
            plansUri = getArguments().getParcelable(ARG_URI);
            isCompleted = Integer.parseInt(PlansEntry.getStatusFromUri(plansUri)) != 0;
        }

        getLoaderManager().initLoader(PLANS_LOADER,null,this);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_budgets, menu);

        if (menu instanceof MenuBuilder) {
            MenuBuilder m = (MenuBuilder) menu;
            m.setOptionalIconsVisible(true);
        }

        MenuItem menuItemEdit = menu.findItem(R.id.action_edit_budget);
        MenuItem menuItemDelete = menu.findItem(R.id.action_delete_budget);

        if (menuItemEdit != null)
            Utility.tintMenuIcon(getActivity(),menuItemEdit,R.color.colorGrey400);

        if (menuItemDelete != null)
            Utility.tintMenuIcon(getActivity(),menuItemDelete,R.color.colorGrey400);
    }

    @Override
    public void onPrepareOptionsMenu (Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuInflater menuInflater = getActivity().getMenuInflater();
            if (plansSwipeAdapter.getEditBtn() | plansSwipeAdapter.getDeleteBtn()) {
                menu.clear();
                menuInflater.inflate(R.menu.menu_done, menu);
            }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_budget:
                addPlans();
                return true;

            case R.id.action_edit_budget:
                if (plansSwipeAdapter.getItemCount() > 0) {
                    plansSwipeAdapter.setEditBtn(true);
                    getActivity().invalidateOptionsMenu();
                }
                return true;

            case R.id.action_delete_budget:
                if (plansSwipeAdapter.getItemCount() > 0) {
                    plansSwipeAdapter.setDeleteBtn(true);
                    getActivity().invalidateOptionsMenu();
                }
                return true;

            case R.id.action_done:
                plansSwipeAdapter.resetEditAndDelete();
                getActivity().invalidateOptionsMenu();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_plans_list, container, false);
        ButterKnife.bind(this, rootView);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.title_plans));

        plansSwipeAdapter = new PlansSwipeAdapter(getActivity(),null);
        plansSwipeAdapter.setMode(Attributes.Mode.Single);

        plansListView.setAdapter(plansSwipeAdapter);
        plansListView.setLayoutManager(new LinearLayoutManager(getActivity()));

        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                plansUri,
                null,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        int emptyViewVisibility;
        if(data.moveToFirst()) {
            List<Plan> plans = new ArrayList<>();
            for(int i = 0; i < data.getCount(); i++) {
                Plan plan = new Plan(data);
                if (!isCompleted && !plan.isCompleted())
                    plans.add(plan);
                 else if (isCompleted && plan.isCompleted())
                    plans.add(plan);
                data.moveToNext();
            }
            plansSwipeAdapter.setData(plans);
            emptyViewVisibility = plans.size() > 0 ? View.GONE : View.VISIBLE;
        } else {
            emptyViewVisibility = View.VISIBLE;
            plansSwipeAdapter.setData(null);
        }
        emptyPlansView.setVisibility(emptyViewVisibility);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        plansSwipeAdapter.setData(null);
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(PLANS_LOADER,null,this);
    }

    public boolean onBackPressed(){
        boolean back = plansSwipeAdapter.getSwipeStatus();
        getActivity().invalidateOptionsMenu();
        return back;
    }

    @OnClick(R.id.empty_view)
    public void addPlans(){
        Bundle args = new Bundle();
        args.putBoolean(ARG_IS_MAIN_PLAN, true);

        PlansEditFragment fragment = new PlansEditFragment();
        fragment.setArguments(args);
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_main_container, fragment, FRAG_TAG_EDIT)
                .addToBackStack(null).commit();
    }
}
