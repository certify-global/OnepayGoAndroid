package com.onepay.onepaygo.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.onepay.onepaygo.model.TransactionDB

@Dao
interface DatabaseStore {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll( users: ArrayList<TransactionDB>?)

    @Query("SELECT * FROM TransactionDB")
    fun findAllRecord(): ArrayList<TransactionDB>
}