#ifndef ANDROID_CLIPBOARD_UTF8_H
#define ANDROID_CLIPBOARD_UTF8_H

#include <stddef.h>
#include <stdint.h>

typedef enum {
    CLIPBOARD_UTF_OK = 0,
    CLIPBOARD_UTF_INVALID = 1,
    CLIPBOARD_UTF_OUT_OF_MEMORY = 2
} ClipboardUtfStatus;

ClipboardUtfStatus clipboard_utf8_to_utf16(
    const char *bytes,
    size_t byte_count,
    uint16_t **out_units,
    size_t *out_unit_count);

ClipboardUtfStatus clipboard_utf16_to_utf8(
    const uint16_t *units,
    size_t unit_count,
    char **out_bytes,
    size_t *out_byte_count);

#endif
