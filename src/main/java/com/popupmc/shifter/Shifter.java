package com.popupmc.shifter;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.popupmc.customtradeevent.CustomTradeEvent;
import dev.dbassett.skullcreator.SkullCreator;
import io.papermc.paper.event.entity.EntityMoveEvent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class Shifter extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);

        createRecipe();

        // Log enabled status
        getLogger().info("Shifter is enabled.");
    }

    public void createRecipe() {
        ItemStack insanityWarp = SkullCreator.itemFromBase64(textureInsanityShift);

        ItemMeta meta = insanityWarp.getItemMeta();
        meta.setDisplayName("Reality Warp");
        insanityWarp.setItemMeta(meta);

        NamespacedKey key = new NamespacedKey(this, "bedrock_to_insanity");
        ShapelessRecipe recipe = new ShapelessRecipe(key, insanityWarp);
        recipe.addIngredient(Material.BEDROCK);
        recipe.addIngredient(Material.CREEPER_HEAD);

        try {
            Bukkit.addRecipe(recipe);
        }
        catch (Exception ignored) {}

        insanityWarp = SkullCreator.itemFromBase64(textureVoidShift);

        meta = insanityWarp.getItemMeta();
        meta.setDisplayName("Void Warp");
        insanityWarp.setItemMeta(meta);

        key = new NamespacedKey(this, "bedrock_to_void");
        recipe = new ShapelessRecipe(key, insanityWarp);
        recipe.addIngredient(Material.BEDROCK);
        recipe.addIngredient(Material.PLAYER_HEAD);

        try {
            Bukkit.addRecipe(recipe);
        }
        catch (Exception ignored) {}

        insanityWarp = SkullCreator.itemFromBase64(textureAmplifiedShift);

        meta = insanityWarp.getItemMeta();
        meta.setDisplayName("Amplified Warp");
        insanityWarp.setItemMeta(meta);

        key = new NamespacedKey(this, "bedrock_to_amplified");
        recipe = new ShapelessRecipe(key, insanityWarp);
        recipe.addIngredient(Material.BEDROCK);
        recipe.addIngredient(Material.ZOMBIE_HEAD);

        try {
            Bukkit.addRecipe(recipe);
        }
        catch (Exception ignored) {}
    }

    // Log disabled status
    @Override
    public void onDisable() {
        getLogger().info("Shifter is disabled");
    }

//    @EventHandler(priority = EventPriority.NORMAL)
//    public void onEntityAddToWorldEvent(EntityAddToWorldEvent event) {
//        // Must be a villager
//        if (!(event.getEntity() instanceof Villager))
//            return;
//
//        // Must have lived less than 20 ticks
//        if(event.getEntity().getTicksLived() > 20)
//            return;
//
//        // Must not be at spawn
//        if (event.getEntity().getWorld().getName().equalsIgnoreCase("imperial_city"))
//            return;
//
//        addRealityPortalTrade((Villager)event.getEntity());
//    }
//
//    @EventHandler(priority = EventPriority.NORMAL)
//    public void onVillagerInteract(PlayerInteractAtEntityEvent event) {
//        // Must be a villager
//        if (!(event.getRightClicked() instanceof Villager))
//            return;
//
//        // Must have lived less than 20 ticks
//        if(event.getRightClicked().getTicksLived() > 20)
//            return;
//
//        // Must not be at spawn
//        if (event.getRightClicked().getWorld().getName().equalsIgnoreCase("imperial_city"))
//            return;
//
//        addRealityPortalTrade((Villager)event.getRightClicked());
//    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityAddToWorldEvent(CustomTradeEvent event) {

        // Must not be at spawn
        if (event.getRelayEvent().getEntity().getWorld().getName().equalsIgnoreCase("imperial_city"))
            return;

        // 10% chance
        if(random.nextInt(100 + 1) > 10)
            return;

        // Randomize result or 1t ingridient replace
        // Randomize reality or void portal
        int portalType = random.nextInt(3);
        boolean isResultItem = random.nextInt(100 + 1) <= 50;

        // Get Reality or Void item
        ItemStack item;

        switch (portalType) {
            case 0: {
                item = SkullCreator.itemFromBase64(textureInsanityShift);

                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName("Reality Warp");
                item.setItemMeta(meta);

                getLogger().info("Villager Trade: Shifter Reality Portal");
                break;
            }
            case 1: {
                item = SkullCreator.itemFromBase64(textureVoidShift);

                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName("Void Warp");
                item.setItemMeta(meta);

                getLogger().info("Villager Trade: Shifter Void Portal");
                break;
            }
            default: {
                item = SkullCreator.itemFromBase64(textureAmplifiedShift);

                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName("Amplified Warp");
                item.setItemMeta(meta);

                getLogger().info("Villager Trade: Shifter Amplified Portal");
                break;
            }
        }

        // Get current recipe
        MerchantRecipe recipe = event.getRelayEvent().getRecipe();

        // Replace result item if needs replacing
        ItemStack result = (isResultItem)
                ? item
                : recipe.getResult();

        // Ensure amount matches
        result.setAmount(recipe.getResult().getAmount());

        // Create new recipe thats a copy of the old one
        MerchantRecipe newRecipe = new MerchantRecipe(result,
                recipe.getUses(),
                recipe.getMaxUses(),
                recipe.hasExperienceReward(),
                recipe.getVillagerExperience(),
                recipe.getPriceMultiplier(),
                false);

        // get ingridients
        List<ItemStack> ings = recipe.getIngredients();

        // Stop if none, this is an error
        if(ings.size() == 0)
            return;

        // Set item as ing1 if not result
        if(!isResultItem) {
            item.setAmount(ings.get(0).getAmount());
            ings.set(0, item);
        }

        // Update new recipe with modified ings
        newRecipe.setIngredients(ings);

        // Set recipe
        event.getRelayEvent().setRecipe(newRecipe);
    }

    @EventHandler
    public void onEntityMoveEvent (EntityMoveEvent e) {
        // Do nothing if canceled
        if(e.isCancelled())
            return;

        this.proccess(e.getEntity());
    }

    @EventHandler
    public void onEntityMoveEvent (PlayerMoveEvent e) {
        // Do nothing if canceled
        if(e.isCancelled())
            return;

        this.proccess(e.getPlayer());
    }

    public void proccess(Entity entity) {
        // Do nothing if a request is already active
        if(requests.containsKey(entity.getUniqueId()))
            return;

        UUID uuid = entity.getUniqueId();

        Shifter self = this;

        // Sets a forced timeout of how long the key can remain
        new BukkitRunnable() {
            @Override
            public void run() {
                requests.remove(uuid);
            }
        }.runTaskLater(self, 20 * 5);

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                // Get player to see if still online
                Entity entity = Bukkit.getEntity(uuid);
                if(entity == null) {
                    cancelRequest();
                    return;
                }

                // Get Block 2 below
                Block block = entity.getWorld().getBlockAt(
                        entity.getLocation().getBlockX(),
                        entity.getLocation().getBlockY() - 2,
                        entity.getLocation().getBlockZ()
                );

                if(block.getType() != Material.PLAYER_HEAD) {
                    cancelRequest();
                    return;
                }

                // If not player head
//                if(block.getType() != Material.PLAYER_HEAD) {
//
//                    // Get block at player feet
//                    block = entity.getWorld().getBlockAt(
//                            entity.getLocation().getBlockX(),
//                            entity.getLocation().getBlockY() - 1,
//                            entity.getLocation().getBlockZ()
//                    );
//
//                    // If still not player head then stop here
//                    if(block.getType() != Material.PLAYER_HEAD) {
//                        cancelRequest();
//                        return;
//                    }
//                }

                // Make sure instance of skull, this should always be true
                if(!(block.getState() instanceof Skull)) {
                    cancelRequest();
                    return;
                }

                // Get skull & skull data
                Skull skull = (Skull)block.getState();
                PlayerProfile profile = skull.getPlayerProfile();

                // Stop if no skull data
                if(profile == null) {
                    cancelRequest();
                    return;
                }

                // Stop if no skull textures
                if(!profile.hasTextures()) {
                    cancelRequest();
                    return;
                }

                // Stop if not powered
                if(block.getBlockPower() <= 0 && !block.isBlockPowered() && !block.isBlockIndirectlyPowered()) {
                    cancelRequest();
                    return;
                }

                // Get skull data properties
                Set<ProfileProperty> profileProperties = profile.getProperties();

                for(ProfileProperty profileProperty : profileProperties) {
                    // Make sure name == "textures" and val equals correct texture
                    String name = profileProperty.getName();
                    String val = profileProperty.getValue();

                    if(!name.equalsIgnoreCase("textures")) {
                        continue;
                    }

                    if(val.equalsIgnoreCase(textureInsanityShift)) {
                        shiftEntityInsanity(entity);
                        break;
                    }
                    else if(val.equalsIgnoreCase(textureVoidShift)) {
                        shiftEntityVoid(entity);
                        break;
                    }
                    else if(val.equalsIgnoreCase(textureAmplifiedShift)) {
                        shiftEntityAmplified(entity);
                        break;
                    }
                }

                cancelRequest();
            }

            public void cancelRequest() {
                requests.remove(uuid);
            }
        }.runTaskLater(this, 20);

        requests.put(entity.getUniqueId(), task);
    }

    public void shiftEntityInsanity(Entity entity) {
        World fromWorld = entity.getWorld();
        World toWorld;
        String name = entity.getWorld().getName();

        // This block only applies to shifting between main and insanity
        if(!name.startsWith("main") &&
                !name.startsWith("insanity"))
            return;

        // Get destination world
        if(fromWorld.getName().startsWith("main"))
            name = name.replace("main", "insanity");
        else
            name = name.replace("insanity", "main");

        toWorld = Bukkit.getWorld(name);

        if(toWorld == null)
            return;

        // Get location
        Location curLocation = entity.getLocation();
        Location newLocation = curLocation.clone();

        // Switch world
        newLocation.setWorld(toWorld);

        // load chunk, generating if need be
        toWorld.getChunkAtAsync(newLocation, true).thenRun(() -> {

            // Get blocks at and above entity
            Block a = newLocation.getBlock();
            Block b = toWorld.getBlockAt(newLocation.getBlockX(), newLocation.getBlockY() + 1, newLocation.getBlockZ());

            // Ensure breathable
            if(!BreathableBlocks.isBreathable(a.getType()))
                a.setType(Material.AIR);
            if(!BreathableBlocks.isBreathable(b.getType()))
                b.setType(Material.AIR);

            // Teleport player
            entity.teleport(newLocation);
        });
    }

    public void shiftEntityVoid(Entity entity) {
        World toWorld;
        String name = entity.getWorld().getName();

        // This block only applies to shifting between main and insanity
        if(!name.equalsIgnoreCase("main") &&
                !name.equalsIgnoreCase("void"))
            return;

        // Get destination world
        if(name.equalsIgnoreCase("main"))
            name = "void";
        else
            name = "main";

        toWorld = Bukkit.getWorld(name);

        if(toWorld == null)
            return;

        // Get location
        Location curLocation = entity.getLocation();
        Location newLocation = curLocation.clone();

        // Switch world
        newLocation.setWorld(toWorld);

        // load chunk, generating if need be
        toWorld.getChunkAtAsync(newLocation, true).thenRun(() -> {

            // Get blocks at, above, and below entity
            Block a = newLocation.getBlock();
            Block b = toWorld.getBlockAt(newLocation.getBlockX(), newLocation.getBlockY() + 1, newLocation.getBlockZ());
            Block foot = toWorld.getBlockAt(newLocation.getBlockX(), newLocation.getBlockY() - 1, newLocation.getBlockZ());

            // Ensure breathable
            if(!BreathableBlocks.isBreathable(a.getType()))
                a.setType(Material.AIR);
            if(!BreathableBlocks.isBreathable(b.getType()))
                b.setType(Material.AIR);

            // Ensure platform
            if(foot.getType().isAir())
                foot.setType(Material.STONE);

            // Teleport player
            entity.teleport(newLocation);
        });
    }

    public void shiftEntityAmplified(Entity entity) {
        World toWorld;
        String name = entity.getWorld().getName();

        // This block only applies to shifting between main and insanity
        if(!name.equalsIgnoreCase("main") &&
                !name.equalsIgnoreCase("amplified"))
            return;

        // Get destination world
        if(name.equalsIgnoreCase("main"))
            name = "amplified";
        else
            name = "main";

        toWorld = Bukkit.getWorld(name);

        if(toWorld == null)
            return;

        // Get location
        Location curLocation = entity.getLocation();
        Location newLocation = curLocation.clone();

        // Switch world
        newLocation.setWorld(toWorld);

        // load chunk, generating if need be
        toWorld.getChunkAtAsync(newLocation, true).thenRun(() -> {

            // Get blocks at, above, and below entity
            Block a = newLocation.getBlock();
            Block b = toWorld.getBlockAt(newLocation.getBlockX(), newLocation.getBlockY() + 1, newLocation.getBlockZ());
            Block foot = toWorld.getBlockAt(newLocation.getBlockX(), newLocation.getBlockY() - 1, newLocation.getBlockZ());

            // Ensure breathable
            if(!BreathableBlocks.isBreathable(a.getType()))
                a.setType(Material.AIR);
            if(!BreathableBlocks.isBreathable(b.getType()))
                b.setType(Material.AIR);

            // Ensure platform
            if(foot.getType().isAir())
                foot.setType(Material.STONE);

            // Teleport player
            entity.teleport(newLocation);
        });
    }

    public HashMap<UUID, BukkitTask> requests = new HashMap<>();
    public static final Random random = new Random();

    public static final String textureInsanityShift = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjE0ZDE3MzM3OWZhNDg3MDdmZjJmMTQ2Yzg3NTE1YmM1YTM3NjI5YzY0YzdjYWE1ZTBmZmJiYzZiMTIxNTM5ZCJ9fX0=";
    public static final String textureVoidShift = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGE2YWM4OTlkY2ZjZmI5NmFiNmI3MmExMzAwNWU5YTExMTU5ZDgyMTU3ZjE1MzhiNmY2YTBiZWU3ODQ0YWEwMiJ9fX0=";
    public static final String textureAmplifiedShift = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmNmZTg2ODQ4MjdiMDUxM2UzMTBiNDVlODAyMzc2ZTEzM2YxYTI4MmZkYzEzNTBjZGQ0ZjdiZWExYmNjNzllZiJ9fX0=";
}
