package iut.dam.powerhome;

import com.google.gson.Gson;

public class User {
    public String firstname;
    public String lastname;
    public String email;
    public String tel;

    public static User getFromJson(String json){
        Gson gson = new Gson();
        User obj = gson.fromJson(json, User.class);
        return obj;
    }
}
