package com.hms.utsmobileprogramming.listener;

import com.hms.utsmobileprogramming.model.MoneyModel;

import java.util.List;

public interface IMoneyLoadListener {
    void onMoneyLoadSuccess(List<MoneyModel> moneyModelList);
    void onMoneyLoadFailed(String message);
}