/*    */ package com.android.volley;
/*    */ 
/*    */ public class Response<T>
/*    */ {
/*    */   public final T result;
/*    */   public final Cache.Entry cacheEntry;
/*    */   public final VolleyError error;
/* 64 */   public boolean intermediate = false;
/*    */ 
/*    */   public static <T> Response<T> success(T result, Cache.Entry cacheEntry)
/*    */   {
/* 43 */     return new Response(result, cacheEntry);
/*    */   }
/*    */ 
/*    */   public static <T> Response<T> error(VolleyError error)
/*    */   {
/* 51 */     return new Response(error);
/*    */   }
/*    */ 
/*    */   public boolean isSuccess()
/*    */   {
/* 70 */     return this.error == null;
/*    */   }
/*    */ 
/*    */   private Response(T result, Cache.Entry cacheEntry)
/*    */   {
/* 75 */     this.result = result;
/* 76 */     this.cacheEntry = cacheEntry;
/* 77 */     this.error = null;
/*    */   }
/*    */ 
/*    */   private Response(VolleyError error) {
/* 81 */     this.result = null;
/* 82 */     this.cacheEntry = null;
/* 83 */     this.error = error;
/*    */   }
/*    */ 
/*    */   public static abstract interface ErrorListener
/*    */   {
/*    */     public abstract void onErrorResponse(VolleyError paramVolleyError);
/*    */   }
/*    */ 
/*    */   public static abstract interface Listener<T>
/*    */   {
/*    */     public abstract void onResponse(T paramT);
/*    */   }
/*    */ }

/* Location:           E:\volley.jar
 * Qualified Name:     com.android.volley.Response
 * JD-Core Version:    0.5.4
 */