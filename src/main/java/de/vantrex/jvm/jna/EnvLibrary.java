package de.vantrex.jvm.jna;

import com.sun.jna.Library;
import com.sun.jna.WString;

public interface EnvLibrary extends Library {

    String GetEnvVariable(String name);

    void SetEnvVariable(String name, String value);

    boolean SetPermanentEnvironmentVariable(WString value, WString data);

    void RestartExplorer();
}
