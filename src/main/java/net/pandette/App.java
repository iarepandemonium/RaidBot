package net.pandette;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.pandette.configuration.BotConfiguration;
import net.pandette.discord.DiscordListener;
import net.pandette.utils.Utility;

import java.io.File;
import java.io.IOException;

/**
 * This is the entry point for the bot.
 */
public class App {

    private static final String BOT_CONFIGURATION_FILE_NAME = "BotConfiguration.json";

    @Getter
    private static JDA jda;

    @Getter
    private static BotConfiguration configuration;

    private static Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /**
     * The main method which initializes the bot
     * @param args - command line args.
     */
    public static void main(String[] args) {
        System.out.println("Karma Bot is starting up!");

        File configFile = new File(BOT_CONFIGURATION_FILE_NAME);
        if (!configFile.exists()) {
            System.out.println("The configuration file is missing, please create it: " + BOT_CONFIGURATION_FILE_NAME);
            System.exit(1);
        }

        try {
            configuration = GSON.fromJson(Utility.readFile(BOT_CONFIGURATION_FILE_NAME), BotConfiguration.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (configuration.getBotToken() == null) {
            System.out.println("The Bot Token is not present in the configuration file! Exiting.");
            System.exit(1);
        }

        jda = createJDA();
    }

    private static JDA createJDA() {
        jda = JDABuilder
                .createDefault(configuration.getBotToken())
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .enableIntents(GatewayIntent.GUILD_MEMBERS)

                .setChunkingFilter(ChunkingFilter.ALL)
                .build();
        jda.addEventListener(new DiscordListener());
        return jda;
    }
}
