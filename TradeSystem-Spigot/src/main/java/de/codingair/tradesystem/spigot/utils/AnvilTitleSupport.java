package de.codingair.tradesystem.spigot.utils;

import com.github.Anon8281.universalScheduler.UniversalScheduler;
import de.codingair.codingapi.player.gui.inventory.InventoryUtils;
import de.codingair.codingapi.server.reflections.IReflection;
import de.codingair.codingapi.server.reflections.PacketUtils;
import de.codingair.codingapi.server.specification.Version;
import de.codingair.tradesystem.spigot.TradeSystem;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class AnvilTitleSupport {
    private static final Logger LOGGER = Logger.getLogger(AnvilTitleSupport.class.getName());

    private AnvilTitleSupport() {
    }

    public static void refreshTitleAfterOpen(@NotNull Player player, @Nullable String title) {
        if (title == null || title.isEmpty()) return;

        TradeSystem plugin = TradeSystem.getInstance();
        if (plugin == null) {
            refreshTitle(player, title);
            return;
        }

        UniversalScheduler.getScheduler(plugin).runTaskLater(() -> refreshTitle(player, title), 1);
    }

    public static void refreshTitle(@NotNull Player player, @Nullable String title) {
        if (title == null || title.isEmpty()) return;
        if (!Version.atLeast(14)) return;

        try {
            Class<?> containerClass = IReflection.getClass(IReflection.ServerPacket.INVENTORY, Version.choose("Container", 21.11, "AbstractContainerMenu"));
            Class<?> openWindowPacketClass = IReflection.getClass(IReflection.ServerPacket.PACKETS, Version.choose("PacketPlayOutOpenWindow", 21.11, "ClientboundOpenScreenPacket"));
            if (containerClass == null || openWindowPacketClass == null || InventoryUtils.CONTAINERS_CLASS == null) return;

            IReflection.FieldAccessor<?> titleField = IReflection.getField(containerClass, PacketUtils.IChatBaseComponentClass, 0);
            IReflection.FieldAccessor<?> anvilContainer = IReflection.getField(InventoryUtils.CONTAINERS_CLASS, getAnvilContainerField());
            IReflection.ConstructorAccessor openWindowPacket = IReflection.getConstructor(openWindowPacketClass, int.class, InventoryUtils.CONTAINERS_CLASS, PacketUtils.IChatBaseComponentClass);
            if (titleField == null || anvilContainer == null || openWindowPacket == null) return;

            Object entityPlayer = PacketUtils.getEntityPlayer(player);
            Object activeContainer = InventoryUtils.getActiveContainer(entityPlayer);
            if (activeContainer == null) return;
            if (!activeContainer.toString().toLowerCase(Locale.ROOT).contains("anvil")) return;

            Object component = PacketUtils.getRawIChatBaseComponent(serializeTitle(title));
            titleField.set(activeContainer, component);

            Object packet = openWindowPacket.newInstance(
                    InventoryUtils.getWindowId(activeContainer),
                    anvilContainer.get(null),
                    component
            );
            PacketUtils.sendPacket(player, packet);
            player.updateInventory();
        } catch (Exception ex) {
            LOGGER.log(Level.FINE, "Could not refresh anvil title.", ex);
        }
    }

    static @NotNull String getAnvilContainerField(boolean mojangMapped, double version) {
        if (mojangMapped) return "ANVIL";
        if (version >= 20.04D) return "i";
        if (version >= 17D) return "h";
        return "ANVIL";
    }

    private static @NotNull String getAnvilContainerField() {
        return getAnvilContainerField(Version.mojangMapped(), Version.get());
    }

    private static @NotNull String serializeTitle(@NotNull String title) {
        return ComponentSerializer.toString(TextComponent.fromLegacyText(title));
    }
}
