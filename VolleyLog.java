/*     */ package com.android.volley;
/*     */ 
/*     */ import android.os.SystemClock;
/*     */ import android.util.Log;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import java.util.Locale;
/*     */ 
/*     */ public class VolleyLog
/*     */ {
/*  33 */   public static String TAG = "Volley";
/*     */ 
/*  35 */   public static boolean DEBUG = Log.isLoggable(TAG, 2);
/*     */ 
/*     */   public static void setTag(String tag)
/*     */   {
/*  46 */     d("Changing log tag to %s", new Object[] { tag });
/*  47 */     TAG = tag;
/*     */ 
/*  50 */     DEBUG = Log.isLoggable(TAG, 2);
/*     */   }
/*     */ 
/*     */   public static void v(String format, Object[] args) {
/*  54 */     if (DEBUG)
/*  55 */       Log.v(TAG, buildMessage(format, args));
/*     */   }
/*     */ 
/*     */   public static void d(String format, Object[] args)
/*     */   {
/*  60 */     Log.d(TAG, buildMessage(format, args));
/*     */   }
/*     */ 
/*     */   public static void e(String format, Object[] args) {
/*  64 */     Log.e(TAG, buildMessage(format, args));
/*     */   }
/*     */ 
/*     */   public static void e(Throwable tr, String format, Object[] args) {
/*  68 */     Log.e(TAG, buildMessage(format, args), tr);
/*     */   }
/*     */ 
/*     */   public static void wtf(String format, Object[] args) {
/*  72 */     Log.wtf(TAG, buildMessage(format, args));
/*     */   }
/*     */ 
/*     */   public static void wtf(Throwable tr, String format, Object[] args) {
/*  76 */     Log.wtf(TAG, buildMessage(format, args), tr);
/*     */   }
/*     */ 
/*     */   private static String buildMessage(String format, Object[] args)
/*     */   {
/*  84 */     String msg = (args == null) ? format : String.format(Locale.US, format, args);
/*  85 */     StackTraceElement[] trace = new Throwable().fillInStackTrace().getStackTrace();
/*     */ 
/*  87 */     String caller = "<unknown>";
/*     */ 
/*  90 */     for (int i = 2; i < trace.length; ++i) {
/*  91 */       Class clazz = trace[i].getClass();
/*  92 */       if (!clazz.equals(VolleyLog.class)) {
/*  93 */         String callingClass = trace[i].getClassName();
/*  94 */         callingClass = callingClass.substring(callingClass.lastIndexOf('.') + 1);
/*  95 */         callingClass = callingClass.substring(callingClass.lastIndexOf('$') + 1);
/*     */ 
/*  97 */         caller = callingClass + "." + trace[i].getMethodName();
/*  98 */         break;
/*     */       }
/*     */     }
/* 101 */     return String.format(Locale.US, "[%d] %s: %s", new Object[] { 
/* 102 */       Long.valueOf(Thread.currentThread().getId()), caller, msg });
/*     */   }
/*     */ 
/*     */   static class MarkerLog
/*     */   {
/* 109 */     public static final boolean ENABLED = VolleyLog.DEBUG;
/*     */     private static final long MIN_DURATION_FOR_LOGGING_MS = 0L;
/* 126 */     private final List<Marker> mMarkers = new ArrayList();
/* 127 */     private boolean mFinished = false;
/*     */ 
/*     */     public synchronized void add(String name, long threadId)
/*     */     {
/* 131 */       if (this.mFinished) {
/* 132 */         throw new IllegalStateException("Marker added to finished log");
/*     */       }
/*     */ 
/* 135 */       this.mMarkers.add(new Marker(name, threadId, SystemClock.elapsedRealtime()));
/*     */     }
/*     */ 
/*     */     public synchronized void finish(String header)
/*     */     {
/* 144 */       this.mFinished = true;
/*     */ 
/* 146 */       long duration = getTotalDuration();
/* 147 */       if (duration <= 0L) {
/* 148 */         return;
/*     */       }
/*     */ 
/* 151 */       long prevTime = ((Marker)this.mMarkers.get(0)).time;
/* 152 */       VolleyLog.d("(%-4d ms) %s", new Object[] { Long.valueOf(duration), header });
/* 153 */       for (Marker marker : this.mMarkers) {
/* 154 */         long thisTime = marker.time;
/* 155 */         VolleyLog.d("(+%-4d) [%2d] %s", new Object[] { Long.valueOf(thisTime - prevTime), Long.valueOf(marker.thread), marker.name });
/* 156 */         prevTime = thisTime;
/*     */       }
/*     */     }
/*     */ 
/*     */     protected void finalize()
/*     */       throws Throwable
/*     */     {
/* 164 */       if (!this.mFinished) {
/* 165 */         finish("Request on the loose");
/* 166 */         VolleyLog.e("Marker log finalized without finish() - uncaught exit point for request", new Object[0]);
/*     */       }
/*     */     }
/*     */ 
/*     */     private long getTotalDuration()
/*     */     {
/* 172 */       if (this.mMarkers.size() == 0) {
/* 173 */         return 0L;
/*     */       }
/*     */ 
/* 176 */       long first = ((Marker)this.mMarkers.get(0)).time;
/* 177 */       long last = ((Marker)this.mMarkers.get(this.mMarkers.size() - 1)).time;
/* 178 */       return last - first;
/*     */     }
/*     */ 
/*     */     private static class Marker
/*     */     {
/*     */       public final String name;
/*     */       public final long thread;
/*     */       public final long time;
/*     */ 
/*     */       public Marker(String name, long thread, long time)
/*     */       {
/* 120 */         this.name = name;
/* 121 */         this.thread = thread;
/* 122 */         this.time = time;
/*     */       }
/*     */     }
/*     */   }
/*     */ }

/* Location:           E:\volley.jar
 * Qualified Name:     com.android.volley.VolleyLog
 * JD-Core Version:    0.5.4
 */