package com.noobfly.containersearcher.compat;

import com.noobfly.containersearcher.ContainerSearcherClient;
import com.noobfly.containersearcher.SearchController;
import com.noobfly.containersearcher.fabric.mixin.ScreenWidgetAccessor;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;

import java.nio.file.Path;
import java.util.function.Consumer;

// Fabric half of Platform, the NeoForge one is in src/neoforge
public final class Platform {
	private static final SearchController.CommandMessenger<FabricClientCommandSource> MESSENGER =
		new SearchController.CommandMessenger<>() {
			@Override
			public void feedback(FabricClientCommandSource source, Component message) {
				source.sendFeedback(message);
			}

			@Override
			public void error(FabricClientCommandSource source, Component message) {
				source.sendError(message);
			}
		};

	private Platform() {
	}

	public static Path configDir() {
		return FabricLoader.getInstance().getConfigDir();
	}

	public static void registerCommands(SearchController controller) {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
			controller.registerCommands(dispatcher, MESSENGER));
	}

	public static void registerEvents(ContainerSearcherClient client) {
		com.noobfly.containersearcher.Analytics.start();
		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> client.onUseBlock(world, hitResult));
		UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) ->
			client.onUseEntity(player, world, entity));
		ClientTickEvents.END_CLIENT_TICK.register(client::onClientTick);
		ScreenEvents.AFTER_INIT.register((minecraft, screen, scaledWidth, scaledHeight) -> {
			if (screen instanceof AbstractContainerScreen<?>) {
				ScreenEvents.remove(screen).register(removedScreen -> client.onContainerScreenClosed());
			}
		});
	}

	public static void registerInventorySearch(Consumer<Screen> openSearch) {
		ScreenEvents.AFTER_INIT.register((minecraft, screen, scaledWidth, scaledHeight) -> {
			if (screen instanceof InventoryScreen || screen instanceof CreativeModeInventoryScreen) {
				Component label = Component.translatable("screen.noobs_container_searcher.inventory_search");
				Button button = Button.builder(label, pressed -> openSearch.accept(screen))
					.bounds(5, 5, minecraft.font.width(label) + 12, 20)
					.tooltip(Tooltip.create(Component.translatable("screen.noobs_container_searcher.inventory_search_hint")))
					.build();
				((ScreenWidgetAccessor) screen).noobsContainerSearcher$addWidget(button);
			}
		});
	}
}
