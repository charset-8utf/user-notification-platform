package com.crud.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "roles")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "com.crud.entity.Role")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Role extends BaseEntity {

    private String name;

    @Version
    private Long version;

    @ManyToMany(mappedBy = "roles")
    @Builder.Default
    private Set<User> users = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Role other)) return false;
        if (getId() != null && other.getId() != null) {
            return Objects.equals(getId(), other.getId());
        }
        if (getName() != null && other.getName() != null) {
            return Objects.equals(getName(), other.getName());
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (getId() != null) {
            return Objects.hash(getId());
        }
        if (getName() != null) {
            return Objects.hash(getName());
        }
        return System.identityHashCode(this);
    }
}
