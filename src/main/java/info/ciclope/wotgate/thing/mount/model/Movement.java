package info.ciclope.wotgate.thing.mount.model;

import java.util.regex.Pattern;

public class Movement {
    private static final String regexPattern = "-?\\d{2}:\\d{2}(:\\d{2})?";

    private String rightAscension;

    private String declination;

    public String getRightAscension() {
        return rightAscension;
    }

    public void setRightAscension(String rightAscension) {
        this.rightAscension = rightAscension;
    }

    public String getDeclination() {
        return declination;
    }

    public void setDeclination(String declination) {
        this.declination = declination;
    }

    @SuppressWarnings("RedundantIfStatement")
    public boolean validate() {
        if (rightAscension != null && declination != null) {
            if (Pattern.matches(regexPattern, rightAscension) && Pattern.matches(regexPattern, declination)) {
                return true;
            }
        }

        return false;
    }
}
