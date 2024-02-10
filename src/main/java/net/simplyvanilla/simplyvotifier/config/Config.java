package net.simplyvanilla.simplyvotifier.config;

import com.bencodez.advancedcore.api.yml.YMLFile;
import com.bencodez.advancedcore.api.yml.annotation.AnnotationHandler;
import com.bencodez.advancedcore.api.yml.annotation.ConfigDataBoolean;
import com.bencodez.advancedcore.api.yml.annotation.ConfigDataInt;
import com.bencodez.advancedcore.api.yml.annotation.ConfigDataString;
import lombok.Getter;
import lombok.Setter;
import net.simplyvanilla.simplyvotifier.SimplyVotifier;

import java.io.File;

@Setter
@Getter
public class Config extends YMLFile {

    public Config(SimplyVotifier plugin) {
        super(plugin, new File(SimplyVotifier.getInstance().getDataFolder(), "config.yml"));
    }

    public void loadValues() {
        new AnnotationHandler().load(getData(), this);
    }

    @Override
    public void onFileCreation() {
        SimplyVotifier.getInstance().saveResource("config.yml", true);
    }

    @ConfigDataString(path = "host")
    private String host = "0.0.0.0";

    @ConfigDataInt(path = "port")
    private int port = 8192;

    @ConfigDataBoolean(path = "debug")
    private boolean debug = false;
}
