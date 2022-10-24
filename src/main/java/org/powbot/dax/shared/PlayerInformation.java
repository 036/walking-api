package org.powbot.dax.shared;

import org.powbot.api.rt4.*;
import org.powbot.api.rt4.walking.model.Skill;
import org.powbot.dax.shared.json.JSONObject;
import org.powbot.dax.shared.json.JSONValue;

import java.util.HashMap;
import java.util.List;


public class PlayerInformation {

    private static final int[] SETTINGS = {176, 32, 71, 273, 144, 63, 179, 145, 68, 655, 10, 964, 399, 869, 314, 794,
            440, 622, 131, 335, 299, 896, 671, 810, 17, 11, 347, 302, 111, 116, 482, 307, 165, 150, 425, 365};
    private static final int[] VARBITS = {5087, 5088, 5089, 5090}; //shared with settings because of terrible hindsight. cross fingers that nothing cross paths.

    private Integer combatLevel;
    private HashMap<String, Integer> skills;
    private HashMap<Integer, Integer> inventoryItems, equipItems, gameSettings;
    private boolean member;

    public static PlayerInformation generatePlayerInformation(){
        try {
            Player rsPlayer = Players.local();
            return new PlayerInformation(
                    Worlds.isCurrentWorldMembers(),
                    rsPlayer.getCombatLevel(),
                    Skill.values(),
                    grabSettings(),
                    Inventory.stream().list(),
                    Equipment.stream().list());
        } catch (Exception e){
            System.out.println("No Player Information to grab.");
            return null;
        }
    }

    private static HashMap<Integer, Integer> grabSettings(){
        HashMap<Integer, Integer> settingsMap = new HashMap<>();
        for (int setting : SETTINGS){
            settingsMap.put(setting, Varpbits.varpbit(setting));
        }
        for (int varbit : VARBITS){
            settingsMap.put(varbit, Varpbits.value(varbit));
        }
        return settingsMap;
    }

    private static HashMap<String, Integer> skillsConversion(Skill[] skills){
        HashMap<String, Integer> skillsMap = new HashMap<>();
        for (Skill skill : skills){
            skillsMap.put(skill.toString(), Skills.realLevel(skill));
        }
        return skillsMap;
    }

    private static HashMap<Integer, Integer> rsItemsConversion(List<Item> items){
        HashMap<Integer, Integer> itemsMap = new HashMap<>();
        for (Item item : items){
            if (itemsMap.containsKey(item.id())){
                itemsMap.put(item.id(), itemsMap.get(item.id()) + item.getStack());
            } else {
                itemsMap.put(item.id(), item.getStack());
            }
        }
        return itemsMap;
    }

    private PlayerInformation(boolean member, int combatLevel, Skill[] skills, HashMap<Integer, Integer> gameSettings, List<Item> inventoryItems, List<Item> equipItems){
        this.member = member;
        this.combatLevel = combatLevel;
        this.skills = skillsConversion(skills);
        this.gameSettings = gameSettings;
        this.inventoryItems = rsItemsConversion(inventoryItems);
        this.equipItems = rsItemsConversion(equipItems);
    }

    private PlayerInformation(boolean member, int combatLevel, HashMap<String, Integer> skills, HashMap<Integer, Integer> gameSettings, HashMap<Integer, Integer> inventoryItems, HashMap<Integer, Integer> equipItems){
        this.member = member;
        this.combatLevel = combatLevel;
        this.skills = skills;
        this.gameSettings = gameSettings;
        this.inventoryItems = inventoryItems;
        this.equipItems = equipItems;
    }

    public int getCombatLevel(){
        return combatLevel;
    }

    public HashMap<String, Integer> getSkills() {
        return skills;
    }

    public HashMap<Integer, Integer> getInventoryItems() {
        return inventoryItems;
    }

    public HashMap<Integer, Integer> getEquipItems() {
        return equipItems;
    }

    public boolean haveItem(int amount, int id){
        if (inventoryItems == null){
            return false;
        }
        Object itemAmount = inventoryItems.get(id);
        return itemAmount != null && Integer.parseInt(itemAmount.toString()) >= amount;
    }

    public boolean skillRequirement(Skill skill, int level){
        if (skills == null){
            return false;
        }
        Object skillLevel = skills.get(skill.toString());
        return skillLevel != null && Integer.parseInt(skillLevel.toString()) >= level;
    }

    public boolean haveEquipItem(int amount, int id){
        if (equipItems == null){
            return false;
        }
        Object equipAmount = equipItems.get(id);
        return equipAmount != null && Integer.parseInt(equipAmount.toString()) >= amount;
    }

    public boolean isMember(){
        return member;
    }

    public JSONObject toJSON(){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("isMember", member);
        jsonObject.put("combatLevel", combatLevel);
        if (skills != null) {
            jsonObject.put("skills", new JSONObject(skills));
        }
        if (gameSettings != null) {
            jsonObject.put("settings", new JSONObject(gameSettings));
        }
        if (inventoryItems != null) {
            jsonObject.put("inventoryItems", new JSONObject(inventoryItems));
        }
        if (equipItems != null) {
            jsonObject.put("equipItems", new JSONObject(equipItems));
        }
        return jsonObject;
    }

    public static PlayerInformation fromJSONString(Object object){
        if (object == null){
            return null;
        }
        try {
            String jsonString = object.toString();
            JSONObject jsonObject = (JSONObject) JSONValue.parseWithException(jsonString);
            boolean member = (boolean) jsonObject.get("isMember");
            int combatLevel = Integer.parseInt(jsonObject.get("combatLevel").toString());
            HashMap<String, Number> skills = (HashMap<String, Number>) jsonObject.get("skills");
            HashMap<String, Number> gameSettings = (HashMap<String, Number>) jsonObject.get("settings");
            HashMap<String, Number> inventoryItems = (HashMap<String, Number>) jsonObject.get("inventoryItems");
            HashMap<String, Number> equipItems = (HashMap<String, Number>) jsonObject.get("equipItems");

            HashMap<String, Integer> skillsParsed = new HashMap<>();
            for (String key : skills.keySet()){
                skillsParsed.put(key, skills.get(key).intValue());
            }
            return new PlayerInformation(
                    member,
                    combatLevel,
                    skillsParsed,
                    intIntHashMapConversion(gameSettings),
                    intIntHashMapConversion(inventoryItems),
                    intIntHashMapConversion(equipItems));
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    private static <V, T> HashMap<Integer, Integer> intIntHashMapConversion(HashMap<V, T> map){
        HashMap<Integer, Integer> newMap = new HashMap<>();
        for (V key : map.keySet()){
            newMap.put(Integer.parseInt(key.toString()), Long.valueOf(map.get(key).toString()).intValue());
        }
        return newMap;
    }

    @Override
    public String toString(){
        return toJSON().toString();
    }

}
