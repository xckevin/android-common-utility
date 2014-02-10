package com.xckevin.android;

import java.io.File;

import android.os.Environment;

public class Env {

	public static  String ROOT_DIR = Environment.getExternalStorageDirectory().getPath() + File.separator + "xckevin";

}
