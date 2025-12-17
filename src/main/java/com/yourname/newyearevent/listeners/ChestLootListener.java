package com.yourname.newyearevent.listeners;

import com.yourname.newyearevent.NewYearEventPlugin;
import com.yourname.newyearevent.managers.CurrencyManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

/**
 * Слушатель для добавления снежинок и кастомных предметов в сундуки
 */
public class ChestLootListener implements Listener {
    
    private final NewYearEventPlugin plugin;
    private final CurrencyManager currencyManager;
    private final Logger logger;
    private final Random random;
    
    private static final String SNOWFLAKE_MARKER = "§8SNOWFLAKE_CURRENCY_ITEM";
    
    // CustomModelData ID
    private static final int SNOWFLAKE_ID = 1;      // snowball
    private static final int MYSTERY_GIFT_ID = 1;   // paper
    private static final int WINTER_ELYTRA_ID = 2;  // elytra
    private static final int FROZEN_APPLE_ID = 2;   // golden_apple
    
    private BukkitTask scanTask;
    
    public ChestLootListener(NewYearEventPlugin plugin, CurrencyManager currencyManager) {
        this.plugin = plugin;
        this.currencyManager = currencyManager;
        this.logger = plugin.getLogger();
        this.random = new Random();
        
        startInventoryScan();
        
        logger.info("📦 ChestLootListener инициализирован (кастомные предметы)");
    }
    
    /**
     * Добавляет снежинки и кастомные предметы в лут натуральных сундуков
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onLootGenerate(LootGenerateEvent event) {
        if (!plugin.isEventActive()) {
            return;
        }
        
        // Добавляем только в натуральные сундуки/вагонетки
        if (event.getLootContext().getLootedEntity() == null) {
            // ВСЕГДА добавляем снежинки (10-25 штук)
            int snowflakeAmount = 10 + random.nextInt(16);
            ItemStack snowflake = createSnowflakeItem(snowflakeAmount);
            event.getLoot().add(snowflake);
            
            logger.info("📦 Добавлена снежинка в лут: " + snowflakeAmount + "❄");
            
            // РЕДКО добавляем кастомные предметы (15% шанс)
            if (random.nextDouble() < 0.15) {
                ItemStack customItem = getRandomCustomItem();
                if (customItem != null) {
                    event.getLoot().add(customItem);
                    logger.info("🎁 Добавлен кастомный предмет в лут");
                }
            }
        }
    }
    
    /**
     * Получает случайный кастомный предмет
     */
    private ItemStack getRandomCustomItem() {
        int roll = random.nextInt(100);
        
        if (roll < 40) {
            // 40% - Загадочный подарок (paper)
            return createCustomMysteryGift();
        } else if (roll < 70) {
            // 30% - Замороженное яблоко (golden_apple)
            return createCustomFrozenApple();
        } else if (roll < 90) {
            // 20% - Декоративная снежинка (snowball)
            return createCustomSnowflake();
        } else {
            // 10% - Зимние элитры (elytra)
            return createCustomWinterElytra();
        }
    }
    
    /**
     * Создает декоративную снежинку
     */
    private ItemStack createCustomSnowflake() {
        ItemStack item = new ItemStack(Material.SNOWBALL, 1);
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
        lore.add(Component.text(""));
        lore.add(Component.text("⚠ Требуется ресурспак!", NamedTextColor.YELLOW)
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
        ItemStack item = new ItemStack(Material.PAPER, 1);
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
        lore.add(Component.text(""));
        lore.add(Component.text("⚠ Требуется ресурспак!", NamedTextColor.YELLOW)
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
        lore.add(Component.text(""));
        lore.add(Component.text("⚠ Требуется ресурспак!", NamedTextColor.YELLOW)
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
        ItemStack item = new ItemStack(Material.GOLDEN_APPLE, 1);
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
        lore.add(Component.text(""));
        lore.add(Component.text("⚠ Требуется ресурспак!", NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false));
        
        meta.lore(lore);
        meta.setEnchantmentGlintOverride(true);
        
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Создает предмет снежинки-валюты с кастомной текстурой
     */
    private ItemStack createSnowflakeItem(int amount) {
        ItemStack item = new ItemStack(Material.SNOWBALL, 1);
        ItemMeta meta = item.getItemMeta();
        
        meta.setCustomModelData(SNOWFLAKE_ID);
        
        meta.displayName(
            Component.text("❄ Снежинки", NamedTextColor.AQUA)
                .decoration(TextDecoration.ITALIC, false)
                .decoration(TextDecoration.BOLD, true)
        );
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Количество: " + amount + " ❄", NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Новогодняя валюта", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text(""));
        lore.add(Component.text("§8SNOWFLAKE_CURRENCY_ITEM")
                .decoration(TextDecoration.ITALIC, false));
        
        meta.lore(lore);
        meta.setEnchantmentGlintOverride(true);
        
        item.setItemMeta(meta);
        return item;
    }
    
    private void startInventoryScan() {
        scanTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                scanPlayerInventory(player);
            }
        }, 5L, 5L);
    }
    
    private void scanPlayerInventory(Player player) {
        ItemStack[] contents = player.getInventory().getContents();
        
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            
            if (item != null && isSnowflakeItem(item)) {
                int amount = getSnowflakeAmount(item);
                
                if (amount > 0) {
                    if (plugin.isEventActive()) {
                        currencyManager.addCurrency(player, amount);
                        player.getInventory().setItem(i, null);
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
                        player.sendActionBar(Component.text("§b+ " + amount + " ❄", NamedTextColor.AQUA));
                        logger.info("💰 " + player.getName() + " получил " + amount + " снежинок из предмета");
                    } else {
                        ItemStack mendingBook = createMendingBook();
                        player.getInventory().setItem(i, mendingBook);
                        player.sendMessage(Component.text("❄ Событие закончилось! Снежинка превратилась в книгу починки.", NamedTextColor.YELLOW));
                        player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.0f);
                        logger.info("📚 " + player.getName() + " получил книгу починки (событие закончилось)");
                    }
                }
            }
        }
    }
    
    private boolean isSnowflakeItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        
        if (meta.hasLore()) {
            List<Component> lore = meta.lore();
            if (lore != null) {
                for (Component line : lore) {
                    String plainText = ((net.kyori.adventure.text.TextComponent) line).content();
                    if (plainText.contains("SNOWFLAKE_CURRENCY_ITEM")) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    private int getSnowflakeAmount(ItemStack item) {
        if (!item.hasItemMeta()) {
            return 0;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasLore()) {
            return 0;
        }
        
        List<Component> lore = meta.lore();
        if (lore == null || lore.isEmpty()) {
            return 0;
        }
        
        for (Component line : lore) {
            String plainText = ((net.kyori.adventure.text.TextComponent) line).content();
            plainText = plainText.replaceAll("§.", "");
            
            if (plainText.contains("Количество:")) {
                String[] parts = plainText.split("\\s+");
                
                for (String part : parts) {
                    try {
                        String digits = part.replaceAll("[^0-9]", "");
                        if (!digits.isEmpty()) {
                            return Integer.parseInt(digits);
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
        
        return 0;
    }
    
    private ItemStack createMendingBook() {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) book.getItemMeta();
        meta.addStoredEnchant(Enchantment.MENDING, 1, true);
        book.setItemMeta(meta);
        return book;
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        
        if (clickedItem != null && isSnowflakeItem(clickedItem)) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                scanPlayerInventory(player);
            }, 2L);
        }
    }
    
    @EventHandler
    public void onItemPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getEntity();
        ItemStack item = event.getItem().getItemStack();
        
        if (isSnowflakeItem(item)) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                scanPlayerInventory(player);
            }, 2L);
        }
    }
    
    public void shutdown() {
        if (scanTask != null) {
            scanTask.cancel();
            logger.info("📦 ChestLootListener остановлен");
        }
    }
}
