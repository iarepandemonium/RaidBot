package net.pandette.discord;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.pandette.App;
import net.pandette.configuration.ServerConfig;
import net.pandette.utils.Utility;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

    }


//    @Override
//    public void onReady(@Nonnull ReadyEvent event) {
//        for (Guild g : event.getJDA().getGuilds()) {
//            applyCommand(g);
//        }
//    }
//
//    private void applyCommand(Guild g) {
//        g.upsertCommand("raidinfo", "Get info about a specific raid")
//                .addOption(OptionType.STRING, "raid", "The name of the raid", true)
//                .queue();
//        g.upsertCommand("addraidinfo", "Add info to the raid info list")
//                .addOption(OptionType.STRING, "raid", "The name of the raid", true)
//                .addOption(OptionType.STRING, "data", "Data to add to the raid documentation", true)
//                .queue();
//        g.upsertCommand("removeraidinfo", "Remove info from the raid info list")
//                .addOption(OptionType.STRING, "raid", "The name of the raid", true)
//                .addOption(OptionType.INTEGER, "index", "Index between 0 & list length minus 1.", true)
//                .queue();
//    }

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



    }

}
