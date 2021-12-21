package ru.sfedu.model;

import com.opencsv.bean.CsvBindByPosition;

import java.util.Objects;

public class Human extends Subject {

    public Human() {
    }

    @CsvBindByPosition(position = 2)
    private String password;

    @CsvBindByPosition(position = 3)
    private String login;

    @CsvBindByPosition(position = 4)
    private String name;

    @CsvBindByPosition(position = 5)
    private String surname;

    @CsvBindByPosition(position = 6)
    private String patronymic;

    @CsvBindByPosition(position = 7)
    private String email;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getPatronymic() {
        return patronymic;
    }

    public void setPatronymic(String patronymic) {
        this.patronymic = patronymic;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Human)) return false;
        if (!super.equals(o)) return false;
        Human human = (Human) o;
        return Objects.equals(password, human.password) && Objects.equals(login, human.login) && Objects.equals(name, human.name) && Objects.equals(surname, human.surname) && Objects.equals(patronymic, human.patronymic) && Objects.equals(email, human.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), password, login, name, surname, patronymic, email);
    }

    @Override
    public String toString() {
        return "Human{" +
                "id=" + id +
                ", type=" + type +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", patronymic='" + patronymic + '\'' +
                ", password='" + password + '\'' +
                ", login='" + login + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
