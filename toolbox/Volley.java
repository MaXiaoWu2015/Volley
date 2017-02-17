/*    */ package com.android.volley.toolbox;
/*    */ 
/*    */ import android.content.Context;
/*    */ import android.content.pm.PackageInfo;
/*    */ import android.content.pm.PackageManager;
/*    */ import android.content.pm.PackageManager.NameNotFoundException;
/*    */ import android.net.http.AndroidHttpClient;
/*    */ import android.os.Build.VERSION;
/*    */ import com.android.volley.Network;
/*    */ import com.android.volley.RequestQueue;
/*    */ import java.io.File;
/*    */ 
/*    */ public class Volley
/*    */ {
/*    */   private static final String DEFAULT_CACHE_DIR = "volley";
/*    */ 
/*    */   public static RequestQueue newRequestQueue(Context context, HttpStack stack)
/*    */   {
/* 43 */     File cacheDir = new File(context.getCacheDir(), "volley");
/*    */ 
/* 45 */     String userAgent = "volley/0";
/*    */     try {
/* 47 */       String packageName = context.getPackageName();
/* 48 */       PackageInfo info = context.getPackageManager().getPackageInfo(packageName, 0);
/* 49 */       userAgent = packageName + "/" + info.versionCode;
/*    */     }
/*    */     catch (PackageManager.NameNotFoundException localNameNotFoundException) {
/*    */     }
/* 53 */     if (stack == null) {
/* 54 */       if (Build.VERSION.SDK_INT >= 9) {
/* 55 */         stack = new HurlStack();
/*    */       }
/*    */       else
/*    */       {
/* 59 */         stack = new HttpClientStack(AndroidHttpClient.newInstance(userAgent));
/*    */       }
/*    */     }
/*    */ 
/* 63 */     Network network = new BasicNetwork(stack);
/*    */ 
/* 65 */     RequestQueue queue = new RequestQueue(new DiskBasedCache(cacheDir), network);
/* 66 */     queue.start();
/*    */ 
/* 68 */     return queue;
/*    */   }
/*    */ 
/*    */   public static RequestQueue newRequestQueue(Context context)
/*    */   {
/* 78 */     return newRequestQueue(context, null);
/*    */   }
/*    */ }

/* Location:           E:\volley.jar
 * Qualified Name:     com.android.volley.toolbox.Volley
 * JD-Core Version:    0.5.4
 */