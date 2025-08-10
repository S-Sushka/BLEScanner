package com.example.blecharacteristicscanner;

public class Device
{
    private String name;
    private final String mac;
    private int strength;

    public Device(String name, String mac, int strength)
    {
        this.name = name;
        this.mac = mac;
        this.strength = strength;
    }

    public String getName() { return name; }
    public String getMac() { return mac; }
    public int getStrength() { return strength; }

    public void setName(String name) { this.name = name; }
    public void setStrength(int strength) { this.strength = strength; }
}
