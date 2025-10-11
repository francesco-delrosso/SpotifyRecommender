package org.example.client;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import org.example.client.Song;

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

    // Aggiungi una canzone ai preferiti
    public String addFavorite(String userEmail, String songId) throws IOException {
        if (!connected) connect();
        out.println("ADD_FAVORITE|" + userEmail + "|" + songId);
        return in.readLine();
    }

    // Rimuovi una canzone dai preferiti
    public String removeFavorite(String userEmail, String songId) throws IOException {
        if (!connected) connect();
        out.println("REMOVE_FAVORITE|" + userEmail + "|" + songId);
        return in.readLine();
    }

    //  Ottieni la lista dei preferiti dell’utente
    public String getFavorites(String userEmail) throws IOException {
        if (!connected) connect();
        out.println("GET_FAVORITES|" + userEmail);
        return in.readLine();
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

    // Ricerca utente per email
    public String searchUser(String email) throws IOException {
        if (!connected) {
            connect();
        }
        out.println("SEARCH_USER|" + email);
        return in.readLine();
    }

    // Invia richiesta di amicizia
    public String sendFriendRequest(String fromEmail, String toEmail) throws IOException {
        if (!connected) {
            connect();
        }
        out.println("SEND_FRIEND_REQUEST|" + fromEmail + "|" + toEmail);
        return in.readLine();
    }

    // Accetta richiesta di amicizia
    public String acceptFriendRequest(String userEmail, String requesterEmail) throws IOException {
        if (!connected) {
            connect();
        }
        out.println("ACCEPT_FRIEND_REQUEST|" + userEmail + "|" + requesterEmail);
        return in.readLine();
    }

    // Rifiuta richiesta di amicizia
    public String rejectFriendRequest(String userEmail, String requesterEmail) throws IOException {
        if (!connected) {
            connect();
        }
        out.println("REJECT_FRIEND_REQUEST|" + userEmail + "|" + requesterEmail);
        return in.readLine();
    }

    // Elimina amicizia
    public String removeFriendship(String email1, String email2) throws IOException {
        if (!connected) {
            connect();
        }
        out.println("REMOVE_FRIENDSHIP|" + email1 + "|" + email2);
        return in.readLine();
    }

    // Lista amici
    public String getFriends(String email) throws IOException {
        if (!connected) {
            connect();
        }
        out.println("GET_FRIENDS|" + email);
        return in.readLine();
    }

    // Lista richieste di amicizia ricevute
    public String getFriendRequests(String email) throws IOException {
        if (!connected) {
            connect();
        }
        out.println("GET_FRIEND_REQUESTS|" + email);
        return in.readLine();
    }

    public String getAllUsers() throws IOException {
        if (!connected) {
            connect();
        }
        out.println("GET_ALL_USERS");
        return in.readLine();
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

