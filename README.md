# Mapper

A powerful Bukkit/Paper plugin for creating, visualizing, and managing regions in your Minecraft world.

## Disclaimer

Mapper is not intended to replace WorldEdit or WorldGuard. It is a lightweight region definition tool designed to be used as a library for developers who need to define and manage regions in their own plugins. While it provides region visualization and management capabilities, it does not include the terrain manipulation features of WorldEdit or the protection features of WorldGuard.

Developers can integrate Mapper into their own projects to leverage its region definition system without having to implement region handling from scratch.

## Overview

Mapper allows server administrators and world editors to define various types of regions in the world - from simple points to complex cuboids and polygons. These regions can be used for defining areas of interest, creating custom features, or integrating with other plugins.

## Features

- **Multiple Region Types**
    - Point Regions
    - Perspective Regions
    - Cuboid Regions
    - Polygon Regions

- **Intuitive Tools**
    - Special tools for creating each type of region
    - Simple selection interface for cuboid regions
    - Point-and-click creation for simple regions
    - Region deletion tool

- **Data Management**
    - Session-based editing
    - JSON export/import
    - Persistent storage of regions

## Installation

1. Download the latest JAR file from the releases page
2. Place the JAR into your server's `plugins` folder
3. Restart your server
4. The plugin will generate necessary configuration files on first run

## Usage

### Region Creation

1. Start an editing session with `/mapper edit`
2. Use the provided tools to create regions:
    - Beacon: Point Region Creator
    - Armor Stand: Perspective Region Creator
    - Blaze Rod: Cuboid Region Wand
    - Breeze Rod: Polygon Region Wand
    - Shears: Region Deletion Tool

### Creating a Cuboid Region

1. Left-click with the Blaze Rod to set the first corner
2. Right-click to set the second corner
3. Shift + Right-click to complete the region
4. Enter a name and select a color for your region

### Creating a Polygon Region

1. Left-click with the Breeze Rod to set the first corner of a polygon part
2. Right-click to set the second corner
3. Shift + Right-click to add that cuboid part to the polygon
4. Repeat steps 1-3 for each additional part
5. Shift + Right-click again without an active corner pair to finalize the polygon

### Saving Your Work

When you're done creating regions:
1. Save your work with `/mapper save [strategy]` (defaults to JSON)
2. Or discard your changes with `/mapper discard`

## Commands

| Command                   | Description                           |
|---------------------------|---------------------------------------|
| `/mapper help`            | Shows the help menu                   |
| `/mapper edit`            | Starts a new editing session          |
| `/mapper save [strategy]` | Saves your regions (defaults to JSON) |
| `/mapper discard`         | Discards your current editing session |

## Permissions

- `mapper.use` - Allows use of the plugin

## Technical Details

Regions are stored in the world folder as `dataPoints.json`. The plugin uses Jackson for serialization and deserialization of region data.

For developers: The plugin provides a clean API for working with regions through the Region interface and supporting classes.
