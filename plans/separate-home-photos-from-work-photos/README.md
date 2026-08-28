# Separate home photos from work photos

## Human problem

People who use one phone for both work and home can have thousands of unrelated photos in one camera roll. At work they often need to find a recent photo of a fault and send it to somebody else quickly, but family photos are mixed into the same thumbnail stream.

Concrete cases:

- a roofer photographs damage, flashing, shingles, a leak, or a roof condition and needs to send the evidence to a supervisor;
- an auto mechanic photographs a failed part, leak, wiring problem, scan-tool screen, or damaged vehicle component and needs to send it to a boss or another technician;
- a factory mechanic photographs a conveyor, motor, bearing, sensor, electrical cabinet, jam, broken part, or other machine condition and needs to send it to a boss, engineer, or another mechanic.

The goal is not perfect photo understanding. If a conservative separator confidently removes even 20–30% of irrelevant thumbnails from the view, it has already reduced thumb-scrolling. If it can confidently separate much more, better. Uncertain photos may remain unclassified.

## Chosen approach

The current approach is:

1. preserve the original photo library;
2. derive a work/home separation from the photos;
3. expose that separation as folders, albums, or views;
4. let the user open the work context and share a fault photo without family thumbnails surrounding it.

"Folder" is the user-facing idea. The first implementation does not need to physically move or duplicate original image files. A MediaStore album, side index, or other derived view can satisfy the need while remaining reversible.

## First separator

Start with deliberately coarse visual evidence rather than a universal semantic classifier.

Useful evidence includes:

- machines, tools, vehicles under repair, industrial equipment, conveyors, motors, bearings, wiring, pipes, roofs and building materials;
- rust, exposed metal, mechanical assemblies, control cabinets, gauges, labels, fault displays and similar work evidence;
- people, faces, children, pets, food, gatherings and other strongly organic/personal scenes.

The useful distinction is not literally `metal = work` and `human = home`. A roofer or mechanic may appear in a work photo, and a lawn mower may appear in a home photo. These cues are only evidence. High-confidence decisions are useful; low-confidence photographs should fall through to `unknown` rather than being forced into the wrong folder.

A minimal result type is therefore roughly:

```text
work_candidate
home_candidate
unknown
```

`unknown` is a feature, not a failure. This utility is allowed to solve only the easy part of the camera roll.

## Implementation choices

Two implementation strategies are being considered first:

1. run a small model locally on the phone;
2. send an image or derived representation across the network to a remote classifier.

These are alternatives for *how* the separator is computed. They are not the only possible algorithms. See `strategies.md` and `computer-science/algorithm-choice-as-adverb.md`.

## Existing related story

This branch descends from `search-text-messages`, where G9 already stated the same central human need: distinct work and family contexts so a technician can show a fault or machine condition without exposing unrelated family photos. This plan makes that concern its own phone utility rather than treating it only as a messaging feature.
