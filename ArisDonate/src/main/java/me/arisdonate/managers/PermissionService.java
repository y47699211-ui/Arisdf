package me.arisdonate.managers;

import me.arisdonate.ArisDonatePlugin;
import me.arisdonate.models.DonateRank;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Раздаёт игрокам реальные Bukkit-пермы по их донат-рангу.
 *
 * Базовая таблица перм заранее зашита в {@link #DEFAULT_PERMS}. Любую запись
 * можно переопределить через config.yml:
 *
 *   ranks:
 *     stellar:
 *       permissions:
 *         - arisdonate.fly
 *         - arisdonate.kit.stellar
 *
 * Если в ранге задан {@code kit:}, право {@code arisdonate.kit.<kitId>}
 * добавляется автоматически. Перм-сет накапливается: владелец высокого ранга
 * получает все права своего ранга и всех более низких (по {@code weight}).
 */
public class PermissionService {

    private static final Map<String, List<String>> DEFAULT_PERMS = new LinkedHashMap<>();
    static {
        DEFAULT_PERMS.put("spark",    List.of());
        DEFAULT_PERMS.put("luna",     List.of("arisdonate.hat", "arisdonate.skull"));
        DEFAULT_PERMS.put("stellar",  List.of("arisdonate.fly", "arisdonate.feed", "arisdonate.heal"));
        DEFAULT_PERMS.put("nova",     List.of("arisdonate.speed"));
        DEFAULT_PERMS.put("comet",    List.of("arisdonate.jump", "arisdonate.top", "arisdonate.back"));
        DEFAULT_PERMS.put("galaxy",   List.of("arisdonate.repair"));
        DEFAULT_PERMS.put("nebula",   List.of("arisdonate.nick"));
        DEFAULT_PERMS.put("cosmos",   List.of("arisdonate.vanish", "arisdonate.god", "arisdonate.cure"));
        DEFAULT_PERMS.put("phoenix",  List.of("arisdonate.smite", "arisdonate.burn"));
        DEFAULT_PERMS.put("aris",     List.of(
                "arisdonate.enderchest", "arisdonate.invsee",
                "arisdonate.tphere", "arisdonate.tppos", "arisdonate.tpall"));
        DEFAULT_PERMS.put("arisplus", List.of(
                "arisdonate.gamemode", "arisdonate.give", "arisdonate.item",
                "arisdonate.more", "arisdonate.effect",
                "arisdonate.commandspy", "arisdonate.socialspy",
                "arisdonate.weather", "arisdonate.time",
                "arisdonate.broadcast", "arisdonate.clearinv",
                "arisdonate.enderchest.others", "arisdonate.kit.bypass-cooldown"));
    }

    private final ArisDonatePlugin plugin;
    private final Map<UUID, PermissionAttachment> attachments = new java.util.HashMap<>();

    public PermissionService(ArisDonatePlugin plugin) {
        this.plugin = plugin;
    }

    /** Полный (кумулятивный) набор перм для ранга, с учётом всех более низких. */
    public List<String> permissionsFor(DonateRank rank) {
        if (rank == null) return List.of();
        Set<String> all = new LinkedHashSet<>();
        for (DonateRank r : plugin.getDonateManager().getRanks().values()) {
            if (r.weight() > rank.weight()) continue;
            all.addAll(permsForRankOnly(r));
        }
        return new ArrayList<>(all);
    }

    private List<String> permsForRankOnly(DonateRank r) {
        Set<String> set = new LinkedHashSet<>();
        // 1) Override из config.yml: ranks.<id>.permissions: [ ... ]
        ConfigurationSection sect = plugin.getConfig().getConfigurationSection("ranks." + r.id());
        if (sect != null && sect.isList("permissions")) {
            for (String p : sect.getStringList("permissions")) {
                if (p != null && !p.isBlank()) set.add(p.trim());
            }
        } else {
            // 2) Базовая таблица
            set.addAll(DEFAULT_PERMS.getOrDefault(r.id(), List.of()));
        }
        // 3) Кит из поля kit: ранга
        if (r.kitId() != null && !r.kitId().isBlank()) {
            set.add("arisdonate.kit." + r.kitId().toLowerCase());
        }
        return new ArrayList<>(set);
    }

    /** Перевыдать пермы конкретному онлайн-игроку (после join / give / set / remove). */
    public void apply(Player p) {
        if (p == null || !p.isOnline()) return;
        PermissionAttachment att = attachments.get(p.getUniqueId());
        if (att == null) {
            att = p.addAttachment(plugin);
            attachments.put(p.getUniqueId(), att);
        } else {
            for (String old : new ArrayList<>(att.getPermissions().keySet())) {
                att.unsetPermission(old);
            }
        }
        DonateRank rank = plugin.getDonateManager().getPlayerRank(p.getName());
        for (String perm : permissionsFor(rank)) {
            att.setPermission(perm, true);
        }
        p.recalculatePermissions();
    }

    /** Сбросить аттач при выходе. */
    public void remove(Player p) {
        if (p == null) return;
        PermissionAttachment att = attachments.remove(p.getUniqueId());
        if (att != null) {
            try { p.removeAttachment(att); } catch (IllegalArgumentException ignored) {}
        }
    }

    /** Перепринять для всех онлайн-игроков (после reload или старта плагина). */
    public void refreshAll() {
        for (Player p : plugin.getServer().getOnlinePlayers()) apply(p);
    }
}
