/*     */ package com.android.volley.toolbox;
/*     */ 
/*     */ import com.android.volley.AuthFailureError;
/*     */ import com.android.volley.Request;
/*     */ import java.io.IOException;
/*     */ import java.net.URI;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import org.apache.http.HttpEntity;
/*     */ import org.apache.http.HttpResponse;
/*     */ import org.apache.http.NameValuePair;
/*     */ import org.apache.http.client.HttpClient;
/*     */ import org.apache.http.client.methods.HttpDelete;
/*     */ import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
/*     */ import org.apache.http.client.methods.HttpGet;
/*     */ import org.apache.http.client.methods.HttpHead;
/*     */ import org.apache.http.client.methods.HttpOptions;
/*     */ import org.apache.http.client.methods.HttpPost;
/*     */ import org.apache.http.client.methods.HttpPut;
/*     */ import org.apache.http.client.methods.HttpTrace;
/*     */ import org.apache.http.client.methods.HttpUriRequest;
/*     */ import org.apache.http.entity.ByteArrayEntity;
/*     */ import org.apache.http.message.BasicNameValuePair;
/*     */ import org.apache.http.params.HttpConnectionParams;
/*     */ import org.apache.http.params.HttpParams;
/*     */ 
/*     */ public class HttpClientStack
/*     */   implements HttpStack
/*     */ {
/*     */   protected final HttpClient mClient;
/*     */   private static final String HEADER_CONTENT_TYPE = "Content-Type";
/*     */ 
/*     */   public HttpClientStack(HttpClient client)
/*     */   {
/*  56 */     this.mClient = client;
/*     */   }
/*     */ 
/*     */   private static void addHeaders(HttpUriRequest httpRequest, Map<String, String> headers) {
/*  60 */     for (String key : headers.keySet())
/*  61 */       httpRequest.setHeader(key, (String)headers.get(key));
/*     */   }
/*     */ 
/*     */   private static List<NameValuePair> getPostParameterPairs(Map<String, String> postParams)
/*     */   {
/*  67 */     List result = new ArrayList(postParams.size());
/*  68 */     for (String key : postParams.keySet()) {
/*  69 */       result.add(new BasicNameValuePair(key, (String)postParams.get(key)));
/*     */     }
/*  71 */     return result;
/*     */   }
/*     */ 
/*     */   public HttpResponse performRequest(Request<?> request, Map<String, String> additionalHeaders)
/*     */     throws IOException, AuthFailureError
/*     */   {
/*  77 */     HttpUriRequest httpRequest = createHttpRequest(request, additionalHeaders);
/*  78 */     addHeaders(httpRequest, additionalHeaders);
/*  79 */     addHeaders(httpRequest, request.getHeaders());
/*  80 */     onPrepareRequest(httpRequest);
/*  81 */     HttpParams httpParams = httpRequest.getParams();
/*  82 */     int timeoutMs = request.getTimeoutMs();
/*     */ 
/*  85 */     HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
/*  86 */     HttpConnectionParams.setSoTimeout(httpParams, timeoutMs);
/*  87 */     return this.mClient.execute(httpRequest);
/*     */   }
/*     */ 
/*     */   static HttpUriRequest createHttpRequest(Request<?> request, Map<String, String> additionalHeaders)
/*     */     throws AuthFailureError
/*     */   {
/*  96 */     switch (request.getMethod())
/*     */     {
/*     */     case -1:
/* 101 */       byte[] postBody = request.getPostBody();
/* 102 */       if (postBody != null) {
/* 103 */         HttpPost postRequest = new HttpPost(request.getUrl());
/* 104 */         postRequest.addHeader("Content-Type", request.getPostBodyContentType());
/*     */ 
/* 106 */         HttpEntity entity = new ByteArrayEntity(postBody);
/* 107 */         postRequest.setEntity(entity);
/* 108 */         return postRequest;
/*     */       }
/* 110 */       return new HttpGet(request.getUrl());
/*     */     case 0:
/* 114 */       return new HttpGet(request.getUrl());
/*     */     case 3:
/* 116 */       return new HttpDelete(request.getUrl());
/*     */     case 1:
/* 118 */       HttpPost postRequest = new HttpPost(request.getUrl());
/* 119 */       postRequest.addHeader("Content-Type", request.getBodyContentType());
/* 120 */       setEntityIfNonEmptyBody(postRequest, request);
/* 121 */       return postRequest;
/*     */     case 2:
/* 124 */       HttpPut putRequest = new HttpPut(request.getUrl());
/* 125 */       putRequest.addHeader("Content-Type", request.getBodyContentType());
/* 126 */       setEntityIfNonEmptyBody(putRequest, request);
/* 127 */       return putRequest;
/*     */     case 4:
/* 130 */       return new HttpHead(request.getUrl());
/*     */     case 5:
/* 132 */       return new HttpOptions(request.getUrl());
/*     */     case 6:
/* 134 */       return new HttpTrace(request.getUrl());
/*     */     case 7:
/* 136 */       HttpPatch patchRequest = new HttpPatch(request.getUrl());
/* 137 */       patchRequest.addHeader("Content-Type", request.getBodyContentType());
/* 138 */       setEntityIfNonEmptyBody(patchRequest, request);
/* 139 */       return patchRequest;
/*     */     }
/*     */ 
/* 142 */     throw new IllegalStateException("Unknown request method.");
/*     */   }
/*     */ 
/*     */   private static void setEntityIfNonEmptyBody(HttpEntityEnclosingRequestBase httpRequest, Request<?> request)
/*     */     throws AuthFailureError
/*     */   {
/* 148 */     byte[] body = request.getBody();
/* 149 */     if (body != null) {
/* 150 */       HttpEntity entity = new ByteArrayEntity(body);
/* 151 */       httpRequest.setEntity(entity);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void onPrepareRequest(HttpUriRequest request)
/*     */     throws IOException
/*     */   {
/*     */   }
/*     */ 
/*     */   public static final class HttpPatch extends HttpEntityEnclosingRequestBase
/*     */   {
/*     */     public static final String METHOD_NAME = "PATCH";
/*     */ 
/*     */     public HttpPatch()
/*     */     {
/*     */     }
/*     */ 
/*     */     public HttpPatch(URI uri)
/*     */     {
/* 177 */       setURI(uri);
/*     */     }
/*     */ 
/*     */     public HttpPatch(String uri)
/*     */     {
/* 185 */       setURI(URI.create(uri));
/*     */     }
/*     */ 
/*     */     public String getMethod()
/*     */     {
/* 190 */       return "PATCH";
/*     */     }
/*     */   }
/*     */ }

/* Location:           E:\volley.jar
 * Qualified Name:     com.android.volley.toolbox.HttpClientStack
 * JD-Core Version:    0.5.4
 */