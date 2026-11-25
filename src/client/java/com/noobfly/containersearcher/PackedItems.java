package com.noobfly.containersearcher;

import com.noobfly.containersearcher.compat.Compat;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ItemContainerContents;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class PackedItems {
	static final int MAX_DEPTH = 16;
	static final String FILTER = "flag:in_shulker";

	private PackedItems() {
	}

	static boolean hasContents(ItemStack stack) {
		BundleContents bundle = stack.get(DataComponents.BUNDLE_CONTENTS);
		ItemContainerContents container = stack.get(DataComponents.CONTAINER);
		return (bundle != null && !bundle.isEmpty()) ||
			(container != null && !container.equals(ItemContainerContents.EMPTY));
	}

	static Stream<ItemStack> contents(ItemStack stack) {
		BundleContents bundle = stack.get(DataComponents.BUNDLE_CONTENTS);
		ItemContainerContents container = stack.get(DataComponents.CONTAINER);
		Stream<ItemStack> boxes = container == null ? Stream.empty() : Compat.containerItems(container);
		Stream<ItemStack> bundles = bundle == null ? Stream.empty() : Compat.bundleItems(bundle);
		return Stream.concat(boxes, bundles).filter(item -> !item.isEmpty());
	}

	static int fingerprint(ItemStack stack, int depth) {
		int value = 31 * ItemStack.hashItemAndComponents(stack) + stack.getCount();
		if (depth >= MAX_DEPTH || !hasContents(stack)) {
			return value;
		}
		return contents(stack).mapToInt(item -> fingerprint(item, depth + 1))
			.reduce(value, (hash, childHash) -> 31 * hash + childHash);
	}

	static boolean anyMatch(ItemStack stack, Predicate<ItemStack> predicate, int depth) {
		return depth < MAX_DEPTH && hasContents(stack) && contents(stack)
			.anyMatch(item -> predicate.test(item) || anyMatch(item, predicate, depth + 1));
	}

	static String pathName(ContainerItemRecord record) {
		if (record.packPath == null || record.packPath.isEmpty()) {
			return record.inside;
		}
		return pathName(record.packPath);
	}

	static String pathName(List<Parent> path) {
		return path.stream().map(Parent::displayName).collect(Collectors.joining(" → "));
	}

	record Parent(String itemId, String customName) {
		static Parent fromStack(ItemStack stack) {
			Component name = stack.get(DataComponents.CUSTOM_NAME);
			if (name == null && stack.hasNonDefault(DataComponents.ITEM_NAME)) {
				name = stack.get(DataComponents.ITEM_NAME);
			}
			return new Parent(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString(),
				name == null ? null : name.getString());
		}

		private String displayName() {
			if (customName != null) {
				return customName;
			}
			Identifier id = Identifier.tryParse(itemId);
			return id != null && BuiltInRegistries.ITEM.containsKey(id)
				? new ItemStack(BuiltInRegistries.ITEM.getValue(id)).getHoverName().getString()
				: itemId;
		}
	}
}
