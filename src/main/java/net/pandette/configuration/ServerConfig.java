package net.pandette.configuration;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ServerConfig {

    @SerializedName("data")
    private Map<String, List<String>> data;
}
