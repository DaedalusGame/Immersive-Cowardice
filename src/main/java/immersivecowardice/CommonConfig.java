package immersivecowardice;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

//Server config is currently defunct, so everything shall be common.
public class CommonConfig {
    public static CommonConfig INSTANCE;

    public final ForgeConfigSpec.BooleanValue arcFurnaceEnable;

    public final ForgeConfigSpec.BooleanValue furnaceHeaterEnable;
    public final ForgeConfigSpec.IntValue furnaceHeaterCost;
    public final ForgeConfigSpec.IntValue furnaceHeaterSpeed;
    public final ForgeConfigSpec.IntValue furnaceHeaterMaxBurnTime;

    public static void register() {
        final Pair<CommonConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(CommonConfig::new);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, specPair.getRight());
        INSTANCE = specPair.getLeft();
    }

    CommonConfig(ForgeConfigSpec.Builder builder) {
        builder.push("machines");

        builder.comment("Config options for Arc Furnaces accepting electrodes piped in.");
        builder.push("arc_furnace");
        arcFurnaceEnable =  builder.comment("Whether this feature should be enabled.").define("enable", true);
        builder.pop();

        builder.comment("Config options for External Heaters heating any furnace.");
        builder.push("furnace_heater");
        furnaceHeaterEnable =  builder.comment("Whether this feature should be enabled.").define("enable", true);
        furnaceHeaterCost = builder.comment("How much FE is consumed per heat unit added to the furnace.").defineInRange("cost", 8,0, Integer.MAX_VALUE);
        furnaceHeaterSpeed = builder.comment("How many heat units are added every tick.").defineInRange("speed", 4,0, Integer.MAX_VALUE);
        furnaceHeaterMaxBurnTime = builder.comment("How much burn time the heater will heat the furnace up to.").defineInRange("max_heat", 200,0, Integer.MAX_VALUE);
        builder.pop();

        builder.pop();
    }
}
