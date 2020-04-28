package com.example.myspotify;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.widget.TextView;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

//import statements for authorization
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;
import com.spotify.sdk.android.auth.AuthorizationClient;

import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;
import com.spotify.sdk.android.auth.app.SpotifyAuthHandler;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1337;
    private static final String CLIENT_ID = "beede1e63d374dc28e256448cb4f886e";
    private static final String REDIRECT_URI = "http://localhost:8888/callback";
    private SpotifyAppRemote mSpotifyAppRemote;
    private static String authToken;

    private Song song;
    private Artist artist;
    private SongService songService;
    private ArrayList<Song> topPlayedTracks;
    private ArrayList<Artist> topPlayedArtists;

    private final String SCOPES = "user-top-read,user-read-recently-played,user-library-modify,user-read-email,user-read-private";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //begin login flow

        // Request code will be used to verify if result comes from the login activity. Can be set to any integer.
        //private static final int REQUEST_CODE = 1337;
        //private static final String REDIRECT_URI = "yourcustomprotocol://callback";

        AuthorizationRequest.Builder builder =
                new AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.TOKEN, REDIRECT_URI);

        builder.setScopes(new String[]{SCOPES});
        AuthorizationRequest request = builder.build();

        AuthorizationClient.openLoginActivity(this, REQUEST_CODE, request);

        songService = new SongService(getApplicationContext());
        SharedPreferences sharedPreferences = this.getSharedPreferences("SPOTIFY", 0);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthorizationResponse response = AuthorizationClient.getResponse(resultCode, intent);

            switch (response.getType()) {
                // Response was successful and contains auth token
                case TOKEN:
                    // Handle successful response
                    System.out.println("Sucessful login to Spotify account! :)");

                    authToken = response.getAccessToken();
                    Log.e("MainActivity","Auth Token: "+ authToken.toString());
                    break;

                // Auth flow returned an error
                case ERROR:
                    // Handle error response
                    System.out.println("Unsucessful login to Spotify account! :(");
                    break;

                // Most likely auth flow was cancelled
                default:
                    // Handle other cases
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public void CS125PlaylistClick(android.view.View view) {
        ConnectionParams connectionParams =
                new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .showAuthView(true)
                        .build();

        SpotifyAppRemote.connect(this, connectionParams,
                new Connector.ConnectionListener() {

                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                        mSpotifyAppRemote = spotifyAppRemote;
                        Log.d("MainActivity", "Connected! Yay!");

                        // Now you can start interacting with App Remote
                        setContentView(R.layout.activity_play);

                    }

                    public void onFailure(Throwable throwable) {
                        Log.e("MyActivity", throwable.getMessage(), throwable);

                        // Something went wrong when attempting to connect! Handle errors here
                    }
                });
    }

    public void playSong(android.view.View view) {
        // Play a playlist
        mSpotifyAppRemote.getPlayerApi().setShuffle(true);
        mSpotifyAppRemote.getPlayerApi().play("spotify:playlist:4QEC6bqhulA5VSLI9J8B5V");

        mSpotifyAppRemote.getPlayerApi()
                .subscribeToPlayerState()
                .setEventCallback(playerState -> {
                    final Track track = playerState.track;
                    if (track != null) {
                        final String currentTrack = track.name;
                        final String currentArtist = track.artist.name;
                        TextView textView = (TextView) findViewById(R.id.currentTrack);
                        textView.setText("Track: " + currentTrack);
                        textView = (TextView) findViewById(R.id.currentArtist);
                        textView.setText("Artist: " + currentArtist);
                    }
                });
    }

    public void topTracks(android.view.View view) {
//        try {
//            URL url = new URL("https://api.spotify.com/v1/me/top/tracks");
//            HttpURLConnection con = (HttpURLConnection) url.openConnection();
//            con.setRequestMethod("GET");
//            Map<String, String> parameters = new HashMap<>();
//            parameters.put("type","tracks");
//            parameters.put("time_range", "long_term");
//            parameters.put("limit", "10");
//            parameters.put("offset", "0");
//            parameters.put("OAuth Token", "BQDpG7vsK_Mv6TpeHYMm-QFlExpo2SdjBxo6xqA-9lGLnIKd5BI8OdSj2BdZx5uFNUWt95XsvSbHz_oNeIJbbuxFR9ucSoyCwUwADWRgOcuu5g5Vb4u2ae3ENYZEGy47ZXZMduItRDEqyb4IUmB3lUSPQQ");
//
////            con.setDoOutput(true);
////            DataOutputStream out = new DataOutputStream(con.getOutputStream());
////            out.writeBytes(getParamsString(parameters));
////            out.flush();
////            out.close();
////
//            con.setRequestProperty("Authorization", "Bearer " + authToken);
////
////            con.setConnectTimeout(5000);
////            con.setReadTimeout(5000);
////
////
////
////            //read the response
////            int status = con.getResponseCode();
////            BufferedReader in = new BufferedReader(
////                    new InputStreamReader(con.getInputStream()));
////            String inputLine;
////            StringBuffer content = new StringBuffer();
////            while ((inputLine = in.readLine()) != null) {
////                content.append(inputLine);
////            }
////            System.out.println(content);
////            in.close();
////
////
//
////            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
////            StringBuilder sb = new StringBuilder();
////            String line;
////            while ((line = br.readLine()) != null) {
////                sb.append(line + "\n");
////            }
////            br.close();
////
////            String jsonString = sb.toString();
////            System.out.println("JSON: " + jsonString);
////            con.disconnect();
//
//
//        } catch (Exception e) {
//            System.out.println("Error attempting to get user's top tracks");
//            return;
//        }

        getTracks();
        setContentView(R.layout.activity_tracks);

    }

    private void getTracks() {
        songService.getTopPlayedTracks(() -> {
            topPlayedTracks = songService.getSongs();
            updateSong();
        });
    }

    private void updateSong() {
        if (topPlayedTracks.size() > 0) {
            //songView.setText(topPlayedTracks.get(0).getName());
            song = topPlayedTracks.get(0);
//            System.out.println(song.getName());
        }
        for (int i = 0; i < topPlayedTracks.size(); i++) {
            System.out.println(topPlayedTracks.get(i).getName());
        }
    }

    private void getArtists() {
        songService.getTopPlayedArtists(() -> {
            topPlayedArtists = songService.getArtists();
            updateArtist();
        });
    }

    private void updateArtist() {
        if (topPlayedArtists.size() > 0) {
            //songView.setText(topPlayedTracks.get(0).getName());
            artist = topPlayedArtists.get(0);
//            System.out.println(song.getName());
        }
        for (int i = 0; i < topPlayedArtists.size(); i++) {
            System.out.println(topPlayedArtists.get(i).getName());
        }
    }

    public void topArtists(android.view.View view) {
        getArtists();
        setContentView(R.layout.activity_artists);
    }

    public void backToMain(android.view.View view) {
        setContentView(R.layout.activity_main);
    }

    public static String getParamsString(Map<String, String> params)
            throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            result.append("&");
        }

        String resultString = result.toString();
        return resultString.length() > 0
                ? resultString.substring(0, resultString.length() - 1)
                : resultString;
    }

    public static String getAuthToken() {
        return authToken;
    }

    @Override
    protected void onStop() {
        super.onStop();
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }
}