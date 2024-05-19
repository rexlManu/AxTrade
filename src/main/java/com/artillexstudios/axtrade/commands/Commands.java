package com.artillexstudios.axtrade.commands;

import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axtrade.AxTrade;
import com.artillexstudios.axtrade.hooks.HookManager;
import com.artillexstudios.axtrade.lang.LanguageManager;
import com.artillexstudios.axtrade.request.Requests;
import com.artillexstudios.axtrade.trade.Trades;
import com.artillexstudios.axtrade.utils.NumberUtils;
import com.artillexstudios.axtrade.utils.SoundUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Optional;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.BukkitCommandHandler;
import revxrsal.commands.bukkit.annotation.CommandPermission;
import revxrsal.commands.orphan.OrphanCommand;
import revxrsal.commands.orphan.Orphans;

import java.util.Map;

import static com.artillexstudios.axtrade.AxTrade.CONFIG;
import static com.artillexstudios.axtrade.AxTrade.GUIS;
import static com.artillexstudios.axtrade.AxTrade.HOOKS;
import static com.artillexstudios.axtrade.AxTrade.LANG;
import static com.artillexstudios.axtrade.AxTrade.MESSAGEUTILS;

@CommandPermission(value = "axtrade.trade")
public class Commands implements OrphanCommand {

    public void help(@NotNull CommandSender sender) {
        if (sender.hasPermission("axtrade.admin")) {
            for (String m : LANG.getStringList("admin-help")) {
                sender.sendMessage(StringUtils.formatToString(m));
            }
        } else {
            for (String m : LANG.getStringList("player-help")) {
                sender.sendMessage(StringUtils.formatToString(m));
            }
        }
    }

    @DefaultFor({"~"})
    public void trade(@NotNull Player sender, @Optional Player other) {
        if (other == null) {
            help(sender);
            return;
        }

        Requests.addRequest(sender, other);
    }

    @Subcommand("accept")
    public void accept(@NotNull Player sender, @NotNull Player other) {
        var request = Requests.getRequest(sender, other);
        if (request == null || request.getSender().equals(sender)) {
            MESSAGEUTILS.sendLang(sender, "request.no-request", Map.of("%player%", other.getName()));
            return;
        }

        Requests.addRequest(sender, other);
    }

    @Subcommand("deny")
    public void deny(@NotNull Player sender, @NotNull Player other) {
        var request = Requests.getRequest(sender, other);
        if (request == null || request.getSender().equals(sender)) {
            MESSAGEUTILS.sendLang(sender, "request.no-request", Map.of("%player%", other.getName()));
            return;
        }

        MESSAGEUTILS.sendLang(request.getSender(), "request.deny-sender", Map.of("%player%", request.getReceiver().getName()));
        MESSAGEUTILS.sendLang(request.getReceiver(), "request.deny-receiver", Map.of("%player%", request.getSender().getName()));
        SoundUtils.playSound(request.getSender(), "deny");
        SoundUtils.playSound(request.getReceiver(), "deny");
    }

    @Subcommand("reload")
    @CommandPermission(value = "axtrade.admin")
    public void reload(@NotNull CommandSender sender) {
        Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#00FFDD[AxTrade] &#AAFFDDReloading configuration..."));
        if (!CONFIG.reload()) {
            MESSAGEUTILS.sendFormatted(sender, "reload.failed", Map.of("%file%", "config.yml"));
            return;
        }
        Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#00FFDD╠ &#AAFFDDReloaded &fconfig.yml&#AAFFDD!"));

        if (!LANG.reload()) {
            MESSAGEUTILS.sendFormatted(sender, "reload.failed", Map.of("%file%", "lang.yml"));
            return;
        }
        Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#00FFDD╠ &#AAFFDDReloaded &flang.yml&#AAFFDD!"));

        if (!GUIS.reload()) {
            MESSAGEUTILS.sendFormatted(sender, "reload.failed", Map.of("%file%", "guis.yml"));
            return;
        }
        Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#00FFDD╠ &#AAFFDDReloaded &fguis.yml&#AAFFDD!"));

        if (!HOOKS.reload()) {
            MESSAGEUTILS.sendFormatted(sender, "reload.failed", Map.of("%file%", "currencies.yml"));
            return;
        }
        Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#00FFDD╠ &#AAFFDDReloaded &fcurrencies.yml&#AAFFDD!"));

        LanguageManager.reload();

        Commands.registerCommand();

        new HookManager().updateHooks();
        NumberUtils.reload();

        Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#00FFDD╚ &#AAFFDDSuccessful reload!"));
        MESSAGEUTILS.sendLang(sender, "reload.success");
    }

    @Subcommand("force")
    @CommandPermission(value = "axtrade.admin")
    public void force(@NotNull Player sender, Player other) {
        if (sender.equals(other)) {
            MESSAGEUTILS.sendLang(sender, "request.cant-trade-self");
            return;
        }
        Trades.addTrade(sender, other);
    }

    public static void registerCommand() {
        final BukkitCommandHandler handler = BukkitCommandHandler.create(AxTrade.getInstance());
        handler.unregisterAllCommands();
        handler.register(Orphans.path(CONFIG.getStringList("command-aliases").toArray(String[]::new)).handler(new Commands()));
        handler.registerBrigadier();
    }
}
