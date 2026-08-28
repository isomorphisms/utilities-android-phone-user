# User stories

## U1 — Find and send a work fault photo without exposing family photos

**Situation:** A worker has one phone containing both family photos and photographs taken to document work problems.

**Story:** When I need to show somebody a problem I found at work, I want to open a work-focused photo view and find the relevant fault photo quickly, so I can send it without scrolling through or exposing unrelated family photos.

**Examples:**

- a roofer sends a photo of roof damage or a leak to a supervisor;
- an auto mechanic sends a photo of a failed or damaged component to a boss or another technician;
- a factory mechanic sends a photo of a conveyor, sensor, motor, bearing, electrical cabinet, jam, leak, fault display, or broken part to a boss or engineer.

**A satisfactory result means:**

- family thumbnails do not dominate the work-focused view;
- the user can reach recent likely-work photos with substantially less scrolling than in the ordinary mixed camera roll;
- choosing a photo for sharing shows exactly what will be sent;
- no family photo is uploaded or shared merely because classification was performed;
- an uncertain photo may remain in the ordinary/unknown view rather than being guessed incorrectly.

## U2 — Keep work clutter out of the home/family view

**Story:** When I browse family or home photos, I want routine work evidence kept out of that view when the separator is confident, so machine faults, labels, roofs, receipts, parts, and equipment do not fill the family photo stream.

**A satisfactory result means:**

- separation is non-destructive;
- originals remain available;
- a photo may be corrected or reclassified later;
- uncertain photos are not silently discarded.

## U3 — Partial automation is useful

**Story:** When automatic classification is uncertain, I want the utility to classify only the easy photographs and leave the rest alone, so an imperfect model can still save substantial scrolling without making confident-looking mistakes everywhere.

**A satisfactory result means:**

- there is an explicit `unknown` result;
- thresholds can favor precision over coverage;
- success is measured partly by how many irrelevant thumbnails can safely be removed from the work view, not by requiring a label for every image;
- a first version that confidently separates only 20–30% of the library can still be useful.

## U4 — Work without a network when the local strategy is selected

**Story:** When I choose local classification, I want the photo contents to stay on the phone and classification to continue without Internet access, so a poor connection at a roof, garage, plant, or job site does not prevent me from finding the photo.

**A satisfactory result means:**

- inference itself performs no network request;
- model installation/download is visibly separate from inference;
- the application reports the model's storage cost before downloading it when practical;
- failure to reach a model-download host does not corrupt the user's photo library.

## U5 — Permit a remote classifier as a different tradeoff

**Story:** When I choose network classification, I want the application to state that photo data will leave the phone and what service it depends on, so I can decide whether the larger remote model or smaller phone footprint is worth the network, latency, availability, and privacy costs.

**A satisfactory result means:**

- remote classification is never disguised as local processing;
- the user knows what image data or derived data is transmitted;
- loss of network access degrades to `unknown` or a local fallback rather than damaging photos;
- remote and local classifiers present the same user-facing separation result where practical.

## U6 — Do not confuse the implementation with the human goal

**Story:** When the implementation changes, I want the work/home photo workflow to remain stable, so replacing MobileNet, changing thresholds, using a remote model, or inventing a better separator later does not require redesigning the user's basic task.

The human goal is to find and send work evidence without family-photo clutter. `folder`, `classifier`, `MobileNet`, `local`, and `remote` are progressively lower-level choices about how to achieve it.
