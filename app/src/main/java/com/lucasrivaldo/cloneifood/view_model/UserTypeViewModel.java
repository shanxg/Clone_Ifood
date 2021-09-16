package com.lucasrivaldo.cloneifood.view_model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class UserTypeViewModel extends ViewModel {

    private MutableLiveData<String> uType = new MutableLiveData<>();

    public LiveData<String> getUType() {
        return uType;
    }

    public void setUType(String uType) {
        this.uType.postValue(uType);
    }
}
