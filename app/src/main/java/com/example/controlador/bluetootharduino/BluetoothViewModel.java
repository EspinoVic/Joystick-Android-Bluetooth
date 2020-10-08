package com.example.controlador.bluetootharduino;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class BluetoothViewModel extends ViewModel {

    private MutableLiveData<String> bluetoothIN;

    public BluetoothViewModel() {
        this.bluetoothIN = new MutableLiveData<>();
    }

    public MutableLiveData<String> getBluetoothIN() {
        return bluetoothIN;
    }
}
