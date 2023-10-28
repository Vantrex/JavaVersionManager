package de.vantrex.jvm.jdk;

import de.vantrex.jvm.util.OSUtil;

public interface Version {


    String version();

    @SuppressWarnings("unused")
    String jdkProvider();

    String operatingSystem();

    String systemArchitecture();

    String downloadUrl();

    String compiledName();

    default boolean canUseOnOperatingSystem() {
        try {
            return this.operatingSystem().equalsIgnoreCase(OSUtil.getOperatingSystem())
                    && this.systemArchitecture().equals(OSUtil.getArchitecture());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    default String toDirString() {
        return this.compiledName() + "-" + this.operatingSystem() + "-" + this.systemArchitecture();
    }

}