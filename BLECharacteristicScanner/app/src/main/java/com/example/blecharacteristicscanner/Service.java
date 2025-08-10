package com.example.blecharacteristicscanner;

import java.util.List;

public class Service
{
    private String code;
    private List<String> characteristics;

    public Service(String code, List<String> characteristics)
    {
        this.code = code;
        this.characteristics = characteristics;
    }

    public String getCode() { return code; }
    public List<String> getCharacteristics() { return characteristics; }

    public void setCode(String code) { this.code = code; }
    public void setCharacteristics(List<String> characteristics) { this.characteristics = characteristics; }
}