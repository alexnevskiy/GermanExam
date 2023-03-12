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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

public class TaskThreeAnswer extends AppCompatActivity {
    final String TASK3_AUDIO_INTRODUCTION = "Task3AudioIntroduction";
    final String TASK3_AUDIO_QUESTION1 = "Task3AudioQuestion1";
    final String TASK3_AUDIO_QUESTION2 = "Task3AudioQuestion2";
    final String TASK3_AUDIO_QUESTION3 = "Task3AudioQuestion3";
    final String TASK3_AUDIO_QUESTION4 = "Task3AudioQuestion4";
    final String TASK3_AUDIO_QUESTION5 = "Task3AudioQuestion5";
    final String TASK3_AUDIO_ENDING = "Task3AudioEnding";
    final String TASK1 = "Task1";
    final String TASK2 = "Task2";
    final String TASK3 = "Task3";
    final String VARIANT = "Variant";
    final String NAME = "Name";
    final String SURNAME = "Surname";
    final String CLASS = "Class";
    final String RESTART = "Restart";

    private final String RESOURCE_PATH = ContentResolver.SCHEME_ANDROID_RESOURCE + "://";

    private final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private String fileName = null;
    private String task3AudioIntroduction = null;
    private String task3AudioQuestion1 = null;
    private String task3AudioQuestion2 = null;
    private String task3AudioQuestion3 = null;
    private String task3AudioQuestion4 = null;
    private String task3AudioQuestion5 = null;
    private String task3AudioEnding = null;
    private boolean isWorking = false;
    private boolean isWorkingPart = false;

    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};

    private MediaRecorder recorder = null;
    private MediaPlayer player = null;

    SharedPreferences sharedPreferences;

    long timeLeft = 0;
    long introductionTimeLeft = 0;
    long question1TimeLeft = 0;
    long question2TimeLeft = 0;
    long question3TimeLeft = 0;
    long question4TimeLeft = 0;
    long question5TimeLeft = 0;
    long endingTimeLeft = 0;
    long partTimeLeft = 0;
    int counter = 0;
    int partOfTask = 1;
    CountDownTimer countDownTimer;
    CountDownTimer timerPartOfTask;

    private TextView timeRemaining;
    private TextView taskPartDuration;
    private TextView taskText;

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

        setContentView(R.layout.task3_answer);
        timeRemaining = findViewById(R.id.time_remaining);
        final ProgressBar timeline = findViewById(R.id.timeline);
        timeline.setMax((int) (timeLeft / 1000));

        taskText = findViewById(R.id.task3_text);
        taskPartDuration = findViewById(R.id.task3_part_duration);

        sharedPreferences = getSharedPreferences("StudentData", MODE_PRIVATE);

        task3AudioIntroduction = sharedPreferences.getString(TASK3_AUDIO_INTRODUCTION, "");
        task3AudioQuestion1 = sharedPreferences.getString(TASK3_AUDIO_QUESTION1, "");
        task3AudioQuestion2 = sharedPreferences.getString(TASK3_AUDIO_QUESTION2, "");
        task3AudioQuestion3 = sharedPreferences.getString(TASK3_AUDIO_QUESTION3, "");
        task3AudioQuestion4 = sharedPreferences.getString(TASK3_AUDIO_QUESTION4, "");
        task3AudioQuestion5 = sharedPreferences.getString(TASK3_AUDIO_QUESTION5, "");
        task3AudioEnding = sharedPreferences.getString(TASK3_AUDIO_ENDING, "");

        introductionTimeLeft = getAudioDuration(task3AudioIntroduction);
        question1TimeLeft = getAudioDuration(task3AudioQuestion1);
        question2TimeLeft = getAudioDuration(task3AudioQuestion2);
        question3TimeLeft = getAudioDuration(task3AudioQuestion3);
        question4TimeLeft = getAudioDuration(task3AudioQuestion4);
        question5TimeLeft = getAudioDuration(task3AudioQuestion5);
        endingTimeLeft = getAudioDuration(task3AudioEnding);
        timeLeft = introductionTimeLeft + question1TimeLeft + question2TimeLeft + question3TimeLeft +
                question4TimeLeft + question5TimeLeft + endingTimeLeft + 40000 * 5 + 1000; // 1 seconds reserve
        timeline.setMax((int) (timeLeft / 1000));
        updateTimer(timeLeft, timeRemaining);

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
                stopRecording();
                Intent intent = new Intent(TaskThreeAnswer.this, Ready.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.putExtra("task", "4");
                intent.putExtra("answer", "no");
                startActivity(intent);
                isWorking = false;
                countDownTimer.cancel();
            }
        }.start();

        isWorking = true;

        startPartOfTask();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (isWorking) {
            countDownTimer.cancel();
            if (isWorkingPart) {
                if (!((partOfTask == 3) || (partOfTask == 5) || (partOfTask == 7) || (partOfTask == 9) ||
                        (partOfTask == 11))) {
                    stopPlaying();
                }
                timerPartOfTask.cancel();
            }

            stopRecording();

            deleteFiles();

            sharedPreferences = getSharedPreferences("StudentData", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(RESTART, true);
            editor.apply();
        }
    }

    private void startPartOfTask() {
        timerPartOfTask = null;
        switch (partOfTask) {
            case 1:
                taskText.setText("Aufgabe 3. Einführung.");
                timerPartOfTask = getTimerPartOfTask(introductionTimeLeft);
                timerPartOfTask.start();
                isWorkingPart = true;
                startPlaying(task3AudioIntroduction);
                break;
            case 2:
                taskText.setText("Aufgabe 3. Frage 1.");
                timerPartOfTask = getTimerPartOfTask(question1TimeLeft);
                timerPartOfTask.start();
                isWorkingPart = true;
                startPlaying(task3AudioQuestion1);
                break;
            case 3: case 5: case 7: case 9: case 11:
                timerPartOfTask = getTimerPartOfTask(40000);
                timerPartOfTask.start();
                isWorkingPart = true;
                break;
            case 4:
                taskText.setText("Aufgabe 3. Frage 2.");
                timerPartOfTask = getTimerPartOfTask(question2TimeLeft);
                timerPartOfTask.start();
                isWorkingPart = true;
                startPlaying(task3AudioQuestion2);
                break;
            case 6:
                taskText.setText("Aufgabe 3. Frage 3.");
                timerPartOfTask = getTimerPartOfTask(question3TimeLeft);
                timerPartOfTask.start();
                isWorkingPart = true;
                startPlaying(task3AudioQuestion3);
                break;
            case 8:
                taskText.setText("Aufgabe 3. Frage 4.");
                timerPartOfTask = getTimerPartOfTask(question4TimeLeft);
                timerPartOfTask.start();
                isWorkingPart = true;
                startPlaying(task3AudioQuestion4);
                break;
            case 10:
                taskText.setText("Aufgabe 3. Frage 5.");
                timerPartOfTask = getTimerPartOfTask(question5TimeLeft);
                timerPartOfTask.start();
                isWorkingPart = true;
                startPlaying(task3AudioQuestion5);
                break;
            case 12:
                taskText.setText("Aufgabe 3. Ende.");
                timerPartOfTask = getTimerPartOfTask(endingTimeLeft);
                timerPartOfTask.start();
                isWorkingPart = true;
                startPlaying(task3AudioEnding);
                break;
            default:
                break;
        }
    }

    private CountDownTimer getTimerPartOfTask(long timeLeft) {
        return new CountDownTimer(timeLeft, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                partTimeLeft = millisUntilFinished;
                updateTimer();
            }

            private void updateTimer() {
                int minutes = (int) (partTimeLeft / 1000) / 60;
                int seconds = (int) (partTimeLeft / 1000) % 60;

                String timeLeftText = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);

                taskPartDuration.setText(timeLeftText);
            }

            @Override
            public void onFinish() {
                if (!((partOfTask == 3) || (partOfTask == 5) || (partOfTask == 7) || (partOfTask == 9) ||
                        (partOfTask == 11))) {
                    stopPlaying();
                }
                partOfTask++;
                isWorkingPart = false;
                this.cancel();
                startPartOfTask();
            }
        };
    }

    private void loadData(String task) {
        sharedPreferences = getSharedPreferences("StudentData", MODE_PRIVATE);
        fileName = sharedPreferences.getString(task, "");
    }

    private  void deleteFiles() {
        loadData(TASK1);
        File file1 = new File(fileName);
        boolean deleted1 = file1.delete();
        Log.i("TaskThreeAnswer", "Audio1 is deleting:" + deleted1);

        loadData(TASK2);
        File file2 = new File(fileName);
        boolean deleted2 = file2.delete();
        Log.i("TaskThreeAnswer", "Audio2 is deleting:" + deleted2);

        loadData(TASK3);
        File file3 = new File(fileName);
        boolean deleted3 = file3.delete();
        Log.i("TaskThreeAnswer", "Audio3 is deleting:" + deleted3);
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_window_title);
        builder.setNegativeButton(R.string.menu, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                stopRecording();
                if (isWorkingPart) {
                    if (!((partOfTask == 3) || (partOfTask == 5) || (partOfTask == 7) || (partOfTask == 9) ||
                            (partOfTask == 11))) {
                        stopPlaying();
                    }
                }
                Intent intent = new Intent(TaskThreeAnswer.this, Menu.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                countDownTimer.cancel();
                timerPartOfTask.cancel();
                deleteFiles();
                isWorking = false;
                isWorkingPart = false;
            }
        });
        builder.setNeutralButton(R.string.desktop, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                stopRecording();
                if (isWorkingPart) {
                    if (!((partOfTask == 3) || (partOfTask == 5) || (partOfTask == 7) || (partOfTask == 9) ||
                            (partOfTask == 11))) {
                        stopPlaying();
                    }
                }
                countDownTimer.cancel();
                timerPartOfTask.cancel();
                deleteFiles();
                isWorking = false;
                isWorkingPart = false;
                finishAffinity();
            }
        });
        builder.setPositiveButton(R.string.variants_menu, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                stopRecording();
                if (isWorkingPart) {
                    if (!((partOfTask == 3) || (partOfTask == 5) || (partOfTask == 7) || (partOfTask == 9) ||
                            (partOfTask == 11))) {
                        stopPlaying();
                    }
                }
                Intent intent = new Intent(TaskThreeAnswer.this, Variants.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                countDownTimer.cancel();
                timerPartOfTask.cancel();
                deleteFiles();
                isWorking = false;
                isWorkingPart = false;
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

    private long getAudioDuration(String audioFilename) {
        String audioPath = RESOURCE_PATH + getPackageName() + "/raw/" + audioFilename;
        Uri audioUri = Uri.parse(audioPath);

        player = new MediaPlayer();
        try {
            player.setDataSource(getApplicationContext(), audioUri);
            player.prepare();
        } catch (IOException e) {
            Log.e("prepare()", "failed");
        }
        long timeLeft = player.getDuration();
        player.reset();
        player = null;
        return timeLeft;
    }

    private void updateTimer(long timeLeft, TextView timeRemaining) {
        String timeLeftText = timeLeftCalculation(timeLeft);
        timeRemaining.setText(timeLeftText);
    }

    private String timeLeftCalculation(long timeLeft) {
        int minutes = (int) (timeLeft / 1000) / 60;
        int seconds = (int) (timeLeft / 1000) % 60;

        return String.format(Locale.getDefault(), "-%02d:%02d", minutes, seconds);
    }

    private void startPlaying(String audioFilename) {
        String audioPath = RESOURCE_PATH + getPackageName() + "/raw/" + audioFilename;
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
        fileName += sharedPreferences.getString(CLASS, "") + "_Aufgabe3_Variant_";
        fileName += sharedPreferences.getInt(VARIANT, 0) + ".mp3";
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(TASK3, fileName);
        editor.apply();
    }
}