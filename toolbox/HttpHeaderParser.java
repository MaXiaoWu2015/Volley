/*     */ package com.android.volley.toolbox;
/*     */ 
/*     */ import com.android.volley.Cache.Entry;
/*     */ import com.android.volley.NetworkResponse;
/*     */ import java.util.Date;
/*     */ import java.util.Map;
/*     */ import org.apache.http.impl.cookie.DateParseException;
/*     */ import org.apache.http.impl.cookie.DateUtils;
/*     */ 
/*     */ public class HttpHeaderParser
/*     */ {
/*     */   public static Cache.Entry parseCacheHeaders(NetworkResponse response)
/*     */   {
/*  40 */     long now = System.currentTimeMillis();
/*     */ 
/*  42 */     Map headers = response.headers;
/*     */ 
/*  44 */     long serverDate = 0L;
/*  45 */     long lastModified = 0L;
/*  46 */     long serverExpires = 0L;
/*  47 */     long softExpire = 0L;
/*  48 */     long finalExpire = 0L;
/*  49 */     long maxAge = 0L;
/*  50 */     long staleWhileRevalidate = 0L;
/*  51 */     boolean hasCacheControl = false;
/*  52 */     boolean mustRevalidate = false;
/*     */ 
/*  54 */     String serverEtag = null;
/*     */ 
/*  57 */     String headerValue = (String)headers.get("Date");
/*  58 */     if (headerValue != null) {
/*  59 */       serverDate = parseDateAsEpoch(headerValue);
/*     */     }
/*     */ 
/*  62 */     headerValue = (String)headers.get("Cache-Control");
/*  63 */     if (headerValue != null) {
/*  64 */       hasCacheControl = true;
/*  65 */       String[] tokens = headerValue.split(",");
/*  66 */       for (int i = 0; i < tokens.length; ++i) {
/*  67 */         String token = tokens[i].trim();
/*  68 */         if ((token.equals("no-cache")) || (token.equals("no-store")))
/*  69 */           return null;
/*  70 */         if (token.startsWith("max-age="))
/*     */           try {
/*  72 */             maxAge = Long.parseLong(token.substring(8));
/*     */           } catch (Exception localException) {
/*     */           }
/*  75 */         else if (token.startsWith("stale-while-revalidate="))
/*     */           try {
/*  77 */             staleWhileRevalidate = Long.parseLong(token.substring(23));
/*     */           } catch (Exception localException1) {
/*     */           }
/*  80 */         else if ((token.equals("must-revalidate")) || (token.equals("proxy-revalidate"))) {
/*  81 */           mustRevalidate = true;
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/*  86 */     headerValue = (String)headers.get("Expires");
/*  87 */     if (headerValue != null) {
/*  88 */       serverExpires = parseDateAsEpoch(headerValue);
/*     */     }
/*     */ 
/*  91 */     headerValue = (String)headers.get("Last-Modified");
/*  92 */     if (headerValue != null) {
/*  93 */       lastModified = parseDateAsEpoch(headerValue);
/*     */     }
/*     */ 
/*  96 */     serverEtag = (String)headers.get("ETag");
/*     */ 
/* 100 */     if (hasCacheControl) {
/* 101 */       softExpire = now + maxAge * 1000L;
/* 102 */       finalExpire = (mustRevalidate) ? 
/* 103 */         softExpire : 
/* 104 */         softExpire + staleWhileRevalidate * 1000L;
/* 105 */     } else if ((serverDate > 0L) && (serverExpires >= serverDate))
/*     */     {
/* 107 */       softExpire = now + (serverExpires - serverDate);
/* 108 */       finalExpire = softExpire;
/*     */     }
/*     */ 
/* 111 */     Cache.Entry entry = new Cache.Entry();
/* 112 */     entry.data = response.data;
/* 113 */     entry.etag = serverEtag;
/* 114 */     entry.softTtl = softExpire;
/* 115 */     entry.ttl = finalExpire;
/* 116 */     entry.serverDate = serverDate;
/* 117 */     entry.lastModified = lastModified;
/* 118 */     entry.responseHeaders = headers;
/*     */ 
/* 120 */     return entry;
/*     */   }
/*     */ 
/*     */   public static long parseDateAsEpoch(String dateStr)
/*     */   {
/*     */     try
/*     */     {
/* 129 */       return DateUtils.parseDate(dateStr).getTime();
/*     */     } catch (DateParseException e) {
/*     */     }
/* 132 */     return 0L;
/*     */   }
/*     */ 
/*     */   public static String parseCharset(Map<String, String> headers, String defaultCharset)
/*     */   {
/* 145 */     String contentType = (String)headers.get("Content-Type");
/* 146 */     if (contentType != null) {
/* 147 */       String[] params = contentType.split(";");
/* 148 */       for (int i = 1; i < params.length; ++i) {
/* 149 */         String[] pair = params[i].trim().split("=");
/* 150 */         if ((pair.length == 2) && 
/* 151 */           (pair[0].equals("charset"))) {
/* 152 */           return pair[1];
/*     */         }
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 158 */     return defaultCharset;
/*     */   }
/*     */ 
/*     */   public static String parseCharset(Map<String, String> headers)
/*     */   {
/* 166 */     return parseCharset(headers, "ISO-8859-1");
/*     */   }
/*     */ }

/* Location:           E:\volley.jar
 * Qualified Name:     com.android.volley.toolbox.HttpHeaderParser
 * JD-Core Version:    0.5.4
 */