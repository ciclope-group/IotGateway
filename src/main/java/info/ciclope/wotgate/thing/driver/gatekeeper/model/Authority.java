package info.ciclope.wotgate.thing.driver.gatekeeper.model;

import io.vertx.core.json.JsonObject;

public class Authority {

    public long id;

    public String name;

    public Authority(JsonObject object) {
        this.id = object.getLong("id");
        this.name = object.getString("name");
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
