package org.synyx.urlaubsverwaltung.person.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.validation.annotation.Validated;

import java.util.Objects;

@Validated
public class PersonProvisionDto {

    @NotEmpty
    private String firstName;
    @NotEmpty
    private String lastName;

    @Email
    @NotEmpty
    private String email;

    PersonProvisionDto(String firstName, String lastName, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (PersonProvisionDto) obj;
        return Objects.equals(this.firstName, that.firstName) &&
            Objects.equals(this.lastName, that.lastName) &&
            Objects.equals(this.email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName, email);
    }
}
