package com.rteenagers.parrot.commands;

import com.rteenagers.parrot.DatabaseHandler;
import com.rteenagers.parrot.Utils;
import com.rteenagers.parrot.commands.manager.UsernotesCommand;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PointListCommand extends UsernotesCommand {
    @Override
    public String getName() {
        return "pointlist";
    }

    @Override
    public String getInfo() {
        return "/pointlist [bans/mutes] [page number]";
    }

    @Override
    public String permission() {
        return "points.lookup";
    }

    @Override
    public int getArgsCount() {
        return 2;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(args[0].equalsIgnoreCase("bans") || args[0].equalsIgnoreCase("mutes"))) { // Check if punishment input is valid
            sender.sendMessage(ChatColor.RED + "Please provide a valid punishment to remove as the first argument. Usage is " + getInfo());
            return;
        }

        if (!Utils.isInteger(args[1])) { // Check if page number is valid
            sender.sendMessage(ChatColor.RED + "Invalid syntax. Please provide a valid number as your second argument.");
            return;
        }

        String pageNumber = args[1];
        if (Integer.parseInt(pageNumber) < 1) {
            pageNumber = "1"; // Page 1 is the absolute minimum
        }

        TextComponent message1 = new TextComponent(ChatColor.AQUA + "" + ChatColor.BOLD + "-=- PAGE " + pageNumber);
        TextComponent message2 = new TextComponent(ChatColor.AQUA + "" + ChatColor.BOLD + " -=-");

        TextComponent backArrow = new TextComponent(" «");
        backArrow.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/pointlist " + args[0] + " " + (Integer.parseInt(pageNumber) - 1)));
        backArrow.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.RED + "Previous page")));

        TextComponent nextArrow = new TextComponent(" »");
        nextArrow.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/pointlist " + args[0] + " " + (Integer.parseInt(pageNumber) + 1)));
        nextArrow.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.RED + "Next page")));

        sender.spigot().sendMessage(message1, backArrow, nextArrow, message2);
        DatabaseHandler.getPointList(StringUtils.chop(args[0]), pageNumber, sender);

    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String alias, String[] args) {

        return switch (args.length) {
            case 1 -> Arrays.asList("bans", "mutes");
            case 2 -> Collections.singletonList("<page number>");
            default -> new ArrayList<>();
        };
    }
}
