#include "utf8.h"

#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

static int failures = 0;

static void fail(const char *label) {
    fprintf(stderr, "FAIL: %s\n", label);
    failures += 1;
}

static void expect_roundtrip(
    const char *label,
    const unsigned char *expected,
    size_t expected_len) {
    uint16_t *units = NULL;
    size_t unit_count = 0;
    ClipboardUtfStatus status = clipboard_utf8_to_utf16(
        (const char *)expected, expected_len, &units, &unit_count);
    if (status != CLIPBOARD_UTF_OK) {
        fail(label);
        return;
    }

    char *actual = NULL;
    size_t actual_len = 0;
    status = clipboard_utf16_to_utf8(units, unit_count, &actual, &actual_len);
    free(units);
    if (status != CLIPBOARD_UTF_OK || actual_len != expected_len ||
        memcmp(actual, expected, expected_len) != 0 || actual[actual_len] != '\0') {
        fail(label);
    }
    free(actual);
}

static void expect_invalid(
    const char *label,
    const unsigned char *bytes,
    size_t len) {
    uint16_t *units = NULL;
    size_t unit_count = 0;
    const ClipboardUtfStatus status = clipboard_utf8_to_utf16(
        (const char *)bytes, len, &units, &unit_count);
    if (status != CLIPBOARD_UTF_INVALID || units != NULL || unit_count != 0) {
        fail(label);
    }
    free(units);
}

int main(void) {
    static const unsigned char boundaries[] = {
        0x00,
        0x7f,
        0xc2, 0x80,
        0xdf, 0xbf,
        0xe0, 0xa0, 0x80,
        0xed, 0x9f, 0xbf,
        0xee, 0x80, 0x80,
        0xef, 0xbf, 0xbf,
        0xf0, 0x90, 0x80, 0x80,
        0xf4, 0x8f, 0xbf, 0xbf,
    };
    expect_roundtrip("Unicode boundary roundtrip", boundaries, sizeof(boundaries));

    static const unsigned char embedded_nul[] = {'A', 0x00, 'B'};
    expect_roundtrip("embedded NUL roundtrip", embedded_nul, sizeof(embedded_nul));

    expect_roundtrip("empty roundtrip", (const unsigned char *)"", 0);

    static const unsigned char overlong[] = {0xc0, 0x80};
    static const unsigned char stray_continuation[] = {0x80};
    static const unsigned char truncated_two[] = {0xc2};
    static const unsigned char truncated_three[] = {0xe2, 0x82};
    static const unsigned char surrogate[] = {0xed, 0xa0, 0x80};
    static const unsigned char too_large[] = {0xf4, 0x90, 0x80, 0x80};
    static const unsigned char bad_continuation[] = {0xe2, 0x28, 0xa1};
    expect_invalid("reject overlong", overlong, sizeof(overlong));
    expect_invalid("reject stray continuation", stray_continuation,
                   sizeof(stray_continuation));
    expect_invalid("reject truncated two-byte", truncated_two,
                   sizeof(truncated_two));
    expect_invalid("reject truncated three-byte", truncated_three,
                   sizeof(truncated_three));
    expect_invalid("reject UTF-8 surrogate", surrogate, sizeof(surrogate));
    expect_invalid("reject above U+10FFFF", too_large, sizeof(too_large));
    expect_invalid("reject bad continuation", bad_continuation,
                   sizeof(bad_continuation));

    static const uint16_t unpaired_high[] = {0xd800};
    static const unsigned char replacement[] = {0xef, 0xbf, 0xbd};
    char *replacement_bytes = NULL;
    size_t replacement_len = 0;
    ClipboardUtfStatus status = clipboard_utf16_to_utf8(
        unpaired_high, 1, &replacement_bytes, &replacement_len);
    if (status != CLIPBOARD_UTF_OK || replacement_len != sizeof(replacement) ||
        memcmp(replacement_bytes, replacement, sizeof(replacement)) != 0) {
        fail("unpaired UTF-16 becomes U+FFFD");
    }
    free(replacement_bytes);

    uint16_t *empty_units = NULL;
    size_t empty_unit_count = 999;
    status = clipboard_utf8_to_utf16(NULL, 0, &empty_units, &empty_unit_count);
    if (status != CLIPBOARD_UTF_OK || empty_units == NULL || empty_unit_count != 0) {
        fail("NULL plus zero length is empty UTF-8");
    }
    free(empty_units);

    uint16_t *bad_units = NULL;
    size_t bad_unit_count = 0;
    status = clipboard_utf8_to_utf16(NULL, 1, &bad_units, &bad_unit_count);
    if (status != CLIPBOARD_UTF_INVALID) {
        fail("NULL plus nonzero length is invalid");
    }

    if (failures != 0) {
        return 1;
    }
    puts("PASS: standard UTF-8/UTF-16 clipboard conversion");
    return 0;
}
