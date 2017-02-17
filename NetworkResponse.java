/*    */ package com.android.volley;
/*    */ 
/*    */ import java.util.Collections;
/*    */ import java.util.Map;
/*    */ 
/*    */ public class NetworkResponse
/*    */ {
/*    */   public final int statusCode;
/*    */   public final byte[] data;
/*    */   public final Map<String, String> headers;
/*    */   public final boolean notModified;
/*    */   public final long networkTimeMs;
/*    */ 
/*    */   public NetworkResponse(int statusCode, byte[] data, Map<String, String> headers, boolean notModified, long networkTimeMs)
/*    */   {
/* 38 */     this.statusCode = statusCode;
/* 39 */     this.data = data;
/* 40 */     this.headers = headers;
/* 41 */     this.notModified = notModified;
/* 42 */     this.networkTimeMs = networkTimeMs;
/*    */   }
/*    */ 
/*    */   public NetworkResponse(int statusCode, byte[] data, Map<String, String> headers, boolean notModified)
/*    */   {
/* 47 */     this(statusCode, data, headers, notModified, 0L);
/*    */   }
/*    */ 
/*    */   public NetworkResponse(byte[] data) {
/* 51 */     this(200, data, Collections.emptyMap(), false, 0L);
/*    */   }
/*    */ 
/*    */   public NetworkResponse(byte[] data, Map<String, String> headers) {
/* 55 */     this(200, data, headers, false, 0L);
/*    */   }
/*    */ }

/* Location:           E:\volley.jar
 * Qualified Name:     com.android.volley.NetworkResponse
 * JD-Core Version:    0.5.4
 */