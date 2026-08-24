package com.isomorphisms.mathcharacters;

import android.inputmethodservice.InputMethodService;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

public final class MathCharactersIme extends InputMethodService {
    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private Button key(String label, View.OnClickListener listener) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextSize(21f);
        button.setMinWidth(dp(54));
        button.setMinHeight(dp(50));
        button.setAllCaps(false);
        button.setOnClickListener(listener);
        return button;
    }

    private void commit(String text) {
        InputConnection input = getCurrentInputConnection();
        if (input != null) {
            input.commitText(text, 1);
        }
    }

    private void backspace() {
        InputConnection input = getCurrentInputConnection();
        if (input != null) {
            input.deleteSurroundingText(1, 0);
        }
    }

    private void enter() {
        InputConnection input = getCurrentInputConnection();
        if (input == null) {
            return;
        }
        input.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
        input.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER));
    }

    private void showImePicker() {
        InputMethodManager manager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (manager != null) {
            manager.showInputMethodPicker();
        }
    }

    @Override
    public View onCreateInputView() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);

        LinearLayout keyboard = new LinearLayout(this);
        keyboard.setOrientation(LinearLayout.VERTICAL);
        keyboard.setPadding(dp(4), dp(4), dp(4), dp(4));
        scroll.addView(keyboard);

        for (String[] characters : MathCharacters.ROWS) {
            HorizontalScrollView rowScroll = new HorizontalScrollView(this);
            rowScroll.setHorizontalScrollBarEnabled(false);

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            for (String character : characters) {
                row.addView(key(character, ignored -> commit(character)));
            }
            rowScroll.addView(row);
            keyboard.addView(rowScroll);
        }

        LinearLayout editing = new LinearLayout(this);
        editing.setOrientation(LinearLayout.HORIZONTAL);

        Button space = key("space", ignored -> commit(" "));
        space.setMinWidth(dp(150));
        editing.addView(space);
        editing.addView(key("⌫", ignored -> backspace()));
        editing.addView(key("↵", ignored -> enter()));
        editing.addView(key("IME", ignored -> showImePicker()));
        keyboard.addView(editing);

        return scroll;
    }
}
