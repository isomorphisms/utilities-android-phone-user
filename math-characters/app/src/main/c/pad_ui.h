#ifndef PROGRAMMERS_UNICODE_PAD_UI_H
#define PROGRAMMERS_UNICODE_PAD_UI_H

#include "pad_model.h"

#include <stddef.h>

typedef struct {
    float left;
    float top;
    float right;
    float bottom;
} PadRect;

typedef enum {
    PAD_HIT_NONE,
    PAD_HIT_PREVIOUS_LAYOUT,
    PAD_HIT_NEXT_LAYOUT,
    PAD_HIT_CURSOR_LEFT,
    PAD_HIT_CURSOR_RIGHT,
    PAD_HIT_BACKSPACE,
    PAD_HIT_UNDO,
    PAD_HIT_CLEAR,
    PAD_HIT_COPY,
    PAD_HIT_KEY
} PadHitKind;

typedef struct {
    PadHitKind kind;
    const PadKey *key;
} PadHit;

PadRect pad_ui_previous_layout_rect(float width, float height);
PadRect pad_ui_next_layout_rect(float width, float height);
PadRect pad_ui_title_rect(float width, float height);
PadRect pad_ui_buffer_rect(float width, float height);
PadRect pad_ui_toolbar_rect(float width, float height, size_t index);
PadRect pad_ui_status_rect(float width, float height);
PadRect pad_ui_key_rect(float width, float height, const PadLayout *layout,
                        size_t row, size_t column);
PadHit pad_ui_hit_test(float width, float height, const PadLayout *layout,
                       float x, float y);

#endif
