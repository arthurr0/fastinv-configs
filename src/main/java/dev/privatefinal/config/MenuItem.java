package dev.privatefinal.config;

import eu.okaeri.configs.OkaeriConfig;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MenuItem extends OkaeriConfig {

    private String material = "STONE";

    private String name = "";

    private List<String> lore = new ArrayList<>();

    private int amount = 1;

    private Character patternChar = null;

    private List<Integer> slots = new ArrayList<>();

    private Map<String, Integer> enchants = new LinkedHashMap<>();

    private boolean glow = false;

    private Integer customModelData = null;

    private List<String> actions = new ArrayList<>();

    public String getMaterial() {
        return this.material;
    }

    public String getName() {
        return this.name;
    }

    public List<String> getLore() {
        return this.lore;
    }

    public int getAmount() {
        return this.amount;
    }

    public Character getPatternChar() {
        return this.patternChar;
    }

    public List<Integer> getSlots() {
        return this.slots;
    }

    public Map<String, Integer> getEnchants() {
        return this.enchants;
    }

    public boolean isGlow() {
        return this.glow;
    }

    public Integer getCustomModelData() {
        return this.customModelData;
    }

    public List<String> getActions() {
        return this.actions;
    }
}
