package com.nthexperience.desktop;

import android.content.*;

/** Test APK only: establishes a real URI grant from the fixture provider owner. */
public final class XpFeatureGrantReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context,Intent intent){
        context.getContentResolver().call(XpFeatureDocumentsProvider.AUTHORITY,"resetFixture","com.nthexperience.desktop.xp",null);
    }
}
