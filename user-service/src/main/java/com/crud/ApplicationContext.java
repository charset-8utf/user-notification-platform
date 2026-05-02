package com.crud;

import com.crud.controller.NoteController;
import com.crud.controller.ProfileController;
import com.crud.controller.RoleController;
import com.crud.controller.UserController;

/**
 * Контекст приложения с зависимостями.
 */
public record ApplicationContext(HibernateUtil hibernateUtil,
                                 UserController userController,
                                 NoteController noteController,
                                 RoleController roleController,
                                 ProfileController profileController) {
}
