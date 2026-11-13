package com.eduvos.promptqualityscorer.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.annotation.NonNull;

import com.eduvos.promptqualityscorer.model.CompanyRuleEntity;
import com.eduvos.promptqualityscorer.model.HistoryEntity;

@Database(entities = {HistoryEntity.class, CompanyRuleEntity.class}, version = 3, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;

    public abstract HistoryDao historyDao();
    public abstract CompanyRuleDao companyRuleDao();

    // Add the company rules table when an older database moves from version 1 to 2.
    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS company_rules (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, title TEXT, description TEXT, enabled INTEGER NOT NULL)");
        }
    };
    // Add the specification score without deleting saved history. Older rows have no score yet.
    public static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE prompt_history ADD COLUMN upgradedScore INTEGER");
        }
    };

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    AppDatabase.class, "prompt_db")
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build();
        }
        return instance;
    }
}
