package kuanying.popularmovies.data;

import android.net.Uri;

import org.parceler.Parcel;

@Parcel
public class Trailer {
    String id;
    String key;
    String name;
    String site;

    public String getName() { return name; }

    //assume it's always youtube
    public Uri getVideoUri() {
        return Uri.parse("https://www.youtube.com/watch").buildUpon().
                appendQueryParameter("v", key).build();
    }

    //ref: http://stackoverflow.com/questions/2068344/how-do-i-get-a-youtube-video-thumbnail-from-the-youtube-api
    public String getThumbnailLink() {
        return String.format("https://img.youtube.com/vi/%s/default.jpg", key);
    }

    public String toString() {
        return key;
    }
}
