package iut.dam.powerhome;

import com.google.gson.Gson;

public class User {
    public String firstname;
    public String lastname;
    public String email;
    public String tel;
    public Habitat habitat;
    public String username;

    public static User getFromJson(String json) {
        return new Gson().fromJson(json, User.class);
    }
}
