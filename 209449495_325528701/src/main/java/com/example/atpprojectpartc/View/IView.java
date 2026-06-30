package com.example.atpprojectpartc.View;

import com.example.atpprojectpartc.ViewModel.MyViewModel;

public interface IView {
    void setViewModel(MyViewModel viewModel);

    void displayMessage(String message);
}