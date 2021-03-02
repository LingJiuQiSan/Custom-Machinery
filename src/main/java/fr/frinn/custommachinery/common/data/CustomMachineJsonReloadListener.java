package fr.frinn.custommachinery.common.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.network.NetworkManager;
import fr.frinn.custommachinery.common.network.SUpdateMachinesPacket;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;

public class CustomMachineJsonReloadListener extends JsonReloadListener {

    public static final Logger LOGGER = LogManager.getLogger("CustomMachinery/MachineLoader");
    public static final Gson GSON = (new GsonBuilder()).create();

    public CustomMachineJsonReloadListener() {
        super(GSON, "machines");
    }

    @ParametersAreNonnullByDefault
    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, IResourceManager resourceManager, IProfiler profiler) {
        LOGGER.info("Reading Custom Machinery files...");

        CustomMachinery.MACHINES.clear();
        map.forEach((id, json) -> {
            LOGGER.info("Parsing " + id);

            if(!json.isJsonObject()) {
                LOGGER.warn("Bad Machine JSON in : " + id);
                return;
            }

            if(CustomMachinery.MACHINES.containsKey(id)) {
                LOGGER.warn("A machine with id : " + id + " already exists");
                return;
            }

            CustomMachine machine = CustomMachine.CODEC.decode(JsonOps.INSTANCE, json).resultOrPartial(LOGGER::error).map(Pair::getFirst).orElseThrow(() -> new JsonParseException("Error while reading machine: " + id));
            machine.setId(id);
            CustomMachinery.MACHINES.put(id, machine);
        });

        LOGGER.info("Finished.");

        if(ServerLifecycleHooks.getCurrentServer() != null)
            NetworkManager.CHANNEL.send(PacketDistributor.ALL.noArg(), new SUpdateMachinesPacket(CustomMachinery.MACHINES));
    }
}