package kuanying.popularmovies;

public class Movie {
    private String posterUrl;
    private long id;
    private String title;

    public Movie(long id, String title, String poster) {
        this.id = id;
        this.title = title;
        this.posterUrl = poster;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public String toString() {
        return title+"("+id+")";
    }
}
