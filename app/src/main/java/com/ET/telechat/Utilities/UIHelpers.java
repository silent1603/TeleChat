package com.ET.telechat.Utilities;

import android.content.Context;
import android.widget.Toast;

public class UIHelpers {
    public static void showToast(Context contentResolver, String message)
    {
        Toast.makeText(contentResolver,message,Toast.LENGTH_SHORT).show();
    }

}

