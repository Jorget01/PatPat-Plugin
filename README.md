[![Modrinth Link-Banner](https://cdn.modrinth.com/data/cached_images/9991553f6a20e5105b9b153b8d817bc3630c18a8.png)](https://modrinth.com/plugin/patpatplugin)

# PatPatPlugin

A Minecraft Paper plugin for petting entities with visual effects and sounds.

## Features

- **Pet entities** by right-clicking or using `/patpat` command
- **Customizable particles** - hearts, confetti, and flash effects
- **Customizable sounds** - choose any Minecraft sound for petting
- **Entity whitelist/blacklist** - control which entities can be petted
- **Animation settings** - jump height and entity rotation
- **Cooldown system** - prevent spam petting
- **Full configuration** - all settings adjustable in config.yml
- **Reload command** - `/patpatreload` to reload config without restarting

## Commands

| Command | Permission | Description |
|---------|------------|-------------|
| `/patpat` | `patpat.pet` | Pet the nearest entity |
| `/patpatreload` | `patpat.reload` | Reload plugin configuration |

## Permissions

| Permission | Default | Description |
|------------|---------|-------------|
| `patpat.pet` | true | Allow players to pet entities |
| `patpat.reload` | op | Allow operators to reload config |

## Configuration

The plugin creates a `config.yml` file with the following options:

### Particles

```yaml
particles:
  enabled: true          # Enable particle effects
  type: HEART            # Particle type (HEART, etc.)
  count: 10              # Number of particles
  spread: 0.5            # Particle spread radius
  size: 1.0              # Particle size
```

### Sound

```yaml
sound:
  enabled: true          # Enable sound effects
  type: ENTITY_EXPERIENCE_ORB_PICKUP  # Sound name
  volume: 0.5           # Sound volume (0.0 - 1.0)
  pitch: 1.5            # Sound pitch (0.0 - 2.0)
```

**Available sounds:**
- `ENTITY_CAT_AMBIENT` - Cat meow
- `ENTITY_WOLF_AMBIENT` - Dog bark
- `ENTITY_PIG_AMBIENT` - Pig oink
- `ENTITY_SHEEP_AMBIENT` - Sheep baa
- `ENTITY_RABBIT_AMBIENT` - Rabbit squeak
- `ENTITY_PARROT_AMBIENT` - Parrot sound
- `ENTITY_FOX_AMBIENT` - Fox chatter
- `ENTITY_EXPERIENCE_ORB_PICKUP` - XP sound
- And many more...

### Entity List

```yaml
entities:
  enabled-list: false    # true = whitelist, false = blacklist
  types:                 # List of entity types
    - CAT
    - WOLF
    - PIG
    - SHEEP
    - COW
    - CHICKEN
    - RABBIT
    - HORSE
    - LLAMA
    - FOX
    - VILLAGER
    # Add more entity types...
```

### Animation

```yaml
animation:
  jump-height: 0.5      # Entity jump height (0.0 to disable)
  rotate-entity: true    # Rotate entity towards player
```

### General

```yaml
range: 4.0              # Distance to find entities
cooldown: 500           # Cooldown in milliseconds
message: true            # Show pet message
```



