package com.keithvongola.android.moneydiary.databases;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class MoneyContract {
    public static final String CONTENT_AUTHORITY = "com.keithvongola.android.moneydiary";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_ACCOUNTS = "accounts";
    public static final String PATH_MAIN_BUDGETS = "main_budgets";
    public static final String PATH_SUB_BUDGETS = "sub_budgets";
    public static final String PATH_PLANS = "plans";
    public static final String PATH_SUB_PLANS = "sub_plans";
    public static final String PATH_TRANSACTION = "transaction";
    public static final String PATH_CURRENCY = "currency";
    public static final String PATH_EXCHANGE_RATE = "exchange_rate";

    public static final class AccountsEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ACCOUNTS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/"  + PATH_ACCOUNTS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ACCOUNTS;

        public static final String TABLE_NAME = "accounts";
        public static final String COLUMN_ACCOUNT_NAME = "accounts_name";
        public static final String COLUMN_ACCOUNT_TYPE = "accounts_type";
        public static final String COLUMN_ACCOUNT_INSTITUTION = "accounts_institution";
        public static final String COLUMN_ACCOUNT_CURRENCY = "accounts_currency";
        public static final String COLUMN_ACCOUNT_IS_ACTIVE= "accounts_is_active";

        public static String getAccountTypeFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static String getAccountIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static Uri buildAccountUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class MainBudgetsEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MAIN_BUDGETS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/"  + PATH_MAIN_BUDGETS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MAIN_BUDGETS;

        public static final String TABLE_NAME = "main_budgets";
        public static final String COLUMN_MAIN_BUDGET_IS_EXPENSE = "main_budgets_is_expense";
        public static final String COLUMN_MAIN_BUDGET_NAME = "main_budgets_name";
        public static final String COLUMN_MAIN_BUDGET_CURRENCY = "main_budgets_currency";
        public static final String COLUMN_MAIN_BUDGET_ICON= "main_budgets_icon";
        public static final String COLUMN_MAIN_BUDGET_IS_ACTIVE = "main_budgets_is_active";

        public static String getMainBudgetsIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static Uri buildMainBudgetsUriWithId(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildMainBudgetsUriWithType(boolean isExpense) {
            String type = isExpense ? "expense" :"income";
            return CONTENT_URI.buildUpon().appendPath("type").appendPath(type).build();
        }

        public static Integer getMainBudgetsTypeFromUri(Uri uri) {
            String type = uri.getPathSegments().get(2);
            if(type.equals("expense")) return 1; // Budget Expense
            else return 0; // Budget Income
        }

        public static Uri buildMainsBudgetsUriWithDate(String startDate, String endDate) {
            return CONTENT_URI.buildUpon()
                    .appendPath("date")
                    .appendQueryParameter("START_DATE",startDate)
                    .appendQueryParameter("END_DATE",endDate).build();
        }

        public static String getMainsBudgetsStartDateFromUri(Uri uri) {
            return uri.getQueryParameter("START_DATE");
        }

        public static String getMainsBudgetsEndDateFromUri(Uri uri) {
            return uri.getQueryParameter("END_DATE");
        }

    }

    public static final class SubBudgetsEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SUB_BUDGETS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/"  + PATH_SUB_BUDGETS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SUB_BUDGETS;

        public static final String TABLE_NAME = "sub_budgets";
        public static final String COLUMN_SUB_BUDGET_NAME = "sub_budgets_name";
        public static final String COLUMN_SUB_BUDGET_PARENT_ID = "sub_budgets_parent_id";
        public static final String COLUMN_SUB_BUDGET_CURRENCY = "sub_budgets_currency";
        public static final String COLUMN_SUB_BUDGET_AMOUNT = "sub_budgets_amount";
        public static final String COLUMN_SUB_BUDGET_ICON = "sub_budgets_icon";
        public static final String COLUMN_SUB_BUDGET_IS_ACTIVE = "sub_budgets_is_active";

        public static String getSubBudgetsIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static Uri buildSubBudgetsUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildSubBudgetsUriWithParentID(long id) {
            Uri uri = Uri.withAppendedPath(CONTENT_URI,"parentId");
            return ContentUris.withAppendedId(uri, id);
        }

        public static String getSubBudgetsIdParentIDFromUri(Uri uri) {
            return uri.getPathSegments().get(2);
        }

        public static Uri buildSubBudgetsUriWithType(long id) {
            Uri uri = Uri.withAppendedPath(CONTENT_URI,"list");
            return ContentUris.withAppendedId(uri, id);
        }

        public static String getSubBudgetsTypeFromUri(Uri uri) {
            return uri.getPathSegments().get(2);
        }
    }

    public static final class PlansEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_PLANS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/"  + PATH_PLANS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PLANS;

        public static final String TABLE_NAME = "plans";
        public static final String COLUMN_PLANS_STATUS = "plans_status";
        public static final String COLUMN_PLANS_NAME = "plans_name";
        public static final String COLUMN_PLANS_DATE_START = "plans_date_start";
        public static final String COLUMN_PLANS_TERMS = "plans_terms";
        public static final String COLUMN_PLANS_CURRENCY = "plans_currency";
        public static final String COLUMN_PLANS_ICON = "plans_icon";

        public static Uri buildPlansUriWithId(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static String getPlansIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static Uri buildPlansUriWithStatus(int id) {
            Uri uri = Uri.withAppendedPath(CONTENT_URI, "status");
            return ContentUris.withAppendedId(uri, id);
        }

        public static String getStatusFromUri(Uri uri) {
            return uri.getPathSegments().get(2);
        }
    }

    public static final class SubPlansEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SUB_PLANS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/"  + PATH_SUB_PLANS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SUB_PLANS;

        public static final String TABLE_NAME = "sub_plans";
        public static final String COLUMN_SUB_PLANS_PARENT= "sub_plans_parent";
        public static final String COLUMN_SUB_PLANS_NAME = "sub_plans_name";
        public static final String COLUMN_PLANS_CURRENCY = "sub_plans_currency";
        public static final String COLUMN_SUB_PLANS_AMOUNT = "sub_plans_amount";
        public static final String COLUMN_SUB_PLANS_ICON = "sub_plans_icon";

        public static Uri buildSubPlansUriWithId(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static String getSubPlansIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static Uri buildSubPlansUriWithParentID(long id) {
            Uri uri = Uri.withAppendedPath(CONTENT_URI,"parentId");
            return ContentUris.withAppendedId(uri, id);
        }

        public static String getSubPlansParentIDFromUri(Uri uri) {
            return uri.getPathSegments().get(2);
        }
    }

    public static final class TransactionEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TRANSACTION).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/"  + PATH_TRANSACTION;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRANSACTION;

        public static final String TABLE_NAME = "transactions";
        public static final String COLUMN_TRANSACTION_TYPE = "transaction_type";
        public static final String COLUMN_TRANSACTION_CURRENCY = "transaction_currency";
        public static final String COLUMN_TRANSACTION_MAIN_ACCOUNT = "transaction_main_account";
        public static final String COLUMN_TRANSACTION_SUB_ACCOUNT = "transaction_sub_account";
        public static final String COLUMN_TRANSACTION_MAIN_CAT = "transaction_main_category";
        public static final String COLUMN_TRANSACTION_SUB_CAT = "transaction_sub_category";
        public static final String COLUMN_TRANSACTION_DATE = "transaction_date";
        public static final String COLUMN_TRANSACTION_AMOUNT = "transaction_amount";
        public static final String COLUMN_TRANSACTION_PLACE = "transaction_place";
        public static final String COLUMN_TRANSACTION_NOTES = "transaction_notes";
        public static final String COLUMN_TRANSACTION_PHOTO_DIR = "transaction_photo_dir";

        public static Uri buildTransactionUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static String getTransactionIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static Uri buildTransactionUriWithAccountID(long id) {
            Uri uri = Uri.withAppendedPath(CONTENT_URI,"account");
            return ContentUris.withAppendedId(uri, id);
        }

        public static String getTransactionAccountFromUri(Uri uri) {
            return uri.getPathSegments().get(2);
        }

        public static Uri buildTransactionUriWithDate(String startDate, String endDate) {
            return CONTENT_URI.buildUpon()
                    .appendPath("date")
                    .appendQueryParameter("START_DATE",startDate)
                    .appendQueryParameter("END_DATE",endDate).build();
        }

        public static Uri buildTransactionUriWithAccountIDAndDate(long id, String startDate, String endDate) {
            return CONTENT_URI.buildUpon()
                    .appendPath("account")
                    .appendPath(String.valueOf(id))
                    .appendPath("date")
                    .appendQueryParameter("START_DATE",startDate)
                    .appendQueryParameter("END_DATE",endDate).build();
        }

        public static String getTransactionStartDateFromUriWithAccount(Uri uri) {
            return uri.getQueryParameter("START_DATE");
        }

        public static String getTransactionEndDateFromUriWithAccount(Uri uri) {
            return uri.getQueryParameter("END_DATE");
        }

        public static Uri buildTransactionUriByTypeAndDate() {
            return CONTENT_URI.buildUpon().appendPath("overview").build();
        }
    }

    public static final class CurrencyEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_CURRENCY).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/"  + PATH_CURRENCY;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CURRENCY;

        public static final String TABLE_NAME = "currency";
        public static final String COLUMN_CURRENCY_ALPHA_CODE= "currency_alpha_code";
        public static final String COLUMN_CURRENECY_NUM_CODE = "currency_numeric_code";
        public static final String COLUMN_CURRENCY_UNIT = "currency_unit";
        public static final String COLUMN_CURRENCY_COUNTRY_CODE= "currency_country_code";

        public static Uri buildCurrencyUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static String getCurrencyIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    public static final class ExchangeRateEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_EXCHANGE_RATE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/"  + PATH_EXCHANGE_RATE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_EXCHANGE_RATE;

        public static final String TABLE_NAME = "exchange_rate";
        public static final String COLUMN_EXCHANGE_RATE_ID = "exchange_rate_id";
        public static final String COLUMN_EXCHANGE_RATE_NAME = "exchange_rate_name";
        public static final String COLUMN_EXCHANGE_IS_MANUAL = "exchange_rate_is_manual";
        public static final String COLUMN_EXCHANGE_ASK = "exchange_rate_ask";
        public static final String COLUMN_EXCHANGE_BID = "exchange_rate_bid";
        public static final String COLUMN_EXCHANGE_MANUAL_ASK = "exchange_rate_manual_ask";

        public static Uri buildExchangeRateUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildExchangeRateUri(String exchangeRateId) {
            return CONTENT_URI.buildUpon()
                    .appendPath(exchangeRateId)
                    .build();
        }

        public static String getExchangeRateIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }
}
