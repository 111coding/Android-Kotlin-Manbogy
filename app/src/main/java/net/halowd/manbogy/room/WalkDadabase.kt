package net.halowd.manbogy.room

import androidx.room.*
import net.halowd.manbogy.Walk

@Database(entities = [Walk::class], version = 1)
abstract class WalkDatabase : RoomDatabase() {
    abstract fun walkDao(): WalkDao
}