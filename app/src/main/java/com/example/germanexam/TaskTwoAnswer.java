package com.example.germanexam;

import android.Manifest;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
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
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

public class TaskTwoAnswer extends AppCompatActivity {
    final String TASK1 = "Task1";
    final String TASK2 = "Task2";
    final String VARIANT = "Variant";
    final String NAME = "Name";
    final String SURNAME = "Surname";
    final String CLASS = "Class";
    final String TASK2PICTURE = "Task2Picture";
    final String TASK2QUESTION1 = "Task2Question1";
    final String TASK2QUESTION2 = "Task2Question2";
    final String TASK2QUESTION3 = "Task2Question3";
    final String TASK2QUESTION4 = "Task2Question4";
    final String TASK2PICTURETEXT = "Task2PictureText";
    final String RESTART = "Restart";

    private final String RESOURCE_PATH = ContentResolver.SCHEME_ANDROID_RESOURCE + "://";

    final String beepFilename = "task2_beep";

    private final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private String fileName = null;
    private boolean isWorking = false;

    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};

    private MediaRecorder recorder = null;
    private MediaPlayer player = null;

    SharedPreferences sharedPreferences;

    long timeLeft = 80000;
    int counter = 0;
    CountDownTimer countDownTimer;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted ) finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        saveFilename();
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
        startRecording();

        setContentView(R.layout.task2_answer);
        final TextView timeRemaining = findViewById(R.id.time_remaining);
        final ProgressBar timeline = findViewById(R.id.timeline);
        final TextView questionText = findViewById(R.id.task2_text);
        timeline.setMax((int) (timeLeft / 1000));

        TextView task2PictureTextView = findViewById(R.id.task2_title_image);
        ImageView task2ImageView = findViewById(R.id.task2_image);
        sharedPreferences = getSharedPreferences("StudentData", MODE_PRIVATE);
        String task2Text = sharedPreferences.getString(TASK2PICTURETEXT, "");
        final String task2Question1 = sharedPreferences.getString(TASK2QUESTION1, "");
        final String task2Question2 = sharedPreferences.getString(TASK2QUESTION2, "");
        final String task2Question3 = sharedPreferences.getString(TASK2QUESTION3, "");
        final String task2Question4 = sharedPreferences.getString(TASK2QUESTION4, "");
        String task2Image = sharedPreferences.getString(TASK2PICTURE, "");
        int pictureId = getResources().getIdentifier(task2Image, "drawable", getPackageName());
        task2PictureTextView.setText(task2Text);

        task2ImageView.setImageDrawable(getResources().getDrawable(pictureId));

        countDownTimer = new CountDownTimer(timeLeft, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeft = millisUntilFinished;
                updateTimer();
                switch (counter) {
                    case 0:
                        questionText.setText("Frage 1.\n" + task2Question1);
                        break;
                    case 20:
                        startPlaying();
                        questionText.setText("Frage 2.\n" + task2Question2);
                        break;
                    case 40:
                        startPlaying();
                        questionText.setText("Frage 3.\n" + task2Question3);
                        break;
                    case 60:
                        startPlaying();
                        questionText.setText("Frage 4.\n" + task2Question4);
                        break;
                }
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
                stopRecording();
                Intent intent = new Intent(TaskTwoAnswer.this, Ready.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.putExtra("task", "3");
                intent.putExtra("answer", "no");
                startActivity(intent);
                isWorking = false;
                countDownTimer.cancel();
            }
        }.start();

        isWorking = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (isWorking) {
            countDownTimer.cancel();
            stopRecording();
            stopPlaying();

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
        Log.i("TaskTwoAnswer", "Audio1 is deleting:" + deleted1);

        loadData(TASK2);
        File file2 = new File(fileName);
        boolean deleted2 = file2.delete();
        Log.i("TaskTwoAnswer", "Audio2 is deleting:" + deleted2);
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_window_title);
        builder.setNegativeButton(R.string.menu, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                stopRecording();
                stopPlaying();
                Intent intent = new Intent(TaskTwoAnswer.this, Menu.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                countDownTimer.cancel();
                deleteFiles();
                isWorking = false;
            }
        });
        builder.setNeutralButton(R.string.desktop, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                stopRecording();
                stopPlaying();
                countDownTimer.cancel();
                deleteFiles();
                isWorking = false;
                finishAffinity();
            }
        });
        builder.setPositiveButton(R.string.variants_menu, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                stopRecording();
                stopPlaying();
                Intent intent = new Intent(TaskTwoAnswer.this, Variants.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                countDownTimer.cancel();
                deleteFiles();
                isWorking = false;
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void startRecording() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFile(fileName);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        recorder.setAudioEncodingBitRate(128000);
        recorder.setAudioSamplingRate(96000);

        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e("startRecording()", "prepare() failed");
        }

        recorder.start();
    }

    private void stopRecording() {
        try {
            recorder.stop();
        } catch (RuntimeException stopException) {
            stopException.printStackTrace();
        }
        recorder.reset();
        recorder.release();
        recorder = null;
        Log.i("Recording", "Recording stopped, file path: " + fileName);
    }

    private void startPlaying() {
        String audioPath = RESOURCE_PATH + getPackageName() + "/raw/" + beepFilename;
        Uri audioUri = Uri.parse(audioPath);

        player = new MediaPlayer();
        try {
            player.setDataSource(getApplicationContext(), audioUri);
            player.prepare();
            player.start();
        } catch (IOException e) {
            Log.e("startPlaying()", "prepare() failed");
        }
    }

    private void stopPlaying() {
        player.reset();
        player.release();
        player = null;
    }

    private void saveFilename() {
        fileName = getFilesDir().getAbsolutePath();
        sharedPreferences = getSharedPreferences("StudentData", MODE_PRIVATE);
        fileName += "/audio/" + sharedPreferences.getString(SURNAME, "") + "_";
        fileName += sharedPreferences.getString(NAME, "") + "_";
        fileName += sharedPreferences.getString(CLASS, "") + "_Aufgabe2_Variant_";
        fileName += sharedPreferences.getInt(VARIANT, 0) + ".mp3";
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(TASK2, fileName);
        editor.apply();
    }
}