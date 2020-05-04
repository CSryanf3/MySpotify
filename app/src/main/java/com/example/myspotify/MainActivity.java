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

import com.spotify.protocol.types.Track;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

// Code for onActivityResult, onStart, and parts of playPlaylist from the Spotify Android SDK...
// Quick Start code at https://developer.spotify.com/documentation/android/quick-start/
public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1337;
    private static final String CLIENT_ID = "39c2a45ece414c3ba42e4d66916de39c";
    private static final String REDIRECT_URI = "http://localhost:8888/callback";
    private SpotifyAppRemote mSpotifyAppRemote;
    private static String authToken;

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
                    System.out.println("Sucessful login to Spotify account!");

                    authToken = response.getAccessToken();
                    Log.e("MainActivity","Auth Token: "+ authToken.toString());
                    break;

                // Auth flow returned an error
                case ERROR:
                    // Handle error response
                    System.out.println("Unsucessful login to Spotify account!");
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
        ConnectionParams connectionParams =
                new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .showAuthView(true)
                        .build();

        SpotifyAppRemote.connect(this, connectionParams,
                new Connector.ConnectionListener() {

                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                        mSpotifyAppRemote = spotifyAppRemote;
                        Log.d("MainActivity", "You are connected.");

                        // Now you can start interacting with App Remote

                    }

                    public void onFailure(Throwable throwable) {
                        Log.e("MyActivity", throwable.getMessage(), throwable);

                        // Something went wrong when attempting to connect! Handle errors here
                    }
                });
    }

    //Plays from the CS 125 Playlist
    public void playPlaylist(android.view.View view) {
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

    //Plays one of user's top 20 tracks
    public void playTopTrack(android.view.View view) {
        Random rand = new Random();
        Song randomTrack = topPlayedTracks.get(rand.nextInt(topPlayedTracks.size()));
        randomTrack.removeFeat();
        randomTrack.shortenName();
        String randomTrackID = randomTrack.getId();
        String randomTrackName = randomTrack.getName();
        mSpotifyAppRemote.getPlayerApi().play("spotify:track:" + randomTrackID);
        TextView textView = (TextView) findViewById(R.id.Header);
        textView.setText(randomTrackName);
    }

    //Plays one of user's top 20 artists
    public void playTopArtist(android.view.View view) {
        Random rand = new Random();
        Artist randomArtist = topPlayedArtists.get(rand.nextInt(topPlayedArtists.size()));
        randomArtist.shortenName();
        String randomArtistID = randomArtist.getId();
        String randomArtistName = randomArtist.getName();
        mSpotifyAppRemote.getPlayerApi().play("spotify:artist:" + randomArtistID);
        TextView textView = (TextView) findViewById(R.id.Header);
        textView.setText(randomArtistName);
    }

    //Fetches the users top tracks and opens the Top Tracks screen
    private void getTracks() {
        songService.getTopPlayedTracks(() -> {
            topPlayedTracks = songService.getSongs();
            updateSong();
        });
    }

    //Updates the screen with the user's top 10 tracks
    private void updateSong() {
        for (int i = 0; i < topPlayedTracks.size(); i++) {
            System.out.println(topPlayedTracks.get(i).getName());
        }
        setContentView(R.layout.activity_tracks);
        TextView textView = (TextView) findViewById(R.id.Track1);
        textView.setText(topPlayedTracks.get(0).getName());
        textView = (TextView) findViewById(R.id.Track2);
        textView.setText(topPlayedTracks.get(1).getName());
        textView = (TextView) findViewById(R.id.Track3);
        textView.setText(topPlayedTracks.get(2).getName());
        textView = (TextView) findViewById(R.id.Track4);
        textView.setText(topPlayedTracks.get(3).getName());
        textView = (TextView) findViewById(R.id.Track5);
        textView.setText(topPlayedTracks.get(4).getName());
        textView = (TextView) findViewById(R.id.Track6);
        textView.setText(topPlayedTracks.get(5).getName());
        textView = (TextView) findViewById(R.id.Track7);
        textView.setText(topPlayedTracks.get(6).getName());
        textView = (TextView) findViewById(R.id.Track8);
        textView.setText(topPlayedTracks.get(7).getName());
        textView = (TextView) findViewById(R.id.Track9);
        textView.setText(topPlayedTracks.get(8).getName());
        textView = (TextView) findViewById(R.id.Track10);
        textView.setText(topPlayedTracks.get(9).getName());
    }

    //Fetches the user's Top Artists and opens the Top Artists Screen
    private void getArtists() {
        songService.getTopPlayedArtists(() -> {
            topPlayedArtists = songService.getArtists();
            updateArtist();
        });
    }

    //Updates the screen with the user's top 10 artists
    private void updateArtist() {
        for (int i = 0; i < topPlayedArtists.size(); i++) {
            System.out.println(topPlayedArtists.get(i).getName());
        }
        setContentView(R.layout.activity_artists);
        TextView textView = (TextView) findViewById(R.id.Artist1);
        textView.setText(topPlayedArtists.get(0).getName());
        textView = (TextView) findViewById(R.id.Artist2);
        textView.setText(topPlayedArtists.get(1).getName());
        textView = (TextView) findViewById(R.id.Artist3);
        textView.setText(topPlayedArtists.get(2).getName());
        textView = (TextView) findViewById(R.id.Artist4);
        textView.setText(topPlayedArtists.get(3).getName());
        textView = (TextView) findViewById(R.id.Artist5);
        textView.setText(topPlayedArtists.get(4).getName());
        textView = (TextView) findViewById(R.id.Artist6);
        textView.setText(topPlayedArtists.get(5).getName());
        textView = (TextView) findViewById(R.id.Artist7);
        textView.setText(topPlayedArtists.get(6).getName());
        textView = (TextView) findViewById(R.id.Artist8);
        textView.setText(topPlayedArtists.get(7).getName());
        textView = (TextView) findViewById(R.id.Artist9);
        textView.setText(topPlayedArtists.get(8).getName());
        textView = (TextView) findViewById(R.id.Artist10);
        textView.setText(topPlayedArtists.get(9).getName());
    }

    //Button method
    public void topTracks(android.view.View view) {
        getTracks();
    }

    //Button Method
    public void topArtists(android.view.View view) {
        getArtists();
    }

    //Button method
    public void toPlayScreen(android.view.View view) {
        setContentView(R.layout.activity_play);
    }

    //Button method
    public void backToMain(android.view.View view) {
        setContentView(R.layout.activity_main);
    }

    //Pauses the current song
    public void pauseSong(android.view.View view) {
        mSpotifyAppRemote.getPlayerApi().pause();
    }

    //Resumes the current song
    public void resumeSong(android.view.View view) {
        mSpotifyAppRemote.getPlayerApi().resume();
    }

    //Skips the current song
    public void skipSong(android.view.View view) {
        mSpotifyAppRemote.getPlayerApi().skipNext();
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

    //Gets the authentication token
    public static String getAuthToken() {
        return authToken;
    }

    @Override
    protected void onStop() {
        super.onStop();
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }
}