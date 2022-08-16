package net.pandette.configuration;

import lombok.Data;

import java.util.List;

@Data
public class PingData {
    private final String pingRole;
    private final List<String> words;
}
