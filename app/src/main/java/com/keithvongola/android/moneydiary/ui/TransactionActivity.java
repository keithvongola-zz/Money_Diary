package com.keithvongola.android.moneydiary.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.keithvongola.android.moneydiary.R;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.keithvongola.android.moneydiary.Utility.ARG_PAGE;
import static com.keithvongola.android.moneydiary.Utility.FRAG_TAG_TF;

public class TransactionActivity extends AppCompatActivity {
    @BindView(R.id.toolbar) Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.transactions_new);

        if (savedInstanceState == null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);

            TransactionFragment fragment = new TransactionFragment();
            int position = getIntent().getExtras() == null ? 0 : getIntent().getExtras().getInt(ARG_PAGE) ;

            Bundle arg = new Bundle();
            arg.putInt(ARG_PAGE,position);
            fragment.setArguments(arg);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_transaction_container, fragment, FRAG_TAG_TF)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        TransactionFragment tf = (TransactionFragment) getSupportFragmentManager().findFragmentByTag(FRAG_TAG_TF);
        int position = tf.getLastPosition();
        Fragment fragment= tf.getChildFragmentManager()
                .findFragmentByTag("android:switcher:" + R.id.transaction_viewpager + ":" +  position);

        if(fragment instanceof ExpenseEditFragment){
            if(((ExpenseEditFragment) fragment).popupWindowIsShowing())
                return;
        } else if(fragment instanceof IncomeEditFragment){
            if(((IncomeEditFragment) fragment).popupWindowIsShowing())
                return;
        } else if (fragment instanceof TransferEditFragment){
            if(((TransferEditFragment) fragment).popupWindowIsShowing())
                return;
        } else if (fragment instanceof SavingEditFragment){
            if(((SavingEditFragment) fragment).popupWindowIsShowing())
                return;
        }
            super.onBackPressed();
        }
}
