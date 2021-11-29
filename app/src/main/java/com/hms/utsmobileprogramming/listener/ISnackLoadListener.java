package com.hms.utsmobileprogramming.listener;

import com.hms.utsmobileprogramming.model.SnackModel;

import java.util.List;

public interface ISnackLoadListener {
    void onSnackLoadSuccess(List<SnackModel> snackModelList);
    void onSnackLoadFailed(String message);
}
