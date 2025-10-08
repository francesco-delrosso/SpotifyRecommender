package org.example;

import org.neo4j.driver.*;
import org.neo4j.driver.Record;

import java.util.ArrayList;
import java.util.List;

public class SongService {
    private Driver driver;

    public SongService(Driver driver) {
        this.driver = driver;
    }

    /**
     * Recupera canzoni con paginazione
     */
    public SongListResult getSongs(int page, int pageSize) {
        try (Session session = driver.session()) {
            int skip = page * pageSize;

            String query = """
                MATCH (s:Song)-[:PERFORMED_BY]->(a:Artist)
                WITH s, collect(a.artistName) AS artists
                RETURN s.songId AS id, s.trackName AS name, artists, 
                       s.popularity AS popularity, s.duration_ms AS duration
                ORDER BY s.popularity DESC
                SKIP $skip LIMIT $limit
                """;

            Result result = session.run(query,
                    Values.parameters("skip", skip, "limit", pageSize));

            List<SongInfo> songs = new ArrayList<>();
            while (result.hasNext()) {
                Record record = result.next();
                songs.add(new SongInfo(
                        record.get("id").asString(),
                        record.get("name").asString(),
                        record.get("artists").asList().toString(),
                        record.get("popularity").asInt(),
                        record.get("duration").asInt()
                ));
            }

            return new SongListResult(true, songs, "");
        } catch (Exception e) {
            e.printStackTrace();
            return new SongListResult(false, null, "Errore del server");
        }
    }

    /**
     * Ricerca canzoni per nome
     */
    public SongListResult searchSongs(String searchTerm, int page, int pageSize) {
        try (Session session = driver.session()) {
            int skip = page * pageSize;

            String query = """
                MATCH (s:Song)-[:PERFORMED_BY]->(a:Artist)
                WHERE toLower(s.trackName) CONTAINS toLower($search)
                   OR toLower(a.artistName) CONTAINS toLower($search)
                WITH s, collect(a.artistName) AS artists
                RETURN s.songId AS id, s.trackName AS name, artists,
                       s.popularity AS popularity, s.duration_ms AS duration
                ORDER BY s.popularity DESC
                SKIP $skip LIMIT $limit
                """;

            Result result = session.run(query,
                    Values.parameters("search", searchTerm, "skip", skip, "limit", pageSize));

            List<SongInfo> songs = new ArrayList<>();
            while (result.hasNext()) {
                Record record = result.next();
                songs.add(new SongInfo(
                        record.get("id").asString(),
                        record.get("name").asString(),
                        record.get("artists").asList().toString(),
                        record.get("popularity").asInt(),
                        record.get("duration").asInt()
                ));
            }

            return new SongListResult(true, songs, "");
        } catch (Exception e) {
            e.printStackTrace();
            return new SongListResult(false, null, "Errore del server");
        }
    }

    public static class SongInfo {
        private String id;
        private String name;
        private String artists;
        private int popularity;
        private int duration;

        public SongInfo(String id, String name, String artists, int popularity, int duration) {
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

    public static class SongListResult {
        private boolean success;
        private List<SongInfo> songs;
        private String message;

        public SongListResult(boolean success, List<SongInfo> songs, String message) {
            this.success = success;
            this.songs = songs;
            this.message = message;
        }

        public boolean isSuccess() { return success; }
        public List<SongInfo> getSongs() { return songs; }
        public String getMessage() { return message; }
    }
}
