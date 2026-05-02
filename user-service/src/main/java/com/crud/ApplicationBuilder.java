package com.crud;

import com.crud.controller.NoteControllerImpl;
import com.crud.controller.ProfileControllerImpl;
import com.crud.controller.RoleControllerImpl;
import com.crud.controller.UserControllerImpl;
import com.crud.repository.NoteRepositoryImpl;
import com.crud.repository.ProfileRepositoryImpl;
import com.crud.repository.RoleRepositoryImpl;
import com.crud.repository.UserRepositoryImpl;
import com.crud.service.NoteServiceImpl;
import com.crud.service.ProfileServiceImpl;
import com.crud.service.RoleServiceImpl;
import com.crud.service.UserServiceImpl;

/**
 * Собирает граф зависимостей приложения без внешнего DI.
 */
public class ApplicationBuilder {

    public ApplicationContext build() {
        HibernateUtil hibernateUtil = new HibernateUtil();
        var sessionFactory = hibernateUtil.getSessionFactory();

        var userRepository = new UserRepositoryImpl(sessionFactory);
        var userService = new UserServiceImpl(userRepository);
        var userController = new UserControllerImpl(userService);

        var noteRepository = new NoteRepositoryImpl(sessionFactory);
        var noteService = new NoteServiceImpl(noteRepository, userRepository);
        var noteController = new NoteControllerImpl(noteService);

        var roleRepository = new RoleRepositoryImpl(sessionFactory);
        var roleService = new RoleServiceImpl(roleRepository, userRepository);
        var roleController = new RoleControllerImpl(roleService);

        var profileRepository = new ProfileRepositoryImpl(sessionFactory);
        var profileService = new ProfileServiceImpl(profileRepository, userRepository);
        var profileController = new ProfileControllerImpl(profileService);

        return new ApplicationContext(hibernateUtil, userController, noteController, roleController, profileController);
    }
}
