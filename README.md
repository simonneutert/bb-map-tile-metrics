# Babashka Map Tile Metrics<!-- omit from toc -->

A small Babashka library and CLI for analyzing sets of
[slippy-map tiles](https://wiki.openstreetmap.org/wiki/Slippy_map_tilenames) by
their `x` and `y` coordinates.

[![Tests](https://github.com/simonneutert/bb-map-tile-metrics/actions/workflows/tests.yaml/badge.svg)](https://github.com/simonneutert/bb-map-tile-metrics/actions/workflows/tests.yaml)

Tiles may also contain a zoom level (`z`). The metrics are two-dimensional, so
`z` and any other extra tile metadata are ignored internally. Inputs should
therefore describe tiles from the same zoom level.

> Il faut toujours se réserver le droit de rire le lendemain de ses idées de la
> veille.
>
> — Napoleon Bonaparte

## Contents

- [Definitions](#definitions)
- [Metrics](#metrics)
- [Example](#example)
- [Requirements](#requirements)
- [Usage](#usage)
  - [Input schema](#input-schema)
  - [CLI options](#cli-options)
  - [Files](#files)
  - [Inline JSON](#inline-json)
  - [Inline EDN](#inline-edn)
  - [Docker](#docker)
  - [Calling from Ruby](#calling-from-ruby)
- [Implementation notes](#implementation-notes)
- [Development](#development)
- [Other](#other)
- [Plans](#plans)

## Definitions

- **Tile / point**: an integer grid coordinate with `x` and `y` values.
- **Visited tile**: a tile present in the input set.
- **Cluster tile**: a visited tile whose four cardinal neighbors are also
  visited:
  - north: `(x, y - 1)`
  - east: `(x + 1, y)`
  - south: `(x, y + 1)`
  - west: `(x - 1, y)`
- **Cluster**: a cardinally connected component of cluster tiles.
- **Square**: a completely filled axis-aligned square of visited tiles. Squares
  smaller than `4 x 4` are ignored.

Coordinates use the usual slippy-map orientation: `x` increases to the right and
`y` increases downward.

## Metrics

The CLI returns three metrics as JSON.

### `clusters`

All connected components of cluster tiles:

```json
{
  "clusters": [
    [
      { "x": 2, "y": 2 },
      { "x": 3, "y": 2 }
    ],
    [
      { "x": 12, "y": 22 },
      { "x": 13, "y": 22 }
    ]
  ]
}
```

### `max_clusters`

All clusters tied for the largest number of cluster tiles:

```json
{
  "max_clusters": [
    [
      { "x": 2, "y": 2 },
      { "x": 3, "y": 2 }
    ]
  ]
}
```

### `squares`

All maximum filled squares. `x` and `y` identify the square's top-left tile and
`size` is its edge length in tiles.

```json
{
  "squares": [
    { "x": 2, "y": 2, "size": 4 }
  ]
}
```

If several maximum squares have the same size, all of them are returned. If no
filled square of at least `4 x 4` exists, `squares` is empty.

## Example

The following simplified grid uses:

- `V` — visited tile
- `c` — cluster tile
- `C` — cluster tile belonging to a maximum cluster
- `Cs` — cluster tile inside a maximum square
- `Vs` — other visited tile belonging to a maximum square

| 0x0 ↓ | -  | -  | -  | -  | - | - | -  | -  | -  | -  | - | - | - |
| ----- | -- | -- | -- | -- | - | - | -- | -- | -- | -- | - | - | - |
| V     | V  | V  |    |    |   |   |    |    |    |    |   |   |   |
| V     | Cs | Cs | Vs | Vs | V | V |    |    |    |    |   |   |   |
| V     | Cs | C  | C  | Vs |   | V | V  | V  | V  | V  | V | V | V |
| V     | Cs | C  | C  | Vs |   |   |    |    | V  | c  | c | c | V |
|       | Vs | Vs | Vs | Vs |   |   |    |    |    | V  | V | V |   |
|       |    |    |    |    |   |   | V  |    |    |    |   |   |   |
|       |    |    |    |    |   |   | V  |    |    |    |   |   |   |
|       |    |    |    |    |   |   | V  |    |    |    |   |   |   |
|       |    |    |    |    |   |   | Vs | Vs | Vs | Vs |   |   |   |
|       |    |    |    |    |   |   | Vs | c  | c  | Vs |   |   |   |
|       |    |    |    |    |   |   | Vs | c  | c  | Vs |   |   |   |
|       |    |    |    |    |   |   | Vs | Vs | Vs | Vs |   |   |   |

In this example:

- the maximum square size is `4`, with two equally large squares;
- the maximum cluster size is `7`;
- there are three clusters with sizes `7`, `4`, and `3`.

## Requirements

- [Babashka](https://babashka.org), version `1.1.171` or newer

## Usage

Clone the repository and run the CLI with Babashka:

```bash
bb -o --main map-tile-metrics.main --file test/map_tile_metrics/resources/test-data.json
```

### Input schema

JSON input is an array of tile objects:

```json
[
  { "x": 4266, "y": 2777 },
  { "x": 4267, "y": 2777 }
]
```

An optional common zoom level is accepted:

```json
[
  { "x": 4266, "y": 2777, "z": 13 },
  { "x": 4267, "y": 2777, "z": 13 }
]
```

`z` and other extra keys are discarded when input is normalized. Metrics operate
only on the canonical `x`/`y` coordinates. Duplicate coordinates collapse to a
single visited tile.

EDN accepts either keyword keys or string keys:

```clojure
[{:x 1 :y 1}
 {:x 2 :y 1}]
```

```clojure
[{"x" 1 "y" 1}
 {"x" 2 "y" 1}]
```

### CLI options

- `--json`, `-j` — JSON content as a string
- `--edn`, `-e` — EDN content as a string
- `--file`, `-f` — path to a `.json` or `.edn` file
- `--help`, `-h` — show CLI help

### Files

```bash
# JSON
bb -o --main map-tile-metrics.main \
  --file test/map_tile_metrics/resources/test-data.json

# EDN with keyword keys
bb -o --main map-tile-metrics.main \
  --file test/map_tile_metrics/resources/test-data.edn

# EDN with string keys
bb -o --main map-tile-metrics.main \
  --file test/map_tile_metrics/resources/test-data-str.edn
```

### Inline JSON

```bash
bb -o --main map-tile-metrics.main \
  --json "$(cat test/map_tile_metrics/resources/test-data.json)"
```

### Inline EDN

```bash
bb -o --main map-tile-metrics.main \
  --edn '#{{:x 1 :y 1} {:x 2 :y 1} {:x 3 :y 1} {:x 1 :y 2} {:x 2 :y 2} {:x 3 :y 2} {:x 1 :y 3} {:x 2 :y 3} {:x 3 :y 3}}'
```

### Docker

Build the image:

```bash
docker build -t bb-map-tile-metrics .
```

The image already defines the application entrypoint, so pass the CLI arguments
directly:

```bash
docker run --rm bb-map-tile-metrics \
  --edn '#{{:x 1 :y 1} {:x 2 :y 1} {:x 3 :y 1} {:x 1 :y 2} {:x 2 :y 2} {:x 3 :y 2} {:x 1 :y 3} {:x 2 :y 3} {:x 3 :y 3}}'
```

### Calling from Ruby

```ruby
data = [
  {x: 1, y: 1}, {x: 2, y: 1}, {x: 3, y: 1},
  {x: 1, y: 2}, {x: 2, y: 2}, {x: 3, y: 2},
  {x: 1, y: 3}, {x: 2, y: 3}, {x: 3, y: 3}
]

metrics = JSON.parse(
  `bb -o --main map-tile-metrics.main --json '#{data.to_json}'`
)
```

## Implementation notes

The implementation is optimized around set membership checks on canonical
`{:x ... :y ...}` coordinates.

### Clusters

Cluster candidates are identified with four cardinal-neighbor lookups per tile.
Connected components are then traversed using a single `unseen` set: a tile is
removed from `unseen` when discovered, so it enters the traversal frontier at
most once.

### Maximum squares

Maximum-square detection uses the already-computed clusters as a pruning step
rather than scanning every visited tile with dynamic programming.

For every completely filled square of size `s >= 4`, the `(s - 2) x (s - 2)`
interior consists entirely of cluster tiles and belongs to one connected
cluster. The algorithm therefore:

1. processes only clusters large enough to contain a candidate interior;
2. runs maximal-square dynamic programming inside each eligible cluster;
3. validates the corresponding visited square by checking the four outer
   corners;
4. processes clusters largest-first and stops when the remaining clusters cannot
   tie the current maximum.

This keeps the dense-input behavior bounded while retaining the pruning that
performs well on sparse and irregular map data.

## Development

Run the complete test suite with:

```bash
./test-runner.clj
```

or explicitly through Babashka:

```bash
bb test-runner.clj
```

The test runner covers utilities/input normalization, cluster detection,
maximum-square detection, and CLI parsing/output behavior.

## Other

Compact JSON with `jq`:

```bash
jq -c . test-data.json > output.json
```

## Plans

- [ ] Publish as a Clojars library
