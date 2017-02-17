/*    */ package com.android.volley;
/*    */ 
/*    */ public class NetworkError extends VolleyError
/*    */ {
/*    */   public NetworkError()
/*    */   {
/*    */   }
/*    */ 
/*    */   public NetworkError(Throwable cause)
/*    */   {
/* 29 */     super(cause);
/*    */   }
/*    */ 
/*    */   public NetworkError(NetworkResponse networkResponse) {
/* 33 */     super(networkResponse);
/*    */   }
/*    */ }

/* Location:           E:\volley.jar
 * Qualified Name:     com.android.volley.NetworkError
 * JD-Core Version:    0.5.4
 */