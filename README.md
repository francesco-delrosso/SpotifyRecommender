<div align="center">

# Spotify Recommender

![JavaFX](https://img.shields.io/badge/JavaFX-Application-blue?style=flat-square&logo=java)
![Status](https://img.shields.io/badge/status-Active-success?style=flat-square)
![Neo4j](https://img.shields.io/badge/Neo4j-Graph%20Database-green?style=flat-square&logo=neo4j)

</div>

---

## Introduction

**Spotify Recommender** is an advanced **JavaFX desktop application** designed to help users explore and discover new music by leveraging both **personal listening preferences** and **social connections**.

The application analyzes the songs a user marks as favorites and compares them with the preferences of their friends to provide **personalized, socially-informed recommendations**. By combining **collaborative filtering** (suggestions based on similar users) and **content-based filtering** (suggestions based on liked tracks), Spotify Recommender offers a **dynamic, community-driven listening experience** inspired by Spotify’s own recommendation philosophy.

---

## Key Features

| Feature | Description |
|---------|-------------|
| Song Browsing | Explore a comprehensive catalog of songs with detailed metadata including title, artist, and popularity. |
| Favorites System | Add or remove tracks from your favorites list seamlessly with a single click. |
| Smart Search | Efficiently search for songs by title or artist. |
| Friend System | Connect with other users, send and accept friend requests, and explore their music preferences. |
| Social Recommendations | Receive song suggestions based on both your favorites and the favorites of your friends. |

---

## How It Works

1. Login – Users authenticate using their credentials.  
2. Data Fetching – The client retrieves songs and user data from the server.  
3. Favorites Tracking – Users’ favorite songs are stored and linked to their profiles.  
4. Recommendation Engine – The system compares the favorites of the user with those of their friends to find patterns and similarities.  
5. Personalized Suggestions – Users receive a curated list of recommendations tailored to both their taste and social network.

---

## Technologies Used

- **JavaFX** — Interactive user interface framework.  
- **Neo4j (Graph Database)** — Represents the music ecosystem as a graph, with nodes for users, songs, and friendships, enabling advanced relationship-based recommendations.  
- **FXML & CSS** — For structured and visually appealing UI design.  
- **Maven** — Project build and dependency management.  
- **Docker** — Containerized environment for both the server and Neo4j database, ensuring portability and reproducibility.

---

## Installation and Setup Guide

### 1. Prerequisites

Ensure the following tools are installed:

- Java JDK 21  
- Maven 3.9+  
- Docker and Docker Compose  

Verify installations:

```bash
java -version
mvn -version
docker -v
```

### 2. Clone the project

```bash
git clone https://github.com/francesco-delrosso/SpotifyRecommender.git
cd spotify-recommender
```

### 3. Download the dataset

Download the Spotify dataset from Kaggle:  
[Spotify Tracks Dataset](https://www.kaggle.com/datasets/maharshipandya/-spotify-tracks-dataset)

Place it in the `import` folder:

```
spotify-recommender/
├── client/
├── server/
├── import/
│   └── dataset.csv
└── docker-compose.yml
```

### 4. Set up Docker environment

Start the environment:

```bash
docker compose up
```

Access Neo4j at [http://localhost:7474](http://localhost:7474) using user: neo4j and password: Madrid2025!, import the dataset, then run the server and client.

---

## Docker Compose Explanation

The `docker-compose.yml` file orchestrates two services: `neo4j` and `jupyter`.

### 1. Neo4j Service

```yaml
neo4j:
  image: neo4j:latest
  container_name: neo4j-demo
  environment:
    - NEO4J_AUTH=neo4j/Madrid2025!
    - NEO4JLABS_PLUGINS=["apoc"]
    - NEO4J_apoc_import_file_enabled=true
    - NEO4J_dbms_directories_import=/import
  ports:
    - "7474:7474"
    - "7687:7687"
  volumes:
    - ./data:/data
    - ./import:/import
    - ./logs:/logs
  restart: always
```

### 2. Jupyter Notebook Service

```yaml
jupyter:
  image: jupyter/base-notebook:latest
  container_name: jupyter-notebook
  ports:
    - "8888:8888"
  volumes:
    - ./notebooks:/home/jovyan/work
  environment:
    - JUPYTER_TOKEN=
    - JUPYTER_ENABLE_LAB=yes
  command: start-notebook.sh --NotebookApp.token='' --NotebookApp.password=''
  depends_on:
    - neo4j
  restart: unless-stopped
```

> Together, these services provide a reproducible environment for both database management and data analysis.

---

## Authors

| Name |
|------|
| Francesco Del Rosso | 
| Davide Cartolano |
| Tommaso Ferloni |

---

## Future Improvements

- Integration with Spotify API for real-time data.  
- Advanced machine learning recommendation engine.  
- Analytics for user listening patterns.

---

### Explanation

1. **Client (JavaFX)**: Users interact with the interface, send requests, and receive recommendations.  
2. **Server (Java)**: Handles login, requests, favorites updates, and computes recommendations. Communicates with Neo4j via Cypher queries.  
3. **Neo4j (Graph Database)**: Stores users, songs, favorites, and friendships; enables complex queries and relationship-based suggestions.  
4. **Jupyter Notebook**: Analyzes datasets and statistics to improve recommendations, using CSV imports and Python libraries (pandas, numpy, networkx, etc.).  
