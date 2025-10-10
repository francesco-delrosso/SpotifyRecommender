package org.example.client;

import javafx.beans.property.*;

public class Song {
    private final StringProperty id;
    private final StringProperty name;
    private final StringProperty artists;
    private final IntegerProperty popularity;
    private final IntegerProperty duration;
    private final BooleanProperty isFavorite;

    public Song(String id, String name, String artists, int popularity, int duration) {
        this.id = new SimpleStringProperty(id);
        this.name = new SimpleStringProperty(name);
        this.artists = new SimpleStringProperty(artists);
        this.popularity = new SimpleIntegerProperty(popularity);
        this.duration = new SimpleIntegerProperty(duration);
        this.isFavorite = new SimpleBooleanProperty(false);
    }

    public StringProperty nameProperty() {
        return name;
    }

    public StringProperty artistsProperty() {
        return artists;
    }

    public IntegerProperty popularityProperty() {
        return popularity;
    }

    public String getId() {
        return id.get();
    }

    public BooleanProperty favoriteProperty() {
        return isFavorite;
    }

    public void setIsFavorite(boolean favorite) {
        this.isFavorite.set(favorite);
    }

    public boolean IsFavorite() {
        return isFavorite.get();
    }

}
