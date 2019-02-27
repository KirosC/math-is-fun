package com.kirosc.mathisfun;

import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Locale;

public class SummaryActivity extends AppCompatActivity implements View.OnClickListener, TextToSpeech.OnInitListener {

    public static final String SUMMARY = "Summary";

    private int correctAnswer;
    private int incorrectAnswer;
    private int giveUpQuestion;

    private TextView correctNo_tv;
    private TextView incorrectNo_tv;
    private TextView givenUpNo_tv;
    private Button restart_btn;

    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        // Set up the back arrow key on Action Bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        correctNo_tv = (TextView) findViewById(R.id.correctNo_tv);
        incorrectNo_tv = (TextView) findViewById(R.id.incorrectNo_tv);
        givenUpNo_tv = (TextView) findViewById(R.id.givenUpNo_tv);
        restart_btn = (Button) findViewById(R.id.restart_btn);

        Intent intent = getIntent();
        if (intent != null) {
            // Extract the data passed from the MainActivity
            Bundle extra = intent.getExtras();
            correctAnswer = extra.getInt(MainActivity.CORRECT_ANSWER);
            incorrectAnswer = extra.getInt(MainActivity.INCORRECT_ANSWER);
            giveUpQuestion = 10 - correctAnswer - incorrectAnswer;

            correctNo_tv.setText(String.valueOf(correctAnswer));
            incorrectNo_tv.setText(String.valueOf(incorrectAnswer));
            givenUpNo_tv.setText(String.valueOf(giveUpQuestion));
        }

        tts = new TextToSpeech(this, this);

        restart_btn.setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            tts.stop();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        return true;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.restart_btn) {
            tts.stop();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        tts.stop();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onInit(int i) {
        if (i == TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.UK);
            tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String s) {

                }

                @Override
                public void onDone(String s) {

                }

                @Override
                public void onError(String s) {

                }
            });
            String summary = getResources().getString(R.string.summary_speech, correctAnswer, incorrectAnswer, giveUpQuestion);
            tts.speak(summary, TextToSpeech.QUEUE_FLUSH, null, SUMMARY);
        }
    }
}