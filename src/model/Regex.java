package model;


import java.io.Serializable;
import java.util.Objects;

public class Regex implements Serializable {
    private static int nextId = 0;
    private int id;
    private String pattern;
    private String description;
    private int rating;

    public Regex(String pattern, String description) {
        this.pattern = pattern;
        this.description = description;
        this.id = ++nextId;
        this.rating = 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Regex regex = (Regex) o;
        return id == regex.id && Objects.equals(pattern, regex.pattern);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, pattern);
    }

    @Override
    public String toString() {
        return "Regex{" +
                "pattern='" + pattern + '\'' +
                ", description='" + description + '\'' +
                ", rating=" + rating +
                '}';
    }
}
