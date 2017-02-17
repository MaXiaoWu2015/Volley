/*    */ package com.android.volley;
/*    */ 
/*    */ import java.util.Collections;
/*    */ import java.util.Map;
/*    */ 
/*    */ public abstract interface Cache
/*    */ {
/*    */   public abstract Entry get(String paramString);
/*    */ 
/*    */   public abstract void put(String paramString, Entry paramEntry);
/*    */ 
/*    */   public abstract void initialize();
/*    */ 
/*    */   public abstract void invalidate(String paramString, boolean paramBoolean);
/*    */ 
/*    */   public abstract void remove(String paramString);
/*    */ 
/*    */   public abstract void clear();
/*    */ 
/*    */   public static class Entry
/*    */   {
/*    */     public byte[] data;
/*    */     public String etag;
/*    */     public long serverDate;
/*    */     public long lastModified;
/*    */     public long ttl;
/*    */     public long softTtl;
/* 87 */     public Map<String, String> responseHeaders = Collections.emptyMap();
/*    */ 
/*    */     public boolean isExpired()
/*    */     {
/* 91 */       return this.ttl < System.currentTimeMillis();
/*    */     }
/*    */ 
/*    */     public boolean refreshNeeded()
/*    */     {
/* 96 */       return this.softTtl < System.currentTimeMillis();
/*    */     }
/*    */   }
/*    */ }

/* Location:           E:\volley.jar
 * Qualified Name:     com.android.volley.Cache
 * JD-Core Version:    0.5.4
 */