package de.pbma.moa.createroomdemo.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public abstract class RoomDao {

    @Insert
    abstract long insert(RoomItem item);

    @Update
    abstract void update(RoomItem item);

    @Delete
    abstract void delete(RoomItem item);

    @Query("SELECT * FROM  dbRoom WHERE id=:roomid")
    abstract LiveData<RoomItem> getById(long roomid);

    @Query("SELECT * FROM  dbRoom WHERE id=:roomid")
    abstract RoomItem getItemByIdNow(long roomid);

    @Query("SELECT id FROM dbRoom " +
            "WHERE roomName= :name AND eMail=:eMail AND fremdId=:fremdId " +
            "OR roomName= :name AND eMail=:eMail AND id=:fremdId AND fremdId IS NULL")
    abstract long getIdOfRoomByRoomTagNow(String name, String eMail, long fremdId);

    @Query("DELETE FROM dbRoom WHERE endTime <(:timeNow-:timeSpanOfTwoWeeks)")
    abstract void deleteAllOlderTwoWeeks(long timeNow, long timeSpanOfTwoWeeks);

    @Query("SELECT * FROM dbRoom WHERE endTime <(:timeNow-:timeSpanOfTwoWeeks)")
    abstract List<RoomItem> getAllOlderTwoWeeks(long timeNow, long timeSpanOfTwoWeeks);

    @Query("SELECT * FROM  dbRoom")
    abstract LiveData<List<RoomItem>> getAll();

    //Dise Funktion liefert alle eigenen Räume entsprechend des RoomStatus
    @Query("SELECT * FROM  dbRoom WHERE status =:roomStatus  AND fremdId IS NULL")
    public abstract List<RoomItem> getAllOwnRoomsWithRoomStatus(int roomStatus);

    //Dise Funktion liefert alle fremden Räume entsprechend des RoomStatus
    @Query("SELECT * FROM dbRoom WHERE fremdId IS NOT NULL AND status =:roomStatus")
    public abstract List<RoomItem> getAllNotOwnRoomsWithRoomStatus(int roomStatus);

    @Query("SELECT * FROM  dbRoom WHERE fremdId IS NULL")
    abstract LiveData<List<RoomItem>> getAllFromMeAsHost();

    @Query("SELECT * FROM  dbRoom WHERE fremdId IS NOT NULL")
    abstract LiveData<List<RoomItem>> getAllFromExceptMeAsHost();

}
