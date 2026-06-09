package com.crud.util;

import com.crud.dto.NoteResponse;
import com.crud.dto.ProfileResponse;
import com.crud.dto.RoleResponse;
import com.crud.dto.UserResponse;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

public class CustomAssert {

    public static UserResponseAssert assertThatUser(UserResponse actual) {
        return new UserResponseAssert(actual);
    }

    public static ProfileResponseAssert assertThatProfile(ProfileResponse actual) {
        return new ProfileResponseAssert(actual);
    }

    public static NoteResponseAssert assertThatNote(NoteResponse actual) {
        return new NoteResponseAssert(actual);
    }

    public static RoleResponseAssert assertThatRole(RoleResponse actual) {
        return new RoleResponseAssert(actual);
    }

    public static class UserResponseAssert extends AbstractAssert<UserResponseAssert, UserResponse> {

        public UserResponseAssert(UserResponse actual) {
            super(actual, UserResponseAssert.class);
        }

        @SuppressWarnings("UnusedReturnValue")
        public UserResponseAssert hasId(Long expected) {
            isNotNull();
            Assertions.assertThat(actual.id()).isEqualTo(expected);
            return this;
        }

        @SuppressWarnings("UnusedReturnValue")
        public UserResponseAssert hasName(String expected) {
            isNotNull();
            Assertions.assertThat(actual.name()).isEqualTo(expected);
            return this;
        }

        @SuppressWarnings("UnusedReturnValue")
        public UserResponseAssert hasEmail(String expected) {
            isNotNull();
            Assertions.assertThat(actual.email()).isEqualTo(expected);
            return this;
        }

        @SuppressWarnings("UnusedReturnValue")
        public UserResponseAssert hasAge(int expected) {
            isNotNull();
            Assertions.assertThat(actual.age()).isEqualTo(expected);
            return this;
        }

        @SuppressWarnings("UnusedReturnValue")
        public UserResponseAssert matches(UserResponse expected) {
            isNotNull();
            hasName(expected.name());
            hasEmail(expected.email());
            hasAge(expected.age());
            return this;
        }
    }

    public static class ProfileResponseAssert extends AbstractAssert<ProfileResponseAssert, ProfileResponse> {

        public ProfileResponseAssert(ProfileResponse actual) {
            super(actual, ProfileResponseAssert.class);
        }

        @SuppressWarnings("UnusedReturnValue")
        public ProfileResponseAssert hasUserId(Long expected) {
            isNotNull();
            Assertions.assertThat(actual.userId()).isEqualTo(expected);
            return this;
        }

        @SuppressWarnings("UnusedReturnValue")
        public ProfileResponseAssert hasPhone(String expected) {
            isNotNull();
            Assertions.assertThat(actual.phone()).isEqualTo(expected);
            return this;
        }

        @SuppressWarnings("UnusedReturnValue")
        public ProfileResponseAssert hasAddress(String expected) {
            isNotNull();
            Assertions.assertThat(actual.address()).isEqualTo(expected);
            return this;
        }
    }

    public static class NoteResponseAssert extends AbstractAssert<NoteResponseAssert, NoteResponse> {

        public NoteResponseAssert(NoteResponse actual) {
            super(actual, NoteResponseAssert.class);
        }

        @SuppressWarnings("UnusedReturnValue")
        public NoteResponseAssert hasId(Long expected) {
            isNotNull();
            Assertions.assertThat(actual.id()).isEqualTo(expected);
            return this;
        }

        @SuppressWarnings("UnusedReturnValue")
        public NoteResponseAssert hasContent(String expected) {
            isNotNull();
            Assertions.assertThat(actual.content()).isEqualTo(expected);
            return this;
        }
    }

    public static class RoleResponseAssert extends AbstractAssert<RoleResponseAssert, RoleResponse> {

        public RoleResponseAssert(RoleResponse actual) {
            super(actual, RoleResponseAssert.class);
        }

        @SuppressWarnings("UnusedReturnValue")
        public RoleResponseAssert hasId(Long expected) {
            isNotNull();
            Assertions.assertThat(actual.id()).isEqualTo(expected);
            return this;
        }

        @SuppressWarnings("UnusedReturnValue")
        public RoleResponseAssert hasName(String expected) {
            isNotNull();
            Assertions.assertThat(actual.name()).isEqualTo(expected);
            return this;
        }
    }
}
