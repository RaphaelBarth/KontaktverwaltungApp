package de.pbma.moa.createroomdemo.RoomRoom;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(tableName = "dbRoom")
public class RoomItem {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "roomName")
    public String roomName;

    @ColumnInfo(name = "open")
    public boolean open;

    @ColumnInfo(name = "host") // Vorname+Nachname+Extra
    public String host;

    @ColumnInfo(name = "eMail")
    public String eMail;

    @ColumnInfo(name = "phone")
    public String phone;

    @ColumnInfo(name = "place") //(Kuerzle) + PLZ + Ort
    public String place;

    @ColumnInfo(name = "address") //Strasse + Nr
    public String address;

    @ColumnInfo(name = "extra")
    public String extra;

    @ColumnInfo(name = "startTime") // in ms since default
    public long startTime;

    @ColumnInfo(name = "endTime") // in ms since default
    public long endTime;


    public static RoomItem createRoom(String roomName,boolean open ,String host,String eMail,
                                      String phone, String place,String address, String extra,
                                      long startTime, long endTime) {
        RoomItem room = new RoomItem();
        room.roomName = roomName;
        room.open =open;
        room.host = host;
        room.eMail = eMail;
        room.phone = phone;
        room.place = place;
        room.address = address;
        room.extra = extra;
        room.startTime = startTime;
        room.endTime = endTime;
        return room;
    }

    @Override
    public String toString() {
        return "RoomItem{" +
                "id=" + id +
                ", roomName='" + roomName + '\'' +
                ", open=" + open +
                ", host='" + host + '\'' +
                ", eMail='" + eMail + '\'' +
                ", phone='" + phone + '\'' +
                ", place='" + place + '\'' +
                ", address='" + address + '\'' +
                ", extra='" + extra + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }

    public String getUri(){
        return this.roomName + "/" + this.eMail + "/" + this.id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoomItem roomItem = (RoomItem) o;
        return id == roomItem.id &&
                open == roomItem.open &&
                startTime == roomItem.startTime &&
                endTime == roomItem.endTime &&
                Objects.equals(roomName, roomItem.roomName) &&
                Objects.equals(host, roomItem.host) &&
                Objects.equals(eMail, roomItem.eMail) &&
                Objects.equals(phone, roomItem.phone) &&
                Objects.equals(place, roomItem.place) &&
                Objects.equals(address, roomItem.address) &&
                Objects.equals(extra, roomItem.extra);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, roomName, open, host, eMail, phone, place, address, extra, startTime, endTime);
    }
}



