package com.hms.utsmobileprogramming.listener;

import com.hms.utsmobileprogramming.model.CartModel;
import com.hms.utsmobileprogramming.model.DrinkModel;

import java.util.List;

public interface ICartLoadListener {
    void onCartLoadSuccess(List<CartModel> cartModelList);
    void onCartLoadFailed(String message);
}
