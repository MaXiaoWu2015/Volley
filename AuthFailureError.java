/*    */ package com.android.volley;
/*    */ 
/*    */ import android.content.Intent;
/*    */ 
/*    */ public class AuthFailureError extends VolleyError
/*    */ {
/*    */   private Intent mResolutionIntent;
/*    */ 
/*    */   public AuthFailureError()
/*    */   {
/*    */   }
/*    */ 
/*    */   public AuthFailureError(Intent intent)
/*    */   {
/* 32 */     this.mResolutionIntent = intent;
/*    */   }
/*    */ 
/*    */   public AuthFailureError(NetworkResponse response) {
/* 36 */     super(response);
/*    */   }
/*    */ 
/*    */   public AuthFailureError(String message) {
/* 40 */     super(message);
/*    */   }
/*    */ 
/*    */   public AuthFailureError(String message, Exception reason) {
/* 44 */     super(message, reason);
/*    */   }
/*    */ 
/*    */   public Intent getResolutionIntent() {
/* 48 */     return this.mResolutionIntent;
/*    */   }
/*    */ 
/*    */   public String getMessage()
/*    */   {
/* 53 */     if (this.mResolutionIntent != null) {
/* 54 */       return "User needs to (re)enter credentials.";
/*    */     }
/* 56 */     return super.getMessage();
/*    */   }
/*    */ }

/* Location:           E:\volley.jar
 * Qualified Name:     com.android.volley.AuthFailureError
 * JD-Core Version:    0.5.4
 */