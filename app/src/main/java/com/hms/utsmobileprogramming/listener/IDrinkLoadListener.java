package com.hms.utsmobileprogramming.listener;

import com.hms.utsmobileprogramming.model.DrinkModel;

import java.util.List;

public interface IDrinkLoadListener {
    void onDrinkLoadSuccess(List<DrinkModel> drinkModelList);
    void onDrinkLoadFailed(String message);
}
