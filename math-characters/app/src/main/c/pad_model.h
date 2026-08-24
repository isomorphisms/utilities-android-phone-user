#ifndef PROGRAMMERS_UNICODE_PAD_MODEL_H
#define PROGRAMMERS_UNICODE_PAD_MODEL_H

#include <stdbool.h>
#include <stddef.h>

#define PAD_BUFFER_CAPACITY 4096u
#define PAD_STATUS_CAPACITY 160u
#define PAD_SLOT_COUNT 3u

typedef enum {
    PAD_INSERT,
    PAD_INSERT_PAIR,
    PAD_INSERT_TOKEN,
    PAD_MODE_MOVEMENT,
    PAD_MODE_TYPE,
    PAD_MOVE_LEFT,
    PAD_MOVE_RIGHT,
    PAD_PREVIOUS_LINE,
    PAD_NEXT_LINE,
    PAD_LINE_START,
    PAD_LINE_END,
    PAD_NEXT_WORD,
    PAD_MATCH_DELIMITER,
    PAD_PAGE_UP,
    PAD_PAGE_DOWN,
    PAD_SET_REPEAT,
    PAD_REPEAT_FORWARD,
    PAD_REPEAT_BACKWARD,
    PAD_BACKSPACE,
    PAD_DELETE_WORD,
    PAD_UNDO,
    PAD_CLEAR,
    PAD_REMEMBER_SLOT,
    PAD_VIEW_SLOT,
    PAD_WRITE_SLOT,
    PAD_CHANT_HISTORY,
    PAD_MESSAGE
} PadAction;

typedef struct {
    const char *label;
    PadAction action;
    const char *text;
    unsigned argument;
} PadKey;

typedef struct {
    const PadKey *keys;
    size_t key_count;
} PadRow;

typedef struct {
    const char *name;
    const PadRow *rows;
    size_t row_count;
} PadLayout;

typedef struct {
    char text[PAD_BUFFER_CAPACITY];
    size_t length;
    size_t cursor;

    char undo_text[PAD_BUFFER_CAPACITY];
    size_t undo_length;
    size_t undo_cursor;
    bool undo_available;

    char slots[PAD_SLOT_COUNT][PAD_BUFFER_CAPACITY];
    size_t slot_lengths[PAD_SLOT_COUNT];
    char copied_text[PAD_BUFFER_CAPACITY];
    size_t copied_length;

    unsigned repeat_count;
    const PadKey *last_key;
    char status[PAD_STATUS_CAPACITY];
} PadState;

void pad_state_init(PadState *state);
size_t pad_layout_count(void);
const PadLayout *pad_layout_at(size_t index);
bool pad_apply_key(PadState *state, const PadKey *key);
void pad_mark_copied(PadState *state);
bool pad_is_valid_utf8(const char *text);

#endif
