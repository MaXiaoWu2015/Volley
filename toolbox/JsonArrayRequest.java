/*    */ package com.android.volley.toolbox;
/*    */ 
/*    */ import com.android.volley.NetworkResponse;
/*    */ import com.android.volley.ParseError;
/*    */ import com.android.volley.Response;
/*    */ import com.android.volley.Response.ErrorListener;
/*    */ import com.android.volley.Response.Listener;
/*    */ import java.io.UnsupportedEncodingException;
/*    */ import org.json.JSONArray;
/*    */ import org.json.JSONException;
/*    */ 
/*    */ public class JsonArrayRequest extends JsonRequest<JSONArray>
/*    */ {
/*    */   public JsonArrayRequest(String url, Response.Listener<JSONArray> listener, Response.ErrorListener errorListener)
/*    */   {
/* 42 */     super(0, url, null, listener, errorListener);
/*    */   }
/*    */ 
/*    */   public JsonArrayRequest(int method, String url, JSONArray jsonRequest, Response.Listener<JSONArray> listener, Response.ErrorListener errorListener)
/*    */   {
/* 57 */     super(method, url, (jsonRequest == null) ? null : jsonRequest.toString(), listener, 
/* 57 */       errorListener);
/*    */   }
/*    */ 
/*    */   protected Response<JSONArray> parseNetworkResponse(NetworkResponse response)
/*    */   {
/*    */     try {
/* 63 */       String jsonString = new String(response.data, 
/* 64 */         HttpHeaderParser.parseCharset(response.headers, "utf-8"));
/* 65 */       return Response.success(new JSONArray(jsonString), 
/* 66 */         HttpHeaderParser.parseCacheHeaders(response));
/*    */     } catch (UnsupportedEncodingException e) {
/* 68 */       return Response.error(new ParseError(e)); } catch (JSONException je) {
/*    */     }
/* 70 */     return Response.error(new ParseError(je));
/*    */   }
/*    */ }

/* Location:           E:\volley.jar
 * Qualified Name:     com.android.volley.toolbox.JsonArrayRequest
 * JD-Core Version:    0.5.4
 */