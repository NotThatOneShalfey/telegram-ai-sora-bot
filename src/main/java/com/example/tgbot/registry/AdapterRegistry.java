package com.example.tgbot.registry;

import com.example.tgbot.models.adapters.IRequestAdapter;
import com.example.tgbot.models.enums.GenerationModel;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AdapterRegistry {
    private final Map<GenerationModel, IRequestAdapter> adapters = new ConcurrentHashMap<>();

    public AdapterRegistry(Collection<IRequestAdapter> adaptersCollection) {
        adaptersCollection.forEach(a -> adapters.put(a.getModel(), a));
    }

    public IRequestAdapter getAdapter(GenerationModel model) {
        return adapters.get(model);
    }
}
