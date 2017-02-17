/*    */ package com.android.volley.toolbox;
/*    */ 
/*    */ import com.android.volley.NetworkResponse;
/*    */ import com.android.volley.ParseError;
/*    */ import com.android.volley.Response;
/*    */ import com.android.volley.Response.ErrorListener;
/*    */ import com.android.volley.Response.Listener;
/*    */ import java.io.UnsupportedEncodingException;
/*    */ import org.json.JSONException;
/*    */ import org.json.JSONObject;
/*    */ 
/*    */ public class JsonObjectRequest extends JsonRequest<JSONObject>
/*    */ {
/*    */   public JsonObjectRequest(int method, String url, JSONObject jsonRequest, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener)
/*    */   {
/* 48 */     super(method, url, (jsonRequest == null) ? null : jsonRequest.toString(), listener, 
/* 48 */       errorListener);
/*    */   }
/*    */ 
/*    */   public JsonObjectRequest(String url, JSONObject jsonRequest, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener)
/*    */   {
/* 60 */     this((jsonRequest == null) ? 0 : 1, url, jsonRequest, 
/* 60 */       listener, errorListener);
/*    */   }
/*    */ 
/*    */   protected Response<JSONObject> parseNetworkResponse(NetworkResponse response)
/*    */   {
/*    */     try {
/* 66 */       String jsonString = new String(response.data, 
/* 67 */         HttpHeaderParser.parseCharset(response.headers, "utf-8"));
/* 68 */       return Response.success(new JSONObject(jsonString), 
/* 69 */         HttpHeaderParser.parseCacheHeaders(response));
/*    */     } catch (UnsupportedEncodingException e) {
/* 71 */       return Response.error(new ParseError(e)); } catch (JSONException je) {
/*    */     }
/* 73 */     return Response.error(new ParseError(je));
/*    */   }
/*    */ }

/* Location:           E:\volley.jar
 * Qualified Name:     com.android.volley.toolbox.JsonObjectRequest
 * JD-Core Version:    0.5.4
 */