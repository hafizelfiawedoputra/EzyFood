package com.hms.utsmobileprogramming.listener;

import com.hms.utsmobileprogramming.model.FoodModel;

import java.util.List;

public interface IFoodLoadListener {
    void onFoodLoadSuccess(List<FoodModel> drinkModelList);
    void onFoodLoadFailed(String message);
}
