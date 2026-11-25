package com.noobfly.containersearcher;

import com.noobfly.containersearcher.compat.Compat;
import com.noobfly.containersearcher.compat.CompatScreen;
import com.noobfly.containersearcher.compat.Gfx;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

final class ContainerOptionsScreen extends CompatScreen {
	private final Screen parent;
	private final SearchController controller;
	private final ContainerRecord record;
	private final Runnable onChanged;
	private EditBox nameBox;

	ContainerOptionsScreen(Screen parent, SearchController controller, ContainerRecord record, Runnable onChanged) {
		super(Component.translatable("screen.noobs_container_searcher.container_options_title"));
		this.parent = parent;
		this.controller = controller;
		this.record = record;
		this.onChanged = onChanged;
	}

	@Override
	protected void init() {
		int contentWidth = Math.min(260, width - 24);
		int x = (width - contentWidth) / 2;
		int y = height / 2 - 19;
		String previousName = nameBox == null ? record.customName : nameBox.getValue();
		Component label = Component.translatable("screen.noobs_container_searcher.container_name");
		nameBox = new EditBox(font, x, y, contentWidth, 20, label);
		nameBox.setHint(label);
		nameBox.setMaxLength(80);
		if (previousName != null) {
			nameBox.setValue(previousName);
		}
		addRenderableWidget(nameBox);
		setInitialFocus(nameBox);

		int buttonWidth = (contentWidth - 6) / 2;
		addRenderableWidget(Button.builder(Component.translatable("screen.noobs_container_searcher.container_name_save"), button -> {
			controller.renameRecord(record, nameBox.getValue());
			onChanged.run();
			onClose();
		}).bounds(x, y + 27, buttonWidth, 20).build());
		addRenderableWidget(Button.builder(Component.translatable("screen.noobs_container_searcher.container_forget"), button -> confirmForget())
			.bounds(x + contentWidth - buttonWidth, y + 27, buttonWidth, 20).build());
		addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, button -> onClose())
			.bounds(x, y + 53, contentWidth, 20).build());
	}

	@Override
	protected void renderContent(Gfx graphics, int mouseX, int mouseY, float partialTick) {
		graphics.fill(0, 0, width, height, 0xED101012);
		graphics.centeredText(font, title, width / 2, height / 2 - 48, 0xFFFFFFFF);
		Component location = Component.translatable("screen.noobs_container_searcher.container_options_location",
			ContainerSearchScreen.containerName(record.containerType), record.x, record.y, record.z);
		graphics.centeredText(font, location, width / 2, height / 2 - 33, 0xFFC5C5C5);
		renderWidgets(mouseX, mouseY, partialTick);
	}

	private void confirmForget() {
		Compat.setScreen(minecraft, new ConfirmScreen(confirmed -> {
			if (confirmed) {
				controller.forgetRecord(record);
				onChanged.run();
				onClose();
			} else {
				Compat.setScreen(minecraft, this);
			}
		}, Component.translatable("screen.noobs_container_searcher.container_forget"),
			Component.translatable("screen.noobs_container_searcher.container_forget_confirm")));
	}

	@Override
	public void onClose() {
		Compat.setScreen(minecraft, parent);
	}
}
