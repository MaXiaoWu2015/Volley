/*    */ package com.android.volley.toolbox;
/*    */ 
/*    */ import com.android.volley.NetworkResponse;
/*    */ import com.android.volley.Request;
/*    */ import com.android.volley.Response;
/*    */ import com.android.volley.Response.ErrorListener;
/*    */ import com.android.volley.Response.Listener;
/*    */ import com.android.volley.VolleyLog;
/*    */ import java.io.UnsupportedEncodingException;
/*    */ 
/*    */ public abstract class JsonRequest<T> extends Request<T>
/*    */ {
/*    */   protected static final String PROTOCOL_CHARSET = "utf-8";
/* 40 */   private static final String PROTOCOL_CONTENT_TYPE = String.format("application/json; charset=%s", new Object[] { "utf-8" });
/*    */   private final Response.Listener<T> mListener;
/*    */   private final String mRequestBody;
/*    */ 
/*    */   /** @deprecated */
/*    */   public JsonRequest(String url, String requestBody, Response.Listener<T> listener, Response.ErrorListener errorListener)
/*    */   {
/* 53 */     this(-1, url, requestBody, listener, errorListener);
/*    */   }
/*    */ 
/*    */   public JsonRequest(int method, String url, String requestBody, Response.Listener<T> listener, Response.ErrorListener errorListener)
/*    */   {
/* 58 */     super(method, url, errorListener);
/* 59 */     this.mListener = listener;
/* 60 */     this.mRequestBody = requestBody;
/*    */   }
/*    */ 
/*    */   protected void deliverResponse(T response)
/*    */   {
/* 65 */     this.mListener.onResponse(response);
/*    */   }
/*    */ 
/*    */   protected abstract Response<T> parseNetworkResponse(NetworkResponse paramNetworkResponse);
/*    */ 
/*    */   /** @deprecated */
/*    */   public String getPostBodyContentType()
/*    */   {
/* 76 */     return getBodyContentType();
/*    */   }
/*    */ 
/*    */   /** @deprecated */
/*    */   public byte[] getPostBody()
/*    */   {
/* 84 */     return getBody();
/*    */   }
/*    */ 
/*    */   public String getBodyContentType()
/*    */   {
/* 89 */     return PROTOCOL_CONTENT_TYPE;
/*    */   }
/*    */ 
/*    */   public byte[] getBody()
/*    */   {
/*    */     try {
/* 95 */       return (this.mRequestBody == null) ? null : this.mRequestBody.getBytes("utf-8");
/*    */     } catch (UnsupportedEncodingException uee) {
/* 97 */       VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", new Object[] { 
/* 98 */         this.mRequestBody, "utf-8" });
/* 99 */     }return null;
/*    */   }
/*    */ }

/* Location:           E:\volley.jar
 * Qualified Name:     com.android.volley.toolbox.JsonRequest
 * JD-Core Version:    0.5.4
 */