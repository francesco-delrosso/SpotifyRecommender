package org.example;

import java.io.*;
import java.net.*;
import org.neo4j.driver.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import org.neo4j.driver.*;

public class Main {
    private static final int PORT = 6666;
    private static Driver neo4jDriver;

    // Aggiungi questo campo statico per renderlo accessibile ai ClientHandler
    public static KafkaService kafkaService;

    public static void main(String[] args) {
        Config config = Config.builder().withoutEncryption().build();

        // ... (configurazione driver esistente) ...
        neo4jDriver = GraphDatabase.driver(
                "bolt://localhost:7687",
                AuthTokens.basic("neo4j", "Madrid2025!"),
                config
        );

        // --- INIZIALIZZA KAFKA ---
        kafkaService = new KafkaService(neo4jDriver);
        kafkaService.startConsumer(); // Avvia il thread consumer

        System.out.println("Server avviato sulla porta " + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                // ... (gestione client esistente) ...
                new ClientHandler(clientSocket, neo4jDriver).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (kafkaService != null) kafkaService.close(); // Chiudi Kafka
            if (neo4jDriver != null) neo4jDriver.close();
        }
    }
}