package com.isomorphisms.mathcharacters;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public final class MainActivity extends Activity {
    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private Button button(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setAllCaps(false);
        return button;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(20), dp(24), dp(20), dp(24));

        TextView title = new TextView(this);
        title.setText("Math Characters");
        title.setTextSize(28f);
        layout.addView(title);

        TextView explanation = new TextView(this);
        explanation.setText(
            "A deliberately restricted keyboard for arrows, number sets, i j k, digits, superscripts, and subscripts.\n\n" +
            "1. Enable Math Characters in Android keyboard settings.\n" +
            "2. Choose Math Characters when you want the symbol pad.\n" +
            "3. Use the IME key on the pad to switch back."
        );
        explanation.setTextSize(17f);
        explanation.setPadding(0, dp(16), 0, dp(16));
        layout.addView(explanation);

        Button settings = button("Enable keyboard");
        settings.setOnClickListener(ignored -> startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)));
        layout.addView(settings);

        Button choose = button("Choose keyboard");
        choose.setOnClickListener(ignored -> {
            InputMethodManager manager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (manager != null) {
                manager.showInputMethodPicker();
            }
        });
        layout.addView(choose);

        setContentView(layout);
    }
}
