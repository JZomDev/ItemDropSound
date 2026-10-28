package com.itemdropsound;

import com.google.inject.Provides;
import javax.inject.Inject;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.ItemSpawned;
import net.runelite.client.RuneLite;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Objects;

import static com.itemdropsound.ItemDropSoundConfig.CONFIG_GROUP;

@Slf4j
@PluginDescriptor(
	name = "Item Drop Sound"
)
public class ItemDropSoundPlugin extends Plugin
{
	public static final File SOUND_FOLDER = new File(RuneLite.RUNELITE_DIR.getPath() + File.separator + "item-drop-sounds");
	public static final File SOUND_FILE =  new File(SOUND_FOLDER, "item_drop_sound.wav");
	
	@Inject
	private Client client;
	
	@Inject
	private ItemDropSoundConfig config;

	ArrayList<Integer> itemIds = null;
	
	@Override
	protected void startUp() throws Exception
	{
		initializeSoundFiles();
		itemIds = getItemIds();
		log.debug("Item Dropper started!");
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.debug("Item Dropper stopped!");
		itemIds.clear();
		itemIds = null;
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged e)
	{
		if (e.getGroup().equals(CONFIG_GROUP))
		{
			itemIds = getItemIds();
		}
	}

	private ArrayList<Integer> getItemIds(){
		ArrayList<Integer> ids = new ArrayList<>();
		for (String value : config.itemIds().split(";"))
		{
			try
			{
				ids.add(Integer.valueOf(value.trim()));
			}
			catch (NumberFormatException ex)
			{
				log.warn("Invalid item id in configuration: '{}'", value);
			}
		}
		return ids;
	}
	private void initializeSoundFiles() {
		if (!SOUND_FOLDER.exists()) {
			if (!SOUND_FOLDER.mkdirs()) {
				log.warn("Failed to create folder for item drop sounds");
			}
		}
		
		try{
			if (SOUND_FILE.exists()) {
				return;
			}
			
			InputStream stream = ItemDropSoundPlugin.class.getClassLoader().getResourceAsStream("item_drop.wav");
			OutputStream out = Files.newOutputStream(SOUND_FILE.toPath());
			byte[] buffer = new byte[8 * 1024];
			int bytesRead;
			while ((bytesRead = Objects.requireNonNull(stream).read(buffer)) != -1) {
				out.write(buffer, 0, bytesRead);
			}
			out.close();
			stream.close();
		}
		catch (Exception e){
			log.error(e.getLocalizedMessage());
		}
	}

	@Subscribe
	public void onItemSpawned(ItemSpawned itemSpawned)
	{
		if (itemIds == null)
		{
			return;
		}

		Clip clip = null;
		
		TileItem item = itemSpawned.getItem();
		if(itemIds.contains(item.getId())) {
			try (InputStream fileStream = new BufferedInputStream(Files.newInputStream(SOUND_FILE.toPath()));
				 AudioInputStream sound = AudioSystem.getAudioInputStream(fileStream))
			{
				clip = AudioSystem.getClip();
				clip.open(sound);
			}
			catch (UnsupportedAudioFileException | IOException | LineUnavailableException e)
			{
				log.warn("Unable to load item drop alert sound", e);
			}
			
			if (clip != null) {
				
				clip.setFramePosition(clip.getFrameLength());
				clip.loop(1);
			}
		}
	}

	@Provides
	ItemDropSoundConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ItemDropSoundConfig.class);
	}
}
