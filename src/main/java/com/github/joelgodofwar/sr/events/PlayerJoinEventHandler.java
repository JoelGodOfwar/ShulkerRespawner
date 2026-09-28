package com.github.joelgodofwar.sr.events;

import com.github.joelgodofwar.sr.ShulkerRespawner;
import com.github.joelgodofwar.sr.enums.Perms;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinEventHandler implements Listener {
    private final ShulkerRespawner plugin;

    public PlayerJoinEventHandler(ShulkerRespawner plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if(plugin.UpdateAvailable&&(Perms.SHOW_UPDATE_AVAILABLE.hasPermissionOrOp(player))){
            // TODO: UpdateCheck onPlayerJoin
            String links = "[\"\",{\"text\":\"<Download>\",\"bold\":true,\"color\":\"gold\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"<DownloadLink>/history\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"<please_update>\"}},{\"text\":\" \",\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"<please_update>\"}},{\"text\":\"| \"},{\"text\":\"<Donate>\",\"bold\":true,\"color\":\"gold\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://ko-fi.com/joelgodofwar\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"<Donate_msg>\"}},{\"text\":\" | \"},{\"text\":\"<Notes>\",\"bold\":true,\"color\":\"gold\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"<DownloadLink>/updates\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"<Notes_msg>\"}}]";
            links = links.replace("<DownloadLink>", plugin.DownloadLink).replace("<Download>", get("sr.version.download"))
                    .replace("<Donate>", get("sr.version.donate")).replace("<please_update>", get("sr.version.please_update"))
                    .replace("<Donate_msg>", get("sr.version.donate.message")).replace("<Notes>", get("sr.version.notes"))
                    .replace("<Notes_msg>", get("sr.version.notes.message"));
            String versions = ChatColor.GRAY + get("sr.version.new_vers") + ": " + ChatColor.GREEN + "{nVers}" + ChatColor.GRAY + " | " + get("sr.version.old_vers") + ": " + ChatColor.RED + "{oVers}";
            player.sendMessage(ChatColor.WHITE + get("sr.version.message").replace("<MyPlugin>", ChatColor.GOLD + ShulkerRespawner.THIS_NAME + ChatColor.WHITE) );
            plugin.coreUtils.sendJsonMessage(player, links);
            player.sendMessage(versions.replace("{nVers}", plugin.UCnewVers).replace("{oVers}", plugin.UColdVers));
        }

        if(player.getDisplayName().equals("JoelYahwehOfWar")||player.getDisplayName().equals("JoelGodOfWar")){
            player.sendMessage(ShulkerRespawner.THIS_NAME + " " + ShulkerRespawner.THIS_VERSION + " Hello father!");
        }
    }

    private String get(String string, String... defaultValue){
        return plugin.get(string, defaultValue);
    }
}
