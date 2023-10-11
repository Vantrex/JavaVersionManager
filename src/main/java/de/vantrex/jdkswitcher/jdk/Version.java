package de.vantrex.jdkswitcher.jdk;

import de.vantrex.jdkswitcher.util.OSUtil;

public interface Version {


    String version();

    String jdkProvider();

    String operatingSystem();

    String systemArchitecture();

    String downloadUrl();

    String compiledName();

    default boolean canUseOnOperatingSystem() {
        try {
          //  System.out.println(OSUtil.getOperatingSystem() + " " + OSUtil.getArchitecture());
        //    System.out.println(this.operatingSystem() + " " + this.systemArchitecture());
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