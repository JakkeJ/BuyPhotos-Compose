package com.example.buyphotos.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.buyphotos.navigation.BottomBarScreen

//Set exportSchema to false, so as not to keep schema version history backups.
@Database(entities = [ShoppingCart::class], version = 1, exportSchema = false)
abstract class AppDatabase: RoomDatabase() {
    abstract fun shoppingCartDao(): ShoppingCartDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * MERK: createFromAsset(...)
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance

                instance
            }
        }
    }
}