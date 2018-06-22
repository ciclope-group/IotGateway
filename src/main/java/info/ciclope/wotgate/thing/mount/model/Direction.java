package info.ciclope.wotgate.thing.mount.model;

public class Direction {
    private String direction;

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    @SuppressWarnings("RedundantIfStatement")
    public boolean validate() {
        if (direction != null) {
            if (direction.equals("Up") || direction.equals("Down") || direction.equals("Right") || direction.equals("left")) {
                return true;
            }
        }

        return false;
    }
}
