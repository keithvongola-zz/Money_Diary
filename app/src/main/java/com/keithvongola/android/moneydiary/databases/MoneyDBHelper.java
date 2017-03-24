package com.keithvongola.android.moneydiary.databases;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.keithvongola.android.moneydiary.R;
import com.keithvongola.android.moneydiary.databases.MoneyContract.PlansEntry;
import com.keithvongola.android.moneydiary.databases.MoneyContract.SubPlansEntry;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static com.keithvongola.android.moneydiary.Utility.formatDateStringAsLong;
import static com.keithvongola.android.moneydiary.databases.MoneyContract.AccountsEntry;
import static com.keithvongola.android.moneydiary.databases.MoneyContract.MainBudgetsEntry;
import static com.keithvongola.android.moneydiary.databases.MoneyContract.SubBudgetsEntry;
import static com.keithvongola.android.moneydiary.databases.MoneyContract.SubBudgetsEntry.COLUMN_SUB_BUDGET_PARENT_ID;

public class MoneyDBHelper extends SQLiteOpenHelper {
    private static String DB_PATH;

    private SQLiteDatabase moneyDataBase;

    private static final int DATABASE_VERSION = 1;
    public static final String DB_NAME = "money.db";
    private final Context context;
    
    public MoneyDBHelper(Context context) {
        super(context, DB_NAME,null,DATABASE_VERSION);
        this.context = context;
        DB_PATH = "/data/data/com.keithvongola.android.moneydiary/databases/";
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + AccountsEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MainBudgetsEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + SubBudgetsEntry.TABLE_NAME);
    }

    /**
     * Check if the database already exist to avoid re-copying the file each time you open the application.
     * @return true if it exists, false if it doesn't
     */
    private boolean checkDataBase(){
        SQLiteDatabase checkDB = null;
        File databasePath = context.getDatabasePath(DB_NAME);
        return databasePath.exists();
//        try{
//            String myPath = DB_PATH + DB_NAME;
//            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
//        }catch(SQLiteException e){
//        }
//        if(checkDB != null){
//            checkDB.close();
//        }
//        return checkDB != null;
    }

    private void copyDataBase() throws IOException{
        //Open local db as the input stream
        InputStream myInput = context.getAssets().open(DB_NAME);
        // Path to the just created empty db
        String outFileName = DB_PATH + DB_NAME;
        //Open the empty db as the output stream
        OutputStream myOutput = new FileOutputStream(outFileName);

        //transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer))>0){
            myOutput.write(buffer, 0, length);
        }

        //Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();
    }

    /**
     * Creates a empty database on the system and rewrites it with your own database.
     * */
    public void createDataBase() throws IOException{
        boolean dbExist = checkDataBase();
        if (!dbExist) {
            SQLiteDatabase db = this.getReadableDatabase();
            try {
                copyDataBase();
                insertDefaultValue(db);

            } catch (IOException e) {
                throw new Error("Error copying database");
            }
        }
    }

    public void openDataBase() throws SQLException {
        //Open the database
        String myPath = DB_PATH + DB_NAME;
        moneyDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
    }

    @Override
    public synchronized void close() {
        if(moneyDataBase != null)
            moneyDataBase.close();
        super.close();
    }

    private void insertDefaultValue(SQLiteDatabase db){
        String insertAccount = "insert into " + AccountsEntry.TABLE_NAME + "("
                + AccountsEntry.COLUMN_ACCOUNT_NAME + ","
                + AccountsEntry.COLUMN_ACCOUNT_TYPE + ","
                + AccountsEntry.COLUMN_ACCOUNT_INSTITUTION + ","
                + AccountsEntry.COLUMN_ACCOUNT_CURRENCY + ","
                + AccountsEntry.COLUMN_ACCOUNT_IS_ACTIVE + ")";

        db.execSQL(insertAccount + "values ( '" + context.getString(R.string.account_cash) + "' , 0 , '' , 'HKD', 1)");
        db.execSQL(insertAccount + "values ( '" + context.getString(R.string.account_checking) + "' , 1 , '' , 'HKD', 1)");
        db.execSQL(insertAccount + "values ( '" + context.getString(R.string.account_saving) + "' , 1 , '' , 'HKD', 1)");
        db.execSQL(insertAccount + "values ( '" + context.getString(R.string.accounts_visa) + "' , 2 , '' , 'HKD', 1)");

        //Default main budgets information
        String insertMainBudget = "insert into " + MainBudgetsEntry.TABLE_NAME + "("
                + MainBudgetsEntry.COLUMN_MAIN_BUDGET_IS_EXPENSE + ","
                + MainBudgetsEntry.COLUMN_MAIN_BUDGET_NAME + ","
                + MainBudgetsEntry.COLUMN_MAIN_BUDGET_CURRENCY + ","
                + MainBudgetsEntry.COLUMN_MAIN_BUDGET_ICON + ","
                + MainBudgetsEntry.COLUMN_MAIN_BUDGET_IS_ACTIVE + ")";

        db.execSQL(insertMainBudget + "values ( '1' , '" + context.getString(R.string.main_budget_food) + "' , 'HKD', " +  R.drawable.budget_6 + " , 1)");
        db.execSQL(insertMainBudget + "values ( '1' , '" + context.getString(R.string.main_budget_clothing) + "' , 'HKD', " +  R.drawable.budget_9 + " , 1)");
        db.execSQL(insertMainBudget + "values ( '1' , '" + context.getString(R.string.main_budget_transportation) + "' , 'HKD', " +  R.drawable.budget_23 + " , 1)");
        db.execSQL(insertMainBudget + "values ( '1' , '" + context.getString(R.string.main_budget_entertainment) + "' , 'HKD', " +  R.drawable.budget_32 + " , 1)");
        db.execSQL(insertMainBudget + "values ( '1' , '" + context.getString(R.string.main_budget_personal_care) + "' , 'HKD', " +  R.drawable.budget_42 + " , 1)");
        db.execSQL(insertMainBudget + "values ( '1' , '" + context.getString(R.string.main_budget_home) + "' , 'HKD', " +  R.drawable.budget_49 + " , 1)");
        db.execSQL(insertMainBudget + "values ( '1' , '" + context.getString(R.string.main_budget_utilities) + "' , 'HKD', " +  R.drawable.budget_61 + " , 1)");
        db.execSQL(insertMainBudget + "values ( '1' , '" + context.getString(R.string.main_budget_medicare) + "' , 'HKD', " +  R.drawable.budget_71 + " , 1)");
        db.execSQL(insertMainBudget + "values ( '1' , '" + context.getString(R.string.main_budget_miscellaneous) + "' , 'HKD', " +  R.drawable.budget_51 + " , 1)");
        db.execSQL(insertMainBudget + "values ( '1' , '" + context.getString(R.string.main_budget_insurance) + "' , 'HKD', " +  R.drawable.budget_78 + " , 1)");
        db.execSQL(insertMainBudget + "values ( '1' , '" + context.getString(R.string.main_budget_payments) + "' , 'HKD', " +  R.drawable.budget_80 + " , 1)");
        db.execSQL(insertMainBudget + "values ( '1' , '" + context.getString(R.string.main_budget_others) + "' , 'HKD', " +  R.drawable.budget_79 + " , 1)");

        db.execSQL(insertMainBudget + "values ( '0' , '" + context.getString(R.string.main_budget_jobs) + "' , 'HKD', " +  R.drawable.budget_81 + " , 1)");
        db.execSQL(insertMainBudget + "values ( '0' , '" + context.getString(R.string.main_budget_others) + "' , 'HKD', " +  R.drawable.budget_79 + " , 1)");

        //Default sub budgets information
        String insertSubBudget = "insert into " + SubBudgetsEntry.TABLE_NAME + "("
                + COLUMN_SUB_BUDGET_PARENT_ID + ","
                + SubBudgetsEntry.COLUMN_SUB_BUDGET_NAME + ","
                + SubBudgetsEntry.COLUMN_SUB_BUDGET_CURRENCY + ","
                + SubBudgetsEntry.COLUMN_SUB_BUDGET_AMOUNT + ","
                + SubBudgetsEntry.COLUMN_SUB_BUDGET_ICON + ","
                +SubBudgetsEntry.COLUMN_SUB_BUDGET_IS_ACTIVE + ")";

        db.execSQL(insertSubBudget + "values ( 4 , '" + context.getString(R.string.sub_budget_restaurants) + "' , 'HKD' , 0, " +  R.drawable.budget_1 + " , 1)");
        db.execSQL(insertSubBudget + "values ( 4 , '" + context.getString(R.string.sub_budget_snacks) + "' , 'HKD' , 0, " +  R.drawable.budget_8 + " , 1)");
        db.execSQL(insertSubBudget + "values ( 4 , '" + context.getString(R.string.sub_budget_beverage) + "' , 'HKD' , 0, " +  R.drawable.budget_4 + " , 1)");
        db.execSQL(insertSubBudget + "values ( 4 , '" + context.getString(R.string.sub_budget_groceries) + "' , 'HKD' , 0, " +  R.drawable.budget_3 + " , 1)");

        db.execSQL(insertSubBudget + "values ( 5 , '" + context.getString(R.string.sub_budget_shirts) + "' , 'HKD' , 0, " +  R.drawable.budget_9 + " , 1)");
        db.execSQL(insertSubBudget + "values ( 5 , '" + context.getString(R.string.sub_budget_pants) + "' , 'HKD' , 0, " +  R.drawable.budget_11 + " , 1)");
        db.execSQL(insertSubBudget + "values ( 5 , '" + context.getString(R.string.sub_budget_skirts) + "' , 'HKD' , 0, " +  R.drawable.budget_10 + " , 1)");
        db.execSQL(insertSubBudget + "values ( 5 , '" + context.getString(R.string.sub_budget_shoes) + "' , 'HKD' , 0, " +  R.drawable.budget_13 + " , 1)");
        db.execSQL(insertSubBudget + "values ( 5 , '" + context.getString(R.string.sub_budget_jewelry) + "' , 'HKD' , 0, " +  R.drawable.budget_15 + " , 1)");
        db.execSQL(insertSubBudget + "values ( 5 , '" + context.getString(R.string.sub_budget_watches) + "' , 'HKD' , 0, " +  R.drawable.budget_17 + " , 1)");
        db.execSQL(insertSubBudget + "values ( 5 , '" + context.getString(R.string.sub_budget_accessories) + "' , 'HKD' , 0, " +  R.drawable.budget_18 + " , 1)");

        db.execSQL(insertSubBudget + "values ( 6 , '" + context.getString(R.string.sub_budget_public_transport) + "' , 'HKD' , 0, " + R.drawable.budget_24 + " , 1)");
        db.execSQL(insertSubBudget + "values ( 6 , '" + context.getString(R.string.sub_budget_car_loan) + "' , 'HKD' , 0, " + R.drawable.budget_26 + " , 1)");
        db.execSQL(insertSubBudget + "values ( 6 , '" + context.getString(R.string.sub_budget_gasoline) + "' , 'HKD' , 0, " + R.drawable.budget_27 + " , 1)");
        db.execSQL(insertSubBudget + "values ( 6 , '" + context.getString(R.string.sub_budget_car_insurance) + "' , 'HKD' , 0, " + R.drawable.budget_29 + " , 1)");

        db.execSQL(insertSubBudget + "values ( 7 , '" + context.getString(R.string.sub_budget_hobbies) + "' , 'HKD' , 0, " + R.drawable.budget_34 + " , 1)");
        db.execSQL(insertSubBudget + "values ( 7 , '" + context.getString(R.string.sub_budget_gathering) + "' , 'HKD' , 0, " + R.drawable.budget_32 + " , 1)");
        db.execSQL(insertSubBudget + "values ( 7 , '" + context.getString(R.string.sub_budget_vacations) + "' , 'HKD' , 0, " + R.drawable.budget_40 + " , 1)");

        db.execSQL(insertSubBudget + "values ( 8 , '" + context.getString(R.string.sub_budget_cosmetics) + "' , 'HKD' , 0, " + R.drawable.budget_44 + " , 1)");

        db.execSQL(insertSubBudget + "values ( 9 , '" + context.getString(R.string.sub_budget_rent) + "' , 'HKD' , 0, " + R.drawable.budget_59 + " , 1)");
        db.execSQL(insertSubBudget + "values ( 9 , '" + context.getString(R.string.sub_budget_maintenance) + "' , 'HKD' , 0, " + R.drawable.budget_51 + " , 1)");
        db.execSQL(insertSubBudget + "values ( 9 , '" + context.getString(R.string.sub_budget_improvements) + "' , 'HKD' , 0, " + R.drawable.budget_53 + " , 1)");
        db.execSQL(insertSubBudget + "values ( 9 , '" + context.getString(R.string.sub_budget_management_fee) + "' , 'HKD' , 0, " + R.drawable.budget_52 + " , 1)");

        db.execSQL(insertSubBudget + "values ( 10 , '" + context.getString(R.string.sub_budget_electricity) + "' , 'HKD' , 0, " + R.drawable.budget_61 + " , 1)");
        db.execSQL(insertSubBudget + "values ( 10 , '" + context.getString(R.string.sub_budget_water) + "' , 'HKD' , 0, " + R.drawable.budget_62 + " , 1)");
        db.execSQL(insertSubBudget + "values ( 10 , '" + context.getString(R.string.sub_budget_natural_gas) + "' , 'HKD' , 0, " + R.drawable.budget_63 + " , 1)");
        db.execSQL(insertSubBudget + "values ( 10 , '" + context.getString(R.string.sub_budget_tel_data) + "' , 'HKD' , 0, " + R.drawable.budget_65 + " , 1)");
        db.execSQL(insertSubBudget + "values ( 10 , '" + context.getString(R.string.sub_budget_internet) + "' , 'HKD' , 0, " + R.drawable.budget_64 + " , 1)");

        db.execSQL(insertSubBudget + "values ( 11 , '" + context.getString(R.string.sub_budget_fitness) + "' , 'HKD' , 0, " + R.drawable.budget_67 + " , 1)");
        db.execSQL(insertSubBudget + "values ( 11 , '" + context.getString(R.string.sub_budget_health_food) + "' , 'HKD' , 0, " + R.drawable.budget_3 + " , 1)");
        db.execSQL(insertSubBudget + "values ( 11 , '" + context.getString(R.string.sub_budget_medicine) + "' , 'HKD' , 0, " + R.drawable.budget_68 + " , 1)");
        db.execSQL(insertSubBudget + "values ( 11 , '" + context.getString(R.string.sub_budget_treatment) + "' , 'HKD' , 0, " + R.drawable.budget_69 + " , 1)");

        db.execSQL(insertSubBudget + "values ( 12 , '" + context.getString(R.string.sub_budget_dedication) + "' , 'HKD' , 0, " + R.drawable.budget_74 + " , 1)");
        db.execSQL(insertSubBudget + "values ( 12 , '" + context.getString(R.string.sub_budget_donation) + "' , 'HKD' , 0, " + R.drawable.budget_75 + " , 1)");
        db.execSQL(insertSubBudget + "values ( 12 , '" + context.getString(R.string.sub_budget_gift) + "' , 'HKD' , 0, " + R.drawable.budget_76 + " , 1)");

        db.execSQL(insertSubBudget + "values ( 13 , '" + context.getString(R.string.sub_budget_health_insurance) + "' , 'HKD' , 0, " + R.drawable.budget_77 + " , 1)");
        db.execSQL(insertSubBudget + "values ( 13 , '" + context.getString(R.string.sub_budget_accident_insurance) + "' , 'HKD' , 0, " + R.drawable.budget_78 + " , 1)");
        db.execSQL(insertSubBudget + "values ( 13 , '" + context.getString(R.string.sub_budget_life_insurance) + "' , 'HKD' , 0, " + R.drawable.budget_71 + " , 1)");

        db.execSQL(insertSubBudget + "values ( 14 , '" + context.getString(R.string.sub_budget_installment) + "' , 'HKD' , 0, " + R.drawable.budget_79 + " , 1)");
        db.execSQL(insertSubBudget + "values ( 14 , '" + context.getString(R.string.sub_budget_student_loan) + "' , 'HKD' , 0, " + R.drawable.budget_30 + " , 1)");

        db.execSQL(insertSubBudget + "values ( 15 , '" + context.getString(R.string.sub_budget_investment_loss) + "' , 'HKD' , 0, " + R.drawable.budget_84 + " , 1)");
        db.execSQL(insertSubBudget + "values ( 15 , '" + context.getString(R.string.sub_budget_accidental_loss) + "' , 'HKD' , 0, " + R.drawable.budget_88 + " , 1)");

        db.execSQL(insertSubBudget + "values ( 16 , '" + context.getString(R.string.sub_budget_salary) + "' , 'HKD' , 0, " + R.drawable.budget_87 + " , 1)");
        db.execSQL(insertSubBudget + "values ( 16 , '" + context.getString(R.string.sub_budget_overtime) + "' , 'HKD' , 0, " + R.drawable.budget_93 + " , 1)");
        db.execSQL(insertSubBudget + "values ( 16 , '" + context.getString(R.string.sub_budget_bonus) + "' , 'HKD' , 0, " + R.drawable.budget_94 + " , 1)");

        db.execSQL(insertSubBudget + "values ( 17 , '" + context.getString(R.string.sub_budget_investment) + "' , 'HKD' , 0, " + R.drawable.budget_83 + " , 1)");

        //Default sub budgets information
        String insertPlansEntry = "insert into " + PlansEntry.TABLE_NAME + "("
                + PlansEntry.COLUMN_PLANS_STATUS + ","
                + PlansEntry.COLUMN_PLANS_NAME + ","
                + PlansEntry.COLUMN_PLANS_DATE_START + ","
                + PlansEntry.COLUMN_PLANS_TERMS + ","
                + PlansEntry.COLUMN_PLANS_CURRENCY + ","
                + PlansEntry.COLUMN_PLANS_ICON + ")";

        db.execSQL(insertPlansEntry + "values ( 1 , '" + context.getString(R.string.main_budget_home) + "' , '" + formatDateStringAsLong("01-03-2017")  + "' , '12' , 'HKD', " + R.drawable.budget_48 +  ")");

        String insertSubPlansEntry = "insert into " + SubPlansEntry.TABLE_NAME + "("
                + SubPlansEntry.COLUMN_SUB_PLANS_PARENT + ","
                + SubPlansEntry.COLUMN_SUB_PLANS_NAME + ","
                + SubPlansEntry.COLUMN_PLANS_CURRENCY + ","
                + SubPlansEntry.COLUMN_SUB_PLANS_AMOUNT + ","
                + SubPlansEntry.COLUMN_SUB_PLANS_ICON + ")";

        db.execSQL(insertSubPlansEntry + "values ( 1 , '" + context.getString(R.string.sub_plan_deposit) + "' , 'HKD' , '4000000' , " + R.drawable.budget_50 +  ")");
        db.execSQL(insertSubPlansEntry + "values ( 1 , '" + context.getString(R.string.sub_plan_furniture) + "' , 'HKD' , '300000' , " + R.drawable.budget_54 +  ")");
        db.execSQL(insertSubPlansEntry + "values ( 1 , '" + context.getString(R.string.sub_plan_property_taxes) + "' , 'HKD' , '200000' , " + R.drawable.budget_96 +  ")");
        db.execSQL(insertSubPlansEntry + "values ( 1 , '" + context.getString(R.string.sub_plan_insurance) + "' , 'HKD' , '100000' , " + R.drawable.budget_78 +  ")");

    }
}
