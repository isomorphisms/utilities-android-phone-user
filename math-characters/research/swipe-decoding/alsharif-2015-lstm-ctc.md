# Alsharif et al. (2015): LSTM keyboard gesture decoding

## Paper

Ouais Alsharif, Tom Ouyang, Françoise Beaufays, Shumin Zhai, Thomas Breuel, and Johan Schalkwyk, **“Long Short Term Memory Neural Network for Keyboard Gesture Decoding,”** ICASSP 2015, pp. 2076–2080.

- DOI: `10.1109/ICASSP.2015.7178336`
- Google Research record: https://research.google/pubs/long-short-term-memory-neural-network-for-keyboard-gesture-recognition/
- The Google Research page currently says “Recognition”; the published PDF says “Decoding.”

## Why this paper matters here

This is a Google-authored paper about the same basic problem as Gboard-style glide typing: convert a continuous finger trajectory over a phone keyboard into a word.

It should be treated as evidence about an important ancestor of Google’s production keyboard decoder, not as a complete description of current Gboard. The paper compares its model against an advanced version of the gesture recognizer used in the Android KitKat keyboard, and predates the modern Gboard product name and later neural language-model work.

The main architectural idea is useful independent of the exact production lineage:

`touch trajectory -> sequence model -> character probabilities -> constrained lexical search -> word`

The paper deliberately borrows the structure of speech recognition. A swipe is a noisy, variable-length signal with co-articulation and uncertain alignment, much like acoustic frames in speech.

## Problem formulation

The decoder receives a variable-length sequence of feature vectors. It must map that sequence to one word from an allowed lexicon.

Important sources of ambiguity:

- The finger necessarily crosses keys that are not in the intended word.
- It may fail to pass directly through keys that are in the word.
- The path used to reach a letter depends on the neighboring letters, giving a co-articulation-like effect.
- Real gestures are offset and noisy.
- Users can mix ordinary taps and continuous movement.
- Gesture boundaries can be imperfect.

The authors argue that simple geometric shape matching handles clean canonical paths well but degrades badly on these kinds of noisy or oddly segmented inputs.

## Input representation

For each input frame the keyboard records:

- `x` position
- `y` position
- time since the previous gesture event
- gesture-event type: move, up, or down

Those are augmented with a **30-dimensional boolean key-membership vector**. One component is set when the current `(x,y)` point lies inside the corresponding key boundary.

The 30 buttons are:

- 26 English letters
- comma
- apostrophe
- period
- space

This gives **34 frame-wise input features** total.

The input frames are normalized with frame-wise global contrast normalization.

### Relevance to our keyboard

This is much cheaper than storing a library of complete swipe templates. The recognizer consumes a stream of small observations derived from the current keyboard geometry.

It also suggests a clean separation between:

1. physical/layout geometry,
2. gesture recognition,
3. lexical/language constraints.

That is useful if our alphabetic QWERTY layer keeps glide typing while math/programming symbol layers use ordinary direct key entry.

## Sequence model

The paper uses an **LSTM** trained with **Connectionist Temporal Classification (CTC)**.

The LSTM emits, for every input frame, a probability distribution over permitted output characters.

They experimented with:

- unidirectional LSTMs
- bidirectional LSTMs
- widths of roughly 50, 100, 200, and 400 hidden units
- shallow and deeper networks

The deeper two-layer models did not materially beat the shallow models in their experiments, so the reported results focus on the shallower versions.

Bidirectional LSTMs consistently performed better than unidirectional LSTMs.

## Why CTC is important

There is no natural frame-by-frame alignment between a swipe and its intended letters. For example, for `data` there is no obvious rule saying which exact sampled point should be labelled `d`, `a`, `t`, or the final `a`.

CTC removes the need to label that alignment manually.

Training sums over frame-wise paths that collapse to the target character sequence after removing:

- repeated labels
- blank labels

So the training target can simply be the intended word while the model learns which parts of the trajectory carry evidence for each character.

The authors compute the CTC objective with the standard forward-backward dynamic program.

A practical consequence is that a swipe corpus only needs something like:

`(trajectory, intended word)`

rather than a costly annotation of each trajectory sample with its intended key.

## Lexicon decoding with finite-state transducers

The neural network alone produces character probabilities. The paper then constrains those probabilities to legal words with a **trie-shaped lexicon finite-state transducer (FST)**.

A second FST handles the CTC blank symbol. The two are composed and searched with **beam search**. Arc costs come from the negative/log probabilities emitted by the LSTM.

Conceptually:

`CTC character evidence ∘ lexicon -> candidate words`

The experiments in this particular paper **do not use a language model**. The authors explicitly note that one can add one by composing another `G` FST containing language-model information.

That distinction matters: this paper demonstrates the spatial/gesture decoder plus lexical constraint, not the full context-aware prediction stack of a modern keyboard.

## Training data

The paper evaluates three datasets.

### Salt

- real user data
- 14,500 total word examples
- 120 unique words
- 40 opt-in participants
- 13,000 training examples
- 1,500 test examples

### ALK

- anonymized real gestures from opt-in Google employees
- 50,000 total word examples
- 5,450 unique words
- 45,000 training examples
- 5,000 test examples
- after preprocessing, about 70% contained a single down/up segment and about 30% contained multiple segments

The multi-segment examples are important because the older baseline recognizer handled them very poorly while the LSTM was much more robust.

### Synthetic Enron set

The authors also generate swipe trajectories synthetically from words in the Enron email corpus.

- about 138,000 total examples
- 8,256 unique words
- 124,000 training examples
- 14,000 test examples

Synthetic gestures are generated by connecting the word’s keys with a trajectory chosen to **minimize jerk**, motivated by models of human motor control, with variability in sequence length.

### Relevance to our keyboard

This is one of the most useful implementation observations in the paper. We can create a very large initial training corpus without manually recording every example:

`word -> key coordinates -> minimum-jerk path -> timing/geometric perturbations`

Then real user swipes can be used for evaluation and later fine-tuning.

A synthetic generator should vary at least:

- lateral path offset
- overshoot/undershoot near keys
- path curvature
- speed profile
- sampling rate
- start/end placement
- skipped or barely touched intermediate letters
- pauses
- accidental segmentation into multiple gesture pieces

## Lexicon size

For the ALK and Enron experiments the FST lexicon contains **100,000 words** drawn from an independent lexicon.

That is worth noting because the word-search structure, not a collection of 100,000 stored gesture templates, supplies the vocabulary constraint.

## Reported model sizes

The paper reports approximate model sizes for its tested LSTMs:

| Model width | Reported size |
| --- | ---: |
| 50 | 55 kB |
| 100 | 148 kB |
| 200 | 451 kB |
| 400 | 1.5 MB |

The best reported bidirectional 400-unit model is only about **1.5 MB**.

This is strong evidence against the idea that hundreds of megabytes are inherently required for swipe decoding. A modern keyboard may spend substantial space on language models, multilingual dictionaries, speech recognition, emoji/media assets, caches, or other features, but the spatial sequence model itself can be small.

## Results

Reported word-recognition accuracies include:

| Model | Salt | ALK | Enron |
| --- | ---: | ---: | ---: |
| BLSTM-34-400 with learning-rate decay | 92% | 89.2% | 93.5% |
| BLSTM-34-200 | 88% | 83.6% | 91.7% |
| BLSTM-34-100 | 86% | 82.8% | 91.1% |
| BLSTM-34-50 | 85% | 75.7% | 88.2% |
| baseline | 88% | 67% | 91.2% |

The paper emphasizes robustness more than the clean-data headline numbers. On clean ALK gestures the baseline could perform around 92%, but on a noisy subset involving multi-taps, mixed gestures, or incorrect finger-lift timing it fell to roughly zero, while the LSTM remained much more stable.

Learning-rate decay improved their strongest ALK result substantially.

## What seems reusable for our implementation

A small first decoder could preserve the paper’s separation of concerns without reproducing its exact 2015 network.

### 1. Geometry adapter

Turn raw Android pointer events into normalized per-frame features:

`x, y, dt, event-kind, key-proximity/key-membership`

We should normalize coordinates relative to the active keyboard layout rather than hard-code screen pixels. That keeps training data reusable across devices and keyboard sizes.

The paper uses hard key-membership booleans. For a new implementation it would be worth comparing those with smoother features such as distance to nearby key centers or normalized position within a key cell.

### 2. Spatial sequence model

Start with a deliberately small sequence recognizer. A modern small GRU/LSTM/TCN/transformer encoder can be compared empirically; the important architectural role is to transform a noisy path into character evidence.

For a faithful reproduction experiment, first build the paper’s LSTM+CTC baseline before replacing it.

### 3. Lexicon search

Keep the vocabulary outside the neural spatial model.

A trie/FST gives us:

- compact vocabulary representation
- prefix pruning during beam search
- deterministic control of allowable words
- straightforward injection/removal of personal vocabulary

### 4. Contextual language model

Add this after the spatial decoder works.

For ordinary typing the ranking problem should eventually look roughly like:

`score(word) = spatial evidence + lexical cost + context/language cost + personalization`

The 2015 paper does not experimentally evaluate this final context term.

### 5. Symbol layers stay separate

The math/programming layers do not need to participate in swipe decoding initially.

The ordinary alphabet layer can support glide typing while dedicated buttons switch to symbol tables such as arrows, number sets, superscripts/subscripts, programming operators, movement keys, and similar hardware-keyboard-derived groups.

This avoids forcing a language recognizer to learn an enormous Unicode output alphabet just to make the symbol keys convenient.

## First reproduction experiment

A useful minimal experiment would be:

1. Fix one QWERTY geometry.
2. Choose a 10k–100k English lexicon.
3. Generate synthetic minimum-jerk trajectories for words.
4. Perturb those trajectories with controlled noise.
5. Encode each sample with the paper’s 34-feature scheme or a geometry-normalized equivalent.
6. Train a small LSTM+CTC model.
7. Decode through a trie/FST with beam search.
8. Measure top-1 and top-N word accuracy as noise increases.
9. Compare model sizes from roughly 50 kB through a few MB.
10. Only then add an n-gram or small neural context model.

The most useful test is not clean-path accuracy. It is the degradation curve as trajectories become sloppy, offset, variably sampled, or incorrectly segmented.

## Questions to carry forward

- Can a 100–500 kB spatial model give acceptable English swipe accuracy on current phone hardware?
- How much does bidirectionality buy us once latency and streaming behavior are considered?
- Is an FST implementation worth its complexity for our target size, or is a compact trie plus beam search sufficient?
- How small can the English context model be while still resolving geometrically ambiguous words well?
- How much user-specific adaptation can be represented as tiny key-position offsets, vocabulary weights, or n-gram deltas rather than retraining the spatial network?
- How should gesture coordinates be normalized so one trained model works across keyboard heights, screen sizes, and one-handed layouts?
- Can synthetic minimum-jerk data get us most of the way before collecting real swipes?
- What portion of a modern keyboard’s installed size is actually spatial decoding versus dictionaries, contextual language models, speech models, emoji/media assets, and caches?

## Related papers to read next

- Ouyang, Rybach, Beaufays, Riley, **“Mobile Keyboard Input Decoding with Finite-State Transducers”** (2017): decoder/FST architecture around mobile keyboard input.
- Google Research, **“The Machine Intelligence Behind Gboard”** (2017): production-oriented description connecting LSTM/CTC spatial modelling, FST search, and language modelling.
- Zhang et al., **“Neural Search Space in Gboard Decoder”** (2024): later work on dynamically constructing decoder search space from a neural language model.
- Kristensson and Zhai, **SHARK²** (2004): geometric word-gesture recognition lineage.
- Quinn and Zhai, **“Modeling Gesture-Typing Movements”** (2018): human gesture-generation model, relevant to synthetic training trajectories.
