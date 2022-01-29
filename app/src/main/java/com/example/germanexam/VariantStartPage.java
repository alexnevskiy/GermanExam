package com.example.germanexam;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

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

public class VariantStartPage extends AppCompatActivity {
    final String TASK1TEXT = "Task1Text";
    final String TASK2TITLE = "Task2Title";
    final String TASK2QUESTIONS = "Task2Questions";
    final String TASK2PICTURE = "Task2Picture";
    final String TASK2QUESTION1 = "Task2Question1";
    final String TASK2QUESTION2 = "Task2Question2";
    final String TASK2QUESTION3 = "Task2Question3";
    final String TASK2QUESTION4 = "Task2Question4";
    final String TASK2QUESTION5 = "Task2Question5";
    final String TASK2PICTURETEXT = "Task2PictureText";
    final String TASK3QUESTIONS = "Task3Questions";
    final String TASK3PICTURE1 = "Task3Picture1";
    final String TASK3PICTURE2 = "Task3Picture2";
    final String TASK3PICTURE3 = "Task3Picture3";
    final String TASK4QUESTIONS = "Task4Questions";
    final String TASK4PICTURE1 = "Task4Picture1";
    final String TASK4PICTURE2 = "Task4Picture2";
    final String JSON = "Json";
    final String VARIANT = "Variant";

    SharedPreferences sharedPreferences;

    private InterstitialAd mInterstitialAd;
    private int state = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.variant_start);

        loadAd();

        Button buttonVariants = findViewById(R.id.button_start_test);

        buttonVariants.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedPreferences = getSharedPreferences("StudentData", MODE_PRIVATE);
                String json = sharedPreferences.getString(JSON, "");
                int variant = sharedPreferences.getInt(VARIANT, 0);
                JsonParser jsonParser = new JsonParser(json, variant);
                saveData(jsonParser);

                Intent intent = new Intent(VariantStartPage.this, Ready.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.putExtra("task", "1");
                intent.putExtra("answer", "no");
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_window_title);
        builder.setNegativeButton(R.string.menu, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                state = 2;
                if (mInterstitialAd != null) {
                    mInterstitialAd.show(VariantStartPage.this);
                } else {
                    Intent intent = new Intent(VariantStartPage.this, Menu.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }
            }
        });
        builder.setNeutralButton(R.string.desktop, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finishAffinity();
            }
        });
        builder.setPositiveButton(R.string.variants_menu, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                state = 1;
                if (mInterstitialAd != null) {
                    mInterstitialAd.show(VariantStartPage.this);
                } else {
                    Intent intent = new Intent(VariantStartPage.this, Variants.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void saveData(JsonParser jsonParser) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(TASK1TEXT, jsonParser.getTask1Text());
        editor.putString(TASK2TITLE, jsonParser.getTask2Title());
        editor.putString(TASK2QUESTIONS, jsonParser.getTask2Questions());
        editor.putString(TASK2PICTURE, jsonParser.getTask2Picture());
        editor.putString(TASK2QUESTION1, jsonParser.getTask2Question1());
        editor.putString(TASK2QUESTION2, jsonParser.getTask2Question2());
        editor.putString(TASK2QUESTION3, jsonParser.getTask2Question3());
        editor.putString(TASK2QUESTION4, jsonParser.getTask2Question4());
        editor.putString(TASK2QUESTION5, jsonParser.getTask2Question5());
        editor.putString(TASK2PICTURETEXT, jsonParser.getTask2PictureText());
        editor.putString(TASK3QUESTIONS, jsonParser.getTask3Questions());
        editor.putString(TASK3PICTURE1, jsonParser.getTask3Picture1());
        editor.putString(TASK3PICTURE2, jsonParser.getTask3Picture2());
        editor.putString(TASK3PICTURE3, jsonParser.getTask3Picture3());
        editor.putString(TASK4QUESTIONS, jsonParser.getTask4Questions());
        editor.putString(TASK4PICTURE1, jsonParser.getTask4Picture1());
        editor.putString(TASK4PICTURE2, jsonParser.getTask4Picture2());
        editor.apply();
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
                                Intent intent1 = new Intent(VariantStartPage.this, Variants.class);
                                intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent1);
                                finish();
                                break;
                            case 2:
                                Intent intent2 = new Intent(VariantStartPage.this, Menu.class);
                                intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent2);
                                finish();
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