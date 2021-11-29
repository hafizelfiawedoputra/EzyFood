package com.hms.utsmobileprogramming.listener;

import com.hms.utsmobileprogramming.model.DrinkModel;
import com.hms.utsmobileprogramming.model.TopUpModel;

import java.util.List;

public interface ITopUpLoadListener {
    void onTopUpLoadSuccess(List<TopUpModel> topUpModelList);
    void onTopUpLoadFailed(String message);
}
