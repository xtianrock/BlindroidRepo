package com.Xtian.Blindroid.Gcm;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

/**
 * Created by xtianrock on 03/05/2015.
 */
public class Util {
    static String getEmail(Context context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String email = prefs.getString("email", "");
        if (TextUtils.isEmpty(email)) {
            Log.i("email", "email not found.");
            AccountManager accountManager = AccountManager.get(context);
            Account account = getAccount(accountManager);
            if (account == null) {
                return null;
            } else {
               storeEmail(context,account.name);
                return account.name;
            }
        }
        else
        {
            return email;
        }
    }
    private static void storeEmail(Context context, String email) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("email", email);
        editor.commit();
    }

    private static Account getAccount(AccountManager accountManager) {
        Account[] accounts = accountManager.getAccountsByType("com.google");
        Account account;
        if (accounts.length > 0) {
            account = accounts[0];
        } else {
            account = null;
        }
        return account;
    }
}
