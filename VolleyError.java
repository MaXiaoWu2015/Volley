/*    */ package com.android.volley;
/*    */ 
/*    */ public class VolleyError extends Exception
/*    */ {
/*    */   public final NetworkResponse networkResponse;
/*    */   private long networkTimeMs;
/*    */ 
/*    */   public VolleyError()
/*    */   {
/* 28 */     this.networkResponse = null;
/*    */   }
/*    */ 
/*    */   public VolleyError(NetworkResponse response) {
/* 32 */     this.networkResponse = response;
/*    */   }
/*    */ 
/*    */   public VolleyError(String exceptionMessage) {
/* 36 */     super(exceptionMessage);
/* 37 */     this.networkResponse = null;
/*    */   }
/*    */ 
/*    */   public VolleyError(String exceptionMessage, Throwable reason) {
/* 41 */     super(exceptionMessage, reason);
/* 42 */     this.networkResponse = null;
/*    */   }
/*    */ 
/*    */   public VolleyError(Throwable cause) {
/* 46 */     super(cause);
/* 47 */     this.networkResponse = null;
/*    */   }
/*    */ 
/*    */   void setNetworkTimeMs(long networkTimeMs) {
/* 51 */     this.networkTimeMs = networkTimeMs;
/*    */   }
/*    */ 
/*    */   public long getNetworkTimeMs() {
/* 55 */     return this.networkTimeMs;
/*    */   }
/*    */ }

/* Location:           E:\volley.jar
 * Qualified Name:     com.android.volley.VolleyError
 * JD-Core Version:    0.5.4
 */