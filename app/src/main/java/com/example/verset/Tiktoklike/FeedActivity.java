package com.example.verset.Tiktoklike;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.verset.R;
import com.example.verset.widgetcustome.widgetcustom;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class FeedActivity extends AppCompatActivity {

    private ViewPager2 pager;
    private FavoritesStore favoritesStore;
    private VersePagerAdapter adapter;

    private FloatingActionButton fabVoice;
    private FloatingActionButton fabLike;

    private BottomNavigationView bottomNav;

    private TextToSpeech tts;
    private boolean ttsReady = false;
    private boolean isSpeaking = false;

    // ✅ Auto-play
    private boolean autoPlay = false;
    private boolean pendingSpeakAfterPage = false;

    // ✅ Animation (pulse) pour 🔊
    private ObjectAnimator fabPulseAnimator;

    // ✅ Animation (pop) pour ❤️
    private ObjectAnimator likePopAnimator;

    // ✅ Animation nav bar (pop)
    private boolean ignoreNavSelection = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        pager = findViewById(R.id.pager);
        fabVoice = findViewById(R.id.fabVoice);
        fabLike = findViewById(R.id.fabLike);
        bottomNav = findViewById(R.id.bottomNav);

        favoritesStore = new FavoritesStore(this);

        // ✅ Exemple de versets (à remplacer par ta DB plus tard)
        List<Verse> verses = Arrays.asList(
                new Verse("1", "The Lord is my shepherd; I shall not want.", "Psalm 23:1"),
                new Verse("2", "For God so loved the world…", "John 3:16"),
                new Verse("3", "Trust in the Lord with all your heart.", "Proverbs 3:5")
        );

        adapter = new VersePagerAdapter(favoritesStore, verse -> favoritesStore.toggle(verse.id));

        pager.setOrientation(ViewPager2.ORIENTATION_VERTICAL);
        pager.setAdapter(adapter);
        pager.setOffscreenPageLimit(1);
        adapter.submit(verses);

        // ✅ Setup Bottom Nav
        setupBottomNav();

        // ✅ Animations
        setupFabPulse();
        setupLikePop();

        // ✅ état initial du coeur (pour le 1er verset)
        refreshLikeIcon();

        // ✅ Quand on change de page :
        // - refresh le coeur selon favori
        // - si autoplay doit parler après swipe, le faire
        pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                refreshLikeIcon();

                if (autoPlay && pendingSpeakAfterPage) {
                    pendingSpeakAfterPage = false;
                    speakCurrentVerse();
                }
            }
        });

        // ✅ Init TextToSpeech
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {

                Locale localeEn = Locale.US;
                int res = tts.setLanguage(localeEn);
                ttsReady = (res != TextToSpeech.LANG_MISSING_DATA && res != TextToSpeech.LANG_NOT_SUPPORTED);

                // ✅ Humaniser
                tts.setSpeechRate(0.95f);
                tts.setPitch(1.05f);

                // ✅ Choisir une voix anglaise si dispo (API 21+)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    try {
                        for (Voice v : tts.getVoices()) {
                            if (v == null || v.getLocale() == null) continue;
                            if ("en".equalsIgnoreCase(v.getLocale().getLanguage())) {
                                tts.setVoice(v);
                                break;
                            }
                        }
                    } catch (Exception ignored) {}
                }

                tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {
                        isSpeaking = true;
                        runOnUiThread(FeedActivity.this::startFabPulse);
                    }

                    @Override
                    public void onDone(String utteranceId) {
                        isSpeaking = false;
                        runOnUiThread(() -> {
                            stopFabPulse();

                            // ✅ Auto-scroll continu
                            if (autoPlay) {
                                goNextVerseAndContinue();
                            }
                        });
                    }

                    @Override
                    public void onError(String utteranceId) {
                        isSpeaking = false;
                        runOnUiThread(FeedActivity.this::stopFabPulse);
                    }
                });

            } else {
                ttsReady = false;
            }
        });

        // ✅ Click sur 🔊 : start/stop autoplay
        fabVoice.setOnClickListener(v -> {
            if (!ttsReady) {
                Toast.makeText(this, "Voix non disponible sur cet appareil", Toast.LENGTH_SHORT).show();
                return;
            }

            if (autoPlay) stopAutoPlay();
            else startAutoPlay();
        });

        // ✅ Click sur ❤️ : toggle favori + animation + update icône
        fabLike.setOnClickListener(v -> {
            Verse current = adapter.getItem(pager.getCurrentItem());
            if (current == null) return;

            favoritesStore.toggle(current.id);
            refreshLikeIcon();
            playLikePop();
        });
    }

    // -----------------------------
    // BOTTOM NAV
    // -----------------------------

    private void setupBottomNav() {
        if (bottomNav == null) return;

        // Sélectionne Home/Feed
        bottomNav.setSelectedItemId(R.id.nav_feed);

        bottomNav.setOnItemSelectedListener(item -> {
            if (ignoreNavSelection) return true;

            int id = item.getItemId();
            animateNavItem(bottomNav, id);

            if (id == R.id.nav_feed) {
                return true; // déjà ici
            } else if (id == R.id.nav_widget) {
                goTo(widgetcustom.class);
                return true;
            } else if (id == R.id.nav_profile) {
                // ⚠️ adapte le package si tu as mis ProfileActivity ailleurs
                goTo(com.example.verset.Tiktoklike.ProfileActivity.class);
                return true;
            }
            return false;
        });
    }

    private void goTo(Class<?> cls) {
        Intent intent = new Intent(this, cls);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void animateNavItem(BottomNavigationView nav, int itemId) {
        View v = nav.findViewById(itemId);
        if (v == null) return;

        v.animate().cancel();
        v.setScaleX(1f);
        v.setScaleY(1f);

        v.animate()
                .scaleX(1.12f)
                .scaleY(1.12f)
                .setDuration(120)
                .withEndAction(() -> v.animate().scaleX(1f).scaleY(1f).setDuration(120).start())
                .start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Re-sélectionner l'item Feed (évite état incohérent après retour)
        if (bottomNav != null) {
            ignoreNavSelection = true;
            bottomNav.setSelectedItemId(R.id.nav_feed);
            ignoreNavSelection = false;
        }
    }

    // -----------------------------
    // FAVORIS / LIKE FAB
    // -----------------------------

    private void refreshLikeIcon() {
        Verse current = adapter.getItem(pager.getCurrentItem());
        if (current == null) return;

        boolean isFav = favoritesStore.isFavorite(current.id);
        fabLike.setImageResource(isFav ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
    }

    private void setupLikePop() {
        PropertyValuesHolder sx = PropertyValuesHolder.ofFloat("scaleX", 1f, 1.18f, 1f);
        PropertyValuesHolder sy = PropertyValuesHolder.ofFloat("scaleY", 1f, 1.18f, 1f);
        likePopAnimator = ObjectAnimator.ofPropertyValuesHolder(fabLike, sx, sy);
        likePopAnimator.setDuration(220);
    }

    private void playLikePop() {
        if (likePopAnimator == null) return;
        likePopAnimator.cancel();
        likePopAnimator.start();
    }

    // -----------------------------
    // AUTO PLAY
    // -----------------------------

    private void startAutoPlay() {
        autoPlay = true;
        fabVoice.setImageResource(android.R.drawable.ic_media_pause);

        if (!isSpeaking) {
            speakCurrentVerse();
        }
    }

    private void stopAutoPlay() {
        autoPlay = false;
        pendingSpeakAfterPage = false;

        if (tts != null) tts.stop();

        isSpeaking = false;
        stopFabPulse();

        fabVoice.setImageResource(R.drawable.ic_sound);
    }

    private void speakCurrentVerse() {
        int pos = pager.getCurrentItem();
        Verse current = adapter.getItem(pos);
        if (current == null) return;

        String toRead = current.text;
        toRead = toRead.replace("…", ". ").replace(";", ", ");

        String utteranceId = UUID.randomUUID().toString();
        tts.speak(toRead, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
    }

    private void goNextVerseAndContinue() {
        int current = pager.getCurrentItem();
        int next = current + 1;

        if (next < adapter.getItemCount()) {
            pendingSpeakAfterPage = true;
            pager.setCurrentItem(next, true);
        } else {
            stopAutoPlay();
        }
    }

    // -----------------------------
    // FAB ANIMATION (PULSE) pour 🔊
    // -----------------------------

    private void setupFabPulse() {
        PropertyValuesHolder sx = PropertyValuesHolder.ofFloat("scaleX", 1f, 1.12f);
        PropertyValuesHolder sy = PropertyValuesHolder.ofFloat("scaleY", 1f, 1.12f);

        fabPulseAnimator = ObjectAnimator.ofPropertyValuesHolder(fabVoice, sx, sy);
        fabPulseAnimator.setDuration(420);
        fabPulseAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        fabPulseAnimator.setRepeatMode(ObjectAnimator.REVERSE);
    }

    private void startFabPulse() {
        if (fabPulseAnimator != null && !fabPulseAnimator.isRunning()) {
            fabPulseAnimator.start();
        }
    }

    private void stopFabPulse() {
        if (fabPulseAnimator != null) {
            fabPulseAnimator.cancel();
        }
        fabVoice.setScaleX(1f);
        fabVoice.setScaleY(1f);
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}