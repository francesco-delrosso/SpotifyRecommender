// ========================================
// STEP 1: Creazione Constraints e Indici
// ========================================

// Constraints per garantire unicità
CREATE CONSTRAINT user_id IF NOT EXISTS
FOR (u:User) REQUIRE u.userId IS UNIQUE;

CREATE CONSTRAINT user_email IF NOT EXISTS
FOR (u:User) REQUIRE u.email IS UNIQUE;

CREATE CONSTRAINT song_id IF NOT EXISTS
FOR (s:Song) REQUIRE s.songId IS UNIQUE;

CREATE CONSTRAINT artist_id IF NOT EXISTS
FOR (a:Artist) REQUIRE a.artistId IS UNIQUE;

CREATE CONSTRAINT album_id IF NOT EXISTS
FOR (al:Album) REQUIRE al.albumId IS UNIQUE;

CREATE CONSTRAINT genre_id IF NOT EXISTS
FOR (g:Genre) REQUIRE g.genreId IS UNIQUE;

// Indici per ottimizzare le query
CREATE INDEX song_name IF NOT EXISTS
FOR (s:Song) ON (s.trackName);

CREATE INDEX artist_name IF NOT EXISTS
FOR (a:Artist) ON (a.artistName);

CREATE INDEX genre_name IF NOT EXISTS
FOR (g:Genre) ON (g.genreName);

CREATE INDEX user_login IF NOT EXISTS
FOR (u:User) ON (u.email);


// ========================================
// STEP 2: Caricamento Dati da CSV Spotify
// ========================================

// IMPORTANTE: Per Neo4j in Docker, il file deve essere nella cartella /import
// Il path è relativo alla cartella import del container
// Se il file si chiama "dataset.csv" e sta in /import, usa:

LOAD CSV WITH HEADERS FROM 'file:///dataset.csv' AS row
WITH row WHERE row.track_id IS NOT NULL AND row.track_id <> ''

// Crea o trova il Genre
MERGE (g:Genre {genreId: row.track_genre})
ON CREATE SET g.genreName = row.track_genre

// Crea o trova l'Album (usando il nome come ID)
WITH row, g
MERGE (album:Album {albumId: row.album_name})
ON CREATE SET album.albumName = row.album_name

// Gestione artisti multipli separati da ";"
WITH row, g, album
UNWIND split(row.artists, ';') AS artistName
WITH row, g, album, trim(artistName) AS cleanArtistName

// Crea o trova ogni Artist
MERGE (artist:Artist {artistId: cleanArtistName})
ON CREATE SET artist.artistName = cleanArtistName

// Crea la Song con tutte le proprietà (una sola volta)
WITH row, g, album, collect(artist) AS artists
MERGE (s:Song {songId: row.track_id})
ON CREATE SET
s.trackName = row.track_name,
s.popularity = toInteger(row.popularity),
s.duration_ms = toInteger(row.duration_ms),
s.explicit = CASE row.explicit WHEN 'True' THEN true WHEN 'False' THEN false ELSE false END,
s.danceability = toFloat(row.danceability),
s.energy = toFloat(row.energy),
s.key = toInteger(row.key),
s.loudness = toFloat(row.loudness),
s.mode = toInteger(row.mode),
s.speechiness = toFloat(row.speechiness),
s.acousticness = toFloat(row.acousticness),
s.instrumentalness = toFloat(row.instrumentalness),
s.liveness = toFloat(row.liveness),
s.valence = toFloat(row.valence),
s.tempo = toFloat(row.tempo),
s.time_signature = toInteger(row.time_signature)

// Crea le relazioni
WITH s, album, g, artists
UNWIND artists AS artist
MERGE (s)-[:PERFORMED_BY]->(artist)
MERGE (album)-[:BY_ARTIST]->(artist)

WITH s, album, g
MERGE (s)-[:IN_ALBUM]->(album)
MERGE (s)-[:HAS_GENRE]->(g);


// ========================================
// STEP 3: Query di Verifica
// ========================================

// Conta i nodi per tipo
MATCH (n)
RETURN labels(n) AS NodeType, count(n) AS Count
ORDER BY Count DESC;

// Verifica le relazioni
MATCH ()-[r]->()
RETURN type(r) AS RelationType, count(r) AS Count
ORDER BY Count DESC;