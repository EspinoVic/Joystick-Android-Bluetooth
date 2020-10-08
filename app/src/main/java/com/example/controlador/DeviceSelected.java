package com.example.controlador;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class DeviceSelected extends ViewModel {

    public MutableLiveData<Device> liveDataDeviceSelected;

    public DeviceSelected() {
        this.liveDataDeviceSelected = new MutableLiveData<>();
    }



    public MutableLiveData<Device> getLiveDataDeviceSelected() {
        return liveDataDeviceSelected;
    }
}
