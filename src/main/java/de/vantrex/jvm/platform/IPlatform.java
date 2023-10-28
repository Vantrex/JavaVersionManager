package de.vantrex.jvm.platform;

import de.vantrex.jvm.jdk.Version;

public interface IPlatform {

    void initPlatform();

    void installVersion(Version version);

    void switchToDefault();

    void findDefaultJdk();

}