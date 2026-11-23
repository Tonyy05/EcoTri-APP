package iut.dam.ecotri;

import android.content.Context;
import android.content.SharedPreferences;

import iut.dam.ecotri.data.LoginResponse;
import iut.dam.ecotri.data.UserDto;

public class SessionManager {

    private static final String PREF_NAME = "eco_tri_prefs";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER_ID = "userId";

    private static final String KEY_FIRST_NAME = "firstName";
    private static final String KEY_LAST_NAME = "lastName";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_ADRESSE = "adresse";
    private static final String KEY_DOB = "dateNaissance";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_ROLE = "role";

    private static final String KEY_NOTIF_EMAIL = "notificationsEmail";

    // ✅ NEW: force change password
    private static final String KEY_MUST_CHANGE_PWD = "mustChangePassword";

    private final SharedPreferences prefs;

    public SessionManager(Context ctx) {
        prefs = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveLogin(LoginResponse response) {
        UserDto u = response.getUser();
        prefs.edit()
                .putString(KEY_TOKEN, response.getToken())
                .putInt(KEY_USER_ID, u.getId()) // ✅ AJOUT ICI
                .putString(KEY_FIRST_NAME, u.getFirstName())
                .putString(KEY_LAST_NAME, u.getLastName())
                .putString(KEY_EMAIL, u.getEmail())
                .putString(KEY_ADRESSE, u.getAdresse())
                .putString(KEY_DOB, u.getDateNaissance())
                .putString(KEY_PHONE, u.getPhone())
                .putString(KEY_ROLE, u.getRole())
                .putBoolean(KEY_NOTIF_EMAIL, u.getNotificationsEmail())
                .putBoolean(KEY_MUST_CHANGE_PWD, response.getMustChangePassword())
                .apply();
    }


    public void updateProfile(UserDto u) {
        prefs.edit()
                .putString(KEY_FIRST_NAME, u.getFirstName())
                .putString(KEY_LAST_NAME, u.getLastName())
                .putString(KEY_EMAIL, u.getEmail())
                .putString(KEY_ADRESSE, u.getAdresse())
                .putString(KEY_DOB, u.getDateNaissance())
                .putString(KEY_PHONE, u.getPhone())
                .putBoolean(KEY_NOTIF_EMAIL, u.getNotificationsEmail())
                .apply();
    }

    public String getToken()     { return prefs.getString(KEY_TOKEN, null); }
    public String getFirstName() { return prefs.getString(KEY_FIRST_NAME, ""); }
    public String getLastName()  { return prefs.getString(KEY_LAST_NAME, ""); }
    public String getEmail()     { return prefs.getString(KEY_EMAIL, ""); }
    public String getAdresse()   { return prefs.getString(KEY_ADRESSE, ""); }
    public String getDob()       { return prefs.getString(KEY_DOB, ""); }
    public String getPhone()     { return prefs.getString(KEY_PHONE, ""); }
    public String getRole()      { return prefs.getString(KEY_ROLE, ""); }

    public boolean getNotificationsEmail() {
        return prefs.getBoolean(KEY_NOTIF_EMAIL, true);
    }

    public void setNotificationsEmail(boolean enabled) {
        prefs.edit().putBoolean(KEY_NOTIF_EMAIL, enabled).apply();
    }

    // ✅ must change password
    public boolean getMustChangePassword() {
        return prefs.getBoolean(KEY_MUST_CHANGE_PWD, false);
    }

    public void setMustChangePassword(boolean value) {
        prefs.edit().putBoolean(KEY_MUST_CHANGE_PWD, value).apply();
    }

    // ✅ logged in helper
    public boolean isLoggedIn() {
        String token = getToken();
        return token != null && !token.isEmpty();
    }

    public void clear() {
        prefs.edit().clear().apply();
    }

    public void logout() {
        prefs.edit().clear().apply();
    }
    public Integer getUserId() {
        if (!prefs.contains(KEY_USER_ID)) return null;
        int id = prefs.getInt(KEY_USER_ID, -1);
        return id == -1 ? null : id;
    }


}
