package com.onepay.onepaygo.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.onepay.onepaygo.model.ReportRecords;

import java.util.List;

@Dao
public interface DatabaseStore {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(  List<ReportRecords> list);

    @Query("SELECT * FROM ReportRecords where dateSearch = :dateValue limit :limit offset :offsetValue") //where dateTime = Date(:dateVal)  ReportRecords
    List<ReportRecords> findAllRecord(String dateValue,int limit,int offsetValue);

    @Query("SELECT * FROM ReportRecords") //where dateTime = Date(:dateVal)  ReportRecords
    List<ReportRecords> AllRecord();
    //  SELECT * FROM ReportRecords where dateSerch = '2022-01-24'  limit 5  offset 0


//    @Query("SELECT * FROM item ORDER BY id DESC LIMIT :count OFFSET :offset")
//    List<Item> getSome(int count, int offset);

//    @Query("select top (20) a.* from (select ROW_NUMBER()over( order by tdate asc) as rnk,* from ttranscationtest) a where CEILING(a.rnk/20)=@var") //where dateTime = Date(:dateVal)  ReportRecords
//    List<ReportRecords> searchData();

}

