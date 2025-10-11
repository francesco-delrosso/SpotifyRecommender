package org.example;

import org.example.client.Song;  // Assicurati che la classe Song sia nel package common
import org.neo4j.driver.*;
import org.neo4j.driver.Record;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FavoriteService {

    private final Driver driver;

    public FavoriteService(Driver driver) {
        this.driver = driver;
    }

    /**
     * Aggiunge una canzone ai preferiti di un utente
     */
    public boolean addFavorite(String email, String songId) {
        try (Session session = driver.session()) {
            String query = """
                MATCH (u:User {email: $email}), (s:Song {id: $id})
                MERGE (u)-[:LIKES]->(s)
            """;
            session.run(query, Map.of("email", email, "id", songId));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Rimuove una canzone dai preferiti di un utente
     */
    public boolean removeFavorite(String email, String songId) {
        try (Session session = driver.session()) {
            String query = """
                MATCH (u:User {email: $email})-[r:LIKES]->(s:Song {id: $id})
                DELETE r
            """;
            session.run(query, Map.of("email", email, "id", songId));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Restituisce tutte le canzoni preferite di un utente
     */
    public List<Song> getFavorites(String email) {
        List<Song> favorites = new ArrayList<>();

        try (Session session = driver.session()) {
            String query = """
            MATCH (u:User {email: $email})-[:LIKES]->(s:Song)
            RETURN s.id AS id, s.name AS name, s.artists AS artists, s.popularity AS popularity
        """;

            Result result = session.run(query, Map.of("email", email));

            while (result.hasNext()) {
                Record record = result.next();

                // Usa .asString() e .asInt() con default values sicuri
                String id = record.get("id").isNull() ? "" : record.get("id").asString();
                String name = record.get("name").isNull() ? "" : record.get("name").asString();
                String artists = record.get("artists").isNull() ? "" : record.get("artists").asString();
                int popularity = record.get("popularity").isNull() ? 0 : record.get("popularity").asInt();
                int duration = record.get("duration_ms").isNull() ? 0 : record.get("duration_ms").asInt();

                favorites.add(new Song(id, name, artists, popularity,duration));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return favorites;
    }

}
