package de.vantrex.jvm.jna;

import com.sun.jna.Native;
import com.sun.jna.WString;

public class NativeHook {

    public static final NativeHook INSTANCE = new NativeHook();

    private final EnvLibrary envLibrary;

    public NativeHook() {
        this.envLibrary = Native.loadLibrary("SystemEnvLib.dll", EnvLibrary.class);
    }

    public String getEnvVariable(String name) {
        return this.envLibrary.GetEnvVariable(name);
    }

    public void setEnvVariable(String name, String value) {
        WString wName = new WString(name);
        WString wValue = new WString(value);
        if (!this.envLibrary.SetPermanentEnvironmentVariable(wName, wValue))
            System.out.println("Something went wrong while setting the env variable!");
    }

    public void restartExplorer() {
        this.envLibrary.RestartExplorer();
    }
}