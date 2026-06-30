package com.pg85.otg.configuration.customobjects;

import com.pg85.otg.configuration.ErroredFunction;
import com.pg85.otg.configuration.world.WorldConfig;
import com.pg85.otg.exception.InvalidConfigException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomObjectResourcesManager
{
    private final Map<Class<?>, Map<String, Class<? extends CustomObjectConfigFunction<?>>>> configFunctions = new HashMap<>();

    public void registerConfigFunction(String name, Class<? extends CustomObjectConfigFunction<?>> value)
    {
        try
        {
            configFunctions.computeIfAbsent(value.newInstance().getHolderType(), k -> new HashMap<>()).put(name.toLowerCase(), value);
        }
        catch(InstantiationException | IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns a config function with the given name.
     * @param <T>    Type of the holder of the config function.
     * @param name   The name of the config function.
     * @param holder The holder of the config function, like
     *               {@link WorldConfig}.
     * @param args   The args of the function.
     * @return A config function with the given name, or null if the config
     * function requires another holder. For invalid or non-existing config
     * functions, it returns an instance of {@link ErroredFunction}.
     */
    @SuppressWarnings("unchecked")
    // It's checked with clazz.getConstructor(holder.getClass(), ...))
    public <T> CustomObjectConfigFunction<T> getConfigFunction(String name, T holder, List<String> args)
    {
        Map<String, Class<? extends CustomObjectConfigFunction<?>>> m = configFunctions.get(holder.getClass());
        if(m == null)
        {
            return new CustomObjectErroredFunction<T>(name, holder, args, "No config functions registered for holder type " + holder.getClass());
        }

        name = name.toLowerCase().trim();

        // If a Block() tag has the parameters of a RandomBlock tag then transform it into a RandomBlock
        // This allows users to edit Bo3's and change Blocks to RandomBlocks with a simple find/replace.
        if(name.equals("block") && args.size() > 5)
        {
            name = "randomblock";
        }

        Class<? extends CustomObjectConfigFunction<T>> clazz = (Class<? extends CustomObjectConfigFunction<T>>) m.get(name);
        if(clazz == null)
        {
            return new CustomObjectErroredFunction<T>(name, holder, args, "No config function registered with name " + name);
        }

        CustomObjectConfigFunction<T> instance;
        try
        {
            instance = clazz.newInstance();
        }
        catch(InstantiationException | IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
        try
        {
            instance.init(holder, args);
        }
        catch(InvalidConfigException e)
        {
            return new CustomObjectErroredFunction<>(name, holder, args, e.getMessage());
        }
        return instance;
    }
}
