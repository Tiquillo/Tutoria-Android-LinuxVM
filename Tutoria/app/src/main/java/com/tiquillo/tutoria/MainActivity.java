package com.tiquillo.tutoria;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;

    private String SERVER_IP; // IP de la máquina Linux
    private int SERVER_PORT; // Puerto en el que la máquina Linux está escuchando

    int contadorDePulsaciones;
    boolean pulsado;

    TextView textView1;
    TextView textView2;
    TextView textView3;
    EditText entry1;
    Button button;

    Spinner spinner1;

    ImageView refresh;

    static String[] ips;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView1 = findViewById(R.id.textView1);
        textView2 = findViewById(R.id.textView2);
        textView3 = findViewById(R.id.textView3);
        textView1.setVisibility(View.VISIBLE);
        entry1 = findViewById(R.id.entry1);
        button = findViewById(R.id.button);
        spinner1 = findViewById(R.id.spinner1);

        refresh = findViewById(R.id.refresh);

        Refresh();

        spinner1.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

                        Object item = parent.getItemAtPosition(pos);
                        //entry1.setText(item.toString());
                    }
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });

        refresh.setOnClickListener(view -> Refresh());

        button.setOnClickListener(view -> {
            SERVER_IP = entry1.getText().toString();
            if (isValidIPv4(SERVER_IP)) {
                textView1.setText("Dirección IP válida");
                Contador();
                EnviarInfo();
            } else {
                NoValidIP();
            }
        });
    }

    private void NoValidIP() {
        textView1.setTextColor(Color.RED);
        textView1.setText("\n\nDirección IP no válida\n\n");
        new Thread(() -> {
            try {
                Thread.sleep(2500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            runOnUiThread(() -> {
                textView1.setTextColor(Color.GRAY);
                textView1.setText(R.string.textview1_string);
            });
        }).start();
    }

    private void Refresh(){
        new Thread(this::getNetworkIPs).start();

        contadorDePulsaciones = 0;
        pulsado = false;
        SERVER_PORT = 5001;
        SERVER_IP = "";
        textView1.setText(R.string.textview1_string);
        textView2.setText("No se ha presionado el botón");
    }

    private void EnviarInfo() {
        textView3.setText("Enviando mensaje\n\nSi ve este texto,\nhay un problema sin resolver");
        new Thread(() -> {
            String a = "";
            try {

                socket = new Socket(SERVER_IP, SERVER_PORT);
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                output = new PrintWriter(socket.getOutputStream(), true);
                if (contadorDePulsaciones == 1) {
                    a = "vez";
                } else {
                    a = "veces";
                }
                String message = "Hola, soy el dispositivo Android, he enviado este mensaje " +
                        contadorDePulsaciones + a;
                output.printf(message);
                Log.d("ENVIADO", message);

                runOnUiThread(() -> textView3.setText("Esperando respuesta\nSi no responde en poco tiempo,\nes posible que no haya conexión"));

                String response = input.readLine();
                Log.d("RESPUESTA", response);
                runOnUiThread(() -> textView3.setText("RESPUESTA: " + response));

                socket.close();
            } catch (IOException e) {
                runOnUiThread(() -> textView3.setText("NADIE ESCUCHA EN ESE PUERTO\n" +
                        "quizás el servidor no está activo,\nla IP no es correcta\n" +
                        "o no hay conexión a la red"));
                Log.d("NO SE PUDO ENVIAR", "");
                e.printStackTrace();
            }
        }).start();
    }

    private static boolean isValidIPv4(String ip) {
        Pattern pattern = Pattern.compile("^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
        Matcher matcher = pattern.matcher(ip);
        return matcher.matches();
    }
    private void Contador() {
        contadorDePulsaciones++;
        if (contadorDePulsaciones == 1) {
            textView2.setText("Se ha presionado el botón " + contadorDePulsaciones + " vez");
        } else {
            textView2.setText("Se ha presionado el botón " + contadorDePulsaciones + " veces");
        }
    }

    public void getNetworkIPs() {

        byte[] ip = new byte[4];
        final StringLinkedList list = new StringLinkedList();
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress() && addr instanceof Inet4Address) {
                        ip = ipv4ToBytes(addr.getHostAddress());
                        Log.d("HOST IP ADDRESS", addr.getHostAddress());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;     // exit method, otherwise "ip might not have been initialized"
        }
        final byte[] ipFinal = ip;

        try {
            String ipID = InetAddress.getByAddress(ipFinal).toString().substring(1);
            ipID = ipID.substring(0, ipID.lastIndexOf(".")+1) + 'X';
            entry1.setText(ipID);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        /*
        for(int i=1;i<255;i++) {
            final int j = i;
            new Thread(() -> {
                ipFinal[3] = (byte)j;
                Runtime runtime = Runtime.getRuntime();
                try {
                    InetAddress address = InetAddress.getByAddress(ipFinal);
                    String output = address.toString().substring(1);
                    Process ipProcess = runtime.exec("/system/bin/ping -c 1 " + output);
                    if (ipProcess.waitFor() == 0) {
                        list.add(output);
                        Log.d("IP", output + " en red");
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

        }
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ips = new String[list.getSize()];
        for (int i = 0; i < list.getSize(); i++) {
            ips[i] = list.get(i);
            Log.d("IP lista", ips[i]);
        }
        Arrays.sort(ips);
        Log.d("IP lista tamaño", ips.length + "");
        final ArrayAdapter<String> adapter = new ArrayAdapter<>
                (this, android.R.layout.simple_spinner_item,
                        ips);
        byte[] finalIp = ip;
        runOnUiThread(() -> {
            spinner1.setAdapter(adapter);

            try {
                String ipID = InetAddress.getByAddress(finalIp).toString().substring(1);
                ipID = ipID.substring(0, ipID.lastIndexOf(".")+1) + 'X';
                entry1.setText(ipID);
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
        }); */
    }

    public byte[] ipv4ToBytes(String ipv4) throws IllegalArgumentException {
        String[] octets = ipv4.split("\\.");
        if (octets.length != 4) {
            throw new IllegalArgumentException("Invalid IPv4 address format");
        }
        byte[] bytes = new byte[4];
        for (int i = 0; i < 4; i++) {
            int octet = Integer.parseInt(octets[i]);
            if (octet < 0 || octet > 255) {
                throw new IllegalArgumentException("Invalid octet value: " + octet);
            }
            bytes[i] = (byte) octet;
        }
        return bytes;
    }

    public void onBackPressed(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setMessage("¿Desea salir de la app?");
        builder.setPositiveButton("Sí", (dialog, which) -> finish());
        builder.setNegativeButton("No", (dialog, which) -> dialog.cancel());
        AlertDialog alert=builder.create();
        alert.show();
    }
}

