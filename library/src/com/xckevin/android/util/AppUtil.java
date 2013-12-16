package com.xckevin.android.util;

import java.util.Iterator;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;

public class AppUtil {

	/**
	 * if the phone has the app named packageName
	 * @param packageName
	 * @return
	 */
	public static boolean isPackageExisted(Context context, String packageName) {
		PackageInfo packageInfo;

		try { 
			packageInfo = context.getPackageManager().getPackageInfo(packageName, 0); 
		} catch (NameNotFoundException e) { 
			packageInfo = null; 
			e.printStackTrace(); 
		} 
		if(packageInfo == null){ 
			return false;
		}else{ 
			return true;
		}
	}

	/**
	 * open another app
	 * @param context
	 * @param packageName
	 * @throws NameNotFoundException 
	 */
	public static void openApp(Context context, String packageName) throws NameNotFoundException {
		PackageInfo pi = context.getPackageManager().getPackageInfo(packageName, 0);

		Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
		resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		resolveIntent.setPackage(pi.packageName);

		List<ResolveInfo> apps = context.getPackageManager().queryIntentActivities(resolveIntent, 0);

		Iterator<ResolveInfo> iterator = apps.iterator();
		while(iterator.hasNext()) {
			ResolveInfo ri = iterator.next();
			if (ri != null ) {
				packageName = ri.activityInfo.packageName;
				String className = ri.activityInfo.name;
				Intent intent = new Intent(Intent.ACTION_MAIN);
				intent.addCategory(Intent.CATEGORY_LAUNCHER);
				ComponentName cn = new ComponentName(packageName, className);
				intent.setComponent(cn);
				context.startActivity(intent);
			}
		}
	}
}
