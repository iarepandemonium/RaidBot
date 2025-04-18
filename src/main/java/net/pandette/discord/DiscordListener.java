package net.pandette.discord;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.internal.utils.PermissionUtil;
import net.pandette.App;
import net.pandette.configuration.PingData;
import net.pandette.configuration.ServerConfig;
import net.pandette.utils.Utility;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class DiscordListener extends ListenerAdapter {

    private static final List<String> HELP_COMMANDS = Arrays.asList("?", "help", "list");

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

        App.getJda().upsertCommand("setpingcheck", "Adds a ping check for specific data")
                .addOption(OptionType.ROLE, "ping", "The role this ping is created for", true)
                .addOption(OptionType.STRING, "data", "Comma separated list of things you wish to be regex ie: hello,how,are,you", false)
                .addOption(OptionType.BOOLEAN, "remove", "If true, this ping will be removed, false does nothing. (I do it this way because lazy.)", false)
                .addOption(OptionType.INTEGER, "count", "How many words in the list must be present before it will trigger the ping.", false)
                .queue();


        App.getJda().upsertCommand("raidconfig", "Sets the raid configuration channel")
                .addOption(OptionType.STRING, "type", "add-admin-channel, remove-admin-channel, add-ping-channel, remove-ping-channel or display", true)
                .addOption(OptionType.CHANNEL, "channelid", "Discord id of the channel", true)
                .queue();
    }

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
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

            String raidName = event.getOption("raid").getAsString().toLowerCase(Locale.ROOT);

            if (HELP_COMMANDS.contains(raidName)) {
                StringBuilder builder = new StringBuilder("Raid Info Available:\n");
                for (String s : config.getData().keySet()) {
                    builder.append(s).append("\n");
                }
                event.reply(builder.toString()).queue();
                return;
            }


            if (!config.getData().containsKey(raidName) || config.getData().get(raidName).isEmpty()) {
                event.reply("The raid specified does not have any data associated with it.").queue();
                return;
            }
            event.reply(raidName + "'s Raid Data: ").queue();

            for (String s : config.getData().get(raidName)) {
                event.getChannel().sendMessage(s).queue();
            }
        } else if (event.getName().equalsIgnoreCase("addraidinfo")) {
            if (config.getAdminChannel() == null || !config.getAdminChannel().contains(event.getChannel().getId())) {
                event.reply("This channel is not a channel to use raid admin commands!").queue();
                return;
            }

            String raidName = event.getOption("raid").getAsString().toLowerCase(Locale.ROOT);

            if (HELP_COMMANDS.contains(raidName)) {
                event.reply("This cannot be a raid name as it is reserved for helper functions.").queue();
                return;
            }


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


        } else if (event.getName().equalsIgnoreCase("removeraidinfo")) {
            if (config.getAdminChannel() == null || !config.getAdminChannel().contains(event.getChannel().getId())) {
                event.reply("This channel is not a channel to use raid admin commands!").queue();
                return;
            }

            String raidName = event.getOption("raid").getAsString().toLowerCase(Locale.ROOT);

            if (HELP_COMMANDS.contains(raidName)) {
                event.reply("This cannot be a raid name as it is reserved for helper functions.").queue();
                return;
            }

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

        } else if (event.getName().equalsIgnoreCase("setpingcheck")) {
            if (config.getAdminChannel() == null || !config.getAdminChannel().contains(event.getChannel().getId())) {
                event.reply("This channel is not a channel to use raid admin commands!").queue();
                return;
            }

            if (config.getPingData() == null) config.setPingData(new ArrayList<>());
            Role ping = event.getOption("ping").getAsRole();
            String[] data = null;
            Integer count = 1;

            if (event.getOption("data") != null) {
                data = event.getOption("data").getAsString().toLowerCase(Locale.ROOT).replace(", ", ",").replace(" ,", ",").split("[, ]");
            }

            if (event.getOption("count") != null) {
                count = event.getOption("count").getAsInt();
            }


            Boolean cancel = null;

            if (event.getOption("remove") != null) {
                cancel = event.getOption("remove").getAsBoolean();
            }

            if (cancel != null && cancel) {
                for (PingData d : new ArrayList<>(config.getPingData())) {
                    if (d.getPingRole().equalsIgnoreCase(ping.getId())) {
                        config.getPingData().remove(d);
                        event.reply("Ping data for " + ping.getName() + " has been removed.").queue();
                        try {
                            Utility.writeFile(filename, gson.toJson(config));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        return;
                    }
                }

                event.reply("Could not find any ping data for " + ping.getName()).queue();
                return;
            }

            PingData pingData = null;
            for (PingData d : config.getPingData()) {
                if (d.getPingRole().equalsIgnoreCase(ping.getId())) {
                    pingData = d;
                    break;
                }
            }

            if (pingData != null && data == null) {
                event.reply("The role " + pingData.getPingRole() + " currently will trigger for " + pingData.getWords()).queue();
                return;
            } else if (data == null) {
                event.reply("This ping doesn't currently exist & requires the data field to be filled to set it up.").queue();
                return;
            }


            if (pingData != null) config.getPingData().remove(pingData);
            pingData = new PingData(ping.getId(), Arrays.asList(data), count);
            config.getPingData().add(pingData);
            event.reply("A new ping was setup for " + ping.getName() + " for the following words " + pingData.getWords()).queue();
            try {
                Utility.writeFile(filename, gson.toJson(config));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if (event.getName().equalsIgnoreCase("raidconfig")) {
            if (event.getMember() == null) return;

            if (!PermissionUtil.checkPermission(event.getMember(), Permission.ADMINISTRATOR)) {
                event.reply("You need administrator in this server to make these changes").queue();
                return;
            }

            TextChannel channelid = event.getOption("channelid").getAsTextChannel();

            switch (event.getOption("type").getAsString().toUpperCase(Locale.ROOT)) {
                case "DISPLAY":
                    config.setOutputChannel(channelid.getId());
                    event.reply("Output channel set to " + channelid.getId()).queue();
                    ;
                    break;
                case "ADD-ADMIN-CHANNEL":
                    if (config.getAdminChannel() == null) config.setAdminChannel(new ArrayList<>());
                    if (!config.getAdminChannel().contains(channelid.getId())) {
                        config.getAdminChannel().add(channelid.getId());
                        event.reply("Admin channel added: " + channelid.getId()).queue();
                    } else {
                        event.reply("Admin channel already existed in the list!").queue();
                    }
                    break;
                case "REMOVE-ADMIN-CHANNEL":
                    if (config.getAdminChannel() == null) config.setAdminChannel(new ArrayList<>());

                    if (!config.getAdminChannel().contains(channelid.getId())) {
                        event.reply("Admin channel wasn't even in this list.").queue();
                    } else {
                        config.getAdminChannel().remove(channelid.getId());
                        event.reply("Admin channel removed: " + channelid.getId()).queue();
                    }
                    break;
                case "ADD-PING-CHANNEL":
                    if (config.getPingChannels() == null) config.setPingChannels(new ArrayList<>());
                    if (channelid == null) {
                        event.reply("The channelid needs to be a channel to get the id of the channel. If it is not, it will not work.").queue();
                        break;
                    }

                    if (!config.getPingChannels().contains(channelid.getId())) {
                        config.getPingChannels().add(channelid.getId());
                        event.reply("Ping channel added: " + channelid.getId()).queue();
                    } else {
                        event.reply("Ping channel already existed in the list!").queue();
                    }
                    break;
                case "REMOVE-PING-CHANNEL":
                    if (config.getPingChannels() == null) config.setPingChannels(new ArrayList<>());

                    if (!config.getPingChannels().contains(channelid.getId())) {
                        event.reply("Ping channel wasn't even in this list.").queue();
                    } else {
                        config.getPingChannels().remove(channelid.getId());
                        event.reply("Ping channel removed: " + channelid.getId()).queue();
                    }
                    break;
                default:
                    event.reply("add-admin-channel, remove-admin-channel, add-ping-channel, remove-ping-channel or display are the only options for this command.").queue();
                    return;
            }

            try {
                Utility.writeFile(filename, gson.toJson(config));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Gson gson = new Gson();
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

        if (config.getPingChannels() == null || !config.getPingChannels().contains(event.getChannel().getId())) return;


        String[] messageSplit = splitClean(event.getMessage().getContentRaw());
        List<String> messages = new ArrayList<>(Arrays.asList(messageSplit));
        for (MessageEmbed e : event.getMessage().getEmbeds()) {
            messages.addAll(Arrays.asList(splitClean(e.getTitle())));
            messages.addAll(Arrays.asList(splitClean(e.getDescription())));
            if (e.getAuthor() != null) {
                messages.addAll(Arrays.asList(splitClean(e.getAuthor().getName())));
            }
            for (MessageEmbed.Field field : e.getFields()) {
                messages.addAll(Arrays.asList(splitClean(field.getName())));
                messages.addAll(Arrays.asList(splitClean(field.getValue())));
            }
        }


        List<String> rolesToMention = new ArrayList<>();

        for (PingData data : new ArrayList<>(config.getPingData())) {
            List<String> words = new ArrayList<>();
            for (String s : messages) {
                for (String d : data.getWords()) {
                    if (words.contains(d)) continue;
                    String cleanAllCharacters = d.trim().toLowerCase(Locale.ROOT);
                    String clean = d.trim().toLowerCase(Locale.ROOT).replaceAll("[^A-Za-z0-9]", "");
                    String cleanWithDash = d.trim().toLowerCase(Locale.ROOT).replaceAll("[^A-Za-z0-9-]", "");

                    if (s.equals(cleanAllCharacters) || s.equals(clean) || s.equals(cleanWithDash)) {
                        words.add(d);
                        break;
                    }
                }
            }

            int num = 1;
            if (data.getCount() != null) num = data.getCount();

            if (words.size() >= num) {
                Role role = event.getGuild().getRoleById(data.getPingRole());
                if (role == null) {
                    config.getPingData().remove(data);
                    try {
                        Utility.writeFile(filename, gson.toJson(config));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    continue;
                }
                rolesToMention.add(data.getPingRole());
            }
        }

        if (rolesToMention.isEmpty()) return;

        StringBuilder builder = new StringBuilder();

        for (String r : rolesToMention) {
            builder.append("<@&").append(r).append("> ");
        }

        event.getChannel().sendMessage(builder.toString()).queue();

    }

    private String[] splitClean(String split) {
        if (split == null) return new String[]{};
        return split.replace("\n", " ").replace("/", " ").replace(":", " ").toLowerCase(Locale.ROOT).split(" ");
    }

}
