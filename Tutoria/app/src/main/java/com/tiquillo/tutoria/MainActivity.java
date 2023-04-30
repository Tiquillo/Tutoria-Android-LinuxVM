package com.tiquillo.tutoria;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
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
    EditText entry1;
    Button button;

    Spinner spinner1;

    static String[] ips;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView1 = findViewById(R.id.textView1);
        textView2 = findViewById(R.id.textView2);
        entry1 = findViewById(R.id.entry1);
        button = findViewById(R.id.button);
        spinner1 = findViewById(R.id.spinner1);
        textView1.setVisibility(View.VISIBLE);

        new Thread(this::getNetworkIPs).start();

        contadorDePulsaciones = 0;
        pulsado = false;
        SERVER_PORT = 5001;
        SERVER_IP = "";

        spinner1.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

                        Object item = parent.getItemAtPosition(pos);
                        //entry1.setText(item.toString());
                    }
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });

        button.setOnClickListener(view -> {
            Contador();
            SERVER_IP = entry1.getText().toString();
            if (isValidIPv4(SERVER_IP)) {
                EnviarInfo();
            } else {
                textView1.setText("Dirección IP no válida");
            }
        });
    }

    private void EnviarInfo() {
        textView1.setText("Enviando mensaje");
        new Thread(() -> {
            String a = "";
            try {

                socket = new Socket(SERVER_IP, SERVER_PORT);
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                output = new PrintWriter(socket.getOutputStream(), true);
                output.println("Hola, soy el dispositivo Android, he enviado este mensaje " +
                        contadorDePulsaciones + " veces");

                String response = input.readLine();
                Log.d("RESPUESTA", response);

                socket.close();
                Log.d("ENVIADO", a);
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("NO SE PUDO ENVIAR", a);
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
        textView2.setText("Se ha presionado: " + contadorDePulsaciones + " veces");
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
                        Log.d("IP", addr.getHostAddress());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;     // exit method, otherwise "ip might not have been initialized"
        }
        final byte[] ipFinal = ip;
        Log.d("IP", "Funciona todavía");

        for(int i=1;i<=254;i++) {
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
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

        }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ips = new String[list.getSize()];
        for (int i = 0; i < list.getSize(); i++) {
            ips[i] = list.get(i);
            Log.d("IP lista", ips[i]);
        }
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_spinner_item,
                        ips);
        //runOnUiThread(() -> spinner1.setAdapter(adapter));
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
}

