package net.pandette.configuration;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

@Data
public class PingData {
    @SerializedName("pingRole")
    private final String pingRole;

    @SerializedName("words")
    private final List<String> words;

    @SerializedName("count")
    private final Integer count;
}
