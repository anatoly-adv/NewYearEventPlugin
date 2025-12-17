package com.yourname.newyearevent.managers;

import com.yourname.newyearevent.NewYearEventPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.logging.Logger;

/**
 * Менеджер ресурспака - автоматически загружает кастомный ресурспак игрокам
 * Содержит текстуры для снежинок, новогодних шапок и других предметов
 */
public class ResourcePackManager implements Listener {
    
    private final NewYearEventPlugin plugin;
    private final Logger logger;
    
    // URL ресурспака (нужно заменить на реальный URL после загрузки)
    // Можно использовать GitHub Releases, Dropbox, Google Drive или свой веб-сервер
    private static final String RESOURCE_PACK_URL = "https://github.com/anatoly-adv/NewYearEvent/releases/download/v1.0.0-resourcepack/NewYearEvent_Custom.zip";
    
    // SHA-1 хеш файла ресурспака (для проверки целостности)
    // Генерируется командой: certutil -hashfile newyear_resourcepack.zip SHA1
    private static final String RESOURCE_PACK_HASH = "1e07ca72198f0c0387013fd11354eff2297ed0a3";
    
    // Обязательна ли установка пака (true = без пака нельзя играть)
    private static final boolean REQUIRED = false;
    
    public ResourcePackManager(NewYearEventPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        
        // ========================================
        // РЕГИСТРАЦИЯ LISTENER - КРИТИЧЕСКИ ВАЖНО!
        // ========================================
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        
        logger.info("📦 ResourcePackManager инициализирован");
        logger.info("📦 URL ресурспака: " + RESOURCE_PACK_URL);
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Отправляем ресурспак через 20 тиков (1 секунду) после входа
        // Задержка нужна чтобы клиент успел полностью загрузиться
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            sendResourcePack(player);
        }, 20L);
    }
    
    /**
     * Отправляет ресурспак игроку
     */
    private void sendResourcePack(Player player) {
		try {
			// ПРИНУДИТЕЛЬНАЯ ОТПРАВКА с промптом
			player.setResourcePack(
					RESOURCE_PACK_URL,
					RESOURCE_PACK_HASH,
					true,  // ← СДЕЛАЙ true (обязательный!)
					Component.text("🎄 Новогодний ресурспак", NamedTextColor.GOLD)
			);
			
			logger.info("📦 Ресурспак отправлен игроку: " + player.getName());
			
		} catch (Exception e) {
			logger.warning("⚠️ Ошибка при отправке ресурспака игроку " + player.getName() + ": " + e.getMessage());
			e.printStackTrace();
		}
	}
    
    /**
     * Принудительная отправка ресурспака (можно вызвать из команды)
     */
    public void forceResourcePack(Player player) {
        sendResourcePack(player);
        player.sendMessage(Component.text("📦 Ресурспак отправлен повторно!", NamedTextColor.GREEN));
    }
}