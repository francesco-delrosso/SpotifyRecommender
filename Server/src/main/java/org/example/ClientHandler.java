package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import org.neo4j.driver.*;

class ClientHandler extends Thread {
    private Socket socket;
    private Driver driver;
    private AuthService authService;
    private BufferedReader in;
    private PrintWriter out;

    public ClientHandler(Socket socket, org.neo4j.driver.Driver driver){
        this.socket = socket;
        this.driver = driver;
        this.authService = new AuthService(driver);
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            String command;
            while ((command = in.readLine()) != null) {
                System.out.println("Comando ricevuto: " + command);
                processCommand(command);
            }
        } catch (IOException e) {
            System.out.println("Client disconnesso");
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void processCommand(String command) {
        String[] parts = command.split("\\|");
        String action = parts[0];

        switch (action) {
            case "LOGIN":
                if (parts.length == 3) {
                    handleLogin(parts[1], parts[2]);
                } else {
                    out.println("ERROR|Formato non valido");
                }
                break;

            case "REGISTER":
                if (parts.length == 3) {
                    handleRegister(parts[1], parts[2]);
                } else {
                    out.println("ERROR|Formato non valido");
                }
                break;

            default:
                out.println("ERROR|Comando sconosciuto");
        }
    }

    private void handleLogin(String email, String password) {
        AuthService.LoginResult result = authService.login(email, password);

        if (result.isSuccess()) {
            out.println("SUCCESS|" + result.getUserId());
            System.out.println("Login riuscito per: " + email);
        } else {
            out.println("ERROR|" + result.getMessage());
        }
    }

    private void handleRegister(String email, String password) {
        AuthService.LoginResult result = authService.register(email, password);

        if (result.isSuccess()) {
            out.println("SUCCESS|" + result.getUserId());
            System.out.println("Nuovo utente registrato: " + email);
        } else {
            out.println("ERROR|" + result.getMessage());
        }
    }
}