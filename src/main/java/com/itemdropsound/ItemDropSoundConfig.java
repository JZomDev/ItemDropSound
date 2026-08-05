package com.itemdropsound;

import net.runelite.api.ItemID;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

import static com.itemdropsound.ItemDropSoundConfig.CONFIG_GROUP;

@ConfigGroup(CONFIG_GROUP)
public interface ItemDropSoundConfig extends Config
{
	public static String CONFIG_GROUP = "ItemDropSoundConfig";
	@ConfigItem(
			keyName = "item_ids",
			name = "Item Ids",
			description = "Item ids to play a drop sound for, enter multiple separated with ;"
	)
	default String itemIds()
	{
		return Integer.toString(ItemID.BLOOD_SHARD);
	}
}
