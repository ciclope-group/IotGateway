package info.ciclope.wotgate.thing.driver.gatekeeper.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;

public class User {

    private long id;
    private String username;

    @JsonIgnore
    private String password;
    private String email;
    private boolean enabled;

    public User(ResultSet resultSet) {
        JsonObject object = resultSet.getRows().get(0);

        this.id = object.getLong("id");
        this.username = object.getString("username");
        this.password = object.getString("password");
        this.email = object.getString("email");
        this.enabled = object.getInteger("enabled") != 0;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
