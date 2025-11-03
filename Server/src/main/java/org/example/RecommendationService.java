package org.example;

import org.neo4j.driver.*;
import org.neo4j.driver.Record;

import java.util.ArrayList;
import java.util.List;

public class RecommendationService {
    private final Driver driver;

    public RecommendationService(Driver driver) {
        this.driver = driver;
    }

    public List<RecommendedSong> getRecommendations(String userEmail, int limit) {
        try (Session session = driver.session()) {
            /*
             * This is a comprehensive hybrid recommendation query that combines social,
             * popularity, and content-based filtering.
             *
             * The query logic is divided into 5 main steps:
             *
             * STEP 1: Identify the user's taste profile.
             * - Find the user's Top 5 most-liked Genres.
             * - Find the user's Top 5 most-liked Artists.
             * - Collect all songs the user has already liked (to use for similarity
             * and exclusion).
             *
             * STEP 2: Find Candidate Songs.
             * - Find all songs liked by the user's direct friends (:FRIEND_OF).
             * - Filter out any songs the user has already liked.
             *
             * STEP 3: Aggregate Candidate Song Details.
             * - For each candidate song, count distinct friends who like it (friendsWhoLike).
             * - Count total distinct users who like it (totalLikes).
             * - **CRITICAL**: Collect *both* artist names/IDs and genre IDs into lists.
             * This aggregation (collect) is essential to ensure each song is
             * processed only ONCE, preventing duplicate rows (fan-out) if a
             * song has multiple artists or genres.
             *
             * STEP 4: Calculate All Scores for the Hybrid Model.
             * - 4a. Content Score (Audio):
             * - Calculate the average difference (avgSimilarityDiff) across 7 audio
             * features between the candidate song and all songs the user likes.
             * - This score is multiplied by a negative weight (-100) because a
             * *smaller* difference means *higher* similarity.
             * - 4b. Genre Affinity Bonus (Content):
             * - Check if *any* of the candidate song's genres (songGenreIds)
             * are present in the user's Top 5 Genres (userTopGenres).
             * - A large bonus (75) is given if there is a match.
             * - 4c. Artist Affinity Bonus (Content):
             * - Check if *any* of the candidate song's artists (artistIds)
             * are present in the user's Top 5 Artists (userTopArtists).
             * - A large bonus (75) is given if there is a match.
             *
             * STEP 5: Calculate Final Score and Return.
             * - The final score is a weighted sum of all parts:
             * - (friendsWhoLike * 50)  [Social]
             * - (totalLikes * 10)      [Popularity]
             * - (avgSimilarityDiff * -100) [Content - Audio]
             * - (genreBonus * 75)      [Content - Genre]
             * - (artistBonus * 75)     [Content - Artist]
             * - The results are ordered by this final score (DESC) and limited.
             */
            String query =
                    // STEP 1: Get User, their Top 5 Genres, Top 5 Artists, and all Liked Songs
                    "MATCH (u:User {email: $email}) " +

                            // Get Top 5 Genres
                            "OPTIONAL MATCH (u)-[:LIKES]->(:Song)-[:HAS_GENRE]->(likedGenre:Genre) " +
                            "WITH u, likedGenre, count(likedGenre) AS genreFrequency " +
                            "ORDER BY genreFrequency DESC " +
                            "WITH u, collect(likedGenre.genreId)[..5] AS userTopGenres " +

                            // Get Top 5 Artists
                            "OPTIONAL MATCH (u)-[:LIKES]->(:Song)-[:PERFORMED_BY]->(likedArtist:Artist) " +
                            "WITH u, userTopGenres, likedArtist, count(likedArtist) AS artistFrequency " +
                            "ORDER BY artistFrequency DESC " +
                            "WITH u, userTopGenres, collect(likedArtist.artistId)[..5] AS userTopArtists " +

                            // Get all liked songs (for similarity calculation)
                            "MATCH (u)-[:LIKES]->(userLikedSong:Song) " +
                            "WITH u, userTopGenres, userTopArtists, collect(userLikedSong) AS userLikedSongs " +

                            // STEP 2: Find Candidate Songs from Friends
                            "MATCH (u)-[:FRIEND_OF]-(friend:User)-[:LIKES]->(recSong:Song) " +
                            "WHERE NOT recSong IN userLikedSongs " +

                            // STEP 3: Get Candidate Song Details (Counts, Artists, Genre)
                            "WITH u, userTopGenres, userTopArtists, userLikedSongs, recSong, count(DISTINCT friend) AS friendsWhoLike " +

                            "MATCH (allUser:User)-[:LIKES]->(recSong) " +
                            "WITH u, userTopGenres, userTopArtists, userLikedSongs, recSong, friendsWhoLike, count(DISTINCT allUser) AS totalLikes " +

                            // ====================================================================
                            // FIX: Collect *both* artists AND genres to prevent duplicate rows
                            // ====================================================================
                            "MATCH (recSong)-[:PERFORMED_BY]->(recArtist:Artist) " +
                            "MATCH (recSong)-[:HAS_GENRE]->(recGenre:Genre) " + // Get all genres
                            "WITH u, userTopGenres, userTopArtists, userLikedSongs, recSong, friendsWhoLike, totalLikes, " +
                            "     collect(DISTINCT recArtist.artistName) AS artistNames, " +
                            "     collect(DISTINCT recArtist.artistId) AS artistIds, " +
                            "     collect(DISTINCT recGenre.genreId) AS songGenreIds " + // <-- FIX: Colleziona i generi

                            // STEP 4: Calculate All Scores

                            // 4a. Expanded Content Similarity (7 proprietà)
                            "UNWIND userLikedSongs AS userSong " +
                            "WITH u, userTopGenres, userTopArtists, recSong, friendsWhoLike, totalLikes, artistNames, artistIds, songGenreIds, userSong, " +
                            "     abs(recSong.danceability - userSong.danceability) AS diffDance, " +
                            "     abs(recSong.energy - userSong.energy) AS diffEnergy, " +
                            "     abs(recSong.valence - userSong.valence) AS diffValence, " +
                            "     abs(recSong.acousticness - userSong.acousticness) AS diffAcoustic, " +
                            "     abs(recSong.instrumentalness - userSong.instrumentalness) AS diffInstrument, " +
                            "     abs(recSong.speechiness - userSong.speechiness) AS diffSpeech, " +
                            "     abs(recSong.liveness - userSong.liveness) AS diffLive " +

                            // Average the differences
                            "WITH u, userTopGenres, userTopArtists, recSong, friendsWhoLike, totalLikes, artistNames, artistIds, songGenreIds, " +
                            "     AVG(diffDance + diffEnergy + diffValence + diffAcoustic + diffInstrument + diffSpeech + diffLive) AS avgSimilarityDiff " +

                            // ====================================================================
                            // FIX: Check if *any* song genre is in the user's top genres
                            // ====================================================================
                            "WITH u, userTopGenres, userTopArtists, recSong, friendsWhoLike, totalLikes, artistNames, artistIds, avgSimilarityDiff, songGenreIds, " +
                            "     CASE WHEN any(id IN songGenreIds WHERE id IN userTopGenres) THEN 1 ELSE 0 END AS genreBonus " + // <-- FIX

                            // 4c. Artist Affinity Bonus
                            "WITH u, userTopArtists, recSong, friendsWhoLike, totalLikes, artistNames, artistIds, avgSimilarityDiff, genreBonus, " +
                            "     CASE WHEN any(id IN artistIds WHERE id IN userTopArtists) THEN 1 ELSE 0 END AS artistBonus " +

                            // STEP 5: Calculate Final Score and Return
                            "WITH recSong.songId AS id, " +
                            "     recSong.trackName AS name, " +
                            "     artistNames, " +
                            "     recSong.popularity AS popularity, " +
                            "     recSong.duration_ms AS duration, " +
                            "     friendsWhoLike, " +
                            "     totalLikes, " +
                            "     (friendsWhoLike * 50) AS friendScore, " +
                            "     (totalLikes * 10) AS popularityScore, " +
                            "     (avgSimilarityDiff * -100) AS contentScore, " +
                            "     (genreBonus * 75) AS genreScore, " +
                            "     (artistBonus * 75) AS artistScore " +

                            // Calculate final score
                            "WITH id, name, artistNames, popularity, duration, friendsWhoLike, totalLikes, " +
                            "     (friendScore + popularityScore + contentScore + genreScore + artistScore) AS finalScore " +

                            "RETURN id, name, artistNames, popularity, duration, friendsWhoLike, totalLikes, finalScore AS similarityScore " +
                            "ORDER BY similarityScore DESC " +
                            "LIMIT $limit";

            Result result = session.run(query,
                    Values.parameters("email", userEmail, "limit", limit));

            List<RecommendedSong> recommendations = new ArrayList<>();
            while (result.hasNext()) {
                Record record = result.next();

                List<String> artistList = record.get("artistNames").asList(Value::asString);
                String artists = String.join(", ", artistList);

                recommendations.add(new RecommendedSong(
                        record.get("id").asString(),
                        record.get("name").asString(),
                        artists,
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