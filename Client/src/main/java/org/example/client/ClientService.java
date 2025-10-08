package org.example.client;

import java.io.*;
import java.net.*;

public class ClientService {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 6666;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private boolean connected = false;

    public void connect() throws IOException {
        socket = new Socket(SERVER_HOST, SERVER_PORT);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        connected = true;
    }

    public String getSongs(int page, int pageSize) throws IOException {
        if (!connected) {
            connect();
        }
        out.println("GET_SONGS|" + page + "|" + pageSize);
        return in.readLine();
    }

    public String searchSongs(String searchTerm, int page, int pageSize) throws IOException {
        if (!connected) {
            connect();
        }
        out.println("SEARCH_SONGS|" + searchTerm + "|" + page + "|" + pageSize);
        return in.readLine();
    }



    public String login(String email, String password) throws IOException {
        if (!connected) {
            connect();
        }

        out.println("LOGIN|" + email + "|" + password);
        String response = in.readLine();
        return response;
    }

    public String register( String email, String password) throws IOException {
        if (!connected) {
            connect();
        }

        out.println("REGISTER" + "|" + email + "|" + password);
        String response = in.readLine();
        return response;
    }

    public void disconnect() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            connected = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        return connected && socket != null && !socket.isClosed();
    }
}

