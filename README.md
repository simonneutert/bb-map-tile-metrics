# Babashka Map Tile Metrics<!-- omit from toc -->

A library and CLI tool for analyzing map tile sets using X and Y coordinates. \
Each tile can also be defined by a zoom level (z, an integer), which represents the map's scale. \
When using this tool, the zoom level (z) must be the same for the given input and is therefore not required.

[Slippy map tilenames on OSM](https://wiki.openstreetmap.org/wiki/Slippy_map_tilenames)

[![Tests](https://github.com/simonneutert/bb-map-tile-metrics/actions/workflows/tests.yaml/badge.svg)](https://github.com/simonneutert/bb-map-tile-metrics/actions/workflows/tests.yaml)

---

> Il faut toujours se réserver le droit de rire le lendemain de ses idées de la veille.
>
> \- Napoleon Bonaparte

---

- [Definitions](#definitions)
- [Metrics Calculated by the Tool (Scores)](#metrics-calculated-by-the-tool-scores)
- [A Visualisation is Worth a Thousand Words](#a-visualisation-is-worth-a-thousand-words)
- [Requirements](#requirements)
- [Run the tool in the terminal](#run-the-tool-in-the-terminal)
  - [Options](#options)
  - [Input file structure / Data Schema](#input-file-structure--data-schema)
  - [Example command](#example-command)
  - [Docker](#docker)
  - [Call from Ruby](#call-from-ruby)
- [Other ...](#other-)
- [Plans, Wishes, Hopes and Dreams](#plans-wishes-hopes-and-dreams)

---

## Definitions

- **Tile**: A tile has coordinates x and y (integer values) representing its position in a grid. Optionally, a tile can have a zoom level `z` (integer value) to represent the scale of the map (tile).
- **Point**: A point has coordinates x and y as integer values. May be used as a substitue for `tile` in this lib.
- **Cluster Tile**: A tile that has neighboring tiles to all its four sides.
  - e.g. for a tile (x, y) the neighbors' coordinates are (x-1, y), (x+1, y), (x, y-1), (x, y+1). A tile is a cluster-tile if all the neighboring tiles (top, right, bottom, left) are present.

## Metrics Calculated by the Tool (Scores)

This tool calculates the following metrics:

- **Clusters**: Lists tiles that are in close proximity to cluster coordinates. Each cluster represents a group of neighboring tiles.

```text
// including the maximum cluster
{
  ..., // other metrics
  "clusters": [
    [
      {
        "x": 2,
        "y": 2,
      },
      {
        "x": 3,
        "y": 2,
      }
    ],
    [
      {
        "x": 12,
        "y": 22,
      } ,
      {
        "x": 13,
        "y": 22,
      },
    ],
    ...
  ]
}
```

- **Maximum Clusters**: Identifies the largest clusters based on either the number of tiles it contains or the area it covers (or a combination of both).

```text
// all clusters with of the largest size
{
  ..., // other metrics
  "max_clusters": [
    [
      {
        "x": 2,
        "y": 2,
      },
      {
        "x": 3,
        "y": 2,
      },
      ...
    ],
    [
      {
        "x": 22,
        "y": 22,
      },
      {
        "x": 23,
        "y": 22,
      },
      ...
    ]
  ]
}
```

- **Maximum Squares**: The biggest detected square formation of visited tiles. To be considered, a square must have a minimum edge length of 4 tiles.

```text
// upper left corner of the square
{
  ..., // other metrics
  "squares": [
    {
      "x": 2,
      "y": 2,
      "edge_length": 3
    }
  ]
}
```

These metrics provide insights into the spatial distribution and patterns within the tile data.

## A Visualisation is Worth a Thousand Words

The following table shows a grid of tiles. 

Coordinates are represented as (x, y) where x is the column and y is the row. The origin (0, 0) is at the top left corner.

- `V` is a visited tile.
- `c` is a cluster tile.
- `C` is a cluster tile that is part of the max cluster.
- `Cs` is a cluster tile that is part of the max square.
- `Vs` is a visited tile that is part of the max square.

| 0x0<br>👇 | -   | -   | -   | -   | -   | -   | -   | -   | -   | -   | -   | -   | -   |
| -------- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| V        | V   | V   |     |     |     |     |     |     |     |     |     |     |     |
| V        | Cs  | Cs  | Vs  | Vs  | V   | V   |     |     |     |     |     |     |     |
| V        | Cs  | C   | C   | Vs  |     | V   | V   | V   | V   | V   | V   | V   | V   |
| V        | Cs  | C   | C   | Vs  |     |     |     |     | V   | c   | c   | c   | V   |
|          | Vs  | Vs  | Vs  | Vs  |     |     |     |     |     | V   | V   | V   |     |
|          |     |     |     |     |     |     | V   |     |     |     |     |     |     |
|          |     |     |     |     |     |     | V   |     |     |     |     |     |     |
|          |     |     |     |     |     |     | V   |     |     |     |     |     |     |
|          |     |     |     |     |     |     | Vs  | Vs  | Vs  | Vs  |     |     |     |
|          |     |     |     |     |     |     | Vs  | c   | c   | Vs  |     |     |     |
|          |     |     |     |     |     |     | Vs  | c   | c   | Vs  |     |     |     |
|          |     |     |     |     |     |     | Vs  | Vs  | Vs  | Vs  |     |     |     |

- Max Squares have a score of 4. 
  - One of two max squares in the example located with its top left corner at x=1, y=1.
  - The other's top left tile is located at x=7, y=8.
- Max Clusters have a score of 7. There is just one cluster with size 7.
- Clusters have a score of 7, 4 and 3. There are three clusters.

## Requirements

- [babashka](https://babashka.org)

## Run the tool in the terminal

In order to use this you need to have your data formatted as JSON or EDN.

### Options

- `--json` to pass JSON as string
- `--edn` to pass EDN as string (strings and keywords supported)
- `--file` to read a .edn/.json file

### Input file structure / Data Schema

The expected JSON object should look like this:

```text
[
  {"x": number, "y": number},
  {"x": number, "y": number},
  ...
]
```

### Example command

This will work after you clone the repository and run the following command in the terminal.

```bash
# --file (with .json)
$ bb -o --main map-tile-metrics.main --file "test/map_tile_metrics/resources/test-data.json"
# --file (with .edn, keys are keywords)
$ bb -o --main map-tile-metrics.main --file "test/map_tile_metrics/resources/test-data.edn"
# --file (with .edn, keys are strings)
$ bb -o --main map-tile-metrics.main --file "test/map_tile_metrics/resources/test-data-str.edn"
```

Read in a JSON string inline, the example shown utilizes `cat` for this:

```bash
# --json
$ bb -o --main map-tile-metrics.main --json "$(cat test/map_tile_metrics/resources/test-data.json)"
```

Reading in `EDN` is also supported 🚀 you can pass it inline - as shown - or use `cat` as in the example above:

```bash
# --edn
$ bb -o --main map-tile-metrics.main --edn '#{{:x 1 :y 1} {:x 2 :y 1} {:x 3 :y 1} {:x 1 :y 2} {:x 2 :y 2} {:x 3 :y 2} {:x 1 :y 3} {:x 2 :y 3} {:x 3 :y 3}}'
```

### Docker

Build the image and run the tool in a container. But please note that the entrypoint is set already, so you just need to pass the option and data.

See example below:

```bash
# build the image first
$ docker build -t bb-map-tile-metrics .
```

```bash
$ docker run --rm -it bb-map-tile-metrics --edn '#{{:x 1 :y 1} {:x 2 :y 1} {:x 3 :y 1} {:x 1 :y 2} {:x 2 :y 2} {:x 3 :y 2} {:x 1 :y 3} {:x 2 :y 3} {:x 3 :y 3}}' > output.json
```

### Call from Ruby

Use this tool in a Ruby project

```ruby
# {x: 2, y: 2} is the only cluster-coordinate
data = [
  {x: 1, y: 1}, {x: 2, y: 1}, {x: 3, y: 1},
  {x: 1, y: 2}, {x: 2, y: 2}, {x: 3, y: 2},
  {x: 1, y: 3}, {x: 2, y: 3}, {x: 3, y: 3}
]
tile_metrics = JSON.parse(
  `bb -o --main map-tile-metrics.main --json '#{data.to_json}'`
)
```

## Other ...

Compact JSON data using jq: `$ cat test-data.json | jq -c > output.json`

## Plans, Wishes, Hopes and Dreams

- [ ] transfer this to a clojar library
