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
    private SongService songService;
    private BufferedReader in;
    private PrintWriter out;


    public ClientHandler(Socket socket, org.neo4j.driver.Driver driver){
        this.socket = socket;
        this.driver = driver;
        this.authService = new AuthService(driver);
        this.songService = new SongService(driver);

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
            case "GET_SONGS":
                if (parts.length == 3) {
                    handleGetSongs(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
                } else {
                    out.println("ERROR|Formato non valido");
                }
                break;

            case "SEARCH_SONGS":
                if (parts.length == 4) {
                    handleSearchSongs(parts[1], Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
                } else {
                    out.println("ERROR|Formato non valido");
                }
                break;

            default:
                out.println("ERROR|Comando sconosciuto");
        }
    }

    private void handleSearchSongs(String searchTerm, int page, int pageSize) {
        SongService.SongListResult result = songService.searchSongs(searchTerm, page, pageSize);

        if (result.isSuccess()) {
            StringBuilder response = new StringBuilder("SONGS|");
            for (SongService.SongInfo song : result.getSongs()) {
                response.append(song.toProtocolString()).append("|");
            }
            out.println(response.toString());
        } else {
            out.println("ERROR|" + result.getMessage());
        }
    }

    private void handleGetSongs(int page, int pageSize) {
        SongService.SongListResult result = songService.getSongs(page, pageSize);

        if (result.isSuccess()) {
            StringBuilder response = new StringBuilder("SONGS|");
            for (SongService.SongInfo song : result.getSongs()) {
                response.append(song.toProtocolString()).append("|");
            }
            out.println(response.toString());
        } else {
            out.println("ERROR|" + result.getMessage());
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