package kuanying.popularmovies.data;

import org.parceler.Parcel;

@Parcel
public class Review {
    String id;
    String author;
    String content;
    String url;
    public String getAuthor() { return author; }
    public String getContent() { return content; }
}
