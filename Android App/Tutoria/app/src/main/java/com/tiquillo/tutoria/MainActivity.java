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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView1 = findViewById(R.id.textView1);
        textView2 = findViewById(R.id.textView2);
        entry1 = findViewById(R.id.entry1);
        button = findViewById(R.id.button);

        contadorDePulsaciones = 0;
        pulsado = false;
        SERVER_PORT = 5001;
        SERVER_IP = "";

        button.setOnClickListener(view -> {
            MostrarHolaMundo();
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

    private void MostrarHolaMundo() {
        textView1.setVisibility(View.VISIBLE);
    }

    private void Contador() {
        contadorDePulsaciones++;
        textView2.setText("Se ha presionado: " + contadorDePulsaciones + " veces");
    }
}