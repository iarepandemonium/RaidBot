package net.pandette.configuration;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

/**
 * This class is a code representation of the json file that stores the bot configuration
 */
@Data
public class BotConfiguration {

    @SerializedName("bot-token")
    private String botToken;

    @SerializedName("production")
    private Boolean production;

    @SerializedName("test-server-id")
    private String testServerId;

    @SerializedName("admin")
    private List<String> admins;

}
