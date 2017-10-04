package us.cyrien.minecordbot.chat.listeners.mcListeners;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.TextChannel;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import us.cyrien.minecordbot.Minecordbot;

import java.awt.*;

public class CommandListener extends MCBListener {

    public CommandListener(Minecordbot mcb) {
        super(mcb);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerCommand(PlayerCommandPreprocessEvent e) {
        boolean seeCommands = configsManager.getModChannelConfig().getBoolean("See_Commands");
        CommandSender s = e.getPlayer();
        String modChannel = configsManager.getModChannelConfig().getString("Mod_TextChannel");
        TextChannel modTextChannel = StringUtils.isEmpty(modChannel) ? null : mcb.getBot().getJda().getTextChannelById(modChannel);
        String msg = "**" + ChatColor.stripColor(s.getName()) + "**: " + e.getMessage();
        if (modChannel != null && seeCommands) {
            EmbedBuilder eb = new EmbedBuilder().setColor(new Color(60, 92, 243));
            eb.addField("Command-Event", langMessageParser.parsePlayer(msg, ChatColor.stripColor(s.getName())), false);
            messenger.sendMessageEmbedToDiscord(modTextChannel, eb.build());
        }
    }
}
