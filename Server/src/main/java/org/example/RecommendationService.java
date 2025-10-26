package org.example;

import org.neo4j.driver.*;

import java.util.ArrayList;
import java.util.List;

public class RecommendationService {
    private final Driver driver;

    public RecommendationService(Driver driver) {
        this.driver = driver;
    }

    public List<RecommendedSong> getRecommendations(String userEmail, int limit) {
        try (Session session = driver.session()) {
            String query =
                    "MATCH (u:User {email: $email})-[:LIKES]->(likedSong:Song) " +
                            "WITH u, collect(likedSong) AS userLikedSongs " +
                            "MATCH (friend:User)-[:FRIEND_OF]-(u) " +
                            "MATCH (friend)-[:LIKES]->(friendSong:Song) " +
                            "WHERE NOT friendSong IN userLikedSongs " +
                            "WITH friendSong, " +
                            "     COUNT(DISTINCT friend) AS friendsWhoLike, " +
                            "     userLikedSongs " +
                            "MATCH (allUser:User)-[:LIKES]->(friendSong) " +
                            "WITH friendSong, " +
                            "     friendsWhoLike, " +
                            "     COUNT(DISTINCT allUser) AS totalLikes, " +
                            "     userLikedSongs " +
                            "MATCH (friendSong)-[:PERFORMED_BY]->(artist:Artist) " +  // ← FIX: recupera gli artisti
                            "WITH friendSong, " +
                            "     friendsWhoLike, " +
                            "     totalLikes, " +
                            "     collect(artist.artistName) AS artistNames, " +  // ← FIX: lista artisti
                            "     userLikedSongs " +
                            "UNWIND userLikedSongs AS userSong " +
                            "WITH friendSong, " +
                            "     friendsWhoLike, " +
                            "     totalLikes, " +
                            "     artistNames, " +
                            "     AVG(abs(friendSong.danceability - userSong.danceability) + " +
                            "         abs(friendSong.energy - userSong.energy) + " +
                            "         abs(friendSong.valence - userSong.valence)) AS avgSimilarity " +
                            "RETURN friendSong.songId AS id, " +
                            "       friendSong.trackName AS name, " +
                            "       artistNames, " +  // ← FIX: restituisci lista artisti
                            "       friendSong.popularity AS popularity, " +
                            "       friendSong.duration_ms AS duration, " +
                            "       friendsWhoLike, " +
                            "       totalLikes, " +
                            "       (friendsWhoLike * 50 + totalLikes * 10 - avgSimilarity * 20) AS similarityScore " +
                            "ORDER BY similarityScore DESC " +
                            "LIMIT $limit";

            Result result = session.run(query,
                    Values.parameters("email", userEmail, "limit", limit));

            List<RecommendedSong> recommendations = new ArrayList<>();
            while (result.hasNext()) {
                org.neo4j.driver.Record record = result.next();

                // Gestisci la lista di artisti
                List<String> artistList = record.get("artistNames").asList(Value::asString);
                String artists = String.join(", ", artistList);  // ← FIX: unisci con ", "

                recommendations.add(new RecommendedSong(
                        record.get("id").asString(),
                        record.get("name").asString(),
                        artists,  // ← FIX: usa gli artisti recuperati
                        record.get("popularity").asInt(),
                        record.get("duration").asInt(),
                        record.get("friendsWhoLike").asInt(),
                        record.get("totalLikes").asInt(),
                        record.get("similarityScore").asDouble()
                ));
            }
            return recommendations;
        }
    }

    public static class RecommendedSong {
        public String id;
        public String name;
        public String artists;
        public int popularity;
        public int duration;
        public int friendsWhoLike;
        public int totalLikes;
        public double similarityScore;

        public RecommendedSong(String id, String name, String artists, int popularity,
                               int duration, int friendsWhoLike, int totalLikes,
                               double similarityScore) {
            this.id = id;
            this.name = name;
            this.artists = artists;
            this.popularity = popularity;
            this.duration = duration;
            this.friendsWhoLike = friendsWhoLike;
            this.totalLikes = totalLikes;
            this.similarityScore = similarityScore;
        }
    }
}
