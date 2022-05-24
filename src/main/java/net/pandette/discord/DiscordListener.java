package net.pandette.discord;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.internal.utils.PermissionUtil;
import net.pandette.App;
import net.pandette.configuration.ServerConfig;
import net.pandette.utils.Utility;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class DiscordListener extends ListenerAdapter {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public DiscordListener() {
        App.getJda().upsertCommand("raidinfo", "Get info about a specific raid")
                .addOption(OptionType.STRING, "raid", "The name of the raid", true)
                .queue();
        App.getJda().upsertCommand("addraidinfo", "Add info to the raid info list")
                .addOption(OptionType.STRING, "raid", "The name of the raid", true)
                .addOption(OptionType.STRING, "data", "Data to add to the raid documentation", true)
                .queue();
        App.getJda().upsertCommand("removeraidinfo", "Remove info from the raid info list")
                .addOption(OptionType.STRING, "raid", "The name of the raid", true)
                .addOption(OptionType.INTEGER, "index", "Index between 0 & list length minus 1.", true)
                .queue();

        App.getJda().upsertCommand("raidconfig", "Sets the raid configuration channel")
                .addOption(OptionType.STRING, "type", "Admin or Display", true)
                .addOption(OptionType.STRING, "channelid", "Discord id of the channel", true)
                .queue();

    }

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
        System.out.println(event.getName());
        File configs = new File("configs");
        if (!configs.exists()) configs.mkdirs();
        String filename = "configs/" + event.getGuild().getId() + ".json";
        File f = new File(filename);
        ServerConfig config;
        if (!f.exists()) config = new ServerConfig();
        else {
            try {
                config = gson.fromJson(Utility.readFile(filename), ServerConfig.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (config.getData() == null) config.setData(new HashMap<>());

        if (event.getName().equalsIgnoreCase("raidinfo")) {
            if (config.getOutputChannel() == null || !event.getChannel().getId().equals(config.getOutputChannel())) {
                event.reply("This channel is not a channel used to display raid info.").queue();
                return;
            }

            String raidName = event.getOption("raid").getAsString();
            if (!config.getData().containsKey(raidName) || config.getData().get(raidName).isEmpty()) {
                event.reply("The raid specified does not have any data associated with it.").queue();
                return;
            }
            event.reply(raidName + "'s Raid Data: ").queue();

            for (String s : config.getData().get(raidName)) {
                event.getChannel().sendMessage(s).queue();
            }
            return;
        }

        if (event.getName().equalsIgnoreCase("addraidinfo")) {
            if (config.getAdminChannel() == null || !event.getChannel().getId().equals(config.getAdminChannel())) {
                event.reply("This channel is not a channel to use raid admin commands!").queue();
                return;
            }

            String raidName = event.getOption("raid").getAsString();
            String data = event.getOption("data").getAsString();
            List<String> dataList = config.getData().getOrDefault(raidName, new ArrayList<>());
            dataList.add(data);
            config.getData().put(raidName, dataList);
            event.reply(raidName + " has been updated with new data.").queue();
            try {
                Utility.writeFile(filename, gson.toJson(config));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return;
        }

        if (event.getName().equalsIgnoreCase("removeraidinfo")) {
            if (config.getAdminChannel() == null || !event.getChannel().getId().equals(config.getAdminChannel())) {
                event.reply("This channel is not a channel to use raid admin commands!").queue();
                return;
            }

            String raidName = event.getOption("raid").getAsString();
            int index = event.getOption("index").getAsInt();
            List<String> dataList = config.getData().getOrDefault(raidName, new ArrayList<>());
            if (index >= dataList.size() || index < 0) {
                event.getHook().sendMessage("The index has to be between 0 and the list size-1. The first picture/info will be listed as 0.").queue();
                return;
            }
            dataList.remove(index);
            config.getData().put(raidName, dataList);
            event.reply((raidName + " no longer has the item at index " + index)).queue();
            try {
                Utility.writeFile(filename, gson.toJson(config));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return;
        }

        if (event.getName().equalsIgnoreCase("raidconfig")) {
            if (event.getMember() == null) return;

            if (!PermissionUtil.checkPermission(event.getMember(), Permission.ADMINISTRATOR)) {
                event.reply("You need administrator in this server to make these changes").queue();
                return;
            }

            String channelid = event.getOption("channelid").getAsString();

            switch (event.getOption("type").getAsString().toUpperCase(Locale.ROOT)) {
                case "DISPLAY":
                    config.setOutputChannel(channelid);
                    event.reply("Output channel set to " + channelid).queue();
                    ;
                    break;
                case "ADMIN":
                    config.setAdminChannel(channelid);
                    event.reply("Admin channel set to " + channelid).queue();
                    break;
                default:
                    event.reply("Admin & Display are the only options for this command.").queue();
                    return;
            }

            try {
                Utility.writeFile(filename, gson.toJson(config));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }


    }

}
