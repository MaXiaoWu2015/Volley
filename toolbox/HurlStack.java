/*     */ package com.android.volley.toolbox;
/*     */ 
/*     */ import com.android.volley.AuthFailureError;
/*     */ import com.android.volley.Request;
/*     */ import java.io.DataOutputStream;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.net.HttpURLConnection;
/*     */ import java.net.URL;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Map.Entry;
/*     */ import javax.net.ssl.HttpsURLConnection;
/*     */ import javax.net.ssl.SSLSocketFactory;
/*     */ import org.apache.http.Header;
/*     */ import org.apache.http.HttpEntity;
/*     */ import org.apache.http.HttpResponse;
/*     */ import org.apache.http.ProtocolVersion;
/*     */ import org.apache.http.StatusLine;
/*     */ import org.apache.http.entity.BasicHttpEntity;
/*     */ import org.apache.http.message.BasicHeader;
/*     */ import org.apache.http.message.BasicHttpResponse;
/*     */ import org.apache.http.message.BasicStatusLine;
/*     */ 
/*     */ public class HurlStack
/*     */   implements HttpStack
/*     */ {
/*     */   private static final String HEADER_CONTENT_TYPE = "Content-Type";
/*     */   private final UrlRewriter mUrlRewriter;
/*     */   private final SSLSocketFactory mSslSocketFactory;
/*     */ 
/*     */   public HurlStack()
/*     */   {
/*  68 */     this(null);
/*     */   }
/*     */ 
/*     */   public HurlStack(UrlRewriter urlRewriter)
/*     */   {
/*  75 */     this(urlRewriter, null);
/*     */   }
/*     */ 
/*     */   public HurlStack(UrlRewriter urlRewriter, SSLSocketFactory sslSocketFactory)
/*     */   {
/*  83 */     this.mUrlRewriter = urlRewriter;
/*  84 */     this.mSslSocketFactory = sslSocketFactory;
/*     */   }
/*     */ 
/*     */   public HttpResponse performRequest(Request<?> request, Map<String, String> additionalHeaders)
/*     */     throws IOException, AuthFailureError
/*     */   {
/*  90 */     String url = request.getUrl();
/*  91 */     HashMap map = new HashMap();
/*  92 */     map.putAll(request.getHeaders());
/*  93 */     map.putAll(additionalHeaders);
/*  94 */     if (this.mUrlRewriter != null) {
/*  95 */       String rewritten = this.mUrlRewriter.rewriteUrl(url);
/*  96 */       if (rewritten == null) {
/*  97 */         throw new IOException("URL blocked by rewriter: " + url);
/*     */       }
/*  99 */       url = rewritten;
/*     */     }
/* 101 */     URL parsedUrl = new URL(url);
/* 102 */     HttpURLConnection connection = openConnection(parsedUrl, request);
/* 103 */     for (String headerName : map.keySet()) {
/* 104 */       connection.addRequestProperty(headerName, (String)map.get(headerName));
/*     */     }
/* 106 */     setConnectionParametersForRequest(connection, request);
/*     */ 
/* 108 */     ProtocolVersion protocolVersion = new ProtocolVersion("HTTP", 1, 1);
/* 109 */     int responseCode = connection.getResponseCode();
/* 110 */     if (responseCode == -1)
/*     */     {
/* 113 */       throw new IOException("Could not retrieve response code from HttpUrlConnection.");
/*     */     }
/* 115 */     StatusLine responseStatus = new BasicStatusLine(protocolVersion, 
/* 116 */       connection.getResponseCode(), connection.getResponseMessage());
/* 117 */     BasicHttpResponse response = new BasicHttpResponse(responseStatus);
/* 118 */     response.setEntity(entityFromConnection(connection));
/* 119 */     for (Map.Entry header : connection.getHeaderFields().entrySet()) {
/* 120 */       if (header.getKey() != null) {
/* 121 */         Header h = new BasicHeader((String)header.getKey(), (String)((List)header.getValue()).get(0));
/* 122 */         response.addHeader(h);
/*     */       }
/*     */     }
/* 125 */     return response;
/*     */   }
/*     */ 
/*     */   private static HttpEntity entityFromConnection(HttpURLConnection connection) {
/* 134 */     BasicHttpEntity entity = new BasicHttpEntity();
/*     */     InputStream inputStream;
/*     */     InputStream inputStream;
/*     */     try {
/* 137 */       inputStream = connection.getInputStream();
/*     */     } catch (IOException ioe) {
/* 139 */       inputStream = connection.getErrorStream();
/*     */     }
/* 141 */     entity.setContent(inputStream);
/* 142 */     entity.setContentLength(connection.getContentLength());
/* 143 */     entity.setContentEncoding(connection.getContentEncoding());
/* 144 */     entity.setContentType(connection.getContentType());
/* 145 */     return entity;
/*     */   }
/*     */ 
/*     */   protected HttpURLConnection createConnection(URL url)
/*     */     throws IOException
/*     */   {
/* 152 */     return (HttpURLConnection)url.openConnection();
/*     */   }
/*     */ 
/*     */   private HttpURLConnection openConnection(URL url, Request<?> request)
/*     */     throws IOException
/*     */   {
/* 162 */     HttpURLConnection connection = createConnection(url);
/*     */ 
/* 164 */     int timeoutMs = request.getTimeoutMs();
/* 165 */     connection.setConnectTimeout(timeoutMs);
/* 166 */     connection.setReadTimeout(timeoutMs);
/* 167 */     connection.setUseCaches(false);
/* 168 */     connection.setDoInput(true);
/*     */ 
/* 171 */     if (("https".equals(url.getProtocol())) && (this.mSslSocketFactory != null)) {
/* 172 */       ((HttpsURLConnection)connection).setSSLSocketFactory(this.mSslSocketFactory);
/*     */     }
/*     */ 
/* 175 */     return connection;
/*     */   }
/*     */ 
/*     */   static void setConnectionParametersForRequest(HttpURLConnection connection, Request<?> request)
/*     */     throws IOException, AuthFailureError
/*     */   {
/* 181 */     switch (request.getMethod())
/*     */     {
/*     */     case -1:
/* 186 */       byte[] postBody = request.getPostBody();
/* 187 */       if (postBody == null) {
/*     */         return;
/*     */       }
/*     */ 
/* 191 */       connection.setDoOutput(true);
/* 192 */       connection.setRequestMethod("POST");
/* 193 */       connection.addRequestProperty("Content-Type", 
/* 194 */         request.getPostBodyContentType());
/* 195 */       DataOutputStream out = new DataOutputStream(connection.getOutputStream());
/* 196 */       out.write(postBody);
/* 197 */       out.close();
/*     */ 
/* 199 */       break;
/*     */     case 0:
/* 203 */       connection.setRequestMethod("GET");
/* 204 */       break;
/*     */     case 3:
/* 206 */       connection.setRequestMethod("DELETE");
/* 207 */       break;
/*     */     case 1:
/* 209 */       connection.setRequestMethod("POST");
/* 210 */       addBodyIfExists(connection, request);
/* 211 */       break;
/*     */     case 2:
/* 213 */       connection.setRequestMethod("PUT");
/* 214 */       addBodyIfExists(connection, request);
/* 215 */       break;
/*     */     case 4:
/* 217 */       connection.setRequestMethod("HEAD");
/* 218 */       break;
/*     */     case 5:
/* 220 */       connection.setRequestMethod("OPTIONS");
/* 221 */       break;
/*     */     case 6:
/* 223 */       connection.setRequestMethod("TRACE");
/* 224 */       break;
/*     */     case 7:
/* 226 */       connection.setRequestMethod("PATCH");
/* 227 */       addBodyIfExists(connection, request);
/* 228 */       break;
/*     */     default:
/* 230 */       throw new IllegalStateException("Unknown method type.");
/*     */     }
/*     */   }
/*     */ 
/*     */   private static void addBodyIfExists(HttpURLConnection connection, Request<?> request) throws IOException, AuthFailureError
/*     */   {
/* 236 */     byte[] body = request.getBody();
/* 237 */     if (body != null) {
/* 238 */       connection.setDoOutput(true);
/* 239 */       connection.addRequestProperty("Content-Type", request.getBodyContentType());
/* 240 */       DataOutputStream out = new DataOutputStream(connection.getOutputStream());
/* 241 */       out.write(body);
/* 242 */       out.close();
/*     */     }
/*     */   }
/*     */ 
/*     */   public static abstract interface UrlRewriter
/*     */   {
/*     */     public abstract String rewriteUrl(String paramString);
/*     */   }
/*     */ }

/* Location:           E:\volley.jar
 * Qualified Name:     com.android.volley.toolbox.HurlStack
 * JD-Core Version:    0.5.4
 */