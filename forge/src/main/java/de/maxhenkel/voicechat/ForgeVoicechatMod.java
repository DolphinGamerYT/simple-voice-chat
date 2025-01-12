package de.maxhenkel.voicechat;

import de.maxhenkel.voicechat.config.ForgeServerConfig;
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import de.maxhenkel.voicechat.intercompatibility.ForgeCommonCompatibilityManager;
import de.maxhenkel.voicechat.permission.ForgePermissionManager;
import de.maxhenkel.voicechat.permission.PermissionManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.io.InputStream;
import java.util.Properties;
import java.util.function.Function;

@Mod(ForgeVoicechatMod.MODID)
public class ForgeVoicechatMod extends Voicechat {

    private final ForgeCommonCompatibilityManager compatibilityManager;

    public ForgeVoicechatMod() {
        compatibilityManager = new ForgeCommonCompatibilityManager();
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);

        SERVER_CONFIG = registerConfig(ModConfig.Type.SERVER, ForgeServerConfig::new);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ForgeVoicechatClientMod::new);
    }

    public void commonSetup(FMLCommonSetupEvent event) {
        initialize();
        MinecraftForge.EVENT_BUS.register(compatibilityManager);
    }

    @Override
    public int readCompatibilityVersion() throws Exception {
        InputStream in = getClass().getClassLoader().getResourceAsStream("compatibility.properties");
        Properties props = new Properties();
        props.load(in);
        return Integer.parseInt(props.getProperty("compatibility_version"));
    }

    @Override
    protected CommonCompatibilityManager createCompatibilityManager() {
        return compatibilityManager;
    }

    @Override
    protected PermissionManager createPermissionManager() {
        ForgePermissionManager permissionManager = new ForgePermissionManager();
        MinecraftForge.EVENT_BUS.register(permissionManager);
        return permissionManager;
    }

    public static <T> T registerConfig(ModConfig.Type type, Function<ForgeConfigSpec.Builder, T> consumer) {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        T config = consumer.apply(builder);
        ForgeConfigSpec spec = builder.build();
        ModLoadingContext.get().registerConfig(type, spec);
        return config;
    }
}