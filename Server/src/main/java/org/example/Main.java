package org.example;

import java.io.*;
import java.net.*;
import org.neo4j.driver.*;

public class Main {
    private static final int PORT = 7777;
    private static Driver neo4jDriver;

    public static void main(String[] args) {
        // Connessione a Neo4j
        neo4jDriver = GraphDatabase.driver(
                "bolt://localhost:7687",
                AuthTokens.basic("neo4j", "Madrid2025!")
        );

        System.out.println("Server avviato sulla porta " + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nuovo client connesso: " + clientSocket.getInetAddress());

                // Gestisci ogni client in un thread separato
                new ClientHandler(clientSocket, neo4jDriver).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            neo4jDriver.close();
        }
    }
}