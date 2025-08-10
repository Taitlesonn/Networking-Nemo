package nemo.networking.Devices;

import java.util.Objects;

public record Dev(String name, Integer type_dev) {
    public Dev {
        Objects.requireNonNull(name, "Null device name");
    }
}
