/*    */ package com.android.volley;
/*    */ 
/*    */ public class ParseError extends VolleyError
/*    */ {
/*    */   public ParseError()
/*    */   {
/*    */   }
/*    */ 
/*    */   public ParseError(NetworkResponse networkResponse)
/*    */   {
/* 27 */     super(networkResponse);
/*    */   }
/*    */ 
/*    */   public ParseError(Throwable cause) {
/* 31 */     super(cause);
/*    */   }
/*    */ }

/* Location:           E:\volley.jar
 * Qualified Name:     com.android.volley.ParseError
 * JD-Core Version:    0.5.4
 */