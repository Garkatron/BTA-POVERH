package deus.momentum;

import deus.momentum.systems.stamina.IStaminaSettings;
import deus.momentum.systems.stamina.hud.HudManager;
import deus.momentum.systems.stamina.network.NetManager;
import net.fabricmc.api.ModInitializer;
import net.minecraft.client.render.texture.stitcher.TextureRegistry;
import net.minecraft.core.data.gamerule.GameRuleBoolean;
import net.minecraft.core.data.gamerule.GameRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import turniplabs.halplibe.util.GameStartEntrypoint;
import turniplabs.halplibe.util.RecipeEntrypoint;

import java.io.IOException;
import java.net.URISyntaxException;

import static net.minecraft.client.render.colorizer.Colorizers.mc;


public class Momentum implements ModInitializer, RecipeEntrypoint, GameStartEntrypoint {
    public static final String MOD_ID = "momentum";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final NetManager netManager = new NetManager();
	public static IStaminaSettings options;

	public static final HudManager hudManager = new HudManager();
	public static GameRuleBoolean DISABLE_STAMINA = GameRules.register(new GameRuleBoolean("disableStamina", false));
	public static GameRuleBoolean DISABLE_STAMINA_ON_SPRITING = GameRules.register(new GameRuleBoolean("disableStaminaOnSpriting", false));
	public static GameRuleBoolean DISABLE_STAMINA_ON_JUMP = GameRules.register(new GameRuleBoolean("disableStaminaOnJump", false));
	public static GameRuleBoolean DISABLE_STAMINA_ON_HURT = GameRules.register(new GameRuleBoolean("disableStaminaOnHurt", false));

	@Override
    public void onInitialize() {
		netManager.onInitialize();
        LOGGER.info("ExampleMod initialized.");
    }

	@Override
	public void onRecipesReady() {

	}

	@Override
	public void initNamespaces() {

	}

	@Override
	public void beforeGameStart() {

		try {
			TextureRegistry.initializeAllFiles(MOD_ID, TextureRegistry.guiSpriteAtlas, true);
		} catch (URISyntaxException | IOException e) {
			System.out.println("ERROR");
			throw new RuntimeException(e);
		}
	}

	@Override
	public void afterGameStart() {
		hudManager.onInitialize();

	}
}
