<div align="center">

# 🎵 **Spotify Recommender**

### *Discover music you’ll love — powered by your friends and your favorites.*

![JavaFX](https://img.shields.io/badge/JavaFX-Application-blue?style=flat-square&logo=java)
![Status](https://img.shields.io/badge/status-Active-success?style=flat-square)
![Neo4j](https://img.shields.io/badge/Neo4j-Graph%20Database-green?style=flat-square&logo=neo4j)

</div>

---

## 📖 **Introduction**

**Spotify Recommender** is an innovative **JavaFX desktop application** that helps users discover new music through a combination of **personal preferences** and **social connections**.

The system analyzes the songs a user has marked as favorites and cross-references them with their friends’ preferences to deliver **personalized, socially-informed music recommendations**.

By blending **collaborative filtering** (suggestions based on similar users) and **content-based filtering** (suggestions based on liked tracks), Spotify Recommender creates a dynamic, community-driven listening experience — inspired by the spirit of Spotify itself.

---

## 🚀 **Key Features**

| 🌟 Feature | 💬 Description |
|-------------|----------------|
| 🎧 **Song Browsing** | Explore a rich catalog of songs, complete with details like title, artist, and popularity. |
| ⭐ **Favorites System** | Add or remove tracks from your favorites list with a single click on a star icon. |
| 🔍 **Smart Search** | Quickly find songs by name or artist. |
| 👥 **Friend System** | Search for users, send and accept friend requests. |
| 💡 **Social Recommendations** | Get song suggestions based on both your favorites and your friends’ favorite tracks. |

---

## 🧠 **How It Works**

> “The more you connect, the better it gets.”  

1. 🧾 **Login** – The user signs into the application using their credentials.  
2. 🌐 **Data Fetching** – The client communicates with the server to retrieve available songs and user data.  
3. ⭐ **Favorites Tracking** – When a user marks songs as favorites, they are saved and linked to their profile.  
4. 🧩 **Recommendation Engine** – The system compares the user’s favorites with those of their friends to identify musical similarities.  
5. 🎯 **Personalized Suggestions** – The user receives a curated list of songs that best match their listening taste and social network.

---

## 🖥️ **Technologies Used**

- 🧩 **JavaFX** — for the interactive user interface  
- - 🧠 **Neo4j (Graph Database)** — for managing relationships between users, songs, and favorites  
  > Neo4j allows the system to represent the music ecosystem as a **graph**, where users, songs, and friendships are interconnected nodes, enabling advanced **relationship-based recommendations**.  
- 🧱 **FXML & CSS** — for structured and visually appealing UI design  
- 🧰 **Maven** — for project build and dependency management
- 🐳 **Docker** — for easy setup and deployment of the server and Neo4j database  
  > Docker ensures a consistent, portable environment — you can spin up the backend and database in seconds using preconfigured containers.

---

## 🧩 **Installation and Setup Guide**

Follow these steps to install and run **Spotify Recommender** from scratch.  
These instructions allow any student to reproduce the demo independently.

---

### 🧰 **1. Prerequisites**

Ensure the following tools are installed:

- ☕ **Java JDK 21**  
- 🧱 **Maven 3.9+**  
- 🐳 **Docker** and **Docker Compose**  

You can verify installations with:
```bash
java -version
mvn -version
docker -v
```

### 🧰 **2. Clone the project**

Clone the Repository.
 ```bash
git clone https://github.com/francesco-delrosso/SpotifyRecommender.git
cd spotify-recommender
 ```

### **3. Download the dataset**

Download the Spotify dataset from Kaggle:  
🔗 [Spotify Tracks Dataset](https://www.kaggle.com/datasets/maharshipandya/-spotify-tracks-dataset)
Put it in the "import" folder.
```
spotify-recommender/
├── client/
├── server/
├── import/
│ └── dataset.csv
└── docker-compose.yml
```

### **🐳 4. Set up docker**

Run the following command from the project root to start the environment:

```bash
docker compose up
```
Connect to http://localhost:7474 and import the dataset.
Run the server and the client.

## 👨‍💻 **Authors**

| Name |
|------|
| Francesco Del Rosso | 
| Davide Cartolano |
| Tommaso Ferloni |

---

## 💬 **Future Improvements**

- 🎶 Integration with the **Spotify API**  
- 🧠 Machine Learning–based recommendation engine   
- 📊 Analytics for listening patterns  

---

<div align="center">

✨ *“Because great music*
