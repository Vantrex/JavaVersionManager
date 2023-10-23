package de.vantrex.jdkswitcher.platform;

import de.vantrex.jdkswitcher.jdk.Version;

public interface IPlatform {

    void initPlatform();

    void installVersion(Version version);

    void switchToDefault();

    void findDefaultJdk();

}