# BeastsPlus Guide

BeastsPlus is an addon for Beasts that allows you to attach FreeMinecraftModels (FMM) loaded models to entities. Using  YAML files, you can bind models, override sounds, and add boss barss.

## 1. File Structure

All beast configurations must be placed in the `plugins/BeastsPlus/content/` directory. The plugin supports sub-directory loading.

Files must end with the `.yml` extension. The plugin parses these on startup or when the FMM reload event fires.

---

## 2. Root Schema

Every beast configuration requires a root `beast_id` and supports optional sections for models, audio, and boss bars.

| Key | Type | Required | Description |
| --- | --- | --- | --- |
| `beast_id` | String | Yes | The ID of the Beast. |
| `model` | Section | No | Defines the FMM model to apply upon spawning. |
| `sounds` | Section | No | Maps event states to sounds. |
| `boss_bar` | Section | No | Defines a proximity-based boss bar for the entity. |

---

## 3. Model Configuration

The `model` section links the beast to a loaded FreeMinecraftModels configuration.

| Key | Type | Default | Description |
| --- | --- | --- | --- |
| `id` | String | (Required) | The models ID.. |

```yaml
model:
  id: "great_dragon"
```

---

## 4. Sound Configuration

The `sounds` section maps event triggers to sound IDs, volume, and pitch.

### Available Sound Keys
Supported event keys:
- `idle` Played randomly every 4 seconds.
- `step` Played when moving.
- `hurt` Played on damage.
- `death` Played on death.


### Sound Entry Schema

| Key | Type | Default | Description |
| --- | --- | --- | --- |
| `sound` | String | (Required) | The Minecraft sound (e.g., `entity.ender_dragon.growl`). |
| `volume` | Float | `1.0` | Playback volume. |
| `pitch` | Float | `1.0` | Playback pitch. |

```yaml
sounds:
  idle:
    sound: "entity.warden.ambient"
    volume: 1.0
    pitch: 0.8
  hurt:
    sound: "entity.warden.hurt"
```

---

## 5. Boss Bar Configuration

Displays a boss bar to players within a certain radius. It automatically tracks health and updates viewers.

| Key | Type | Default | Description |
| --- | --- | --- | --- |
| `title` | String | (Required) | MiniMessage string for the text. |
| `color` | Enum | `RED` | Valid options: `RED`, `BLUE`, `GREEN`, `YELLOW`, `PINK`, `PURPLE`, `WHITE`. |
| `style` | Enum | `PROGRESS` | Valid options: `PROGRESS`, `NOTCHED_6`, `NOTCHED_10`, `NOTCHED_12`, `NOTCHED_20`. |
| `radius` | Double | `32.0` | Distance to see the boss bar. |

```yaml
boss_bar:
  title: "<red><bold>The Great Dragon"
  color: RED
  style: NOTCHED_10
  radius: 50.0
```

---

## 6. Complete Example

```yaml
beast_id: "great_dragon"
model:
  id: "dragon_model"
sounds:
  idle:
    sound: "entity.ender_dragon.ambient"
    volume: 1.5
    pitch: 0.9
  step:
    sound: "entity.ender_dragon.flap"
    volume: 1.0
    pitch: 1.0
  hurt:
    sound: "entity.ender_dragon.hurt"
    volume: 2.0
    pitch: 1.1
  death:
    sound: "entity.ender_dragon.death"
    volume: 3.0
    pitch: 1.0
boss_bar:
  title: "<dark_red><bold>Ashwing, The Desolator"
  color: RED
  style: NOTCHED_20
  radius: 64.0
```