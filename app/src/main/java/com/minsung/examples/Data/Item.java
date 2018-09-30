package com.minsung.examples.Data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by minsung on 2018-09-30.
 */

public class Item implements Parcelable {
    private String address;
    private double rssi;
    private int txPower;
    private double distance;
    private int light;
    private int second;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int i) {
        dest.writeString(address);
        dest.writeDouble(rssi);
        dest.writeInt(txPower);
        dest.writeDouble(distance);
        dest.writeInt(light);
        dest.writeInt(second);
    }

    public static final Parcelable.Creator<Item> CREATOR = new Creator<Item>() {
        @Override
        public Item createFromParcel(Parcel source) {
            Item item = new Item();
            item.address=source.readString();
            item.rssi = source.readDouble();
            item.txPower = source.readInt();
            item.distance=source.readDouble();
            item.light=source.readInt();
            item.second=source.readInt();
            return item;
        }

        @Override
        public Item[] newArray(int i) {
            return new Item[0];
        }
    };


    public Item(){

    }

    public Item(String address, double rssi, int txPower, double distance, int light, int second) {
        this.address = address;
        this.rssi = rssi;
        this.txPower = txPower;
        this.distance = distance;
        this.light = light;
        this.second = second;

    }

    public int getLight() {
        return light;
    }

    public int getSecond() {
        return second;
    }

    public String getAddress() {
        return address;
    }

    public double getRssi() {
        return rssi;
    }

    public int getTxPower() {
        return txPower;
    }



    public double getDistance() {
        return distance;
    }

    public void setRssi(double rssi) {
        this.rssi = rssi;
    }

    public void setTxPower(int txPower) {
        this.txPower = txPower;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

}
