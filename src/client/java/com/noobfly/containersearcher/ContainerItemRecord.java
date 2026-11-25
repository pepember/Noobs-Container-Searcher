package com.noobfly.containersearcher;

import java.util.ArrayList;
import java.util.List;

public final class ContainerItemRecord {
	public String itemId;
	// Kept for older saved data. A packed item also records its current path below.
	public String inside;
	public List<PackedItems.Parent> packPath = List.of();
	public Integer slot;
	public String stackData;
	public int count;
	public String name;
	public String searchText;
	public boolean enchanted;
	public boolean damageable;
	public int damage;
	public int maxDamage;
	public List<String> tooltipLines = new ArrayList<>();
	public List<String> loreLines = new ArrayList<>();
	public List<String> enchantments = new ArrayList<>();
	public List<String> enchantmentNames = new ArrayList<>();
}
