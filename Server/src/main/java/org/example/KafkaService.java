package org.example;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class KafkaService {
    private static final String TOPIC = "song-plays";
    private static final String BOOTSTRAP_SERVERS = "localhost:9092"; // Assicurati che corrisponda al docker-compose

    private final KafkaProducer<String, String> producer;
    private final Driver neo4jDriver;
    private boolean running = true;

    public KafkaService(Driver neo4jDriver) {
        this.neo4jDriver = neo4jDriver;

        // --- Configurazione Producer ---
        Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        this.producer = new KafkaProducer<>(producerProps);
    }

    // 1. PRODUCER: Invia l'evento su Kafka
    public void sendPlayEvent(String songId, String userId) {
        // Chiave = userId (per ordinamento opzionale), Valore = songId
        ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC, userId, songId);

        producer.send(record, (metadata, exception) -> {
            if (exception != null) {
                System.err.println("❌ Errore Kafka: " + exception.getMessage());
            } else {
                System.out.println("✅ Evento PLAY inviato: Song=" + songId + " [Offset: " + metadata.offset() + "]");
            }
        });
    }

    // 2. CONSUMER: Ascolta Kafka e aggiorna Neo4j
    public void startConsumer() {
        new Thread(() -> {
            Properties consumerProps = new Properties();
            consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
            consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "spotify-recommender-group"); // Gruppo univoco
            consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

            try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps)) {
                consumer.subscribe(Collections.singletonList(TOPIC));
                System.out.println("🎧 Kafka Consumer in ascolto su topic: " + TOPIC);

                while (running) {
                    // Polling dei messaggi (aspetta max 100ms)
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                    for (ConsumerRecord<String, String> record : records) {
                        String songId = record.value();
                        updateSongPopularity(songId);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // 3. DATABASE UPDATE: Incrementa popolarità
    private void updateSongPopularity(String songId) {
        try (Session session = neo4jDriver.session()) {
            session.executeWrite(tx -> {
                // Invece di incrementare solo un numero, traccia CHI ha ascoltato COSA e QUANDO
                String query = "MATCH (u:User {email: $email}), (s:Song {songId: $songId}) " +
                        "MERGE (u)-[r:LISTENED_TO]->(s) " +
                        "ON CREATE SET r.count = 1, r.lastListened = datetime() " +
                        "ON MATCH SET r.count = r.count + 1, r.lastListened = datetime() " +
                        "SET s.popularity = coalesce(s.popularity, 0) + 1"; // Mantieni anche la popolarità globale

                var result = tx.run(query, org.neo4j.driver.Values.parameters("id", songId));
                if (result.hasNext()) {
                    var record = result.next();
                    System.out.println("📈 DB Aggiornato: " + record.get("name").asString() +
                            " -> Popolarità: " + record.get("pop").asInt());
                }
                return null;
            });
        } catch (Exception e) {
            System.err.println("Errore Neo4j: " + e.getMessage());
        }
    }

    public void close() {
        running = false;
        producer.close();
    }
}