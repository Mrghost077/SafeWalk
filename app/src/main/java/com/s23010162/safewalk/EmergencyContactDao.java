package com.s23010162.safewalk;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface EmergencyContactDao {
    @Insert
    void insert(EmergencyContact contact);

    @Update
    void update(EmergencyContact contact);

    @Delete
    void delete(EmergencyContact contact);

    @Query("DELETE FROM emergency_contacts WHERE id = :contactId")
    void deleteById(int contactId);

    @Query("SELECT * FROM emergency_contacts")
    LiveData<List<EmergencyContact>> getAllContacts();

    @Query("SELECT * FROM emergency_contacts")
    List<EmergencyContact> getAllContactsBlocking();

    @Query("SELECT * FROM emergency_contacts")
    List<EmergencyContact> getAll();

    @Query("SELECT COUNT(*) FROM emergency_contacts")
    int getContactCount();
} 