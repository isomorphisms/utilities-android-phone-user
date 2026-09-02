# Model smoke benchmark

This directory contains a deliberately small synthetic fixture set for exercising pinned text classifiers in CI.

It is not a real-world accuracy corpus and must not be used to claim production precision or recall. Its purpose is to verify that a pinned model can load on a GitHub Actions CPU runner, classify political and fundraising properties independently, and emit inspectable predictions and summary metrics.

Real SMS evaluation should replace or supplement `fixtures.jsonl` with labeled, privacy-reviewed messages covering personal, work, transactional, local political, out-of-state political, and fundraising traffic.
