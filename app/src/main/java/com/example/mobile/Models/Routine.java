package com.example.mobile.Models;

import com.example.mobile.Models.Product;

import java.util.List;

public class Routine {
    public String routineID;
    public String skinType;
    public String routineName;
    public String routineDescription;
    public List<Product> productDTOS;

    public Routine(String routineID, String skinType, String routineName, String routineDescription, List<Product> productDTOS) {
        this.routineID = routineID;
        this.skinType = skinType;
        this.routineName = routineName;
        this.routineDescription = routineDescription;
        this.productDTOS = productDTOS;
    }
    public String getRoutineName() {
        return routineName;
    }

    public String getRoutineDescription() {
        return routineDescription;
    }
    public String getRoutineID() {
        return routineID;
    }
}