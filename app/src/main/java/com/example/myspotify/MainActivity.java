package com.example.myspotify;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.widget.TextView;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1337;
    private static final String CLIENT_ID = "39c2a45ece414c3ba42e4d66916de39c";
    private static final String REDIRECT_URI = "http://localhost:8888/callback";
    private SpotifyAppRemote mSpotifyAppRemote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
        setContentView(R.layout.activity_tracks);
    }

    public void topArtists(android.view.View view) {
        setContentView(R.layout.activity_artists);
    }

    public void backToMain(android.view.View view) {
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStop() {
        super.onStop();
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }
}