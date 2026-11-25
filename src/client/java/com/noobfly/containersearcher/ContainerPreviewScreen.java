package com.noobfly.containersearcher;

import com.noobfly.containersearcher.compat.Compat;
import com.noobfly.containersearcher.compat.CompatScreen;
import com.noobfly.containersearcher.compat.Gfx;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class ContainerPreviewScreen extends CompatScreen {
	private static final int SLOT_SIZE = 18;
	private static final String KEY = "screen.noobs_container_searcher.preview_";

	private final List<ItemStack> slots;
	private final Component position;
	private final boolean empty;
	private int columns;
	private int rows;
	private int x;
	private int y;
	private int page;
	private int pageCount;
	private Button previous;
	private Button next;

	ContainerPreviewScreen(ContainerRecord record) {
		super(Component.translatable(KEY + "title", record.customName == null
			? ContainerSearchScreen.containerName(record.containerType) : Component.literal(record.customName)));
		slots = snapshot(record);
		position = Component.translatable(KEY + "position", record.x, record.y, record.z);
		empty = slots.stream().allMatch(ItemStack::isEmpty);
	}

	private static List<ItemStack> snapshot(ContainerRecord record) {
		List<ContainerItemRecord> items = ContainerSearchScreen.entries(record).stream()
			.filter(item -> item.inside == null)
			.toList();
		if (record.slotCount <= 0) {
			// Snapshots made before slot indices were recorded can still be displayed compactly.
			return items.stream().map(ContainerPreviewScreen::previewStack).toList();
		}
		List<ItemStack> stacks = new ArrayList<>(Collections.nCopies(record.slotCount, ItemStack.EMPTY));
		for (ContainerItemRecord item : items) {
			if (item.slot != null && item.slot >= 0 && item.slot < stacks.size()) {
				stacks.set(item.slot, previewStack(item));
			}
		}
		return stacks;
	}

	private static ItemStack previewStack(ContainerItemRecord item) {
		Identifier id = Identifier.tryParse(item.itemId);
		return id != null && BuiltInRegistries.ITEM.containsKey(id)
			? ContainerSearchScreen.displayStack(BuiltInRegistries.ITEM.getValue(id), item)
			: ItemStack.EMPTY;
	}

	@Override
	protected void init() {
		columns = Math.clamp((width - 20) / SLOT_SIZE, 1, 9);
		int wantedRows = Math.max(1, Math.ceilDiv(slots.size(), columns));
		rows = Math.min(wantedRows, Math.clamp((height - 84) / SLOT_SIZE, 1, 6));
		pageCount = Math.max(1, Math.ceilDiv(slots.size(), columns * rows));
		page = Math.min(page, pageCount - 1);
		x = (width - columns * SLOT_SIZE) / 2;
		y = (height - rows * SLOT_SIZE - 12) / 2;
		int buttonY = y + rows * SLOT_SIZE + 24;
		addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
			.bounds(width / 2 - 32, buttonY, 64, 20).build());
		if (pageCount > 1) {
			previous = addRenderableWidget(Button.builder(Component.translatable(KEY + "previous"), button -> changePage(-1))
				.bounds(width / 2 - 90, buttonY, 54, 20).build());
			next = addRenderableWidget(Button.builder(Component.translatable(KEY + "next"), button -> changePage(1))
				.bounds(width / 2 + 36, buttonY, 54, 20).build());
			changePage(0);
		}
	}

	private void changePage(int direction) {
		page = Math.clamp(page + direction, 0, pageCount - 1);
		previous.active = page > 0;
		next.active = page < pageCount - 1;
	}

	@Override
	protected void renderContent(Gfx g, int mouseX, int mouseY, float partialTick) {
		g.fill(0, 0, width, height, 0xAA000000);
		int gridWidth = columns * SLOT_SIZE;
		int gridHeight = rows * SLOT_SIZE;
		int panelHalf = Math.min(width / 2, Math.max(100, gridWidth / 2 + 16));
		g.fill(width / 2 - panelHalf, y - 30, width / 2 + panelHalf, y + gridHeight + 50, 0xFF202329);
		Component title = getTitle();
		if (font.width(title) > 2 * panelHalf - 10) {
			title = Component.literal(font.plainSubstrByWidth(title.getString(), 2 * panelHalf - 18) + "…");
		}
		g.centeredText(font, title, width / 2, y - 25, 0xFFFFFFFF);
		g.centeredText(font, position, width / 2, y - 13, 0xFFB3BAC5);
		ItemStack hovered = ItemStack.EMPTY;
		for (int cell = 0; cell < columns * rows; cell++) {
			int index = page * columns * rows + cell;
			int cellX = x + cell % columns * SLOT_SIZE;
			int cellY = y + cell / columns * SLOT_SIZE;
			boolean hover = mouseX >= cellX && mouseX < cellX + SLOT_SIZE && mouseY >= cellY && mouseY < cellY + SLOT_SIZE;
			g.fill(cellX, cellY, cellX + SLOT_SIZE - 1, cellY + SLOT_SIZE - 1, hover ? 0xFF525965 : 0xFF30353D);
			if (index < slots.size()) {
				ItemStack stack = slots.get(index);
				if (!stack.isEmpty()) {
					g.fakeItem(stack, cellX, cellY);
					g.itemDecorations(font, stack, cellX, cellY, null);
					if (hover) {
						hovered = stack;
					}
				}
			}
		}
		if (pageCount > 1 || empty) {
			Component caption = empty
				? Component.translatable(KEY + "empty")
				: Component.translatable(KEY + "page", page + 1, pageCount);
			g.centeredText(font, caption, width / 2, y + gridHeight + 8, 0xFFB3BAC5);
		}
		renderWidgets(mouseX, mouseY, partialTick);
		if (!hovered.isEmpty()) {
			g.tooltip(font, Screen.getTooltipFromItem(minecraft, hovered), mouseX, mouseY);
		}
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void onClose() {
		Compat.setScreen(minecraft, null);
	}
}
