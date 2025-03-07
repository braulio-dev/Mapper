package dev.brauw.mapper.session.display;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import dev.brauw.mapper.MapperPlugin;
import dev.brauw.mapper.region.PointRegion;
import io.papermc.paper.entity.LookAnchor;
import io.papermc.paper.math.Rotations;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Renders a glowing armorstand at the region's location.
 */
public class ArmorStandStrategy implements RegionDisplayStrategy<PointRegion> {

    private final Map<PointRegion, ArmorStand> displays = new HashMap<>();
    private final Multimap<PointRegion, UUID> viewers = ArrayListMultimap.create();
    private final Map<UUID, Map<PointRegion, String>> playerTeams = new HashMap<>();
    private final MapperPlugin plugin;

    public ArmorStandStrategy(MapperPlugin plugin) {
        this.plugin = plugin;
    }

    private ArmorStand getEntity(PointRegion region) {
        final Location location = region.getLocation();

        return displays.computeIfAbsent(region, key -> {
            return location.getWorld().spawn(location, ArmorStand.class, spawned -> {
                spawned.setGlowing(true);
                spawned.setVisibleByDefault(false);
                spawned.setSmall(true);
                spawned.setAI(false); // no gravity or physics
                spawned.setDisabledSlots(EquipmentSlot.values());
                spawned.setArms(true);
                spawned.setInvulnerable(true);
                spawned.setGravity(false);
                spawned.setNoPhysics(true);

                // make it so we can see the armorstand head with an item
                spawned.getEquipment().setHelmet(new ItemStack(Material.GOLDEN_HELMET));

                // Important, because we don't want the entity to be saved in case the server shuts down
                spawned.setPersistent(false);

                // Direction to rotation angles
                Vector direction = location.getDirection();
                final Location lookAt = location.clone().add(direction);
                spawned.lookAt(lookAt, LookAnchor.EYES);
            });
        });
    }

    private void setupTeamForPlayer(PointRegion region, Player player, ArmorStand armorStand) {
        Scoreboard scoreboard = player.getScoreboard();
        String teamName = "mapper_" + player.getUniqueId().toString().substring(0, 8) + "_" + region.hashCode();

        // Remove from any existing teams first
        for (Team team : scoreboard.getTeams()) {
            if (team.hasEntry(armorStand.getUniqueId().toString())) {
                team.removeEntry(armorStand.getUniqueId().toString());
            }
        }

        // Get or create the team
        Team team = scoreboard.getTeam(teamName);
        if (team == null) {
            team = scoreboard.registerNewTeam(teamName);
            final Color color = region.getOptions().getColor();
            team.color(NamedTextColor.nearestTo(TextColor.color(color.asRGB())));
        }

        // Add the armorstand to the team
        team.addEntry(armorStand.getUniqueId().toString());

        // Store the team name for cleanup
        playerTeams.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>())
                .put(region, teamName);
    }

    private void cleanupTeamForPlayer(PointRegion region, Player player) {
        UUID playerUUID = player.getUniqueId();
        Map<PointRegion, String> teams = playerTeams.get(playerUUID);

        if (teams != null && teams.containsKey(region)) {
            String teamName = teams.remove(region);
            Scoreboard scoreboard = player.getScoreboard();
            Team team = scoreboard.getTeam(teamName);

            if (team != null) {
                team.unregister();
            }

            if (teams.isEmpty()) {
                playerTeams.remove(playerUUID);
            }
        }
    }

    @Override
    public void display(@NotNull PointRegion region, @NotNull Player player) {
        // Get or create the armorstand for this region
        final ArmorStand armorStand = getEntity(region);

        // Show the armorstand to the player
        player.showEntity(plugin, armorStand);

        // Setup team with color for this player
        setupTeamForPlayer(region, player, armorStand);

        viewers.put(region, player.getUniqueId());
    }

    @Override
    public void update(@NotNull PointRegion region, @NotNull Player player) {
        final ArmorStand removed = displays.remove(region);
        if (removed != null && removed.isValid()) {
            cleanupTeamForPlayer(region, player);
            removed.remove();
        }
        display(region, player);
    }

    @Override
    public void hide(@NotNull PointRegion region, @NotNull Player player) {
        final UUID playerUUID = player.getUniqueId();
        if (viewers.remove(region, playerUUID)) {
            // Cleanup the team
            cleanupTeamForPlayer(region, player);

            // Hide entity
            final ArmorStand entity = Objects.requireNonNull(displays.get(region));
            player.hideEntity(plugin, entity);

            // If there are no more viewers, remove the display
            if (viewers.get(region).isEmpty()) {
                final ArmorStand armorStand = displays.remove(region);
                if (armorStand != null && armorStand.isValid()) {
                    armorStand.remove();
                }
            }
        }
    }
}