package com.example.germanexam;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import java.io.File;
import java.util.Locale;

public class TaskTwo extends AppCompatActivity {

    final String TASK2TITLE = "Task2Title";
    final String TASK2QUESTIONS = "Task2Questions";
    final String TASK2PICTURE = "Task2Picture";
    final String TASK2PICTURETEXT = "Task2PictureText";
    final String TASK1 = "Task1";
    final String RESTART = "Restart";

    private String fileName = null;
    private boolean isWorking = false;

    SharedPreferences sharedPreferences;

    long timeLeft = 90000;
    int counter = 0;
    CountDownTimer countDownTimer;

    private InterstitialAd mInterstitialAd;
    private int state = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task2);

        loadAd();

        final TextView timeRemaining = findViewById(R.id.time_remaining);
        final ProgressBar timeline = findViewById(R.id.timeline);

        TextView task2TextView = findViewById(R.id.task2_text);
        TextView task2QuestionsView = findViewById(R.id.task2_questions);
        TextView task2PictureTextView = findViewById(R.id.task2_title_image);
        ImageView task2ImageView = findViewById(R.id.task2_image);
        sharedPreferences = getSharedPreferences("StudentData", MODE_PRIVATE);
        String task2PictureText = sharedPreferences.getString(TASK2PICTURETEXT, "");
        String task2Text = sharedPreferences.getString(TASK2TITLE, "");
        String task2Questions = sharedPreferences.getString(TASK2QUESTIONS, "");
        String task2Image = sharedPreferences.getString(TASK2PICTURE, "");
        int pictureId = getResources().getIdentifier(task2Image, "drawable", getPackageName());
        task2TextView.setText("Aufgabe 2. Sehen Sie sich folgende Anzeige an.\n" + task2Text);
        task2QuestionsView.setText(task2Questions);
        task2PictureTextView.setText(task2PictureText);

        task2PictureTextView.setVisibility(View.INVISIBLE);  //  Временно

        task2ImageView.setImageDrawable(getResources().getDrawable(pictureId));

        countDownTimer = new CountDownTimer(timeLeft, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeft = millisUntilFinished;
                updateTimer();
                counter++;
                timeline.setProgress(counter);
            }

            private void updateTimer() {
                int minutes = (int) (timeLeft / 1000) / 60;
                int seconds = (int) (timeLeft / 1000) % 60;

                String timeLeftText = String.format(Locale.getDefault(), "-%02d:%02d", minutes, seconds);

                timeRemaining.setText(timeLeftText);
            }

            @Override
            public void onFinish() {
                Intent intent = new Intent(TaskTwo.this, Ready.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.putExtra("task", "2");
                intent.putExtra("answer", "yes");
                startActivity(intent);
                isWorking = false;
                countDownTimer.cancel();
            }
        }.start();

        isWorking = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isWorking) {
            countDownTimer.cancel();

            deleteFiles();

            sharedPreferences = getSharedPreferences("StudentData", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(RESTART, true);
            editor.apply();
        }
    }

    private void loadData(String task) {
        sharedPreferences = getSharedPreferences("StudentData", MODE_PRIVATE);
        fileName = sharedPreferences.getString(task, "");
    }

    private  void deleteFiles() {
        loadData(TASK1);
        File file1 = new File(fileName);
        boolean deleted1 = file1.delete();
        Log.i("TaskFourAnswer", "Audio1 is deleting:" + deleted1);
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_window_title);
        builder.setNegativeButton(R.string.menu, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                state = 2;
                if (mInterstitialAd != null) {
                    mInterstitialAd.show(TaskTwo.this);
                } else {
                    Intent intent = new Intent(TaskTwo.this, Menu.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    countDownTimer.cancel();
                    deleteFiles();
                    isWorking = false;
                }
            }
        });
        builder.setNeutralButton(R.string.desktop, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                countDownTimer.cancel();
                deleteFiles();
                isWorking = false;
                finishAffinity();
            }
        });
        builder.setPositiveButton(R.string.variants_menu, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                state = 1;
                if (mInterstitialAd != null) {
                    mInterstitialAd.show(TaskTwo.this);
                } else {
                    Intent intent = new Intent(TaskTwo.this, Variants.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    countDownTimer.cancel();
                    deleteFiles();
                    isWorking = false;
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void loadAd() {
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(this,"ca-app-pub-4327528430123865/7721312778", adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                mInterstitialAd = interstitialAd;
                mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback(){
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        switch (state) {
                            case 1:
                                Intent intent1 = new Intent(TaskTwo.this, Variants.class);
                                intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent1);
                                countDownTimer.cancel();
                                deleteFiles();
                                isWorking = false;
                                break;
                            case 2:
                                Intent intent2 = new Intent(TaskTwo.this, Menu.class);
                                intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent2);
                                countDownTimer.cancel();
                                deleteFiles();
                                isWorking = false;
                                break;
                            default:
                                break;
                        }
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                        Log.d("TAG", "The ad failed to show.");
                    }

                    @Override
                    public void onAdShowedFullScreenContent() {
                        mInterstitialAd = null;
                        Log.d("TAG", "The ad was shown.");
                    }
                });
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                Log.i("onAdFailedToLoad", loadAdError.getMessage());
                mInterstitialAd = null;
            }
        });
    }
}