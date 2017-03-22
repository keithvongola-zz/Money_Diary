package com.keithvongola.android.moneydiary.databases;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;

import static com.keithvongola.android.moneydiary.databases.MoneyContract.AccountsEntry;
import static com.keithvongola.android.moneydiary.databases.MoneyContract.AccountsEntry.getAccountIdFromUri;
import static com.keithvongola.android.moneydiary.databases.MoneyContract.CONTENT_AUTHORITY;
import static com.keithvongola.android.moneydiary.databases.MoneyContract.CurrencyEntry;
import static com.keithvongola.android.moneydiary.databases.MoneyContract.ExchangeRateEntry;
import static com.keithvongola.android.moneydiary.databases.MoneyContract.ExchangeRateEntry.getExchangeRateIdFromUri;
import static com.keithvongola.android.moneydiary.databases.MoneyContract.MainBudgetsEntry;
import static com.keithvongola.android.moneydiary.databases.MoneyContract.MainBudgetsEntry.getMainBudgetsIdFromUri;
import static com.keithvongola.android.moneydiary.databases.MoneyContract.MainBudgetsEntry.getMainBudgetsTypeFromUri;
import static com.keithvongola.android.moneydiary.databases.MoneyContract.PATH_ACCOUNTS;
import static com.keithvongola.android.moneydiary.databases.MoneyContract.PATH_CURRENCY;
import static com.keithvongola.android.moneydiary.databases.MoneyContract.PATH_EXCHANGE_RATE;
import static com.keithvongola.android.moneydiary.databases.MoneyContract.PATH_MAIN_BUDGETS;
import static com.keithvongola.android.moneydiary.databases.MoneyContract.PATH_PLANS;
import static com.keithvongola.android.moneydiary.databases.MoneyContract.PATH_SUB_BUDGETS;
import static com.keithvongola.android.moneydiary.databases.MoneyContract.PATH_SUB_PLANS;
import static com.keithvongola.android.moneydiary.databases.MoneyContract.PATH_TRANSACTION;
import static com.keithvongola.android.moneydiary.databases.MoneyContract.PlansEntry;
import static com.keithvongola.android.moneydiary.databases.MoneyContract.PlansEntry.getPlansIdFromUri;
import static com.keithvongola.android.moneydiary.databases.MoneyContract.SubBudgetsEntry;
import static com.keithvongola.android.moneydiary.databases.MoneyContract.SubBudgetsEntry.getSubBudgetsIdParentIDFromUri;
import static com.keithvongola.android.moneydiary.databases.MoneyContract.SubBudgetsEntry.getSubBudgetsTypeFromUri;
import static com.keithvongola.android.moneydiary.databases.MoneyContract.SubPlansEntry;
import static com.keithvongola.android.moneydiary.databases.MoneyContract.SubPlansEntry.getSubPlansParentIDFromUri;
import static com.keithvongola.android.moneydiary.databases.MoneyContract.TransactionEntry;
import static com.keithvongola.android.moneydiary.databases.dbUtility.sAccountIdSelection;
import static com.keithvongola.android.moneydiary.databases.dbUtility.sAccountTypeSelection;
import static com.keithvongola.android.moneydiary.databases.dbUtility.sAccountsWithAmount;
import static com.keithvongola.android.moneydiary.databases.dbUtility.sCurrencyIdSelection;
import static com.keithvongola.android.moneydiary.databases.dbUtility.sExchangeRate;
import static com.keithvongola.android.moneydiary.databases.dbUtility.sExchangeRateIdSelection;
import static com.keithvongola.android.moneydiary.databases.dbUtility.sMainBudgetsIdSelection;
import static com.keithvongola.android.moneydiary.databases.dbUtility.sMainBudgetsWithDate;
import static com.keithvongola.android.moneydiary.databases.dbUtility.sMainBudgetsWithType;
import static com.keithvongola.android.moneydiary.databases.dbUtility.sPlansIdSelection;
import static com.keithvongola.android.moneydiary.databases.dbUtility.sPlansWithStatus;
import static com.keithvongola.android.moneydiary.databases.dbUtility.sSubBudgetsIdSelection;
import static com.keithvongola.android.moneydiary.databases.dbUtility.sSubBudgetsWithParentID;
import static com.keithvongola.android.moneydiary.databases.dbUtility.sSubBudgetsList;
import static com.keithvongola.android.moneydiary.databases.dbUtility.sSubPlansIdSelection;
import static com.keithvongola.android.moneydiary.databases.dbUtility.sSubPlansWithParentID;
import static com.keithvongola.android.moneydiary.databases.dbUtility.sTransactionByMonthAndType;
import static com.keithvongola.android.moneydiary.databases.dbUtility.sTransactionIdSelection;
import static com.keithvongola.android.moneydiary.databases.dbUtility.sTransactionWithAccountID;
import static com.keithvongola.android.moneydiary.databases.dbUtility.sTransactionWithAccountIdAndDate;
import static com.keithvongola.android.moneydiary.databases.dbUtility.sTransactionWithDate;

public class MoneyProvider extends ContentProvider {
    private MoneyDBHelper mOpenHelper;
    private static final UriMatcher mUriMatcher = buildUriMatcher();
    public static final Object[] dbLock = new Object[0];

    static final int ACCOUNTS = 100;
    static final int ACCOUNTS_WITH_TYPE = 101;
    static final int ACCOUNTS_WITH_ID = 102;
    static final int MAIN_BUDGETS = 200;
    static final int MAIN_BUDGETS_WITH_ID = 201;
    static final int MAIN_BUDGETS_WITH_TYPE = 202;
    static final int MAIN_BUDGETS_WITH_DATE = 203;
    static final int SUB_BUDGETS = 300;
    static final int SUB_BUDGETS_WITH_ID = 301;
    static final int SUB_BUDGETS_WITH_PARENT_ID = 302;
    static final int SUB_BUDGETS_LIST = 303;
    static final int PLANS = 400;
    static final int PLANS_WITH_ID = 401;
    static final int PLANS_WITH_STATUS = 402;
    static final int SUB_PLANS = 500;
    static final int SUB_PLANS_WITH_ID = 501;
    static final int SUB_PLANS_WITH_PARENT_ID = 502;
    static final int TRANSACTION = 600;
    static final int TRANSACTION_WITH_ID = 601;
    static final int TRANSACTION_WITH_ACCOUNT_ID = 602;
    static final int TRANSACTION_WITH_ACCOUNT_ID_AND_START_AND_END_DATE = 603;
    static final int TRANSACTION_WITH_START_AND_END_DATE = 604;
    static final int TRANSACTION_BY_MONTH_AND_TYPE = 605;
    static final int CURRENCY = 700;
    static final int CURRENCY_WITH_ID = 701;
    static final int EXCHANGE_RATE = 800;
    static final int EXCHANGE_RATE_WITH_ID = 801;

    private static final SQLiteQueryBuilder sMoneyQueryBuilder  = new SQLiteQueryBuilder();

    static {
        sMoneyQueryBuilder.setTables(
                AccountsEntry.TABLE_NAME +
                        " , " + MainBudgetsEntry.TABLE_NAME +
                        " , " + SubBudgetsEntry.TABLE_NAME +
                        " , " + PlansEntry.TABLE_NAME +
                        " , " + SubPlansEntry.TABLE_NAME +
                        " , " + TransactionEntry.TABLE_NAME +
                        " , " + ExchangeRateEntry.TABLE_NAME
        );
    }

    static UriMatcher buildUriMatcher(){
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = CONTENT_AUTHORITY;
        matcher.addURI(authority, PATH_ACCOUNTS,ACCOUNTS);
        matcher.addURI(authority, PATH_ACCOUNTS + "/#" , ACCOUNTS_WITH_TYPE);
        matcher.addURI(authority, PATH_ACCOUNTS + "/#/#" ,ACCOUNTS_WITH_ID);

        matcher.addURI(authority, PATH_MAIN_BUDGETS, MAIN_BUDGETS);
        matcher.addURI(authority, PATH_MAIN_BUDGETS + "/#", MAIN_BUDGETS_WITH_ID);
        matcher.addURI(authority, PATH_MAIN_BUDGETS + "/type/*", MAIN_BUDGETS_WITH_TYPE);
        matcher.addURI(authority, PATH_MAIN_BUDGETS + "/date", MAIN_BUDGETS_WITH_DATE);

        matcher.addURI(authority, PATH_SUB_BUDGETS, SUB_BUDGETS);
        matcher.addURI(authority, PATH_SUB_BUDGETS + "/#", SUB_BUDGETS_WITH_ID);
        matcher.addURI(authority, PATH_SUB_BUDGETS + "/parentId/#", SUB_BUDGETS_WITH_PARENT_ID);
        matcher.addURI(authority, PATH_SUB_BUDGETS + "/list/#", SUB_BUDGETS_LIST);

        matcher.addURI(authority, PATH_PLANS, PLANS);
        matcher.addURI(authority, PATH_PLANS + "/#", PLANS_WITH_ID);
        matcher.addURI(authority, PATH_PLANS + "/status/#", PLANS_WITH_STATUS);

        matcher.addURI(authority, PATH_SUB_PLANS, SUB_PLANS);
        matcher.addURI(authority, PATH_SUB_PLANS + "/#", SUB_PLANS_WITH_ID);
        matcher.addURI(authority, PATH_SUB_PLANS + "/parentId/#", SUB_PLANS_WITH_PARENT_ID);

        matcher.addURI(authority, PATH_TRANSACTION, TRANSACTION);
        matcher.addURI(authority, PATH_TRANSACTION + "/#", TRANSACTION_WITH_ID);
        matcher.addURI(authority, PATH_TRANSACTION + "/account/#", TRANSACTION_WITH_ACCOUNT_ID);
        matcher.addURI(authority, PATH_TRANSACTION + "/account/#/date/", TRANSACTION_WITH_ACCOUNT_ID_AND_START_AND_END_DATE);
        matcher.addURI(authority, PATH_TRANSACTION + "/date/", TRANSACTION_WITH_START_AND_END_DATE);
        matcher.addURI(authority, PATH_TRANSACTION + "/overview", TRANSACTION_BY_MONTH_AND_TYPE);

        matcher.addURI(authority, PATH_CURRENCY, CURRENCY);
        matcher.addURI(authority, PATH_CURRENCY + "/#", CURRENCY_WITH_ID);

        matcher.addURI(authority, PATH_EXCHANGE_RATE, EXCHANGE_RATE);
        matcher.addURI(authority, PATH_EXCHANGE_RATE + "/*", EXCHANGE_RATE_WITH_ID);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new MoneyDBHelper(getContext());
        try {
            mOpenHelper.createDataBase();
        } catch (IOException ioe) {
            throw new Error("Unable to create database");
        }

        try {
            mOpenHelper.openDataBase();
        } catch (SQLException sqle){
            throw sqle;
        }

        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        synchronized (dbLock) {
            Cursor retCursor;
            final int match = mUriMatcher.match(uri);

            switch (match) {
                case ACCOUNTS: {
                    retCursor = mOpenHelper.getReadableDatabase().rawQuery(sAccountsWithAmount, selectionArgs);
                    break;
                }

                case ACCOUNTS_WITH_TYPE: {
                    String accountType = AccountsEntry.getAccountTypeFromUri(uri);
                    retCursor = mOpenHelper.getReadableDatabase().query(
                            AccountsEntry.TABLE_NAME,
                            projection,
                            sAccountTypeSelection,
                            new String[]{accountType},
                            null,
                            null,
                            sortOrder);
                    break;
                }

                case ACCOUNTS_WITH_ID: {
                    String accountId = getAccountIdFromUri(uri);
                    retCursor = mOpenHelper.getReadableDatabase().query(
                            AccountsEntry.TABLE_NAME,
                            projection,
                            sAccountIdSelection,
                            new String[]{accountId},
                            null,
                            null,
                            sortOrder);
                    break;
                }

                case MAIN_BUDGETS: {
                    retCursor = mOpenHelper.getReadableDatabase().query(
                            MainBudgetsEntry.TABLE_NAME,
                            projection,
                            selection,
                            selectionArgs,
                            null,
                            null,
                            sortOrder);
                    break;
                }

                case MAIN_BUDGETS_WITH_ID: {
                    String mainBudgetsID = getMainBudgetsIdFromUri(uri);
                    retCursor = mOpenHelper.getReadableDatabase().query(
                            MainBudgetsEntry.TABLE_NAME,
                            projection,
                            sMainBudgetsIdSelection,
                            new String[]{mainBudgetsID},
                            null,
                            null,
                            sortOrder);
                    break;
                }

                case MAIN_BUDGETS_WITH_TYPE: {
                    retCursor = mOpenHelper.getReadableDatabase().rawQuery(sMainBudgetsWithType,
                            new String[]{String.valueOf(getMainBudgetsTypeFromUri(uri))});
                    break;
                }

                case MAIN_BUDGETS_WITH_DATE: {
                    String startDate = MainBudgetsEntry.getMainsBudgetsStartDateFromUri(uri);
                    String endDate = MainBudgetsEntry.getMainsBudgetsEndDateFromUri(uri);

                    retCursor = mOpenHelper.getReadableDatabase().rawQuery(sMainBudgetsWithDate(startDate, endDate), null);
                    break;
                }

                case SUB_BUDGETS: {
                    retCursor = mOpenHelper.getReadableDatabase().query(
                            SubBudgetsEntry.TABLE_NAME,
                            projection,
                            selection,
                            selectionArgs,
                            null,
                            null,
                            sortOrder);
                    break;
                }

                case SUB_BUDGETS_WITH_ID: {
                    String subBudgetsID = SubBudgetsEntry.getSubBudgetsIdFromUri(uri);
                    retCursor = mOpenHelper.getReadableDatabase().query(
                            SubBudgetsEntry.TABLE_NAME,
                            projection,
                            sSubBudgetsIdSelection,
                            new String[]{subBudgetsID},
                            null,
                            null,
                            sortOrder);
                    break;
                }

                case SUB_BUDGETS_WITH_PARENT_ID: {
                    String mainBudgetID = getSubBudgetsIdParentIDFromUri(uri);
                    retCursor = mOpenHelper.getReadableDatabase()
                            .rawQuery(sSubBudgetsWithParentID, new String[]{mainBudgetID});
                    break;
                }

                case SUB_BUDGETS_LIST: {
                    String type = getSubBudgetsTypeFromUri(uri);
                    retCursor = mOpenHelper.getReadableDatabase()
                            .rawQuery(sSubBudgetsList, new String[]{type});
                    break;
                }

                case PLANS: {
                    retCursor = mOpenHelper.getReadableDatabase().query(
                            PlansEntry.TABLE_NAME,
                            projection,
                            selection,
                            selectionArgs,
                            null,
                            null,
                            sortOrder);
                    break;
                }

                case PLANS_WITH_ID: {
                    String plansId = getPlansIdFromUri(uri);
                    retCursor = mOpenHelper.getReadableDatabase().query(
                            PlansEntry.TABLE_NAME,
                            projection,
                            sPlansIdSelection,
                            new String[]{plansId},
                            null,
                            null,
                            sortOrder);
                    break;
                }

                case PLANS_WITH_STATUS: {
                    retCursor = mOpenHelper.getReadableDatabase().rawQuery(sPlansWithStatus, null);
                    break;
                }

                case SUB_PLANS: {
                    retCursor = mOpenHelper.getReadableDatabase().query(
                            SubPlansEntry.TABLE_NAME,
                            projection,
                            selection,
                            selectionArgs,
                            null,
                            null,
                            sortOrder);
                    break;
                }

                case SUB_PLANS_WITH_ID: {
                    String subPlansId = SubPlansEntry.getSubPlansIdFromUri(uri);
                    retCursor = mOpenHelper.getReadableDatabase().query(
                            SubPlansEntry.TABLE_NAME,
                            projection,
                            sSubPlansIdSelection,
                            new String[]{subPlansId},
                            null,
                            null,
                            sortOrder);
                    break;
                }

                case SUB_PLANS_WITH_PARENT_ID: {
                    String parentId = getSubPlansParentIDFromUri(uri);
                    retCursor = mOpenHelper.getReadableDatabase()
                            .rawQuery(sSubPlansWithParentID, new String[]{parentId});
                    break;
                }

                case TRANSACTION: {
                    retCursor = mOpenHelper.getReadableDatabase().query(
                            TransactionEntry.TABLE_NAME,
                            projection,
                            selection,
                            selectionArgs,
                            null,
                            null,
                            sortOrder);
                    break;
                }

                case TRANSACTION_WITH_ID: {
                    String transactionID = TransactionEntry.getTransactionIdFromUri(uri);
                    retCursor = mOpenHelper.getReadableDatabase().query(
                            SubBudgetsEntry.TABLE_NAME,
                            projection,
                            sTransactionIdSelection,
                            new String[]{transactionID},
                            null,
                            null,
                            sortOrder);
                    break;
                }

                case TRANSACTION_WITH_ACCOUNT_ID: {
                    String accountID = TransactionEntry.getTransactionAccountFromUri(uri);
                    retCursor = mOpenHelper.getReadableDatabase()
                            .rawQuery(sTransactionWithAccountID, new String[]{accountID});
                    break;
                }

                case TRANSACTION_WITH_ACCOUNT_ID_AND_START_AND_END_DATE: {
                    String accountID = TransactionEntry.getTransactionAccountFromUri(uri);
                    String startDate = TransactionEntry.getTransactionStartDateFromUriWithAccount(uri);
                    String endDate = TransactionEntry.getTransactionEndDateFromUriWithAccount(uri);

                    retCursor = mOpenHelper.getReadableDatabase()
                            .rawQuery(sTransactionWithAccountIdAndDate(startDate, endDate), new String[]{accountID});
                    break;
                }

                case TRANSACTION_WITH_START_AND_END_DATE: {
                    String startDate = TransactionEntry.getTransactionStartDateFromUriWithAccount(uri);
                    String endDate = TransactionEntry.getTransactionEndDateFromUriWithAccount(uri);

                    retCursor = mOpenHelper.getReadableDatabase().rawQuery(sTransactionWithDate(startDate, endDate), null);
                    break;
                }

                case TRANSACTION_BY_MONTH_AND_TYPE: {
                    retCursor = mOpenHelper.getReadableDatabase().rawQuery(sTransactionByMonthAndType(), null);
                    break;
                }

                case CURRENCY: {
                    retCursor = mOpenHelper.getReadableDatabase().query(
                            CurrencyEntry.TABLE_NAME,
                            projection,
                            selection,
                            selectionArgs,
                            null,
                            null,
                            sortOrder);
                    break;
                }

                case CURRENCY_WITH_ID: {
                    String currencyID = CurrencyEntry.getCurrencyIdFromUri(uri);
                    retCursor = mOpenHelper.getReadableDatabase().query(
                            CurrencyEntry.TABLE_NAME,
                            projection,
                            sCurrencyIdSelection,
                            new String[]{currencyID},
                            null,
                            null,
                            sortOrder);
                    break;
                }

                case EXCHANGE_RATE: {
                    retCursor = mOpenHelper.getReadableDatabase().rawQuery(sExchangeRate, selectionArgs);
                    break;
                }

                case EXCHANGE_RATE_WITH_ID: {
                    String exchangeRateID = getExchangeRateIdFromUri(uri);
                    retCursor = mOpenHelper.getReadableDatabase().query(
                            ExchangeRateEntry.TABLE_NAME,
                            projection,
                            sExchangeRateIdSelection,
                            new String[]{exchangeRateID},
                            null,
                            null,
                            sortOrder);
                    break;
                }
                default:
                    throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
            retCursor.setNotificationUri(getContext().getContentResolver(), uri);
            return retCursor;
        }
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = mUriMatcher.match(uri);

        switch (match) {
            case ACCOUNTS:
                return AccountsEntry.CONTENT_TYPE;
            case ACCOUNTS_WITH_TYPE:
                return AccountsEntry.CONTENT_TYPE;
            case ACCOUNTS_WITH_ID:
                return AccountsEntry.CONTENT_ITEM_TYPE;
            case MAIN_BUDGETS:
                return MainBudgetsEntry.CONTENT_TYPE;
            case MAIN_BUDGETS_WITH_ID:
                return MainBudgetsEntry.CONTENT_ITEM_TYPE;
            case SUB_BUDGETS:
                return SubBudgetsEntry.CONTENT_TYPE;
            case SUB_BUDGETS_WITH_ID:
                return SubBudgetsEntry.CONTENT_ITEM_TYPE;
            case SUB_BUDGETS_WITH_PARENT_ID:
                return SubBudgetsEntry.CONTENT_ITEM_TYPE;
            case TRANSACTION:
                return TransactionEntry.CONTENT_TYPE;
            case TRANSACTION_WITH_ID:
                return TransactionEntry.CONTENT_TYPE;
            case TRANSACTION_WITH_ACCOUNT_ID:
                return TransactionEntry.CONTENT_ITEM_TYPE;
            case TRANSACTION_WITH_START_AND_END_DATE:
                return TransactionEntry.CONTENT_ITEM_TYPE;
            case TRANSACTION_WITH_ACCOUNT_ID_AND_START_AND_END_DATE:
                return TransactionEntry.CONTENT_ITEM_TYPE;
            case CURRENCY:
                return CurrencyEntry.CONTENT_TYPE;
            case CURRENCY_WITH_ID:
                return CurrencyEntry.CONTENT_ITEM_TYPE;
            case EXCHANGE_RATE:
                return ExchangeRateEntry.CONTENT_TYPE;
            case EXCHANGE_RATE_WITH_ID:
                return ExchangeRateEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        synchronized (dbLock) {
            final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
            final int match = mUriMatcher.match(uri);
            Uri returnUri;

            switch (match) {
                case ACCOUNTS: {
                    long _id = db.insert(AccountsEntry.TABLE_NAME, null, values);
                    if (_id > 0)
                        returnUri = AccountsEntry.buildAccountUri(_id);
                    else
                        throw new android.database.SQLException("Failed to insert row into " + uri);
                    break;
                }
                case MAIN_BUDGETS: {
                    long _id = db.insert(MainBudgetsEntry.TABLE_NAME, null, values);
                    if (_id > 0)
                        returnUri = MainBudgetsEntry.buildMainBudgetsUriWithId(_id);
                    else
                        throw new android.database.SQLException("Failed to insert row into " + uri);
                    break;
                }
                case SUB_BUDGETS: {
                    long _id = db.insert(SubBudgetsEntry.TABLE_NAME, null, values);
                    if (_id > 0)
                        returnUri = SubBudgetsEntry.buildSubBudgetsUri(_id);
                    else
                        throw new android.database.SQLException("Failed to insert row into " + uri);
                    break;
                }
                case PLANS: {
                    long _id = db.insert(PlansEntry.TABLE_NAME, null, values);
                    if (_id > 0)
                        returnUri = PlansEntry.buildPlansUriWithId(_id);
                    else
                        throw new android.database.SQLException("Failed to insert row into " + uri);
                    break;
                }
                case SUB_PLANS: {
                    long _id = db.insert(SubPlansEntry.TABLE_NAME, null, values);
                    if (_id > 0)
                        returnUri = SubPlansEntry.buildSubPlansUriWithId(_id);
                    else
                        throw new android.database.SQLException("Failed to insert row into " + uri);
                    break;
                }
                case TRANSACTION: {
                    long _id = db.insert(TransactionEntry.TABLE_NAME, null, values);
                    if (_id > 0)
                        returnUri = TransactionEntry.buildTransactionUri(_id);
                    else
                        throw new android.database.SQLException("Failed to insert row into " + uri);
                    break;
                }
                case CURRENCY: {
                    long _id = db.insert(CurrencyEntry.TABLE_NAME, null, values);
                    if (_id > 0)
                        returnUri = CurrencyEntry.buildCurrencyUri(_id);
                    else
                        throw new android.database.SQLException("Failed to insert row into " + uri);
                    break;
                }
                case EXCHANGE_RATE: {
                    long _id = db.insert(ExchangeRateEntry.TABLE_NAME, null, values);
                    if (_id > 0)
                        returnUri = ExchangeRateEntry.buildExchangeRateUri(_id);
                    else
                        throw new android.database.SQLException("Failed to insert row into " + uri);
                    break;
                }
                default:
                    throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
            getContext().getContentResolver().notifyChange(uri, null);
            return returnUri;
        }
    }
    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        synchronized (dbLock) {
            final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
            final int match = mUriMatcher.match(uri);
            int rowsDeleted;

            if (null == selection) selection = "1";
            switch (match) {
                case ACCOUNTS:
                    rowsDeleted = db.delete(
                            AccountsEntry.TABLE_NAME, selection, selectionArgs);
                    break;
                case MAIN_BUDGETS:
                    rowsDeleted = db.delete(
                            MainBudgetsEntry.TABLE_NAME, selection, selectionArgs);
                    break;
                case SUB_BUDGETS:
                    rowsDeleted = db.delete(
                            SubBudgetsEntry.TABLE_NAME, selection, selectionArgs);
                    break;
                case PLANS:
                    rowsDeleted = db.delete(
                            PlansEntry.TABLE_NAME, selection, selectionArgs);
                    break;
                case SUB_PLANS:
                    rowsDeleted = db.delete(
                            SubPlansEntry.TABLE_NAME, selection, selectionArgs);
                    break;
                case TRANSACTION:
                    rowsDeleted = db.delete(
                            TransactionEntry.TABLE_NAME, selection, selectionArgs);
                    break;
                case CURRENCY:
                    rowsDeleted = db.delete(
                            CurrencyEntry.TABLE_NAME, selection, selectionArgs);
                    break;
                case EXCHANGE_RATE:
                    rowsDeleted = db.delete(
                            ExchangeRateEntry.TABLE_NAME, selection, selectionArgs);
                    break;
                default:
                    throw new UnsupportedOperationException("Unknown uri: " + uri);
            }

            if (rowsDeleted != 0) {
                Log.d("this uri", uri.toString());
                getContext().getContentResolver().notifyChange(uri, null);
            }
            return rowsDeleted;
        }
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        synchronized (dbLock) {
            final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
            final int match = mUriMatcher.match(uri);
            int rowsUpdated;

            switch (match) {
                case ACCOUNTS: {
                    rowsUpdated = db.update(AccountsEntry.TABLE_NAME, values, selection,
                            selectionArgs);
                    break;
                }
                case MAIN_BUDGETS: {
                    rowsUpdated = db.update(MainBudgetsEntry.TABLE_NAME, values, selection,
                            selectionArgs);
                    break;
                }
                case SUB_BUDGETS: {
                    rowsUpdated = db.update(SubBudgetsEntry.TABLE_NAME, values, selection,
                            selectionArgs);
                    break;
                }
                case PLANS: {
                    rowsUpdated = db.update(PlansEntry.TABLE_NAME, values, selection,
                            selectionArgs);
                    break;
                }
                case SUB_PLANS: {
                    rowsUpdated = db.update(SubPlansEntry.TABLE_NAME, values, selection,
                            selectionArgs);
                    break;
                }
                case TRANSACTION: {
                    rowsUpdated = db.update(TransactionEntry.TABLE_NAME, values, selection,
                            selectionArgs);
                    break;
                }
                case CURRENCY: {
                    rowsUpdated = db.update(CurrencyEntry.TABLE_NAME, values, selection,
                            selectionArgs);
                    break;
                }
                case EXCHANGE_RATE: {
                    rowsUpdated = db.update(ExchangeRateEntry.TABLE_NAME, values, selection,
                            selectionArgs);
                    break;
                }
                default:
                    throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
            if (rowsUpdated != 0) {
                getContext().getContentResolver().notifyChange(uri, null);
            }
            return rowsUpdated;
        }
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        synchronized (dbLock) {
            final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
            final int match = mUriMatcher.match(uri);
            switch (match) {
                case EXCHANGE_RATE: {
                    db.beginTransaction();
                    int i = 0;
                    try {
                        for (ContentValues value : values) {
                            long _id = db.update(
                                    ExchangeRateEntry.TABLE_NAME,
                                    value,
                                    ExchangeRateEntry.COLUMN_EXCHANGE_RATE_ID + " = ? ",
                                    new String[]{value.getAsString(ExchangeRateEntry.COLUMN_EXCHANGE_RATE_ID)});
                            if (_id == 0) {
                                value.put(ExchangeRateEntry.COLUMN_EXCHANGE_IS_MANUAL, 0);
                                db.insert(
                                        ExchangeRateEntry.TABLE_NAME,
                                        null,
                                        value);
                            }
                        }
                        db.setTransactionSuccessful();
                    } finally {
                        db.endTransaction();
                    }
                    getContext().getContentResolver().notifyChange(uri, null);
                    return i;
                }
                default:
                    return super.bulkInsert(uri, values);
            }
        }
    }
}
