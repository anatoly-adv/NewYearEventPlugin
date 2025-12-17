package com.yourname.newyearevent.commands;

import com.yourname.newyearevent.NewYearEventPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class GiftCommand implements CommandExecutor {
    
    private final NewYearEventPlugin plugin;
    
    // CustomModelData ID
    private static final int SNOWFLAKE_ID = 1;      // snowball
    private static final int MYSTERY_GIFT_ID = 1;   // paper
    private static final int WINTER_ELYTRA_ID = 2;  // elytra
    private static final int FROZEN_APPLE_ID = 2;   // golden_apple
    
    public GiftCommand(NewYearEventPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("newyear.admin.gift")) {
            sender.sendMessage("§c❌ У вас нет прав на использование этой команды!");
            return true;
        }
        
        // /newyear_gift <игрок> fulfil
        // /newyear_gift <игрок> custom <материал> <количество> [custom_model_data]
        // /newyear_gift <игрок> preset <тип>
        
        if (args.length < 2) {
            sender.sendMessage("§c❌ Использование:");
            sender.sendMessage("§e/newyear_gift <игрок> fulfil §7- выдать подарки из письма");
            sender.sendMessage("§e/newyear_gift <игрок> custom <материал> <кол-во> [cmd] §7- кастомный подарок");
            sender.sendMessage("§e/newyear_gift <игрок> preset <тип> §7- новогодний предмет");
            sender.sendMessage("§7Типы: snowflake, gift, elytra, apple");
            return true;
        }
        
        String playerName = args[0];
        String action = args[1];
        
        Player target = Bukkit.getPlayer(playerName);
        if (target == null || !target.isOnline()) {
            sender.sendMessage("§c❌ Игрок §e" + playerName + " §cне в сети!");
            return true;
        }
        
        if (action.equalsIgnoreCase("fulfil")) {
            fulfilLetter(sender, target);
            return true;
        }
        
        if (action.equalsIgnoreCase("custom")) {
            if (args.length < 4) {
                sender.sendMessage("§c❌ Использование: /newyear_gift <игрок> custom <материал> <кол-во> [custom_model_data]");
                return true;
            }
            
            String materialName = args[2].toUpperCase();
            int amount;
            
            try {
                amount = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                sender.sendMessage("§c❌ Неверное количество: " + args[3]);
                return true;
            }
            
            // Опциональный custom_model_data
            Integer customModelData = null;
            if (args.length >= 5) {
                try {
                    customModelData = Integer.parseInt(args[4]);
                } catch (NumberFormatException e) {
                    sender.sendMessage("§c❌ Неверный custom_model_data: " + args[4]);
                    return true;
                }
            }
            
            giveCustomGift(sender, target, materialName, amount, customModelData);
            return true;
        }
        
        if (action.equalsIgnoreCase("preset")) {
            if (args.length < 3) {
                sender.sendMessage("§c❌ Использование: /newyear_gift <игрок> preset <тип>");
                sender.sendMessage("§7Типы: snowflake, gift, elytra, apple");
                return true;
            }
            
            String presetType = args[2].toLowerCase();
            givePresetGift(sender, target, presetType);
            return true;
        }
        
        sender.sendMessage("§c❌ Неизвестное действие: " + action);
        sender.sendMessage("§7Используйте: §efulfil§7, §ecustom §7или §epreset");
        return true;
    }
    
    /**
     * Выдаёт заготовленный новогодний предмет
     */
    private void givePresetGift(CommandSender sender, Player target, String type) {
        ItemStack item = null;
        
        switch (type) {
            case "snowflake":
                item = createCustomSnowflake();
                break;
            case "gift":
                item = createCustomMysteryGift();
                break;
            case "elytra":
                item = createCustomWinterElytra();
                break;
            case "apple":
                item = createCustomFrozenApple();
                break;
            default:
                sender.sendMessage("§c❌ Неизвестный тип: " + type);
                sender.sendMessage("§7Доступные типы: snowflake, gift, elytra, apple");
                return;
        }
        
        target.getInventory().addItem(item);
        
        sender.sendMessage("§a✓ Выдан новогодний предмет §e" + type + " §aигроку §e" + target.getName());
        target.sendMessage("§6╔═══════════════════════════════════════╗");
        target.sendMessage("§6║  §e§l🎁 ПОДАРОК ОТ ДЕДА МОРОЗА! §6    ║");
        target.sendMessage("§6╚═══════════════════════════════════════╝");
        target.sendMessage("");
        target.sendMessage("§aВы получили новогодний предмет!");
        target.sendMessage("");
        
        target.playSound(target.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        plugin.getFireworkManager().launchFireworkAbovePlayer(target);
    }
    
    /**
     * Создает декоративную снежинку
     */
    private ItemStack createCustomSnowflake() {
        ItemStack item = new ItemStack(Material.SNOWBALL, 3);
        ItemMeta meta = item.getItemMeta();
        
        meta.setCustomModelData(SNOWFLAKE_ID);
        
        meta.displayName(Component.text("❄ Декоративная снежинка", NamedTextColor.AQUA)
                .decoration(TextDecoration.ITALIC, false)
                .decoration(TextDecoration.BOLD, true));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text(""));
        lore.add(Component.text("Красивая снежинка", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("для украшения", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        
        meta.lore(lore);
        meta.setEnchantmentGlintOverride(true);
        
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Создает загадочный подарок
     */
    private ItemStack createCustomMysteryGift() {
        ItemStack item = new ItemStack(Material.PAPER, 2);
        ItemMeta meta = item.getItemMeta();
        
        meta.setCustomModelData(MYSTERY_GIFT_ID);
        
        meta.displayName(Component.text("🎁 Загадочный подарок", NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false)
                .decoration(TextDecoration.BOLD, true));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text(""));
        lore.add(Component.text("Декоративный подарок", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Что же внутри?", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        
        meta.lore(lore);
        meta.setEnchantmentGlintOverride(true);
        
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Создает зимние элитры
     */
    private ItemStack createCustomWinterElytra() {
        ItemStack item = new ItemStack(Material.ELYTRA, 1);
        ItemMeta meta = item.getItemMeta();
        
        meta.setCustomModelData(WINTER_ELYTRA_ID);
        
        meta.displayName(Component.text("🦅 Зимние крылья", NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, false)
                .decoration(TextDecoration.BOLD, true));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text(""));
        lore.add(Component.text("Элитры с зимней текстурой", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Полностью рабочие!", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        
        meta.lore(lore);
        meta.setEnchantmentGlintOverride(true);
        
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Создает замороженное золотое яблоко
     */
    private ItemStack createCustomFrozenApple() {
        ItemStack item = new ItemStack(Material.GOLDEN_APPLE, 2);
        ItemMeta meta = item.getItemMeta();
        
        meta.setCustomModelData(FROZEN_APPLE_ID);
        
        meta.displayName(Component.text("🍎 Замороженное золотое яблоко", NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false)
                .decoration(TextDecoration.BOLD, true));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text(""));
        lore.add(Component.text("Золотое яблоко покрытое льдом", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Сохраняет все свойства!", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        
        meta.lore(lore);
        meta.setEnchantmentGlintOverride(true);
        
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Выдаёт подарки из письма с предметами
     */
    private void fulfilLetter(CommandSender sender, Player target) {
        File lettersFolder = plugin.getLettersFolder();
        File blockFolder = new File(lettersFolder, "block");
        
        File letterFile = findLetterFile(blockFolder, target.getName());
        
        if (letterFile == null) {
            sender.sendMessage("§c❌ Блочное письмо от §e" + target.getName() + " §cне найдено!");
            sender.sendMessage("§7Возможно игрок отправил текстовое письмо. Используйте §epreset §7или §ecustom §7для выдачи подарка.");
            return;
        }
        
        List<ItemStack> items = new ArrayList<>();
        
        try {
            List<String> lines = Files.readAllLines(letterFile.toPath(), StandardCharsets.UTF_8);
            
            for (String line : lines) {
                if (!line.contains("Материал:")) continue;
                
                String materialName = line.split(":")[1].trim().toUpperCase();
                
                int lineIndex = lines.indexOf(line);
                if (lineIndex + 1 < lines.size()) {
                    String quantityLine = lines.get(lineIndex + 1);
                    if (quantityLine.contains("Количество:")) {
                        String[] parts = quantityLine.split(":");
                        if (parts.length > 1) {
                            String countStr = parts[1].trim().split(" ")[0];
                            int count = Integer.parseInt(countStr);
                            
                            Material material = Material.getMaterial(materialName);
                            if (material != null) {
                                items.add(new ItemStack(material, count));
                            } else {
                                sender.sendMessage("§c⚠ Пропущен неизвестный материал: " + materialName);
                            }
                        }
                    }
                }
            }
            
        } catch (IOException e) {
            sender.sendMessage("§c❌ Ошибка чтения письма: " + e.getMessage());
            return;
        } catch (Exception e) {
            sender.sendMessage("§c❌ Ошибка парсинга письма: " + e.getMessage());
            return;
        }
        
        if (items.isEmpty()) {
            sender.sendMessage("§c❌ В письме не найдено предметов для выдачи!");
            return;
        }
        
        for (ItemStack item : items) {
            target.getInventory().addItem(item);
        }
        
        File fulfilledFile = new File(letterFile.getParent(), "FULFILLED_" + letterFile.getName());
        letterFile.renameTo(fulfilledFile);
        
        sender.sendMessage("§a✓ Выдано §e" + items.size() + " §aтипов предметов игроку §e" + target.getName());
        target.sendMessage("§6╔═══════════════════════════════════════╗");
        target.sendMessage("§6║  §e§l🎁 ПОДАРОК ОТ ДЕДА МОРОЗА! §6    ║");
        target.sendMessage("§6╚═══════════════════════════════════════╝");
        target.sendMessage("");
        target.sendMessage("§aВаше письмо было прочитано!");
        target.sendMessage("§aПодарки добавлены в инвентарь! §6🎁");
        target.sendMessage("");
        
        target.playSound(target.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        plugin.getFireworkManager().launchFireworkAbovePlayer(target);
    }
    
    /**
     * Выдаёт кастомный подарок
     */
    private void giveCustomGift(CommandSender sender, Player target, String materialName, int amount, Integer customModelData) {
        Material material = Material.getMaterial(materialName);
        
        if (material == null) {
            sender.sendMessage("§c❌ Неизвестный материал: " + materialName);
            sender.sendMessage("§7Примеры: DIAMOND, EMERALD, NETHERITE_INGOT");
            return;
        }
        
        if (amount <= 0 || amount > 6400) {
            sender.sendMessage("§c❌ Количество должно быть от 1 до 6400");
            return;
        }
        
        ItemStack item = new ItemStack(material, amount);
        
        // Если указан CustomModelData
        if (customModelData != null) {
            ItemMeta meta = item.getItemMeta();
            meta.setCustomModelData(customModelData);
            item.setItemMeta(meta);
            sender.sendMessage("§7CustomModelData установлен: " + customModelData);
        }
        
        target.getInventory().addItem(item);
        
        String cmdInfo = customModelData != null ? " (CMD: " + customModelData + ")" : "";
        sender.sendMessage("§a✓ Выдан подарок игроку §e" + target.getName() + "§a: §f" + materialName + " x" + amount + cmdInfo);
        target.sendMessage("§6╔═══════════════════════════════════════╗");
        target.sendMessage("§6║  §e§l🎁 ПОДАРОК ОТ ДЕДА МОРОЗА! §6    ║");
        target.sendMessage("§6╚═══════════════════════════════════════╝");
        target.sendMessage("");
        target.sendMessage("§aВы получили специальный подарок:");
        target.sendMessage("§f  " + materialName + " §7x§e" + amount);
        target.sendMessage("");
        
        target.playSound(target.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        plugin.getFireworkManager().launchFireworkAbovePlayer(target);
    }
    
    /**
     * Находит файл письма по имени игрока
     */
    private File findLetterFile(File folder, String playerName) {
        if (!folder.exists()) {
            return null;
        }
        
        File[] files = folder.listFiles((dir, name) -> 
            name.toLowerCase().startsWith(playerName.toLowerCase() + "_") && 
            name.endsWith(".txt") &&
            !name.startsWith("FULFILLED_")
        );
        
        return (files != null && files.length > 0) ? files[0] : null;
    }
}
