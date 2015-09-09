package kuanying.popularmovies;

import org.parceler.Parcel;

@Parcel
public class Trailer {
    String id;
    String key;
    String name;
    String site;

    public String toString() {
        //ref: http://stackoverflow.com/questions/2068344/how-do-i-get-a-youtube-video-thumbnail-from-the-youtube-api
        //thumbnail: http://img.youtube.com/vi/<key>/default.jpg

        //link: https://www.youtube.com/watch?v=<key>

        //assume it's always youtube
        //return String.format("https://www.youtube.com/watch?v=%s", key);
        return site;
    }
}
