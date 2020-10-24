package com.example.controlador;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.controlador.bluetootharduino.BluetoothViewModel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
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
    private ConnectedThread myConexionBTConnectedThread;
    // Identificador unico de servicio - SPP UUID
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    // String para la direccion MAC
    private static String address = null;

    public static int anglePivot = 0;
    public static int velocityPivot = 0;
    public final static int ERROR_RATE = 5;

    BluetoothViewModel bluetoothViewModelIn;

    private boolean appActive;
    public DeviceControl() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        if (getArguments() != null) {

        }
        viewMDeviceSelected = new ViewModelProvider(requireActivity()).get(DeviceSelected.class);

        this.bluetoothViewModelIn = new ViewModelProvider(requireActivity()).get(BluetoothViewModel.class);

       /* bluetoothIn = new Handler(Looper.getMainLooper()) {

            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {
                    String readMessage = (String) msg.obj;
                    dataStringIN.append(readMessage);

                    int endOfLineIndex = dataStringIN.length();*//*dataStringIN.indexOf("#");*//*

                    if (endOfLineIndex > 0) {
                        String dataInPrint = dataStringIN.substring(0, endOfLineIndex);

                        if(dataInPrint.contains(";")){*//*CONFIRM;angle;velocity*//*

                            dataInPrint = dataInPrint.substring(0,dataInPrint.length()-1);*//*Remove #*//*
                            final String[] confirmationSplit = dataInPrint.split(";");
                            if(confirmationSplit[0].isEmpty() || confirmationSplit[1].isEmpty()){
                                Log.d("ONE EMPTY",confirmationSplit[0] +confirmationSplit[1] + confirmationSplit[0].isEmpty()+ " "+confirmationSplit[1].isEmpty());
                                return;
                            }
                            anglePivot = Integer.parseInt( confirmationSplit[1] );
                            velocityPivot = Integer.parseInt( confirmationSplit[0] );

                            *//*String beforetxt = txtLog.getText().toString();
                            txtLog.setText(beforetxt + dataStringIN);*//*
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
*/
        btAdapter = BluetoothAdapter.getDefaultAdapter(); // get Bluetooth adapter
        VerificarEstadoBT();

    }

    @Override
    public void onResume()
    {
        super.onResume();
        new Thread(new Runnable() {
            @Override
            public void run() {
                //Consigue la direccion MAC desde DeviceListActivity via intent

                //Consigue la direccion MAC desde DeviceListActivity via EXTRA
                address = viewMDeviceSelected.getLiveDataDeviceSelected().getValue().addres;
                //Setea la direccion MAC
                BluetoothDevice device = btAdapter.getRemoteDevice(address);

                try
                {
                    //crea un conexion de salida segura para el dispositivo
                    //usando el servicio UUID
                    btSocket = device.createRfcommSocketToServiceRecord(BTMODULEUUID);//createBluetoothSocket(device);
                } catch (IOException e) {
                    Toast.makeText(getContext(), "La creación del Socket fallo", Toast.LENGTH_SHORT).show();
                }
                // Establece la conexión con el socket Bluetooth.
                try
                {
                    appActive   = true;
                    btSocket.connect();
                } catch (IOException e) {
                    try {
                        btSocket.close();
                    } catch (IOException e2) {

                    }
                }
                myConexionBTConnectedThread = new ConnectedThread(btSocket);
                myConexionBTConnectedThread.start();
            }
        }).start();

    }

    @Override
    public void onPause()
    {
        super.onPause();
        try
        { // Cuando se sale de la aplicación esta parte permite
            // que no se deje abierto el socket
            btSocket.close();
            this.appActive   = false;
        } catch (IOException e2) {

        }
    }

    TextView txtInfoTemp;
    TextView txtLog;
    long timeLastChange = 0;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root =  inflater.inflate(R.layout.fragment_device_control, container, false);

         imgBtn_acelerar = root.findViewById(R.id.btn_acelerar);

         /*imgBtn_acelerar.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                for(int i = 0; i<10;i++){
                    myConexionBTConnectedThread.write("CHANGE;"+(i*10)+";"+((i*10)+1)+"#");//Backward
                }
             }
         });
*/
      /*   imgBtn_acelerar.setOnTouchListener(new View.OnTouchListener() {
             @Override
             public boolean onTouch(View v, MotionEvent event) {
                 switch(event.getAction()) {
                     case MotionEvent.ACTION_DOWN:
                         myConexionBTConnectedThread.write("presse");//Backward
                         return true;
                     case MotionEvent.ACTION_UP:
                         myConexionBTConnectedThread.write("relesesd");//Backward
                         return true;
                 }
                 return false;
             }
         });*/
        Device value = viewMDeviceSelected.getLiveDataDeviceSelected().getValue();

         txtInfoTemp = root.findViewById(R.id.txtInfo);
        txtLog = root.findViewById(R.id.txtLog);


        txtInfoTemp.setText(value.info);

        /*Notify Arduino confirmation to establish the new pivote values.*/
        this.bluetoothViewModelIn.getBluetoothIN().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String dataInPrint) {
/*
                Pattern confirmation
                angle;velocity
*/
                if(timeLastChange==0){
                    timeLastChange = System.currentTimeMillis();
                }
               /* if(timeLastChange>)*/
                final String[] confirmationSplit = dataInPrint.split(";");
                if(confirmationSplit[0].isEmpty() || confirmationSplit[1].isEmpty()){
                    Log.d("ONE EMPTY",confirmationSplit[0] +confirmationSplit[1] + confirmationSplit[0].isEmpty()+ " "+confirmationSplit[1].isEmpty());
                    return;
                }
                anglePivot = Integer.parseInt( confirmationSplit[0] );
                velocityPivot = Integer.parseInt( confirmationSplit[1] );

                    /*String beforetxt = txtLog.getText().toString();
                    txtLog.setText(beforetxt + dataStringIN);*/
//                            System.out.println(dataInPrint);
                Log.d("CONFIRMATION",dataInPrint);

            }
        });

        JoystickView joystickDirection = root.findViewById(R.id.joystickView_lanchaDirection);
        joystickDirection.setOnMoveListener(new JoystickView.OnMoveListener() {

            @Override
            public synchronized void onMove(final int angle, final int strength) {
                String text = angle + "° " + strength ;
                txtInfoTemp.setText(text);
                boolean angleChange = false;
                boolean speedChange = false;

                if( angle>= anglePivot-ERROR_RATE && angle<= anglePivot+ERROR_RATE){
                    //if new angle is in range (+- ERROR RATE), it will write nothing to bluetooth

                }else{
                    String ifAngleLess = "angle>= anglePivot-ERROR_RATE: "
                            + angle +">=" + anglePivot+"-"+ERROR_RATE +
                            "="+(angle>= anglePivot-ERROR_RATE);
                    String ifAnglePlus = "angle<= anglePivot+ERROR_RATE: "
                            + angle +"<=" + anglePivot+"+"+ERROR_RATE +
                            "="+(angle<= anglePivot+ERROR_RATE);

                    Log.d("ANGLE PIVOTE CHANGE","\nLast angle: " + anglePivot + "\nNew angle: "+angle
                    + "\nOperation: "+"\n"+ifAngleLess+"\n"+ifAnglePlus
                    );
                    anglePivot = angle;
                    angleChange = true;
                }

                if(strength <= velocityPivot+ERROR_RATE && strength >= velocityPivot-ERROR_RATE){
                    //if new VELOCITY (strength) is in range (+- ERROR RATE), it will write nothing to bluetooth

                }else{
                    String ifVelociPlus = "strength <= velocityPivot+ERROR_RATE: "
                            + strength +"<=" + velocityPivot+"+"+ERROR_RATE +
                            "="+(strength>= velocityPivot+ERROR_RATE);
                    String ifVelociLess = "strength >= velocityPivot-ERROR_RATE: "
                            + strength +">=" + velocityPivot+"-"+ERROR_RATE +
                            "="+(strength >= velocityPivot-ERROR_RATE);

                    Log.d("VELOCITY PIVOTE CHANGE","\nLast velocity: " + velocityPivot + "\nNew velocity: "+strength
                    + "\nOperation: " + "\n" +ifVelociLess +"\n" + ifVelociPlus
                    );
                    velocityPivot = strength;
                    speedChange = true;
                }
                /**/
                if(speedChange||angleChange){
                    final String change = "$"+angle+";"+strength+"#";
                    /*new Thread(new Runnable() {
                        @Override
                        public void run() {
                            myConexionBTConnectedThread.write(change);
                        }
                    }).start();*/
                    if(myConexionBTConnectedThread!=null)
                        myConexionBTConnectedThread.addChange(change);
                }


            }
        });

        JoystickView joystickViewVelocity  = root.findViewById(R.id.joystickView_lanchaVelocidad);
        joystickViewVelocity.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                String text = angle + "° " + strength ;
                txtInfoTemp.setText(text);
                boolean speedChange = false;

                if(strength <= velocityPivot+ERROR_RATE && strength >= velocityPivot-ERROR_RATE){
                    //if new VELOCITY (strength) is in range (+- ERROR RATE), it will write nothing to bluetooth

                }else{
                    String ifVelociPlus = "strength <= velocityPivot+ERROR_RATE: "
                            + strength +"<=" + velocityPivot+"+"+ERROR_RATE +
                            "="+(strength>= velocityPivot+ERROR_RATE);
                    String ifVelociLess = "strength >= velocityPivot-ERROR_RATE: "
                            + strength +">=" + velocityPivot+"-"+ERROR_RATE +
                            "="+(strength >= velocityPivot-ERROR_RATE);

                    Log.d("VELOCITY PIVOTE CHANGE","\nLast velocity: " + velocityPivot + "\nNew velocity: "+strength
                            + "\nOperation: " + "\n" +ifVelociLess +"\n" + ifVelociPlus
                    ); +
                    velocityPivot = strength;
                    speedChange = true;
                }
                /**/
                if(speedChange||angleChange){
                    final String change = "$"+angle+";"+strength+"#";

                    if(myConexionBTConnectedThread!=null)
                        myConexionBTConnectedThread.addChange(change);
                }


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
        private MutableLiveData<String> bluetootINLiveData;
        private ArrayList<String> listChangeState;
        public ConnectedThread(BluetoothSocket socket)
        {
            this.listChangeState = new ArrayList<>();
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try
            {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
                   // int maxTransmitPacketSize = socket.getMaxTransmitPacketSize();


            } catch (IOException e) { }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        @Override
        public void run() {
            while(appActive){
                if(listChangeState.size()>0){
                    this.write(listChangeState.get(0));
                    this.listChangeState.remove(0);
                }

                try {
                    sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void addChange(String changeState){
            this.listChangeState.add(changeState);
        }

        /*@Override*/
        public void ruhn()
        {
            this.bluetootINLiveData = bluetoothViewModelIn.getBluetoothIN();
            byte[] buffer = new byte[256];
            int bytesAmountRead;

            // Se mantiene en modo escucha para determinar el ingreso de datos
            while (appActive) {
                try {
                    //if(bytesAmountAvailable>2){/*so it wont read only one char, incompleting the next reading*/
                        bytesAmountRead = mmInStream.read(buffer,0,buffer.length);

                        String readMessage = new String(buffer, 0, bytesAmountRead);
                        if(bytesAmountRead==1){
                           /* try {*/
                               /* sleep(30);*/
                                String previouseMsg = readMessage;
                                bytesAmountRead = mmInStream.read(buffer,0,buffer.length);
                                readMessage = new String(buffer, 0, bytesAmountRead);
                                readMessage = previouseMsg + readMessage;

                            /*} catch (InterruptedException e) {
                                e.printStackTrace();
                            }*/
                        }
                        String[] splitReadMessage = readMessage.split("#");
                        int msgsCount = splitReadMessage.length;
                        if(msgsCount>0){
                            Log.d("RAW DATA READ",readMessage);
                            /*Can be modified to only confirm the last one. that shall be validated */
                            /*for(String currentMessage : splitReadMessage)*/
                            /* if(currentMessage.length()>2){*/
                            // Envia los datos obtenidos hacia el evento via handler
                            //bluetoothIn.obtainMessage(handlerState, bytesAmountRead, -1, readMessage).sendToTarget();
                            /*String lastMessageIndex = splitReadMessage[msgsCount-1];*/
                            boolean validMessageFound = false;
                            /*
                            * it'll start from end to start, to found the last valid pivot, in case the bluettooth
                            * connection gets interfered
                            * */
                            for(int i = msgsCount-1; i>=0&& !validMessageFound;i--){
                                String currentMsg = splitReadMessage[i];/*70;71*/
                                if(!currentMsg.isEmpty())
                                    if(currentMsg.length()>2)
                                        if(currentMsg.contains(";")){
                                            final String[] splitCurrentMessage = currentMsg.split(";");
                                            if(!splitCurrentMessage[0].isEmpty() && !splitCurrentMessage[1].isEmpty()){
                                                this.bluetootINLiveData.postValue(currentMsg);
                                                validMessageFound = true;

                                            }
                                        }
                            }
                            /*if(!validMessageFound){
                                //something maybe v:
                            }*/


                        }

                } catch (IOException e) {
                    Log.e("Error reading buffer in", "It wasnt possible to read the buffer in. So the reading routine will be shut downed xd ",e);
                    break;
                }
            }
        }
        //Envio de trama
        public synchronized void write(String input)
        {
            try {
               /* mmOutStream.flush();*/
                mmOutStream.write(input.getBytes(),0,input.length());
               /* mmOutStream.flush();*/

               /* try {
                    sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
                /*Log.d("Writed",input);*/
            }
            catch (IOException e)
            {
                //si no es posible enviar datos se cierra la conexión
                /*Toast.makeText(getContext(), "La Conexión fallo", Toast.LENGTH_SHORT).show();*/

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
