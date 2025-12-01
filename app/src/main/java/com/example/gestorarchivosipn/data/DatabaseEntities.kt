package com.example.gestorarchivosipn.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "favorites")
data class FavoriteFile(
    @PrimaryKey val path: String,
    val name: String,
    val isDirectory: Boolean
)

@Entity(tableName = "history")
data class HistoryFile(
    @PrimaryKey val path: String,
    val name: String,
    val lastOpened: Long
)

@Dao
interface FileDao {
    // Favoritos
    @Query("SELECT * FROM favorites")
    fun getAllFavorites(): Flow<List<FavoriteFile>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(file: FavoriteFile)

    @Query("DELETE FROM favorites WHERE path = :path")
    suspend fun removeFavorite(path: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE path = :path)")
    fun isFavorite(path: String): Flow<Boolean>

    // Historial
    @Query("SELECT * FROM history ORDER BY lastOpened DESC LIMIT 20")
    fun getHistory(): Flow<List<HistoryFile>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToHistory(file: HistoryFile)
}

@Database(entities = [FavoriteFile::class, HistoryFile::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun fileDao(): FileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "file_manager_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}