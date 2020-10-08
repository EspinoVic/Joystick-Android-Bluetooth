package com.example.controlador;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.controlador.bluetootharduino.BluetoothViewModel;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.UUID;

import io.github.controlwear.virtual.joystick.android.JoystickView;

public class DeviceControl extends Fragment {


    ImageButton imgBtn_acelerar;
    DeviceSelected viewMDeviceSelected;


    Handler bluetoothIn;
    final int handlerState = 0;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder dataStringIN = new StringBuilder();
    private ConnectedThread myConexionBT;
    // Identificador unico de servicio - SPP UUID
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    // String para la direccion MAC
    private static String address = null;

    public static int anglePivot = 90;
    public static int velocityPivot = 0;
    public final static int ERROR_RATE = 5;

    BluetoothViewModel bluetoothViewModelIn;

    public DeviceControl() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
        viewMDeviceSelected = new ViewModelProvider(requireActivity()).get(DeviceSelected.class);

        this.bluetoothViewModelIn = new ViewModelProvider(requireActivity()).get(BluetoothViewModel.class);
        this.bluetoothViewModelIn.getBluetoothIN().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {

                int endOfLineIndex = s.length();

                if (endOfLineIndex > 2) {
                    String dataInPrint = s; /*dataStringIN.substring(0, endOfLineIndex);*/

                    if(dataInPrint.contains(";")){/*CONFIRM;angle;velocity*/

                        dataInPrint = dataInPrint.substring(0,dataInPrint.length()-1);/*Remove #*/
                        final String[] confirmationSplit = dataInPrint.split(";");
                        if(confirmationSplit[0].isEmpty() || confirmationSplit[1].isEmpty()){
                            Log.d("ONE EMPTY",confirmationSplit[0] +confirmationSplit[1] + confirmationSplit[0].isEmpty()+ " "+confirmationSplit[1].isEmpty());
                            return;
                        }
                        anglePivot = Integer.parseInt( confirmationSplit[1] );
                        velocityPivot = Integer.parseInt( confirmationSplit[0] );

                            /*String beforetxt = txtLog.getText().toString();
                            txtLog.setText(beforetxt + dataStringIN);*/
//                            System.out.println(dataInPrint);
                        Log.d("CONFIRMATION",dataInPrint);

                    }
                    //txt datra in
                    //txtInfoTemp.setText("Dato: " + dataInPrint);//<-<- PARTE A MODIFICAR >->->
                    //dataStringIN.delete(0, dataStringIN.length());
                }
            }
        });

        bluetoothIn = new Handler(Looper.getMainLooper()) {

            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {
                    String readMessage = (String) msg.obj;
                    dataStringIN.append(readMessage);

                    int endOfLineIndex = dataStringIN.length();/*dataStringIN.indexOf("#");*/

                    if (endOfLineIndex > 0) {
                        String dataInPrint = dataStringIN.substring(0, endOfLineIndex);

                        if(dataInPrint.contains(";")){/*CONFIRM;angle;velocity*/

                            dataInPrint = dataInPrint.substring(0,dataInPrint.length()-1);/*Remove #*/
                            final String[] confirmationSplit = dataInPrint.split(";");
                            if(confirmationSplit[0].isEmpty() || confirmationSplit[1].isEmpty()){
                                Log.d("ONE EMPTY",confirmationSplit[0] +confirmationSplit[1] + confirmationSplit[0].isEmpty()+ " "+confirmationSplit[1].isEmpty());
                                return;
                            }
                            anglePivot = Integer.parseInt( confirmationSplit[1] );
                            velocityPivot = Integer.parseInt( confirmationSplit[0] );

                            /*String beforetxt = txtLog.getText().toString();
                            txtLog.setText(beforetxt + dataStringIN);*/
//                            System.out.println(dataInPrint);
                            Log.d("CONFIRMATION",dataInPrint);

                        }
                        //txt datra in
                        //txtInfoTemp.setText("Dato: " + dataInPrint);//<-<- PARTE A MODIFICAR >->->
                        //dataStringIN.delete(0, dataStringIN.length());
                    }
                }
            }
        };

        btAdapter = BluetoothAdapter.getDefaultAdapter(); // get Bluetooth adapter
        VerificarEstadoBT();

    }

    @Override
    public void onResume()
    {
        super.onResume();
        //Consigue la direccion MAC desde DeviceListActivity via intent

        //Consigue la direccion MAC desde DeviceListActivity via EXTRA
        address = viewMDeviceSelected.getLiveDataDeviceSelected().getValue().addres;
        //Setea la direccion MAC
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        try
        {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getContext(), "La creacción del Socket fallo", Toast.LENGTH_SHORT).show();
        }
        // Establece la conexión con el socket Bluetooth.
        try
        {
            btSocket.connect();
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {}
        }
        myConexionBT = new ConnectedThread(btSocket);
        myConexionBT.start();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        try
        { // Cuando se sale de la aplicación esta parte permite
            // que no se deje abierto el socket
            btSocket.close();
        } catch (IOException e2) {

        }
    }
    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException
    {
        //crea un conexion de salida segura para el dispositivo
        //usando el servicio UUID
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }
    TextView txtInfoTemp;
    TextView txtLog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root =  inflater.inflate(R.layout.fragment_device_control, container, false);

         imgBtn_acelerar = root.findViewById(R.id.btn_acelerar);

         imgBtn_acelerar.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                for(int i = 0; i<10;i++){
                    myConexionBT.write("CHANGE;"+(i*10)+";"+((i*10)+1)+"#");//Backward
                }
             }
         });
        Device value = viewMDeviceSelected.getLiveDataDeviceSelected().getValue();

         txtInfoTemp = root.findViewById(R.id.txtInfo);
        txtLog = root.findViewById(R.id.txtLog);


        txtInfoTemp.setText(value.info);



        JoystickView joystickLeft = root.findViewById(R.id.joystickView_lanchaDirection);
        joystickLeft.setOnMoveListener(new JoystickView.OnMoveListener() {

            @Override
            public void onMove(final int angle, final int strength) {
                String text = angle + "° " + strength ;
                txtInfoTemp.setText(text);
                boolean angleChange = false;
                boolean speedChange = false;

                if(angle<= anglePivot+ERROR_RATE && angle>= anglePivot-ERROR_RATE){
                    //if new angle is in range (+- ERROR RATE), it will write nothing to bluetooth

                }else{
                    angleChange = true;
                }

                if(strength <= velocityPivot+ERROR_RATE && strength >= velocityPivot-ERROR_RATE){
                    //if new VELOCITY (strength) is in range (+- ERROR RATE), it will write nothing to bluetooth

                }else{
                    speedChange = true;
                }
                /**/
                if(speedChange||angleChange){
                    myConexionBT.write("CHANGE;"+angle+";"+strength+"#");

                }

               /* if(strength>90 ){

                    if(angle>=45 && angle<135){

                    }else
                    if(angle>=135 && angle<225){

                    }else
                    if(angle>=225 && angle<315){

                    }else
                    if(angle>=315 && angle<=360 || angle<45){

                    }
                    if(angle>179){//179 a 360
                        myConexionBT.write("B");//Backward
                    }
                    else if(angle>-1){//0 a 179
                        myConexionBT.write("F");//Forward
                    }
                }else{
                    myConexionBT.write("0");
                }*/
            }
        });
        return root;
    }

    //Comprueba que el dispositivo Bluetooth Bluetooth está disponible y solicita que se active si está desactivado
    private void VerificarEstadoBT() {

        if(btAdapter==null) {
            Toast.makeText(getContext(), "El dispositivo no soporta bluetooth", Toast.LENGTH_LONG).show();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    //Crea la clase que permite crear el evento de conexion
    private class ConnectedThread extends Thread
    {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket)
        {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try
            {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        @Override
        public void run()
        {
            byte[] buffer = new byte[256];
            int bytes;

            // Se mantiene en modo escucha para determinar el ingreso de datos
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);
                    if(readMessage.length()>2)
                        // Envia los datos obtenidos hacia el evento via handler
                        bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }
        //Envio de trama
        public synchronized void write(String input)
        {
            try {
                mmOutStream.write(input.getBytes());
            }
            catch (IOException e)
            {
                //si no es posible enviar datos se cierra la conexión
                Toast.makeText(getContext(), "La Conexión fallo", Toast.LENGTH_SHORT).show();

            }
        }

        public void read(){
            try {
                mmInStream.read();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
