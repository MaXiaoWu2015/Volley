/*    */ package com.android.volley.toolbox;
/*    */ 
/*    */ import com.android.volley.NetworkResponse;
/*    */ import com.android.volley.Request;
/*    */ import com.android.volley.Response;
/*    */ import com.android.volley.Response.ErrorListener;
/*    */ import com.android.volley.Response.Listener;
/*    */ import java.io.UnsupportedEncodingException;
/*    */ 
/*    */ public class StringRequest extends Request<String>
/*    */ {
/*    */   private final Response.Listener<String> mListener;
/*    */ 
/*    */   public StringRequest(int method, String url, Response.Listener<String> listener, Response.ErrorListener errorListener)
/*    */   {
/* 43 */     super(method, url, errorListener);
/* 44 */     this.mListener = listener;
/*    */   }
/*    */ 
/*    */   public StringRequest(String url, Response.Listener<String> listener, Response.ErrorListener errorListener)
/*    */   {
/* 55 */     this(0, url, listener, errorListener);
/*    */   }
/*    */ 
/*    */   protected void deliverResponse(String response)
/*    */   {
/* 60 */     this.mListener.onResponse(response);
/*    */   }
/*    */ 
/*    */   protected Response<String> parseNetworkResponse(NetworkResponse response) {
/*    */     String parsed;
/*    */     String parsed;
/*    */     try {
/* 67 */       parsed = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
/*    */     } catch (UnsupportedEncodingException e) {
/* 69 */       parsed = new String(response.data);
/*    */     }
/* 71 */     return Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response));
/*    */   }
/*    */ }

/* Location:           E:\volley.jar
 * Qualified Name:     com.android.volley.toolbox.StringRequest
 * JD-Core Version:    0.5.4
 */