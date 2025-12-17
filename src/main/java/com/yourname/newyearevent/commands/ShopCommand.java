package com.yourname.newyearevent.commands;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.yourname.newyearevent.NewYearEventPlugin;
import com.yourname.newyearevent.managers.CurrencyManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.components.FoodComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Команда /shop - открывает магазин с предметами за снежинки
 * Содержит обычные предметы и кастомные новогодние предметы
 */
public class ShopCommand implements CommandExecutor, Listener {
    
    private final NewYearEventPlugin plugin;
    private final CurrencyManager currencyManager;
    private final Logger logger;
    
    private static final String SHOP_TITLE = "§6§l🛒 Новогодний Магазин";
    
    // CustomModelData ID для предметов (из ресурспака)
    private static final int SNOWFLAKE_ID = 1;      // snowball
    private static final int MYSTERY_GIFT_ID = 1;   // paper
    private static final int WINTER_ELYTRA_ID = 2;  // elytra
    private static final int FROZEN_APPLE_ID = 2;   // golden_apple
    
    public ShopCommand(NewYearEventPlugin plugin, CurrencyManager currencyManager) {
        this.plugin = plugin;
        this.currencyManager = currencyManager;
        this.logger = plugin.getLogger();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("Команда доступна только игрокам!", NamedTextColor.RED));
            return true;
        }
        
        Player player = (Player) sender;
        openShop(player);
        return true;
    }
    
    /**
     * Открывает магазин игроку
     */
    private void openShop(Player player) {
        Inventory shop = Bukkit.createInventory(null, 54, Component.text(SHOP_TITLE));
        
        int balance = currencyManager.getCurrency(player);
        
        // Слот 0: Книга с балансом
        shop.setItem(0, createBalanceBook(balance));
        
        // Слот 8: Голова помощи
        shop.setItem(8, createHelpHead());
        
        // ========================================
        // РЯД 2 (слоты 10-16): ОБЫЧНЫЕ ПРЕДМЕТЫ
        // ========================================
        shop.setItem(10, createShopItem(Material.SNOW_BLOCK, 64, 10, "§b§lСнежные блоки", "Для строительства"));
        shop.setItem(11, createShopItem(Material.ICE, 32, 15, "§b§lЛёд", "Скользкий и холодный"));
        shop.setItem(12, createShopItem(Material.FIREWORK_ROCKET, 3, 20, "§c§lФейерверки", "Праздничные залпы"));
        shop.setItem(13, createEnchantedBook(50));
        shop.setItem(14, createShopItem(Material.DIAMOND, 4, 40, "§b§lАлмазы", "Драгоценные камни"));
        shop.setItem(15, createShopItem(Material.EMERALD, 4, 35, "§a§lИзумруды", "Торговая валюта"));
        shop.setItem(16, createShopItem(Material.GOLDEN_APPLE, 2, 25, "§6§lЗолотые яблоки", "Для восстановления"));
        
        // ========================================
        // РЯД 3 (слоты 19-25): ДОПОЛНИТЕЛЬНЫЕ
        // ========================================
        shop.setItem(19, createShopItem(Material.EXPERIENCE_BOTTLE, 16, 30, "§d§lПузырьки опыта", "Для зачарования"));
        shop.setItem(20, createShopItem(Material.ENDER_PEARL, 8, 45, "§5§lЖемчуг Края", "Для телепортации"));
        shop.setItem(21, createShopItem(Material.TOTEM_OF_UNDYING, 1, 200, "§6§lТотем бессмертия", "Спасает от смерти"));
        shop.setItem(22, createShopItem(Material.ELYTRA, 1, 150, "§f§lЭлитры", "Для полётов"));
        shop.setItem(23, createShopItem(Material.SHULKER_BOX, 1, 100, "§d§lШалкеровый ящик", "Портативное хранилище"));
        shop.setItem(24, createShopItem(Material.NETHERITE_INGOT, 1, 250, "§8§lНезеритовый слиток", "Самый прочный металл"));
        shop.setItem(25, createShopItem(Material.ENCHANTED_GOLDEN_APPLE, 1, 300, "§6§lЗачарованное золотое яблоко", "Легендарное"));
        
        // ========================================
        // РЯД 4 (слоты 28-34): НОВОГОДНИЕ ПРЕДМЕТЫ
        // ========================================
        shop.setItem(28, createCustomSnowflake(80));
        shop.setItem(29, createCustomMysteryGift(120));
        shop.setItem(30, createCustomWinterElytra(500));
        shop.setItem(31, createCustomFrozenApple(200));
        
        // Заполняем пустые слоты стеклом
        ItemStack glass = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.displayName(Component.text(" "));
        glass.setItemMeta(glassMeta);
        
        for (int i = 0; i < 54; i++) {
            if (shop.getItem(i) == null) {
                shop.setItem(i, glass);
            }
        }
        
        player.openInventory(shop);
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
        
        logger.info("🛒 " + player.getName() + " открыл магазин (баланс: " + balance + "❄)");
    }
    
    /**
     * Создает декоративную снежинку (снежок с кастомной текстурой)
     */
    private ItemStack createCustomSnowflake(int price) {
        ItemStack item = new ItemStack(Material.SNOWBALL, 16);
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
        lore.add(Component.text(""));
        lore.add(Component.text("Цена: " + price + " ❄", NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false)
                .decoration(TextDecoration.BOLD, true));
        lore.add(Component.text(""));
        lore.add(Component.text("▶ Нажмите для покупки", NamedTextColor.GREEN)
                .decoration(TextDecoration.ITALIC, false));
        
        meta.lore(lore);
        meta.setEnchantmentGlintOverride(true);
        
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Создает загадочный подарок (бумага с кастомной текстурой)
     */
    private ItemStack createCustomMysteryGift(int price) {
        ItemStack item = new ItemStack(Material.PAPER, 5);
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
        lore.add(Component.text(""));
        lore.add(Component.text("Цена: " + price + " ❄", NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false)
                .decoration(TextDecoration.BOLD, true));
        lore.add(Component.text(""));
        lore.add(Component.text("▶ Нажмите для покупки", NamedTextColor.GREEN)
                .decoration(TextDecoration.ITALIC, false));
        
        meta.lore(lore);
        meta.setEnchantmentGlintOverride(true);
        
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Создает зимние элитры
     */
    private ItemStack createCustomWinterElytra(int price) {
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
        lore.add(Component.text(""));
        lore.add(Component.text("Цена: " + price + " ❄", NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false)
                .decoration(TextDecoration.BOLD, true));
        lore.add(Component.text(""));
        lore.add(Component.text("▶ Нажмите для покупки", NamedTextColor.GREEN)
                .decoration(TextDecoration.ITALIC, false));
        
        meta.lore(lore);
        meta.setEnchantmentGlintOverride(true);
        
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Создает замороженное золотое яблоко
     */
    private ItemStack createCustomFrozenApple(int price) {
        ItemStack item = new ItemStack(Material.GOLDEN_APPLE, 3);
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
        lore.add(Component.text(""));
        lore.add(Component.text("Цена: " + price + " ❄", NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false)
                .decoration(TextDecoration.BOLD, true));
        lore.add(Component.text(""));
        lore.add(Component.text("▶ Нажмите для покупки", NamedTextColor.GREEN)
                .decoration(TextDecoration.ITALIC, false));
        
        meta.lore(lore);
        meta.setEnchantmentGlintOverride(true);
        
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Создает книгу с балансом
     */
    private ItemStack createBalanceBook(int balance) {
        ItemStack book = new ItemStack(Material.BOOK);
        ItemMeta meta = book.getItemMeta();
        
        meta.displayName(Component.text("💰 Ваш баланс", NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false)
                .decoration(TextDecoration.BOLD, true));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text(""));
        lore.add(Component.text("Снежинок: " + balance + " ❄", NamedTextColor.AQUA)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text(""));
        lore.add(Component.text("Зарабатывайте снежинки:", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("• Убивая мобов (10%)", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("• Находя в сундуках", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        
        meta.lore(lore);
        book.setItemMeta(meta);
        return book;
    }
    
    /**
     * Создает голову с новогодней текстурой (помощь)
     */
    private ItemStack createHelpHead() {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        
        PlayerProfile profile = Bukkit.createProfile(UUID.fromString("c8050621-83db-4b05-af96-b5dcb4dce12c"));
        profile.setProperty(new ProfileProperty(
            "textures",
            "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWU3OTQ0NWI0ZmJiMWU4MTkwYTMwNmZlYWEwMjJkOWM1MThjNTY1ZGQwMDEzYTU2Nzc3Y2YxYThlMDMxNWZmNiJ9fX0="
        ));
        
        meta.setPlayerProfile(profile);
        
        meta.displayName(Component.text("❓ Помощь", NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false)
                .decoration(TextDecoration.BOLD, true));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text(""));
        lore.add(Component.text("Нажмите на предмет для покупки", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text(""));
        lore.add(Component.text("Новогодние предметы требуют", NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("установки ресурспака!", NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false));
        
        meta.lore(lore);
        head.setItemMeta(meta);
        return head;
    }
    
    /**
     * Создает обычный предмет магазина
     */
    private ItemStack createShopItem(Material material, int amount, int price, String name, String description) {
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();
        
        meta.displayName(Component.text(name)
                .decoration(TextDecoration.ITALIC, false));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text(""));
        lore.add(Component.text(description, NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text(""));
        lore.add(Component.text("Цена: " + price + " ❄", NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false)
                .decoration(TextDecoration.BOLD, true));
        lore.add(Component.text(""));
        lore.add(Component.text("▶ Нажмите для покупки", NamedTextColor.GREEN)
                .decoration(TextDecoration.ITALIC, false));
        
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Создает зачарованную книгу
     */
    private ItemStack createEnchantedBook(int price) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = book.getItemMeta();
        
        meta.displayName(Component.text("§5§lКнига зачарований")
                .decoration(TextDecoration.ITALIC, false));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text(""));
        lore.add(Component.text("Случайное зачарование", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text(""));
        lore.add(Component.text("Цена: " + price + " ❄", NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false)
                .decoration(TextDecoration.BOLD, true));
        lore.add(Component.text(""));
        lore.add(Component.text("▶ Нажмите для покупки", NamedTextColor.GREEN)
                .decoration(TextDecoration.ITALIC, false));
        
        meta.lore(lore);
        book.setItemMeta(meta);
        return book;
    }
    
    /**
     * Обработка кликов в магазине
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Component title = event.getView().title();
        String titleString = ((net.kyori.adventure.text.TextComponent) title).content();
        if (!titleString.equals(SHOP_TITLE)) {
            return;
        }
        
        event.setCancelled(true);
        
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        
        if (clicked == null || !clicked.hasItemMeta()) {
            return;
        }
        
        int slot = event.getSlot();
        if (slot == 0 || slot == 8) {
            return;
        }
        
        // Проверка на декоративное стекло
        if (clicked.getType() == Material.LIGHT_BLUE_STAINED_GLASS_PANE) {
            return;
        }
        
        int price = getPriceFromLore(clicked);
        if (price <= 0) {
            return;
        }
        
        int balance = currencyManager.getCurrency(player);
        if (balance < price) {
            player.sendMessage(Component.text("❌ Недостаточно снежинок! Нужно: " + price + " ❄, у вас: " + balance + " ❄", NamedTextColor.RED));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }
        
        currencyManager.takeCurrency(player, price);
        
        ItemStack reward = createRewardItem(clicked);
        
        player.getInventory().addItem(reward);
        
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
        player.sendMessage(Component.text("✅ Покупка успешна! -" + price + " ❄", NamedTextColor.GREEN));
        
        logger.info("🛒 " + player.getName() + " купил предмет за " + price + "❄ (остаток: " + currencyManager.getCurrency(player) + "❄)");
        
        player.closeInventory();
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> openShop(player), 1L);
    }
    
    /**
     * Получает цену из лора предмета
     */
    private int getPriceFromLore(ItemStack item) {
        if (!item.hasItemMeta()) {
            return 0;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasLore()) {
            return 0;
        }
        
        List<Component> lore = meta.lore();
        if (lore == null) {
            return 0;
        }
        
        for (Component line : lore) {
            String text = ((net.kyori.adventure.text.TextComponent) line).content();
            
            if (text.contains("Цена:")) {
                text = text.replaceAll("§.", "");
                
                String[] parts = text.split("\\s+");
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
    
    /**
     * Создает награду без служебного лора
     */
    private ItemStack createRewardItem(ItemStack shopItem) {
        ItemStack reward = shopItem.clone();
        ItemMeta meta = reward.getItemMeta();
        
        if (meta == null) {
            return reward;
        }
        
        if (meta.hasLore()) {
            List<Component> oldLore = meta.lore();
            List<Component> newLore = new ArrayList<>();
            
            if (oldLore != null) {
                for (Component line : oldLore) {
                    String text = ((net.kyori.adventure.text.TextComponent) line).content();
                    
                    if (!text.contains("Цена:") && !text.contains("Нажмите для покупки") && !text.contains("Требуется ресурспак")) {
                        newLore.add(line);
                    }
                }
            }
            
            while (!newLore.isEmpty()) {
                Component last = newLore.get(newLore.size() - 1);
                String text = ((net.kyori.adventure.text.TextComponent) last).content();
                if (text.trim().isEmpty()) {
                    newLore.remove(newLore.size() - 1);
                } else {
                    break;
                }
            }
            
            meta.lore(newLore.isEmpty() ? null : newLore);
        }
        
        reward.setItemMeta(meta);
        return reward;
    }
}
