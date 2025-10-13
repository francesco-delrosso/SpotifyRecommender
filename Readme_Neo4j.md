# Introduction to Neo4j

![Neo4j Logo](https://upload.wikimedia.org/wikipedia/commons/2/2e/Neo4j_Logo.png)

Neo4j is a **graph database**: instead of storing data in tables (like relational databases), Neo4j stores data as **nodes** and **relationships** between them. This model is particularly useful for representing connections between data, such as friends, favorite songs, playlists, and music recommendations.  

In SpotifyRecommender, Neo4j is used to:
- Create users and friendship relationships.
- Save songs to favorites.
- Generate song recommendations based on connections between users and musical tastes.

---

# How Neo4j Works

## Nodes
Nodes represent the main entities in your domain, such as:
- `User` (user)
- `Song` (music track)
- `Playlist` (playlist)

Each node can have **properties**, for example:

```cypher
CREATE (u:User {username: 'Alice', age: 25})
CREATE (s:Song {title: 'Imagine', artist: 'John Lennon'})
```

## Relationships

Relationships connect nodes and can also have properties:

(:User)-[:FRIEND]->(:User) → represents a friendship
(:User)-[:LIKES]->(:Song) → represents a song saved as favorite
```
MATCH (u1:User {username: 'Alice'}), (u2:User {username: 'Bob'})
CREATE (u1)-[:FRIEND]->(u2)
```
This query returns all songs liked by user Alice.

# Advantages and Disadvantages of Neo4j

| Advantages                                      | Disadvantages                                             |
|-------------------------------------------------|----------------------------------------------------------|
| Intuitive modeling of connected data           | Vertical scalability: may require powerful hardware      |
| Fast queries on connections                     | Less suitable for purely tabular data                    |
| Flexibility: add new nodes and relationships easily | Learning curve: Cypher and graph concepts require practice |

