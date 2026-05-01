package com.crud.api.command.factory;

import com.crud.api.ConsoleInput;
import com.crud.api.MenuState;
import com.crud.api.command.Command;
import com.crud.api.command.profile.CreateProfileCommand;
import com.crud.api.command.profile.DeleteProfileCommand;
import com.crud.api.command.profile.GetProfileCommand;
import com.crud.api.command.profile.ListProfilesCommand;
import com.crud.api.command.profile.UpdateProfileCommand;
import com.crud.controller.ProfileController;

import java.util.HashMap;
import java.util.Map;

/**
 * Фабрика команд управления профилями.
 */
public class ProfilesMenuCommandFactory implements MenuCommandFactory {
    private final ProfileController profileController;
    private final ConsoleInput consoleInput;

    public ProfilesMenuCommandFactory(ProfileController profileController, ConsoleInput consoleInput) {
        this.profileController = profileController;
        this.consoleInput = consoleInput;
    }

    @Override
    public MenuState state() {
        return MenuState.PROFILES;
    }

    @Override
    public Map<Integer, Command> createCommands() {
        Map<Integer, Command> commands = new HashMap<>();
        commands.put(1, new CreateProfileCommand(profileController, consoleInput));
        commands.put(2, new UpdateProfileCommand(profileController, consoleInput));
        commands.put(3, new GetProfileCommand(profileController, consoleInput));
        commands.put(4, new DeleteProfileCommand(profileController, consoleInput));
        commands.put(5, new ListProfilesCommand(profileController, consoleInput));
        return commands;
    }
}
