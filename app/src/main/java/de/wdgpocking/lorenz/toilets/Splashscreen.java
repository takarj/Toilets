package de.wdgpocking.lorenz.toilets;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.Slide;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class Splashscreen extends AppCompatActivity {

    private Intent i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);

        getWindow().setExitTransition(new Slide().setDuration(500));

        setContentView(R.layout.activity_splashscreen);

        final ImageView splashIcon = findViewById(R.id.splashIcon);
        final Animation anim = AnimationUtils.loadAnimation(getBaseContext(), R.anim.pop_up);

        i = new Intent(getApplicationContext(), MainActivity.class);

        splashIcon.startAnimation(anim);

        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                finish();
                startActivity(i);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }
}
