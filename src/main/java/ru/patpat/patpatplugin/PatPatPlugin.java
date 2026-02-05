package ru.patpat.patpatplugin;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Plugin for petting entities in Minecraft
 * Supports Paper 1.21.x
 */
public final class PatPatPlugin extends JavaPlugin implements Listener {

    // Last pat time for each player
    private final Map<UUID, Long> lastPatTime = new HashMap<>();

    // Plugin settings
    private boolean enableParticles = true;
    private boolean enableSound = true;
    private boolean showMessage = true;
    private String particleType = "HEART";
    private float particleSize = 1.0f;
    private int particleCount = 10;
    private double particleSpread = 0.5;
    private float soundVolume = 0.5f;
    private float soundPitch = 1.5f;
    private Sound patSound = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
    
    // Cooldown settings
    private long cooldown = 500L;
    private double range = 4.0;
    
    // Entity settings
    private boolean entitiesEnabledList = false;
    private Set<EntityType> allowedEntities = new HashSet<>();
    
    // Animation settings
    private float jumpHeight = 0.5f;
    private boolean rotateEntity = true;

    @Override
    public void onEnable() {
        // Save default config
        saveDefaultConfig();
        
        // Load settings
        loadConfig();
        
        // Register events
        getServer().getPluginManager().registerEvents(this, this);
        
        getLogger().info("PatPatPlugin enabled! Use right-click or /patpat to pet entities.");
        getLogger().info("Allowed entity types: " + allowedEntities.size());
    }

    @Override
    public void onDisable() {
        getLogger().info("PatPatPlugin disabled.");
    }

    /**
     * Reload configuration
     */
    public void reloadConfig() {
        super.reloadConfig();
        loadConfig();
        getLogger().info("Configuration reloaded.");
        getLogger().info("Allowed entity types: " + allowedEntities.size());
    }

    private void loadConfig() {
        enableParticles = getConfig().getBoolean("particles.enabled", true);
        enableSound = getConfig().getBoolean("sound.enabled", true);
        showMessage = getConfig().getBoolean("message", true);
        particleType = getConfig().getString("particles.type", "HEART");
        particleSize = (float) getConfig().getDouble("particles.size", 1.0);
        particleCount = getConfig().getInt("particles.count", 10);
        particleSpread = getConfig().getDouble("particles.spread", 0.5);
        
        // Load sound from config
        String soundName = getConfig().getString("sound.type", "ENTITY_EXPERIENCE_ORB_PICKUP");
        try {
            patSound = Sound.valueOf(soundName);
        } catch (IllegalArgumentException e) {
            getLogger().warning("Unknown sound " + soundName + ", using default.");
            patSound = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
        }
        soundVolume = (float) getConfig().getDouble("sound.volume", 0.5);
        soundPitch = (float) getConfig().getDouble("sound.pitch", 1.5);
        
        // Load entity settings
        entitiesEnabledList = getConfig().getBoolean("entities.enabled-list", false);
        allowedEntities.clear();
        
        List<String> entityTypes = getConfig().getStringList("entities.types");
        for (String type : entityTypes) {
            try {
                EntityType entityType = EntityType.valueOf(type.toUpperCase());
                allowedEntities.add(entityType);
            } catch (IllegalArgumentException e) {
                getLogger().warning("Unknown entity type: " + type);
            }
        }
        
        // Load animation settings
        jumpHeight = (float) getConfig().getDouble("animation.jump-height", 0.5);
        rotateEntity = getConfig().getBoolean("animation.rotate-entity", true);
        
        // Load cooldown and range
        cooldown = getConfig().getLong("cooldown", 500L);
        range = getConfig().getDouble("range", 4.0);
    }

    /**
     * Check if entity can be petted
     */
    private boolean canPatEntity(Entity entity) {
        EntityType type = entity.getType();
        
        if (entitiesEnabledList) {
            // Whitelist - only listed entities
            return allowedEntities.contains(type);
        } else {
            // Blacklist - all entities except listed
            return !allowedEntities.contains(type);
        }
    }

    /**
     * Command handler
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("patpatreload")) {
            // Reload command
            if (!sender.hasPermission("patpat.reload")) {
                sender.sendMessage("§cYou don't have permission to reload the plugin!");
                return true;
            }
            
            reloadConfig();
            
            // Send message to sender
            String reloadMessage = "§aPatPatPlugin configuration reloaded.";
            if (sender instanceof Player) {
                sender.sendMessage(reloadMessage);
            } else {
                getLogger().info(reloadMessage);
            }
            return true;
        }
        
        // /patpat command
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by a player!");
            return true;
        }

        Player player = (Player) sender;
        
        if (!player.hasPermission("patpat.pet")) {
            player.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }

        // Find nearest entity
        Entity nearestEntity = findNearestEntity(player);
        
        if (nearestEntity == null) {
            player.sendMessage("§7No entities nearby to pet!");
            return true;
        }

        // Check if entity can be petted
        if (!canPatEntity(nearestEntity)) {
            player.sendMessage("§7This entity cannot be petted!");
            return true;
        }

        // Check cooldown
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        long lastTime = lastPatTime.getOrDefault(playerId, 0L);
        
        if (currentTime - lastTime < cooldown) {
            player.sendMessage("§7Wait before petting again!");
            return true;
        }

        // Pet the entity
        patEntity(player, nearestEntity);
        lastPatTime.put(playerId, currentTime);

        return true;
    }

    /**
     * Right-click entity handler
     */
    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();

        if (!player.hasPermission("patpat.pet")) {
            return;
        }

        // Check if entity can be petted
        if (!canPatEntity(entity)) {
            return;
        }

        // Check cooldown
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        long lastTime = lastPatTime.getOrDefault(playerId, 0L);
        
        if (currentTime - lastTime < cooldown) {
            return;
        }

        // Pet the entity
        patEntity(player, entity);
        lastPatTime.put(playerId, currentTime);
    }

    /**
     * Find nearest entity to player
     */
    private Entity findNearestEntity(Player player) {
        Location playerLoc = player.getLocation();
        Entity nearestEntity = null;
        double nearestDistance = range;

        for (Entity entity : player.getWorld().getEntities()) {
            if (entity.equals(player)) {
                continue;
            }

            // Check if entity can be petted
            if (!canPatEntity(entity)) {
                continue;
            }

            double distance = playerLoc.distance(entity.getLocation());
            
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestEntity = entity;
            }
        }

        return nearestEntity;
    }

    /**
     * Pet entity with visual effects
     */
    private void patEntity(Player player, Entity entity) {
        Location entityLoc = entity.getLocation();
        World world = entity.getWorld();

        // Play sound
        if (enableSound) {
            playPatSound(world, entityLoc);
        }

        // Create visual effects
        if (enableParticles) {
            createPatParticles(world, entityLoc);
        }

        // Send message to player
        if (showMessage) {
            String entityName = entity.getName();
            if (entityName.isEmpty()) {
                entityName = entity.getType().name().toLowerCase().replace("_", " ");
            }

            player.sendMessage("§d✿ You petted " + entityName + " ✿");
        }
        
        // Send heart particles to player
        player.spawnParticle(Particle.HEART, entityLoc.add(0, 1, 0), 5, 0.5, 0.5, 0.5, 0.1);

        // Pet animation
        if (entity instanceof org.bukkit.entity.LivingEntity) {
            org.bukkit.entity.LivingEntity livingEntity = (org.bukkit.entity.LivingEntity) entity;
            
            // Rotate entity towards player
            if (rotateEntity) {
                entity.setRotation(player.getLocation().getYaw(), player.getLocation().getPitch());
            }
            
            // Jump animation
            if (jumpHeight > 0) {
                Vector velocity = livingEntity.getVelocity();
                velocity.setY(jumpHeight);
                livingEntity.setVelocity(velocity);
            }
        }
    }

    /**
     * Create particle effects when petting
     */
    private void createPatParticles(World world, Location loc) {
        // Hearts around entity
        for (int i = 0; i < particleCount; i++) {
            double angle = (Math.PI * 2 / particleCount) * i;
            double x = Math.cos(angle) * particleSpread;
            double z = Math.sin(angle) * particleSpread;
            
            Location particleLoc = loc.clone().add(x, 1.2, z);
            
            world.spawnParticle(Particle.HEART, particleLoc, 1, 0, 0, 0, 0.1);
        }

        // Flash effect
        world.spawnParticle(Particle.FLASH, loc.add(0, 1, 0), 1, 0, 0, 0, 0);

        // Confetti using DUST particles
        Particle[] particles = {
            Particle.DUST,
            Particle.END_ROD,
            Particle.DUST
        };

        for (int i = 0; i < particleCount; i++) {
            double angle = Math.random() * Math.PI * 2;
            double distance = Math.random() * particleSpread + 0.3;
            double x = Math.cos(angle) * distance;
            double z = Math.sin(angle) * distance;
            double y = Math.random() * particleSpread + 0.5;

            Particle randomParticle = particles[(int) (Math.random() * particles.length)];
            
            Location particleLoc = loc.clone().add(x, y, z);
            
            if (randomParticle == Particle.DUST) {
                world.spawnParticle(randomParticle, particleLoc, 1, 
                    new Particle.DustOptions(Color.fromRGB(255, 105, 180), particleSize));
            } else {
                world.spawnParticle(randomParticle, particleLoc, 1, 0, 0, 0, particleSize * 0.1f);
            }
        }
    }

    /**
     * Play sound when petting
     */
    private void playPatSound(World world, Location loc) {
        // Sound from config
        world.playSound(loc, patSound, soundVolume, soundPitch);
        
        // Additional chime sound
        world.playSound(loc, Sound.BLOCK_AMETHYST_BLOCK_CHIME, soundVolume * 0.6f, soundPitch * 0.8f);
    }

    /**
     * Tab completion
     */
    @Override
    public java.util.List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return java.util.Collections.emptyList();
    }
}
