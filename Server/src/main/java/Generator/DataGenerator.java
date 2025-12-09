package Generator;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class DataGenerator {

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 6666;

    // Canzoni con 0 popularità per testare l'inserimento di nuovi nodi
    private static final List<String> SONG_IDS = Arrays.asList(
            "5IfCZDRXZrqZSm8AwE44PG",
            "0dzKBptH2P5j5a0MifBMwM",
            "5QAMZTM5cmLg3fHX9ZbTZi",
            "2qESE1ZeWly7I3YjyTXmXh",
            "3EQV1ZHtHvq9OnVRYIdbg3"
    );

    private static final List<String> USERS = Arrays.asList(
            "fdr.delrosso@gmail.com",
            "frank.fdr2003@gmail.com",
            "ciao@gmail.com",
            "prova04@gmail.com"
    );

    public static void main(String[] args) {
        System.out.println("🚀 Avvio Data Generator (Traffic Simulator)...");
        Random random = new Random();

        // Tenta di connettersi al server
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            System.out.println("✅ Connesso al Server sulla porta " + SERVER_PORT);
            System.out.println("Inizio invio stream di eventi PLAY... (Premi Ctrl+C per fermare)");

            while (true) {
                // 1. Seleziona casualmente una canzone e un utente
                String songId = SONG_IDS.get(random.nextInt(SONG_IDS.size()));
                String userEmail = USERS.get(random.nextInt(USERS.size()));

                // 2. Costruisci il comando secondo il protocollo del tuo ClientHandler
                // Formato: PLAY | ID_CANZONE | ID_UTENTE
                String command = "PLAY|" + songId + "|" + userEmail;

                // 3. Invia il comando al server
                out.println(command);
                System.out.println("📤 SENT: " + command);

                // 4. Attesa casuale tra 500ms e 1.5s per simulare comportamento umano/traffico variabile
                int sleepTime = 500 + random.nextInt(1000);
                Thread.sleep(sleepTime);
            }

        } catch (IOException e) {
            System.err.println("❌ Errore di connessione: " + e.getMessage());
            System.err.println("Assicurati che il Server (Main.java) sia in esecuzione sulla porta 6666!");
        } catch (InterruptedException e) {
            System.out.println("⏹ Generatore interrotto.");
        }
    }
}