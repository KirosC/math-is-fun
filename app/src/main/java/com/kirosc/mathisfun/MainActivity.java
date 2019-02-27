package com.kirosc.mathisfun;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.dd.morphingbutton.MorphingButton;
import com.github.lzyzsd.circleprogress.DonutProgress;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, TextToSpeech.OnInitListener {

    public static final String CORRECT_ANSWER = "Number of correct answer";
    public static final String INCORRECT_ANSWER = "Number of incorrect answer";

    private static final String INCORRECT = "INCORRECT";
    private static final String CORRECT = "CORRECT";
    private static final String TAG = "MathIsFun_Debug";

    private TextView question_no;
    private TextView question_tv;
    private EditText answer_et;
    private DonutProgress timeCounter;
    private MorphingButton nextBtn;
    private MorphingButton submitBtn;
    private Context context;

    private int answer, inputAnswer, correctAnswer, incorrectAnswer, counter;
    private long timerTime = 300000;
    private boolean timeIsUp, checked;

    private String[] question;
    private TextToSpeech tts;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        question_no = (TextView) findViewById(R.id.question_no);
        question_tv = (TextView) findViewById(R.id.question_tv);
        answer_et = (EditText) findViewById(R.id.answer_tv);
        nextBtn = (MorphingButton) findViewById(R.id.next_btn);
        submitBtn = (MorphingButton) findViewById(R.id.submit_btn);
        timeCounter = (DonutProgress) findViewById(R.id.time_bar);

        initializeQuestion();

        answer_et.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (!checked) {
                        checkAnswer();
                    }
                }
                return true;
            }
        });

        nextBtn.setOnClickListener(this);
        submitBtn.setOnClickListener(this);

        tts = new TextToSpeech(this, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Avoid memory leak
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        // Stop the timer
        countDownTimer.cancel();
    }

    @Override
    protected void onStop() {
        super.onStop();
        countDownTimer.cancel();
    }

    @Override
    protected void onPause() {
        super.onPause();
        countDownTimer.cancel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startCountDownTimer();
    }

    public void startCountDownTimer() {
        // Setup a 5 minutes timer
        timeCounter.setMax(300);
        countDownTimer = new CountDownTimer(timerTime, 1000) {
            boolean changedColor;

            // Change the timer color when less than about one minute
            public void onTick(long millisUntilFinished) {
                // Keep track on the remaining time
                timerTime = millisUntilFinished;
                if (!changedColor && millisUntilFinished <= 61000) {
                    changedColor = true;
                    timeCounter.setFinishedStrokeColor(Color.RED);
                    timeCounter.setTextColor(Color.RED);
                }

                String time = String.format(Locale.getDefault(), "%d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))
                );
                // Update the Progress Bar
                timeCounter.setProgress((int) (millisUntilFinished / 1000));
                timeCounter.setText(time);
            }

            public void onFinish() {
                timeIsUp = true;
                timeCounter.setProgress(0);
                timeCounter.setText("0:00");
                // Do not interrupt the tts
                if (!tts.isSpeaking()) {
                    finishTest();
                }
            }
        }.start();
    }

    public void initializeQuestion() {
        counter++;
        // nextQuestion No. TextView
        question_no.setText(getResources().getString(R.string.question_no, counter));

        // The nextQuestion TextView
        question_tv.setTextColor(ContextCompat.getColor(this, R.color.textDarkPrimary));
        question = QuestionGenerator.getQuestion();
        question[0] += " = ?";
        question_tv.setText(question[0]);
        answer = Integer.parseInt(question[1]);
        Log.d(TAG, "Q" + counter + " Answer: " + String.valueOf(answer));
    }

    public void checkAnswer() {
        checked = true;
        // Disable further input from user
        submitBtn.blockTouch();

        // Update the question TextView to show answer
        question[0] = question[0].replace("?", question[1]);
        question_tv.setText(question[0]);

        // Determine user input any answer
        if (!TextUtils.isEmpty(answer_et.getText().toString().trim())) {
            inputAnswer = Integer.parseInt(answer_et.getText().toString().trim());
            // Correct case
            if (inputAnswer == answer) {
                correctAnswer++;
                question_tv.setTextColor(ContextCompat.getColor(this, R.color.green));
                tts.speak(getString(R.string.correct_speech), TextToSpeech.QUEUE_FLUSH, null, CORRECT);
            } else {
                // Incorrect case
                incorrectAnswer++;
                question_tv.setTextColor(ContextCompat.getColor(this, R.color.red));
                tts.speak(getString(R.string.incorrect_speech) + answer, TextToSpeech.QUEUE_FLUSH, null, INCORRECT);
            }
        } else {
            // Skip case
            question_tv.setTextColor(ContextCompat.getColor(this, R.color.red));
            tts.speak(getString(R.string.answer_speech) + answer, TextToSpeech.QUEUE_FLUSH, null, INCORRECT);
        }
    }

    public void nextQuestion() {
        if (!checked) {
            checkAnswer();
        } else {
            if (counter == 10) {
                finishTest();
                return;
            }
            // Clear the EditText
            answer_et.setText("");
            counter++;
            // Update the question no.
            question_no.setText(getResources().getString(R.string.question_no, counter));
            checked = false;
            // Set up the animation for the Button changing back to original rectangle shape
            MorphingButton.Params rectangle = MorphingButton.Params.create()
                    .duration(200)
                    .cornerRadius(0)
                    .width((int) getResources().getDimension(R.dimen.nextButton_width))
                    .height((int) getResources().getDimension(R.dimen.uniformButton_height))
                    .color(ContextCompat.getColor(context, R.color.blue))
                    .colorPressed(ContextCompat.getColor(context, R.color.blue))
                    .text(getResources().getString(R.string.submit));
            // Start the animation
            submitBtn.morph(rectangle);
            // Enable the input from user
            submitBtn.unblockTouch();

            // Set up for the next question
            question_tv.setTextColor(ContextCompat.getColor(this, R.color.textDarkPrimary));
            question = QuestionGenerator.getQuestion();
            question[0] += " = ?";
            question_tv.setText(question[0]);
            answer = Integer.parseInt(question[1]);
            Log.d(TAG, "Q" + counter + " Answer: " + String.valueOf(answer));
        }
    }

    public void finishTest() {
        Intent intent = new Intent(this, SummaryActivity.class);
        intent.putExtra(CORRECT_ANSWER, correctAnswer);
        intent.putExtra(INCORRECT_ANSWER, incorrectAnswer);

        // Reset variable
        correctAnswer = incorrectAnswer = counter = 0;
        // Proceed to the SummaryActivity
        startActivity(intent);
        finish();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.submit_btn) {
            if (!checked) {
                checkAnswer();
            }
        } else if (id == R.id.next_btn) {
            if (!tts.isSpeaking()) {
                nextQuestion();
            }
        }
    }

    @Override
    public void onInit(int i) {
        if (i == TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.UK);
            tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String s) {
                    if (s.equals(CORRECT)) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // Change the Button to a tick Button
                                MorphingButton.Params circle = MorphingButton.Params.create()
                                        .duration(200)
                                        .cornerRadius((int) getResources().getDimension(R.dimen.uniformButton_height))
                                        .width((int) getResources().getDimension(R.dimen.uniformButton_height))
                                        .height((int) getResources().getDimension(R.dimen.uniformButton_height))
                                        .color(ContextCompat.getColor(context, R.color.green)) // Normal state color
                                        .icon(R.drawable.ic_done); // Button icon
                                submitBtn.morph(circle);
                                // Disable input from user
                                submitBtn.blockTouch();
                            }
                        });
                    } else if (s.equals(INCORRECT)) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // Change the Button to a cross Button
                                MorphingButton.Params circle = MorphingButton.Params.create()
                                        .duration(200)
                                        .cornerRadius((int) getResources().getDimension(R.dimen.uniformButton_height))
                                        .width((int) getResources().getDimension(R.dimen.uniformButton_height))
                                        .height((int) getResources().getDimension(R.dimen.uniformButton_height))
                                        .color(ContextCompat.getColor(context, R.color.red)) // normal state color
                                        .icon(R.drawable.ic_close); // icon
                                submitBtn.morph(circle);
                                submitBtn.blockTouch();
                            }
                        });
                    }
                }

                @Override
                public void onDone(String s) {
                    if (s.equals(CORRECT)) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // Check if the time is up
                                if (timeIsUp) {
                                    finishTest();
                                }
                            }
                        });
                    } else if (s.equals(INCORRECT)) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (timeIsUp) {
                                    finishTest();
                                }
                            }
                        });
                    }
                }

                @Override
                public void onError(String s) {
                }
            });
        }
    }
}