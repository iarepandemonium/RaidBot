package net.pandette.configuration;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ServerConfig {

    @SerializedName("data")
    private Map<String, List<String>> data;

    @SerializedName("outputChannel")
    private String outputChannel;

    @SerializedName("adminChannel")
    private List<String> adminChannel;

    @SerializedName("pingChannels")
    private List<String> pingChannels;

    @SerializedName("pingData")
    private List<PingData> pingData;
}
