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

    @Query("SELECT * FROM ReportRecords where dateSearch = :dateValue limit :limit offset :offsetValue")
    List<ReportRecords> findAllRecord(String dateValue,int limit,int offsetValue);

    @Query("SELECT * FROM ReportRecords where dateSearch = :dateValue and transactionId LIKE :transactionIdValue and merchantTerminalID = :merchantTerminalIDValue limit :limit offset :offsetValue")
    List<ReportRecords> transactionIdSearch(String dateValue,int limit,int offsetValue,String transactionIdValue,int merchantTerminalIDValue);

    @Query("SELECT * FROM ReportRecords where dateSearch = :dateValue and firstName LIKE :value and merchantTerminalID = :merchantTerminalIDValue limit :limit offset :offsetValue")
    List<ReportRecords> firstNameSearch(String dateValue,int limit,int offsetValue,String value,int merchantTerminalIDValue);

    @Query("SELECT * FROM ReportRecords where dateSearch = :dateValue and email LIKE :value and merchantTerminalID = :merchantTerminalIDValue limit :limit offset :offsetValue")
    List<ReportRecords> emailSearch(String dateValue,int limit,int offsetValue,String value,int merchantTerminalIDValue);

    @Query("SELECT * FROM ReportRecords where dateSearch = :dateValue and phoneNumber LIKE :value and merchantTerminalID = :merchantTerminalIDValue limit :limit offset :offsetValue")
    List<ReportRecords> phoneNumberSearch(String dateValue,int limit,int offsetValue,String value,int merchantTerminalIDValue);

    @Query("SELECT * FROM ReportRecords where dateSearch = :dateValue and transactionAmount LIKE :value and merchantTerminalID = :merchantTerminalIDValue limit :limit offset :offsetValue")
    List<ReportRecords> transactionAmountSearch(String dateValue,int limit,int offsetValue,String value,int merchantTerminalIDValue);

    @Query("SELECT * FROM ReportRecords where dateSearch = :dateValue and last4 LIKE :value and merchantTerminalID = :merchantTerminalIDValue limit :limit offset :offsetValue")
    List<ReportRecords> last4Search(String dateValue,int limit,int offsetValue,String value,int merchantTerminalIDValue);

    @Query("SELECT * FROM ReportRecords where dateSearch = :dateValue and lastName LIKE :value and merchantTerminalID = :merchantTerminalIDValue limit :limit offset :offsetValue")
    List<ReportRecords> lastNameSearch(String dateValue,int limit,int offsetValue,String value,int merchantTerminalIDValue);

    @Query("SELECT * FROM ReportRecords where dateSearch = :dateValue and customerId LIKE :value and merchantTerminalID = :merchantTerminalIDValue limit :limit offset :offsetValue")
    List<ReportRecords> customerIdSearch(String dateValue,int limit,int offsetValue,String value,int merchantTerminalIDValue);

    @Query("SELECT * FROM ReportRecords where dateSearch = :dateValue and sourceApplication LIKE :value and merchantTerminalID = :merchantTerminalIDValue limit :limit offset :offsetValue")
    List<ReportRecords> sourceApplicationSearch(String dateValue,int limit,int offsetValue,String value,int merchantTerminalIDValue);

    @Query("DELETE FROM ReportRecords")
    void deleteAll();

}

