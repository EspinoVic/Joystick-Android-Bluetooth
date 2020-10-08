package com.example.controlador;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import androidx.navigation.Navigation;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;


public class FragmentListaDevices extends Fragment {


    DeviceSelected viewMDeviceSelected;

    //1)
    // Depuración de LOGCAT
    private static final String TAG = "DispositivosBT"; //<-<- PARTE A MODIFICAR >->->
    // Declaracion de ListView
    ListView IdLista;
    // String que se enviara a la actividad principal, mainactivity
    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    // Declaracion de campos
    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter mPairedDevicesArrayAdapter;

    public FragmentListaDevices() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewMDeviceSelected = new ViewModelProvider(requireActivity()).get(DeviceSelected.class);



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root =  inflater.inflate(R.layout.fragment_lista_devices, container, false);

        VerificarEstadoBT();
        //  new Temperatura(24.3f, true, new Date(1502186),new Time(654986)).getFecha().
        // Inicializa la array que contendra la lista de los dispositivos bluetooth vinculados
        mPairedDevicesArrayAdapter = new ArrayAdapter(getContext(), R.layout.nombre_dispositivos);//<-<- PARTE A MODIFICAR >->->
        // Presenta los disposisitivos vinculados en el ListView
        IdLista = (ListView) root.findViewById(R.id.IdLista);
        IdLista.setAdapter(mPairedDevicesArrayAdapter);
        IdLista.setOnItemClickListener(mDeviceClickListener);
        // Obtiene el adaptador local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        //------------------- EN CASO DE ERROR -------------------------------------
        //SI OBTIENES UN ERROR EN LA LINEA (BluetoothDevice device : pairedDevices)
        //CAMBIA LA SIGUIENTE LINEA POR
        //Set <BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
        //------------------------------------------------------------------------------

        //check generics
        // Obtiene un conjunto de dispositivos actualmente emparejados y agregua a 'pairedDevices'
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

        // Adiciona un dispositivos previo emparejado al array
        if (pairedDevices.size() > 0)
        {
            for (BluetoothDevice device : pairedDevices) { //EN CASO DE ERROR LEER LA ANTERIOR EXPLICACION
                mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        }

        return root;
    }

    // Configura un (on-click) para la lista
    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView av, View v, int arg2, long arg3) {

            // Obtener la dirección MAC del dispositivo, que son los últimos 17 caracteres en la vista
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            // Realiza un intent para iniciar la siguiente actividad
            // mientras toma un EXTRA_DEVICE_ADDRESS que es la dirección MAC.
            //Intent i = new Intent(getContext(), UserInterfaz.class);//<-<- PARTE A MODIFICAR >->->
            //i.putExtra(EXTRA_DEVICE_ADDRESS, address);
            ////startActivity(i);
            viewMDeviceSelected.getLiveDataDeviceSelected().setValue(new Device(info,address));
            Navigation.findNavController(v).navigate(R.id.action_fragmentListaDevices_to_deviceControl);

        }
    };

    private void VerificarEstadoBT() {
        // Comprueba que el dispositivo tiene Bluetooth y que está encendido.
        mBtAdapter= BluetoothAdapter.getDefaultAdapter();
        if(mBtAdapter==null) {
            Toast.makeText(getContext(), "El dispositivo no soporta Bluetooth", Toast.LENGTH_SHORT).show();
        } else {
            if (mBtAdapter.isEnabled()) {
                Log.d(TAG, "...Bluetooth Activado...");
            } else {
                //Solicita al usuario que active Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);

            }
        }
    }
}
