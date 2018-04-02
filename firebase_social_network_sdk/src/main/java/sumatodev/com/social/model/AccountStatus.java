package sumatodev.com.social.model;

import java.util.Calendar;

import sumatodev.com.social.enums.Consts;

/**
 * Created by Ali on 02/04/2018.
 */

public class AccountStatus {

    public String profileStatus;
    public long actionDate;

    public AccountStatus() {

    }

    public AccountStatus(String profileStatus) {
        if (profileStatus.isEmpty()) {
            this.profileStatus = Consts.ACCOUNT_ACTIVE;
        } else {
            this.profileStatus = profileStatus;
        }
        this.actionDate = Calendar.getInstance().getTimeInMillis();
    }

}
