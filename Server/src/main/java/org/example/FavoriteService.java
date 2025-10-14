package org.example;

import org.neo4j.driver.*;
import java.util.ArrayList;
import java.util.List;

public class FavoriteService {
    private final Driver driver;

    public FavoriteService(Driver driver) {
        this.driver = driver;
    }

    public boolean addFavorite(String email, String songId) {
        try (Session session = driver.session()) {
            session.executeWrite(tx -> {
                tx.run("MATCH (u:User {email: $email}), (s:Song {songId: $songId}) " +
                                "MERGE (u)-[:FAVORITES]->(s)",
                        Values.parameters("email", email, "songId", songId));
                return null;
            });
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean removeFavorite(String email, String songId) {
        try (Session session = driver.session()) {
            session.executeWrite(tx -> {
                tx.run("MATCH (u:User {email: $email})-[r:FAVORITES]->(s:Song {songId: $songId}) " +
                                "DELETE r",
                        Values.parameters("email", email, "songId", songId));
                return null;
            });
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<FavoriteSong> getFavorites(String email) {
        try (Session session = driver.session()) {
            return session.executeRead(tx -> {
                Result result = tx.run(
                        "MATCH (u:User {email: $email})-[:FAVORITES]->(s:Song) " +
                                "RETURN s.songId AS id, s.trackName AS name, " +
                                "s.artists AS artists, s.popularity AS popularity, " +
                                "s.duration_ms AS duration",
                        Values.parameters("email", email)
                );

                List<FavoriteSong> favorites = new ArrayList<>();
                while (result.hasNext()) {
                    org.neo4j.driver.Record record = result.next();

                    // Gestisci il caso in cui artists sia una lista
                    String artistsStr = "";
                    try {
                        var artistsValue = record.get("artists");
                        if (!artistsValue.isNull()) {
                            artistsStr = artistsValue.asString();
                        }
                    } catch (Exception e) {
                        artistsStr = "Unknown";
                    }

                    favorites.add(new FavoriteSong(
                            record.get("id").asString(),
                            record.get("name").asString(),
                            artistsStr,
                            record.get("popularity").asInt(),
                            record.get("duration").asInt()
                    ));
                }
                return favorites;
            });
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }


    public static class FavoriteSong {
        private String id;
        private String name;
        private String artists;
        private int popularity;
        private int duration;

        public FavoriteSong(String id, String name, String artists, int popularity, int duration) {
            this.id = id;
            this.name = name;
            this.artists = artists;
            this.popularity = popularity;
            this.duration = duration;
        }

        public String toProtocolString() {
            return id + ";" + name + ";" + artists + ";" + popularity + ";" + duration;
        }
    }
}
