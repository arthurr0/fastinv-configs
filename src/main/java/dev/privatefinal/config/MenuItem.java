package dev.privatefinal.config;

import eu.okaeri.configs.OkaeriConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MenuItem extends OkaeriConfig {

    private String material = "STONE";

    private String name = "";

    private List<String> lore = new ArrayList<>();

    private int amount = 1;

    private String patternChar = null;

    private List<Integer> slots = new ArrayList<>();

    private Map<String, Integer> enchants = new LinkedHashMap<>();

    private boolean glow = false;

    private Integer customModelData = null;

    private List<String> actions = new ArrayList<>();

    public MenuItem material(String material) {
        this.material = material;
        return this;
    }

    public MenuItem name(String name) {
        this.name = name;
        return this;
    }

    public MenuItem lore(String... lore) {
        this.lore = new ArrayList<>(Arrays.asList(lore));
        return this;
    }

    public MenuItem amount(int amount) {
        this.amount = amount;
        return this;
    }

    public MenuItem patternChar(char patternChar) {
        this.patternChar = String.valueOf(patternChar);
        return this;
    }

    public MenuItem slots(int... slots) {
        List<Integer> list = new ArrayList<>();
        for (int slot : slots) {
            list.add(slot);
        }
        this.slots = list;
        return this;
    }

    public MenuItem enchant(String enchantment, int level) {
        this.enchants.put(enchantment, level);
        return this;
    }

    public MenuItem glow() {
        this.glow = true;
        return this;
    }

    public MenuItem glow(boolean glow) {
        this.glow = glow;
        return this;
    }

    public MenuItem customModelData(int customModelData) {
        this.customModelData = customModelData;
        return this;
    }

    public MenuItem action(String... actions) {
        this.actions.addAll(Arrays.asList(actions));
        return this;
    }

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

    public String getPatternChar() {
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
