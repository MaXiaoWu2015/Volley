/*    */ package com.android.volley.toolbox;
/*    */ 
/*    */ import android.os.Handler;
/*    */ import android.os.Looper;
/*    */ import com.android.volley.Cache;
/*    */ import com.android.volley.NetworkResponse;
/*    */ import com.android.volley.Request;
/*    */ import com.android.volley.Request.Priority;
/*    */ import com.android.volley.Response;
/*    */ 
/*    */ public class ClearCacheRequest extends Request<Object>
/*    */ {
/*    */   private final Cache mCache;
/*    */   private final Runnable mCallback;
/*    */ 
/*    */   public ClearCacheRequest(Cache cache, Runnable callback)
/*    */   {
/* 41 */     super(0, null, null);
/* 42 */     this.mCache = cache;
/* 43 */     this.mCallback = callback;
/*    */   }
/*    */ 
/*    */   public boolean isCanceled()
/*    */   {
/* 49 */     this.mCache.clear();
/* 50 */     if (this.mCallback != null) {
/* 51 */       Handler handler = new Handler(Looper.getMainLooper());
/* 52 */       handler.postAtFrontOfQueue(this.mCallback);
/*    */     }
/* 54 */     return true;
/*    */   }
/*    */ 
/*    */   public Request.Priority getPriority()
/*    */   {
/* 59 */     return Request.Priority.IMMEDIATE;
/*    */   }
/*    */ 
/*    */   protected Response<Object> parseNetworkResponse(NetworkResponse response)
/*    */   {
/* 64 */     return null;
/*    */   }
/*    */ 
/*    */   protected void deliverResponse(Object response)
/*    */   {
/*    */   }
/*    */ }

/* Location:           E:\volley.jar
 * Qualified Name:     com.android.volley.toolbox.ClearCacheRequest
 * JD-Core Version:    0.5.4
 */