# SMS test boundary for a public repository

No personal phone number belongs in this repository, in test fixtures, in expected-output files, in screenshots, in CI variables printed to logs, or in captured Binder/Parcel traces.

## Default acceptance path

A programmatic SMS experiment should stop before radio transmission by default. Useful acceptance points include:

- construct the semantic send request;
- encode the exact public-SDK arguments or Binder Parcel;
- validate the destination syntactically without preserving it;
- send the prepared text to a local notification, clipboard/paste workflow, or fake transport;
- compare generated Binder bytes against a redacted or synthetic fixture;
- exercise a fake `ISms`-shaped endpoint rather than the real telephony service.

This gives us useful compiler/backend coverage without making accidental message transmission part of ordinary development.

## If a real send is deliberately tested

The destination must be supplied at runtime from outside Git: interactive input, an ignored local file, or another non-committed secret source. It must never have a repository default. The program should display the destination and message immediately before the irreversible operation and require an explicit final action that cannot be reached by an ordinary automated test.

CI must never send a real SMS. Test code must not infer or retrieve the user's own number and then silently use it as a destination.

## Why not discover the phone's own number automatically?

Apart from privacy, Android cannot reliably promise that the line's dialable telephone number is available to an ordinary application. SIM/subscription metadata, carrier provisioning, permissions, and modern Android restrictions make it an unsuitable test oracle. A local runtime value is both more explicit and less likely to leak.

## Receive/read work

Read-only SMS-store exploration is a separate capability. Tests should use synthetic rows or locally redacted snapshots wherever possible. Real message bodies, correspondent numbers, and identifying metadata should not be committed merely because the operation is read-only.
