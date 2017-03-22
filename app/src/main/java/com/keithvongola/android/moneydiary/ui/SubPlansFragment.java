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
import com.keithvongola.android.moneydiary.adapter.SubPlansSwipeAdapter;
import com.keithvongola.android.moneydiary.databases.MoneyContract;
import com.keithvongola.android.moneydiary.pojo.SubPlan;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.keithvongola.android.moneydiary.Utility.ARG_IS_MAIN_PLAN;
import static com.keithvongola.android.moneydiary.Utility.ARG_PARENT_ID;
import static com.keithvongola.android.moneydiary.Utility.ARG_URI;
import static com.keithvongola.android.moneydiary.Utility.FRAG_TAG_EDIT;
import static com.keithvongola.android.moneydiary.Utility.tintMenuIcon;

public class SubPlansFragment extends Fragment implements LoaderCallbacks<Cursor>{
    @BindView(R.id.plans_list_child_view)RecyclerView plansChildList;
    @BindView(R.id.empty_view) TextView emptyPlansView;
    private static final int PLANS_CHILD_LOADER = 0;

    private Uri plansChildUri;
    private SubPlansSwipeAdapter subPlansSwipeAdapter;

    public SubPlansFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null)
            plansChildUri = getArguments().getParcelable(ARG_URI);

        getLoaderManager().initLoader(PLANS_CHILD_LOADER,null,this);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_budgets, menu);

        if(menu instanceof MenuBuilder){
            MenuBuilder m = (MenuBuilder) menu;
            m.setOptionalIconsVisible(true);
        }

        MenuItem menuItemEdit = menu.findItem(R.id.action_edit_budget);
        MenuItem menuItemDelete = menu.findItem(R.id.action_delete_budget);

        if (menuItemEdit != null)
            tintMenuIcon(getActivity(),menuItemEdit,R.color.colorGrey400);

        if (menuItemDelete!=null)
            tintMenuIcon(getActivity(),menuItemDelete,R.color.colorGrey400);
    }

    @Override
    public void onPrepareOptionsMenu (Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuInflater menuInflater = getActivity().getMenuInflater();
            if (subPlansSwipeAdapter.getEditBtn() | subPlansSwipeAdapter.getDeleteBtn()) {
                menu.clear();
                menuInflater.inflate(R.menu.menu_done, menu);
            }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_budget:
                addSubPlan();
                return true;

            case R.id.action_edit_budget:
                if (subPlansSwipeAdapter.getItemCount() > 0) {
                    subPlansSwipeAdapter.setEditBtn(true);
                    getActivity().invalidateOptionsMenu();
                }
                return true;

            case R.id.action_delete_budget:
                if (subPlansSwipeAdapter.getItemCount() > 0) {
                    subPlansSwipeAdapter.setDeleteBtn(true);
                    getActivity().invalidateOptionsMenu();
                }
                return true;

            case R.id.action_done:
                subPlansSwipeAdapter.resetEditAndDelete();
                getActivity().invalidateOptionsMenu();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_plans_child, container, false);
        ButterKnife.bind(this, rootView);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.title_sub_plans));

        subPlansSwipeAdapter = new SubPlansSwipeAdapter(getActivity(),null);
        subPlansSwipeAdapter.setMode(Attributes.Mode.Single);

        plansChildList.setAdapter(subPlansSwipeAdapter);
        plansChildList.setLayoutManager(new LinearLayoutManager(getActivity()));

        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                plansChildUri,
                null,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        int emptyViewVisibility;
        if(data.moveToFirst()) {
            List<SubPlan> children = new ArrayList<>();
            for(int i=0; i<data.getCount(); i++){
                SubPlan child = new SubPlan(data.getInt(0),
                        data.getInt(1),
                        data.getString(2),
                        data.getString(3),
                        data.getInt(6),
                        data.getString(4),
                        data.getInt(5));

                children.add(child);
                data.moveToNext();
            }
            subPlansSwipeAdapter.setData(children);
            emptyViewVisibility = children.size() > 0 ? View.GONE : View.VISIBLE;
        } else {
            subPlansSwipeAdapter.setData(null);
            emptyViewVisibility = View.VISIBLE;
        }
        emptyPlansView.setVisibility(emptyViewVisibility);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        subPlansSwipeAdapter.setData(null);
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(PLANS_CHILD_LOADER,null,this);
    }


    public boolean onBackPressed(){
        boolean back = subPlansSwipeAdapter.getSwipeStatus();
        getActivity().invalidateOptionsMenu();
        return back;
    }

    @OnClick(R.id.empty_view)
    public void addSubPlan(){
        Bundle args = new Bundle();
        args.putBoolean(ARG_IS_MAIN_PLAN, false);
        args.putString(ARG_PARENT_ID, MoneyContract.SubPlansEntry.getSubPlansParentIDFromUri(plansChildUri));
        PlansEditFragment fragment = new PlansEditFragment();
        fragment.setArguments(args);
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_main_container, fragment, FRAG_TAG_EDIT)
                .addToBackStack(null).commit();
    }
}
