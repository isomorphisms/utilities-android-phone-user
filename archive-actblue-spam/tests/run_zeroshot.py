#!/usr/bin/env python3
import json
import os
import sys
import time
from pathlib import Path

from transformers import pipeline

ROOT = Path(__file__).resolve().parent
FIXTURES = ROOT / "fixtures.jsonl"
RESULTS = ROOT / "results"


def load_cases():
    with FIXTURES.open(encoding="utf-8") as f:
        return [json.loads(line) for line in f if line.strip()]


def classify_binary(classifier, text, positive, negative):
    out = classifier(
        text,
        candidate_labels=[positive, negative],
        hypothesis_template="This message is {}.",
        multi_label=False,
    )
    scores = dict(zip(out["labels"], out["scores"]))
    return {
        "prediction": out["labels"][0] == positive,
        "positive_score": scores.get(positive),
        "negative_score": scores.get(negative),
    }


def accuracy(rows, field):
    return sum(row[field]["prediction"] == row["expected"][field] for row in rows) / len(rows)


def main():
    model = os.environ.get("MODEL")
    revision = os.environ.get("REVISION")
    model_id = os.environ.get("MODEL_ID", "model")
    if not model or not revision:
        print("MODEL and REVISION are required", file=sys.stderr)
        return 2

    cases = load_cases()
    started = time.time()
    classifier = pipeline(
        "zero-shot-classification",
        model=model,
        revision=revision,
        device=-1,
    )

    rows = []
    for case in cases:
        political = classify_binary(classifier, case["text"], "political", "not political")
        fundraising = classify_binary(classifier, case["text"], "fundraising", "not fundraising")
        rows.append(
            {
                "id": case["id"],
                "text": case["text"],
                "expected": {
                    "political": case["political"],
                    "fundraising": case["fundraising"],
                },
                "political": political,
                "fundraising": fundraising,
            }
        )

    summary = {
        "model": model,
        "revision": revision,
        "cases": len(rows),
        "political_accuracy": accuracy(rows, "political"),
        "fundraising_accuracy": accuracy(rows, "fundraising"),
        "joint_accuracy": sum(
            row["political"]["prediction"] == row["expected"]["political"]
            and row["fundraising"]["prediction"] == row["expected"]["fundraising"]
            for row in rows
        ) / len(rows),
        "elapsed_seconds": round(time.time() - started, 3),
    }

    payload = {"summary": summary, "predictions": rows}
    RESULTS.mkdir(exist_ok=True)
    output = RESULTS / f"{model_id}.json"
    output.write_text(json.dumps(payload, indent=2) + "\n", encoding="utf-8")

    print(json.dumps(summary, indent=2))
    for row in rows:
        print(
            f"{row['id']}: "
            f"political={row['political']['prediction']} expected={row['expected']['political']} "
            f"fundraising={row['fundraising']['prediction']} expected={row['expected']['fundraising']}"
        )
    print(f"wrote {output}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
