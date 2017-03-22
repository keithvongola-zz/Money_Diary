package com.keithvongola.android.moneydiary.databases;


import com.keithvongola.android.moneydiary.databases.MoneyContract.AccountsEntry;
import com.keithvongola.android.moneydiary.databases.MoneyContract.CurrencyEntry;
import com.keithvongola.android.moneydiary.databases.MoneyContract.ExchangeRateEntry;
import com.keithvongola.android.moneydiary.databases.MoneyContract.MainBudgetsEntry;
import com.keithvongola.android.moneydiary.databases.MoneyContract.PlansEntry;
import com.keithvongola.android.moneydiary.databases.MoneyContract.SubBudgetsEntry;
import com.keithvongola.android.moneydiary.databases.MoneyContract.SubPlansEntry;
import com.keithvongola.android.moneydiary.databases.MoneyContract.TransactionEntry;

import static com.keithvongola.android.moneydiary.Utility.getCurrentYear;
import static com.keithvongola.android.moneydiary.databases.MoneyContract.AccountsEntry.COLUMN_ACCOUNT_TYPE;
import static com.keithvongola.android.moneydiary.databases.MoneyContract.MainBudgetsEntry.COLUMN_MAIN_BUDGET_CURRENCY;

public class dbUtility {
    //accounts._id = ?
    public static final String sAccountIdSelection =
            AccountsEntry.TABLE_NAME + "." + AccountsEntry._ID + " = ? ";

    //accounts.account_type = ?
    public static final String sAccountTypeSelection =
            AccountsEntry.TABLE_NAME + "." + COLUMN_ACCOUNT_TYPE + " = ? ";

    //main_budgets._id = ?
    public static final String sMainBudgetsIdSelection =
            MainBudgetsEntry.TABLE_NAME + "." + MainBudgetsEntry._ID + " = ? ";

    //sub_budgets._id = ?
    public static final String sSubBudgetsIdSelection =
            SubBudgetsEntry.TABLE_NAME + "." + SubBudgetsEntry._ID + " = ? ";

    //plans._id = ?
    public static final String sPlansIdSelection =
            PlansEntry.TABLE_NAME + "." + PlansEntry._ID + " = ? ";

    //plans.plans_status = ?
    public static final String sPlansStatusSelection =
            PlansEntry.TABLE_NAME + "." + PlansEntry.COLUMN_PLANS_STATUS + " = ? ";

    //sub_plans._id = ?
    public static final String sSubPlansIdSelection =
            SubPlansEntry.TABLE_NAME + "." + SubPlansEntry._ID + " = ? ";

    //sub_plans.sub_plans_parent = ?
    public static final String sSubPlansParentIdSelection =
            SubPlansEntry.TABLE_NAME + "." + SubPlansEntry.COLUMN_SUB_PLANS_PARENT + " = ? ";

    //sub_plans._id = ?
    public static final String sSubPlansWithID =
            SubPlansEntry.TABLE_NAME + "." + SubPlansEntry._ID + " = ? ";

    //transaction._id = ?
    public static final String sTransactionIdSelection =
            TransactionEntry.TABLE_NAME + "." + TransactionEntry._ID + " = ? ";

    //transaction.transaction_account = ?
    public static final String sTransactionAccountSelection =
            TransactionEntry.TABLE_NAME + "." + TransactionEntry.COLUMN_TRANSACTION_MAIN_ACCOUNT + " = ? ";

    //transaction.transaction_sub_account = ?
    public static final String sTransactionSubAccountSelection =
            TransactionEntry.TABLE_NAME + "." + TransactionEntry.COLUMN_TRANSACTION_SUB_ACCOUNT + " = ? ";

    //transaction.transaction_main_category = ?
    public static final String sTransactionMainCategory =
            TransactionEntry.TABLE_NAME + "." + TransactionEntry.COLUMN_TRANSACTION_MAIN_CAT + " = ? ";

    //transaction.transaction_sub_category = ?
    public static final String sTransactionSubCategory =
            TransactionEntry.TABLE_NAME + "." + TransactionEntry.COLUMN_TRANSACTION_SUB_CAT + " = ? ";

    //sub_budget.transaction_main_category = ?
    public static final String sSubBudgetParentIdSelection =
            SubBudgetsEntry.TABLE_NAME + "." + SubBudgetsEntry.COLUMN_SUB_BUDGET_PARENT_ID + " = ? ";

    public static final String sSubBudgetsList =
            "SELECT " + SubBudgetsEntry.TABLE_NAME + "." + SubBudgetsEntry.COLUMN_SUB_BUDGET_PARENT_ID + " as parentId, "
                    + MainBudgetsEntry.TABLE_NAME + "." + MainBudgetsEntry.COLUMN_MAIN_BUDGET_NAME + ", "
                    + SubBudgetsEntry.TABLE_NAME + "." + SubBudgetsEntry._ID + ", "
                    + SubBudgetsEntry.TABLE_NAME + "." + SubBudgetsEntry.COLUMN_SUB_BUDGET_NAME + ", "
                    + SubBudgetsEntry.TABLE_NAME + "." + SubBudgetsEntry.COLUMN_SUB_BUDGET_CURRENCY
                    + " FROM " + SubBudgetsEntry.TABLE_NAME
                    + " LEFT JOIN " + MainBudgetsEntry.TABLE_NAME + " ON "
                    + " parentId = " + MainBudgetsEntry.TABLE_NAME + "." + MainBudgetsEntry._ID
                    + " WHERE " + MainBudgetsEntry.TABLE_NAME + "." + MainBudgetsEntry.COLUMN_MAIN_BUDGET_IS_EXPENSE + " = ? "
                    + " AND " + MainBudgetsEntry.TABLE_NAME + "." + MainBudgetsEntry.COLUMN_MAIN_BUDGET_IS_ACTIVE + " = 1 "
                    + " ORDER BY parentId ASC";

    //currency.currency_id = ?
    public static final String sCurrencyIdSelection =
            CurrencyEntry.TABLE_NAME + "." + ExchangeRateEntry.COLUMN_EXCHANGE_RATE_ID + " = ? ";

    //exchange_rate._id = ?
    public static final String sExchangeRateIdSelection =
            ExchangeRateEntry.TABLE_NAME + "." + ExchangeRateEntry.COLUMN_EXCHANGE_RATE_ID + " = ? ";

    //exchange_rate.exchange_rate_is_manual = ?
    public static final String sExchangeRateIsManualSelection =
            ExchangeRateEntry.TABLE_NAME + "." + ExchangeRateEntry.COLUMN_EXCHANGE_IS_MANUAL + " = ? ";

    //exchange_rate.exchange_rate_name = ?
    public static final String sExchangeRateNameSelection =
            ExchangeRateEntry.TABLE_NAME + "." + ExchangeRateEntry.COLUMN_EXCHANGE_RATE_NAME + " = ?";

    static final String sMainBudgetsWithType = "SELECT " + MainBudgetsEntry.TABLE_NAME + "." + MainBudgetsEntry._ID + ", "
            + MainBudgetsEntry.TABLE_NAME + "." + MainBudgetsEntry.COLUMN_MAIN_BUDGET_NAME + ", "
            + MainBudgetsEntry.TABLE_NAME + "." + COLUMN_MAIN_BUDGET_CURRENCY + ", "
            + "subbudgets.total_amount" + ", "
            + "SUM(" + TransactionEntry.TABLE_NAME + "." + TransactionEntry.COLUMN_TRANSACTION_AMOUNT + ")" + ", "
            + MainBudgetsEntry.TABLE_NAME + "." + MainBudgetsEntry.COLUMN_MAIN_BUDGET_IS_EXPENSE + ", "
            + MainBudgetsEntry.TABLE_NAME + "." + MainBudgetsEntry.COLUMN_MAIN_BUDGET_ICON + ", "
            + CurrencyEntry.TABLE_NAME + "." + CurrencyEntry.COLUMN_CURRENCY_UNIT

            + " FROM " + MainBudgetsEntry.TABLE_NAME + " LEFT JOIN "
            + "(SELECT " + SubBudgetsEntry.TABLE_NAME + "." + SubBudgetsEntry.COLUMN_SUB_BUDGET_PARENT_ID + " as _id,"
            + " SUM(" + SubBudgetsEntry.TABLE_NAME + "." + SubBudgetsEntry.COLUMN_SUB_BUDGET_AMOUNT + ") as total_amount"
            + " FROM " + SubBudgetsEntry.TABLE_NAME
            + " GROUP BY " + SubBudgetsEntry.TABLE_NAME + "." + SubBudgetsEntry.COLUMN_SUB_BUDGET_PARENT_ID + ") as subbudgets"
            + " ON " + MainBudgetsEntry.TABLE_NAME + "." + MainBudgetsEntry._ID + " = " + "subbudgets._id"

            + " LEFT JOIN " + TransactionEntry.TABLE_NAME
            + " ON " + MainBudgetsEntry.TABLE_NAME + "." + MainBudgetsEntry._ID + " = "
            + TransactionEntry.TABLE_NAME + "." + TransactionEntry.COLUMN_TRANSACTION_MAIN_CAT
            + " AND " + " date(datetime(" + TransactionEntry.TABLE_NAME + "." + TransactionEntry.COLUMN_TRANSACTION_DATE
            + "/1000 , 'unixepoch')) >= date('now','start of month','-1 day')"
            + " AND " + " date(datetime(" + TransactionEntry.TABLE_NAME + "." + TransactionEntry.COLUMN_TRANSACTION_DATE
            + "/1000 , 'unixepoch')) < date('now','start of month','+1 month','-1 day')"
            + " AND " + TransactionEntry.TABLE_NAME + "." + TransactionEntry.COLUMN_TRANSACTION_TYPE + " != 3"

            + " LEFT JOIN " + CurrencyEntry.TABLE_NAME
            + " ON " + MainBudgetsEntry.TABLE_NAME + "." + COLUMN_MAIN_BUDGET_CURRENCY + " = "
            + CurrencyEntry.TABLE_NAME + "." + CurrencyEntry.COLUMN_CURRENCY_ALPHA_CODE
            + " WHERE " + MainBudgetsEntry.TABLE_NAME + "." + MainBudgetsEntry.COLUMN_MAIN_BUDGET_IS_EXPENSE + " =?"
            + " AND " + MainBudgetsEntry.TABLE_NAME + "." + MainBudgetsEntry.COLUMN_MAIN_BUDGET_IS_ACTIVE + " = 1 "

            + " GROUP BY " + MainBudgetsEntry.TABLE_NAME + "." + MainBudgetsEntry._ID;

    public static String sMainBudgetsWithDate(String startDate, String endDate) {
        return "SELECT " + MainBudgetsEntry.TABLE_NAME + "." + MainBudgetsEntry._ID + " as budgetId, "
                + MainBudgetsEntry.TABLE_NAME + "." + MainBudgetsEntry.COLUMN_MAIN_BUDGET_NAME + ", "
                + TransactionEntry.TABLE_NAME + "." + TransactionEntry.COLUMN_TRANSACTION_CURRENCY + " as currency, "
                + CurrencyEntry.TABLE_NAME + "." + CurrencyEntry.COLUMN_CURRENCY_UNIT + ", "
                + " SUM(" + TransactionEntry.TABLE_NAME + "." + TransactionEntry.COLUMN_TRANSACTION_AMOUNT + ")"
                + " FROM " + TransactionEntry.TABLE_NAME + " LEFT JOIN " + MainBudgetsEntry.TABLE_NAME
                + " ON " + TransactionEntry.TABLE_NAME + "." + TransactionEntry.COLUMN_TRANSACTION_MAIN_CAT + " = "
                + MainBudgetsEntry.TABLE_NAME + "." + MainBudgetsEntry._ID
                + " LEFT JOIN " + CurrencyEntry.TABLE_NAME
                + " ON " + TransactionEntry.TABLE_NAME + "." + TransactionEntry.COLUMN_TRANSACTION_CURRENCY + " = "
                + CurrencyEntry.TABLE_NAME + "." + CurrencyEntry.COLUMN_CURRENCY_ALPHA_CODE
                + " WHERE " + MainBudgetsEntry.TABLE_NAME + "." + MainBudgetsEntry.COLUMN_MAIN_BUDGET_IS_EXPENSE + " = 1"
                + " AND " + MainBudgetsEntry.TABLE_NAME + "." + MainBudgetsEntry.COLUMN_MAIN_BUDGET_IS_ACTIVE + " = 1 "
                + " AND " + " date(datetime(" + TransactionEntry.TABLE_NAME + "." + TransactionEntry.COLUMN_TRANSACTION_DATE + "/1000 , 'unixepoch')) >= date('" + startDate + "')"
                + " AND " + " date(datetime(" + TransactionEntry.TABLE_NAME + "." + TransactionEntry.COLUMN_TRANSACTION_DATE + "/1000 , 'unixepoch')) <= date('" + endDate + "')"
                + " AND " + TransactionEntry.TABLE_NAME + "." + TransactionEntry.COLUMN_TRANSACTION_TYPE + " = 0"
                + " GROUP BY budgetId , currency";
    }

    public static final String sSubBudgetsWithParentID =
            "SELECT " + SubBudgetsEntry.TABLE_NAME + "." + SubBudgetsEntry.COLUMN_SUB_BUDGET_PARENT_ID + ", "
                    + SubBudgetsEntry.TABLE_NAME + "." + SubBudgetsEntry._ID + ", "
                    + MainBudgetsEntry.TABLE_NAME + "." + MainBudgetsEntry.COLUMN_MAIN_BUDGET_NAME + ", "
                    + SubBudgetsEntry.TABLE_NAME + "." + SubBudgetsEntry.COLUMN_SUB_BUDGET_NAME + ", "
                    + SubBudgetsEntry.TABLE_NAME + "." + SubBudgetsEntry.COLUMN_SUB_BUDGET_CURRENCY + ", "
                    + CurrencyEntry.TABLE_NAME + "." + CurrencyEntry.COLUMN_CURRENCY_UNIT + ", "
                    + SubBudgetsEntry.TABLE_NAME + "." + SubBudgetsEntry.COLUMN_SUB_BUDGET_ICON + ","
                    + SubBudgetsEntry.TABLE_NAME + "." + SubBudgetsEntry.COLUMN_SUB_BUDGET_AMOUNT + ", "
                    + "SUM(" + TransactionEntry.TABLE_NAME + "." + TransactionEntry.COLUMN_TRANSACTION_AMOUNT + ")" + ", "
                    + MainBudgetsEntry.TABLE_NAME + "." + MainBudgetsEntry.COLUMN_MAIN_BUDGET_IS_EXPENSE

                    + " FROM " + SubBudgetsEntry.TABLE_NAME + " LEFT JOIN " + TransactionEntry.TABLE_NAME
                    + " ON " + SubBudgetsEntry.TABLE_NAME + "." + SubBudgetsEntry._ID + " = "
                    + TransactionEntry.TABLE_NAME + "." + TransactionEntry.COLUMN_TRANSACTION_SUB_CAT
                    + " AND " + SubBudgetsEntry.TABLE_NAME + "." + SubBudgetsEntry.COLUMN_SUB_BUDGET_PARENT_ID + " = "
                    + TransactionEntry.TABLE_NAME + "." + TransactionEntry.COLUMN_TRANSACTION_MAIN_CAT
                    + " AND " + " date(datetime(" + TransactionEntry.TABLE_NAME + "." + TransactionEntry.COLUMN_TRANSACTION_DATE
                    + "/1000 , 'unixepoch')) >= date('now','start of month','-1 day')"
                    + " AND " + " date(datetime(" + TransactionEntry.TABLE_NAME + "." + TransactionEntry.COLUMN_TRANSACTION_DATE
                    + "/1000 , 'unixepoch')) < date('now','start of month','+1 month','-1 day')"
                    + " AND " + TransactionEntry.TABLE_NAME + "." + TransactionEntry.COLUMN_TRANSACTION_TYPE + " != 3"

                    + " LEFT JOIN " + MainBudgetsEntry.TABLE_NAME
                    + " ON " + SubBudgetsEntry.TABLE_NAME + "." + SubBudgetsEntry.COLUMN_SUB_BUDGET_PARENT_ID + " = "
                    + MainBudgetsEntry.TABLE_NAME + "." + MainBudgetsEntry._ID

                    + " LEFT JOIN " + CurrencyEntry.TABLE_NAME
                    + " ON " + SubBudgetsEntry.TABLE_NAME + "." + SubBudgetsEntry.COLUMN_SUB_BUDGET_CURRENCY + " = "
                    + CurrencyEntry.TABLE_NAME + "." + CurrencyEntry.COLUMN_CURRENCY_ALPHA_CODE
                    + " WHERE " + SubBudgetsEntry.TABLE_NAME + "." + SubBudgetsEntry.COLUMN_SUB_BUDGET_PARENT_ID + " =? "
                    + " AND " + SubBudgetsEntry.TABLE_NAME + "." + SubBudgetsEntry.COLUMN_SUB_BUDGET_IS_ACTIVE + " = 1"
                    + " GROUP BY " + SubBudgetsEntry.TABLE_NAME + "." + SubBudgetsEntry._ID;

    public static final String sAccountsWithAmount =
            "SELECT " + AccountsEntry.TABLE_NAME + ".* " + ", "
                    + CurrencyEntry.TABLE_NAME + "." + CurrencyEntry.COLUMN_CURRENCY_UNIT + ", "
                    + "SUM(" + TransactionEntry.TABLE_NAME + "." + TransactionEntry.COLUMN_TRANSACTION_AMOUNT + "),"
                    + "saving.amount"

                    + " FROM " + AccountsEntry.TABLE_NAME

                    + " LEFT JOIN " + TransactionEntry.TABLE_NAME
                    + " ON " + AccountsEntry.TABLE_NAME + "." + AccountsEntry._ID + " = "
                    + TransactionEntry.TABLE_NAME + "." + TransactionEntry.COLUMN_TRANSACTION_MAIN_ACCOUNT

                    + " LEFT JOIN " + CurrencyEntry.TABLE_NAME
                    + " ON " + AccountsEntry.TABLE_NAME + "." + AccountsEntry.COLUMN_ACCOUNT_CURRENCY + " = "
                    + CurrencyEntry.TABLE_NAME + "." + CurrencyEntry.COLUMN_CURRENCY_ALPHA_CODE

                    + " LEFT JOIN (SELECT " + TransactionEntry.TABLE_NAME + "." + TransactionEntry.COLUMN_TRANSACTION_MAIN_ACCOUNT + " as _id,"
                    + " SUM( " + TransactionEntry.TABLE_NAME + "." + TransactionEntry.COLUMN_TRANSACTION_AMOUNT + ")as amount"
                    + " FROM " + TransactionEntry.TABLE_NAME
                    + " WHERE " + TransactionEntry.TABLE_NAME + "." + TransactionEntry.COLUMN_TRANSACTION_TYPE + " = 3 "
                    + " GROUP BY " + TransactionEntry.TABLE_NAME + "." + TransactionEntry.COLUMN_TRANSACTION_MAIN_ACCOUNT + ") as saving"
                    + " ON " + AccountsEntry.TABLE_NAME + "." + AccountsEntry._ID + " = saving._id"

                    + " WHERE " + AccountsEntry.TABLE_NAME + "." + AccountsEntry.COLUMN_ACCOUNT_IS_ACTIVE + " = 1 "
                    + " GROUP BY " + AccountsEntry.TABLE_NAME + "." + AccountsEntry._ID
                    + " ORDER BY " + AccountsEntry.COLUMN_ACCOUNT_TYPE + " ASC" + " , " + AccountsEntry._ID + " ASC";

    public static final String sPlansWithStatus = "SELECT " + PlansEntry.TABLE_NAME + ".*" + ", "
            + "subplans.target_amount" + ", "
            + "SUM(" + TransactionEntry.TABLE_NAME + "." + TransactionEntry.COLUMN_TRANSACTION_AMOUNT + ")" + ", "
            + "current_saving.amount" + ", "
            + CurrencyEntry.TABLE_NAME + "." + CurrencyEntry.COLUMN_CURRENCY_UNIT

            + " FROM " + PlansEntry.TABLE_NAME + " LEFT JOIN "
            + "(SELECT " + SubPlansEntry.TABLE_NAME + "." + SubPlansEntry.COLUMN_SUB_PLANS_PARENT + " as _id,"
            + " SUM(" + SubPlansEntry.TABLE_NAME + "." + SubPlansEntry.COLUMN_SUB_PLANS_AMOUNT + ") as target_amount"
            + " FROM " + SubPlansEntry.TABLE_NAME
            + " GROUP BY " + SubPlansEntry.TABLE_NAME + "." + SubPlansEntry.COLUMN_SUB_PLANS_PARENT + ") as subplans"
            + " ON " + PlansEntry.TABLE_NAME + "." + PlansEntry._ID + " = " + "subplans._id"

            + " LEFT JOIN " + TransactionEntry.TABLE_NAME
            + " ON " + PlansEntry.TABLE_NAME + "." + PlansEntry._ID + " = "
            + TransactionEntry.TABLE_NAME + "." + TransactionEntry.COLUMN_TRANSACTION_SUB_CAT
            + " AND " + TransactionEntry.TABLE_NAME + "." + TransactionEntry.COLUMN_TRANSACTION_TYPE + " = 3 "

            + " LEFT JOIN " + CurrencyEntry.TABLE_NAME
            + " ON " + PlansEntry.TABLE_NAME + "." + PlansEntry.COLUMN_PLANS_CURRENCY + " = "
            + CurrencyEntry.TABLE_NAME + "." + CurrencyEntry.COLUMN_CURRENCY_ALPHA_CODE

            + " LEFT JOIN "
            + "(SELECT " + TransactionEntry.TABLE_NAME + "." + TransactionEntry.COLUMN_TRANSACTION_SUB_CAT + " as _id,"
            + " SUM(" + TransactionEntry.TABLE_NAME + "." + TransactionEntry.COLUMN_TRANSACTION_AMOUNT + ") as amount"
            + " FROM " + TransactionEntry.TABLE_NAME
            + " WHERE " + TransactionEntry.TABLE_NAME + "." + TransactionEntry.COLUMN_TRANSACTION_TYPE + " = 3 "
            + " AND " + " date(datetime(" + TransactionEntry.TABLE_NAME + "." + TransactionEntry.COLUMN_TRANSACTION_DATE
            + "/1000 , 'unixepoch')) >= date('now','start of month','-1 day')"
            + " AND " + " date(datetime(" + TransactionEntry.TABLE_NAME + "." + TransactionEntry.COLUMN_TRANSACTION_DATE
            + "/1000 , 'unixepoch')) < date('now','start of month','+1 month','-1 day')"
            + " GROUP BY " + TransactionEntry.TABLE_NAME + "." + TransactionEntry.COLUMN_TRANSACTION_SUB_CAT + ") as current_saving"
            + " ON " + PlansEntry.TABLE_NAME + "." + PlansEntry._ID + " = " + "current_saving._id"

            + " WHERE " + PlansEntry.TABLE_NAME + "." + PlansEntry.COLUMN_PLANS_STATUS + " = 1 "
            + " GROUP BY " + PlansEntry.TABLE_NAME + "." + PlansEntry._ID;


    public static final String sSubPlansWithParentID = "SELECT " + SubPlansEntry.TABLE_NAME + ".*" + ", "
            + CurrencyEntry.TABLE_NAME + "." + CurrencyEntry.COLUMN_CURRENCY_UNIT
            + " FROM " + SubPlansEntry.TABLE_NAME
            + " LEFT JOIN " + CurrencyEntry.TABLE_NAME
            + " ON " + SubPlansEntry.TABLE_NAME + "." + SubPlansEntry.COLUMN_PLANS_CURRENCY + " = "
            + CurrencyEntry.TABLE_NAME + "." + CurrencyEntry.COLUMN_CURRENCY_ALPHA_CODE
            + " WHERE " + SubPlansEntry.TABLE_NAME + "." + SubPlansEntry.COLUMN_SUB_PLANS_PARENT + " =? ";

    public static final String sExchangeRate =
            "SELECT " + ExchangeRateEntry.TABLE_NAME + "." + ExchangeRateEntry._ID + ", "
                    + ExchangeRateEntry.TABLE_NAME + "." + ExchangeRateEntry.COLUMN_EXCHANGE_RATE_ID + ", "
                    + ExchangeRateEntry.TABLE_NAME + "." + ExchangeRateEntry.COLUMN_EXCHANGE_RATE_NAME + ", "
                    + ExchangeRateEntry.TABLE_NAME + "." + ExchangeRateEntry.COLUMN_EXCHANGE_BID + ", "
                    + ExchangeRateEntry.TABLE_NAME + "." + ExchangeRateEntry.COLUMN_EXCHANGE_IS_MANUAL + ", "
                    + ExchangeRateEntry.TABLE_NAME + "." + ExchangeRateEntry.COLUMN_EXCHANGE_MANUAL_ASK + ", "
                    + CurrencyEntry.TABLE_NAME + "." + CurrencyEntry.COLUMN_CURRENCY_ALPHA_CODE + ", "
                    + CurrencyEntry.TABLE_NAME + "." + CurrencyEntry.COLUMN_CURRENCY_COUNTRY_CODE
                    + " FROM " + ExchangeRateEntry.TABLE_NAME + " INNER JOIN " + CurrencyEntry.TABLE_NAME
                    + " ON " + CurrencyEntry.TABLE_NAME + "." + CurrencyEntry.COLUMN_CURRENCY_ALPHA_CODE + " = "
                    + "substr(" + ExchangeRateEntry.TABLE_NAME + "." + ExchangeRateEntry.COLUMN_EXCHANGE_RATE_ID + ",4,6)"
                    + " WHERE " + "substr(" + ExchangeRateEntry.TABLE_NAME + "." + ExchangeRateEntry.COLUMN_EXCHANGE_RATE_ID + ",1,3)" + " =? "
                    + " ORDER BY " + CurrencyEntry.TABLE_NAME + "." + CurrencyEntry.COLUMN_CURRENCY_ALPHA_CODE + " ASC";

    public static String sTransactionWithAccountID =
            "SELECT " + TransactionEntry.TABLE_NAME + ".*" + ", "
                    + SubBudgetsEntry.TABLE_NAME + "." + SubBudgetsEntry.COLUMN_SUB_BUDGET_ICON + ", "
                    + CurrencyEntry.TABLE_NAME + "." + CurrencyEntry.COLUMN_CURRENCY_UNIT + ", "
                    + SubBudgetsEntry.TABLE_NAME + "." + SubBudgetsEntry.COLUMN_SUB_BUDGET_NAME
                    + " FROM " + TransactionEntry.TABLE_NAME

                    + " LEFT JOIN " + CurrencyEntry.TABLE_NAME + " ON "
                    + TransactionEntry.TABLE_NAME + "." + TransactionEntry.COLUMN_TRANSACTION_CURRENCY
                    + " = " + CurrencyEntry.TABLE_NAME + "." + CurrencyEntry.COLUMN_CURRENCY_ALPHA_CODE

                    + " LEFT JOIN " + SubBudgetsEntry.TABLE_NAME + " ON "
                    + TransactionEntry.TABLE_NAME + "." + TransactionEntry.COLUMN_TRANSACTION_MAIN_CAT + " = "
                    + SubBudgetsEntry.TABLE_NAME + "." + SubBudgetsEntry.COLUMN_SUB_BUDGET_PARENT_ID
                    + " AND " + TransactionEntry.TABLE_NAME + "." + TransactionEntry.COLUMN_TRANSACTION_SUB_CAT + " = "
                    + SubBudgetsEntry.TABLE_NAME + "." + SubBudgetsEntry._ID

                    + " WHERE " + TransactionEntry.TABLE_NAME + "." + TransactionEntry.COLUMN_TRANSACTION_MAIN_ACCOUNT + " = ? "
                    + " ORDER BY " + TransactionEntry.TABLE_NAME + "." + TransactionEntry.COLUMN_TRANSACTION_DATE + " ASC";

    public static String sTransactionWithDate(String startDate, String endDate) {
        String sTransactionWithDate = "SELECT " + TransactionEntry.TABLE_NAME + ".*" + ", "
                + SubBudgetsEntry.TABLE_NAME + "." + SubBudgetsEntry.COLUMN_SUB_BUDGET_ICON + ", "
                + CurrencyEntry.TABLE_NAME + "." + CurrencyEntry.COLUMN_CURRENCY_UNIT + ", "
                + MainBudgetsEntry.TABLE_NAME + "." + MainBudgetsEntry.COLUMN_MAIN_BUDGET_NAME + ", "
                + SubBudgetsEntry.TABLE_NAME + "." + SubBudgetsEntry.COLUMN_SUB_BUDGET_NAME + ", "
                //column account name for main account
                + "(SELECT " + AccountsEntry.TABLE_NAME + "." + AccountsEntry.COLUMN_ACCOUNT_NAME
                + " FROM " + AccountsEntry.TABLE_NAME
                + " WHERE " + AccountsEntry.TABLE_NAME + "." + AccountsEntry._ID + " = "
                + TransactionEntry.TABLE_NAME + "." + TransactionEntry.COLUMN_TRANSACTION_MAIN_ACCOUNT + "), "
                //column account name for sub account
                + "(SELECT " + AccountsEntry.TABLE_NAME + "." + AccountsEntry.COLUMN_ACCOUNT_NAME
                + " FROM " + AccountsEntry.TABLE_NAME
                + " WHERE " + AccountsEntry.TABLE_NAME + "." + AccountsEntry._ID + " = "
                + TransactionEntry.TABLE_NAME + "." + TransactionEntry.COLUMN_TRANSACTION_SUB_ACCOUNT + "), "

                + PlansEntry.TABLE_NAME + "." + PlansEntry.COLUMN_PLANS_NAME

                + " FROM " + TransactionEntry.TABLE_NAME
                + " LEFT JOIN " + CurrencyEntry.TABLE_NAME + " ON "
                + TransactionEntry.TABLE_NAME + "." + TransactionEntry.COLUMN_TRANSACTION_CURRENCY
                + " = " + CurrencyEntry.TABLE_NAME + "." + CurrencyEntry.COLUMN_CURRENCY_ALPHA_CODE

                + " LEFT JOIN " + MainBudgetsEntry.TABLE_NAME + " ON "
                + TransactionEntry.TABLE_NAME + "." + TransactionEntry.COLUMN_TRANSACTION_MAIN_CAT + " = "
                + MainBudgetsEntry.TABLE_NAME + "." + MainBudgetsEntry._ID

                + " LEFT JOIN " + SubBudgetsEntry.TABLE_NAME + " ON "
                + TransactionEntry.TABLE_NAME + "." + TransactionEntry.COLUMN_TRANSACTION_MAIN_CAT + " = "
                + SubBudgetsEntry.TABLE_NAME + "." + SubBudgetsEntry.COLUMN_SUB_BUDGET_PARENT_ID
                + " AND " + TransactionEntry.TABLE_NAME + "." + TransactionEntry.COLUMN_TRANSACTION_SUB_CAT + " = "
                + SubBudgetsEntry.TABLE_NAME + "." + SubBudgetsEntry._ID

                + " LEFT JOIN " + PlansEntry.TABLE_NAME + " ON "
                + TransactionEntry.TABLE_NAME + "." + TransactionEntry.COLUMN_TRANSACTION_TYPE + " = 3"
                + " AND " + TransactionEntry.TABLE_NAME + "." + TransactionEntry.COLUMN_TRANSACTION_SUB_CAT + " = "
                + PlansEntry.TABLE_NAME + "." + PlansEntry._ID

                + " WHERE"
                + " date(datetime(" + TransactionEntry.TABLE_NAME + "." + TransactionEntry.COLUMN_TRANSACTION_DATE + "/1000 , 'unixepoch')) >= date('" + startDate + "')"
                + " AND date(datetime(" + TransactionEntry.TABLE_NAME + "." + TransactionEntry.COLUMN_TRANSACTION_DATE + "/1000 , 'unixepoch')) <= date('" + endDate + "')"
                + " ORDER BY " + TransactionEntry.TABLE_NAME + "." + TransactionEntry.COLUMN_TRANSACTION_DATE + " ASC";
        return sTransactionWithDate;
    }

    public static String sTransactionWithAccountIdAndDate(String startDate, String endDate) {
        String sTransactionWithAccountIdAndDate = "SELECT " + TransactionEntry.TABLE_NAME + ".*" + ", "
                + SubBudgetsEntry.TABLE_NAME + "." + SubBudgetsEntry.COLUMN_SUB_BUDGET_ICON + ", "
                + CurrencyEntry.TABLE_NAME + "." + CurrencyEntry.COLUMN_CURRENCY_UNIT + ", "
                + MainBudgetsEntry.TABLE_NAME + "." + MainBudgetsEntry.COLUMN_MAIN_BUDGET_NAME + ", "
                + SubBudgetsEntry.TABLE_NAME + "." + SubBudgetsEntry.COLUMN_SUB_BUDGET_NAME + ", "
                //column account name for main account
                + "(SELECT " + AccountsEntry.TABLE_NAME + "." + AccountsEntry.COLUMN_ACCOUNT_NAME
                + " FROM " + AccountsEntry.TABLE_NAME
                + " WHERE " + AccountsEntry.TABLE_NAME + "." + AccountsEntry._ID + " = "
                + TransactionEntry.TABLE_NAME + "." + TransactionEntry.COLUMN_TRANSACTION_MAIN_ACCOUNT + "), "
                //column account name for sub account
                + "(SELECT " + AccountsEntry.TABLE_NAME + "." + AccountsEntry.COLUMN_ACCOUNT_NAME
                + " FROM " + AccountsEntry.TABLE_NAME
                + " WHERE " + AccountsEntry.TABLE_NAME + "." + AccountsEntry._ID + " = "
                + TransactionEntry.TABLE_NAME + "." + TransactionEntry.COLUMN_TRANSACTION_SUB_ACCOUNT + "), "

                + PlansEntry.TABLE_NAME + "." + PlansEntry.COLUMN_PLANS_NAME

                + " FROM " + TransactionEntry.TABLE_NAME
                + " LEFT JOIN " + CurrencyEntry.TABLE_NAME + " ON "
                + TransactionEntry.TABLE_NAME + "." + TransactionEntry.COLUMN_TRANSACTION_CURRENCY
                + " = " + CurrencyEntry.TABLE_NAME + "." + CurrencyEntry.COLUMN_CURRENCY_ALPHA_CODE

                + " LEFT JOIN " + SubBudgetsEntry.TABLE_NAME + " ON "
                + TransactionEntry.TABLE_NAME + "." + TransactionEntry.COLUMN_TRANSACTION_SUB_CAT
                + " = " + SubBudgetsEntry.TABLE_NAME + "." + SubBudgetsEntry._ID
                + " AND " + TransactionEntry.TABLE_NAME + "." + TransactionEntry.COLUMN_TRANSACTION_MAIN_CAT
                + " = " + SubBudgetsEntry.TABLE_NAME + "." + SubBudgetsEntry.COLUMN_SUB_BUDGET_PARENT_ID

                + " LEFT JOIN " + MainBudgetsEntry.TABLE_NAME + " ON "
                + TransactionEntry.TABLE_NAME + "." + TransactionEntry.COLUMN_TRANSACTION_MAIN_CAT + " = "
                + MainBudgetsEntry.TABLE_NAME + "." + MainBudgetsEntry._ID

                + " LEFT JOIN " + PlansEntry.TABLE_NAME + " ON "
                + TransactionEntry.TABLE_NAME + "." + TransactionEntry.COLUMN_TRANSACTION_TYPE + " = 3"
                + " AND " + TransactionEntry.TABLE_NAME + "." + TransactionEntry.COLUMN_TRANSACTION_SUB_CAT + " = "
                + PlansEntry.TABLE_NAME + "." + PlansEntry._ID

                + " WHERE " + TransactionEntry.TABLE_NAME + "." + TransactionEntry.COLUMN_TRANSACTION_MAIN_ACCOUNT + " = ?"
                + " AND date(datetime(" + TransactionEntry.TABLE_NAME + "." + TransactionEntry.COLUMN_TRANSACTION_DATE + "/1000 , 'unixepoch')) >= date('" + startDate + "')"
                + " AND date(datetime(" + TransactionEntry.TABLE_NAME + "." + TransactionEntry.COLUMN_TRANSACTION_DATE + "/1000 , 'unixepoch')) < date('" + endDate + "')"
                + " ORDER BY " + TransactionEntry.TABLE_NAME + "." + TransactionEntry.COLUMN_TRANSACTION_DATE + " ASC";
        return sTransactionWithAccountIdAndDate;
    }

    public static String sTransactionByMonthAndType() {
        int currentYear = getCurrentYear();
        String sTransactionByMonthAndType =
                "SELECT " + "strftime('%m', date(datetime(" + TransactionEntry.TABLE_NAME + "." + TransactionEntry.COLUMN_TRANSACTION_DATE + "/1000 , 'unixepoch'))) as DATE, "
                        + TransactionEntry.TABLE_NAME + "." + TransactionEntry.COLUMN_TRANSACTION_TYPE + " as TYPE, "
                        + TransactionEntry.TABLE_NAME + "." + TransactionEntry.COLUMN_TRANSACTION_CURRENCY + " as CURRENCY, "
                        + CurrencyEntry.TABLE_NAME + "." + CurrencyEntry.COLUMN_CURRENCY_UNIT + ", "
                        + " SUM(" + TransactionEntry.TABLE_NAME + "." + TransactionEntry.COLUMN_TRANSACTION_AMOUNT + ") "

                        + " FROM " + TransactionEntry.TABLE_NAME
                        + " LEFT JOIN " + CurrencyEntry.TABLE_NAME + " ON "
                        + TransactionEntry.TABLE_NAME + "." + TransactionEntry.COLUMN_TRANSACTION_CURRENCY
                        + " = " + CurrencyEntry.TABLE_NAME + "." + CurrencyEntry.COLUMN_CURRENCY_ALPHA_CODE

                        + " WHERE " + " date(datetime(" + TransactionEntry.TABLE_NAME + "." + TransactionEntry.COLUMN_TRANSACTION_DATE + "/1000 , 'unixepoch')) >= date('" + currentYear + "-01-01" + "')"
                        + " AND date(datetime(" + TransactionEntry.TABLE_NAME + "." + TransactionEntry.COLUMN_TRANSACTION_DATE + "/1000 , 'unixepoch')) < date('" + currentYear + "-12-31" + "')"
                        + " AND " + TransactionEntry.TABLE_NAME + "." + TransactionEntry.COLUMN_TRANSACTION_SUB_CAT + " > 2 "
                        + " GROUP BY " + "TYPE, CURRENCY, DATE"
                        + " ORDER BY " + "DATE ASC";
        return sTransactionByMonthAndType;
    }
}