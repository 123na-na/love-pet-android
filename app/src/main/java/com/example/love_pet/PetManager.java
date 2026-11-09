package com.example.love_pet;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PetManager {

    private static PetManager instance;
    private static final String PREF_NAME = "pet_data";
    private static final String KEY_PETS = "pets";

    public static class Pet {
        public String id;
        public String name;
        public String type; // cat, dog
        public String breed;
        public String gender; // male, female
        public String avatarPath;
        public String age;
        public String weight;
        public String deviceName;
        public String[] tags;

        public Pet() {
        }
    }

    private PetManager() {
    }

    public static PetManager getInstance() {
        if (instance == null) {
            instance = new PetManager();
        }
        return instance;
    }

    private static String getPrefKey(Context context) {
        return PREF_NAME + "_" + UserManager.getInstance().getCurrentUserId();
    }

    public List<Pet> getPets(Context context) {
        List<Pet> pets = new ArrayList<>();
        if (context == null) return pets;

        SharedPreferences prefs = context.getSharedPreferences(getPrefKey(context), Context.MODE_PRIVATE);
        String jsonStr = prefs.getString(KEY_PETS, "");
        if (jsonStr.isEmpty()) return pets;

        try {
            JSONArray arr = new JSONArray(jsonStr);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                Pet pet = new Pet();
                pet.id = obj.optString("id", "");
                pet.name = obj.optString("name", "");
                pet.type = obj.optString("type", "");
                pet.breed = obj.optString("breed", "");
                pet.gender = obj.optString("gender", "");
                pet.avatarPath = obj.optString("avatarPath", "");
                pet.age = obj.optString("age", "");
                pet.weight = obj.optString("weight", "");
                pet.deviceName = obj.optString("deviceName", "");

                JSONArray tagsArr = obj.optJSONArray("tags");
                if (tagsArr != null) {
                    pet.tags = new String[tagsArr.length()];
                    for (int j = 0; j < tagsArr.length(); j++) {
                        pet.tags[j] = tagsArr.optString(j, "");
                    }
                }
                pets.add(pet);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pets;
    }

    public Pet getFirstPet(Context context) {
        List<Pet> pets = getPets(context);
        return pets.isEmpty() ? null : pets.get(0);
    }

    public Pet getPetById(Context context, String petId) {
        if (petId == null || petId.isEmpty()) return null;
        List<Pet> pets = getPets(context);
        for (Pet pet : pets) {
            if (petId.equals(pet.id)) {
                return pet;
            }
        }
        return null;
    }

    public void addPet(Context context, Pet pet) {
        if (context == null || pet == null) return;

        List<Pet> pets = getPets(context);
        if (pet.id == null || pet.id.isEmpty()) {
            pet.id = "pet_" + System.currentTimeMillis();
        }
        pets.add(pet);
        savePets(context, pets);
    }

    public void updatePet(Context context, Pet pet) {
        if (context == null || pet == null || pet.id == null) return;

        List<Pet> pets = getPets(context);
        for (int i = 0; i < pets.size(); i++) {
            if (pet.id.equals(pets.get(i).id)) {
                pets.set(i, pet);
                savePets(context, pets);
                return;
            }
        }
    }

    public void deletePet(Context context, String petId) {
        if (context == null || petId == null) return;

        List<Pet> pets = getPets(context);
        for (int i = 0; i < pets.size(); i++) {
            if (petId.equals(pets.get(i).id)) {
                Pet pet = pets.get(i);
                if (pet.avatarPath != null && !pet.avatarPath.isEmpty()) {
                    try {
                        java.io.File avatarFile = new java.io.File(pet.avatarPath);
                        if (avatarFile.exists()) {
                            avatarFile.delete();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                pets.remove(i);
                savePets(context, pets);
                return;
            }
        }
    }

    private void savePets(Context context, List<Pet> pets) {
        if (context == null) return;
        try {
            JSONArray arr = new JSONArray();
            for (Pet pet : pets) {
                JSONObject obj = new JSONObject();
                obj.put("id", pet.id);
                obj.put("name", pet.name);
                obj.put("type", pet.type);
                obj.put("breed", pet.breed);
                obj.put("gender", pet.gender);
                obj.put("avatarPath", pet.avatarPath);
                obj.put("age", pet.age);
                obj.put("weight", pet.weight);
                obj.put("deviceName", pet.deviceName);
                if (pet.tags != null) {
                    JSONArray tagsArr = new JSONArray();
                    for (String tag : pet.tags) {
                        tagsArr.put(tag);
                    }
                    obj.put("tags", tagsArr);
                }
                arr.put(obj);
            }
            SharedPreferences prefs = context.getSharedPreferences(getPrefKey(context), Context.MODE_PRIVATE);
            prefs.edit().putString(KEY_PETS, arr.toString()).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
